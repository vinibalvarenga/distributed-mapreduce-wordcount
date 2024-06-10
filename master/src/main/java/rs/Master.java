package rs;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Master {
    private static final String localfileName = "./master/random_lines.txt";
    private static final String fileName = "random_lines.txt";
    private static final List<String> servers = Arrays.asList("tp-1a252-24", "tp-1a252-25", "tp-1a252-26");
    private static final int FTP_PORT = 3456;
    private static final int SOCKET_PORT = 3457;
    private static final String username = "toto";
    private static final String password = "tata";

    private static final List<Socket> sockets = new ArrayList<>();
    private static final List<BufferedReader> readers = new ArrayList<>();
    private static final List<BufferedWriter> writers = new ArrayList<>();


    public static void main(String[] args) {
        List<String> contents = readFileContents();
        List<FTPClient> ftpClients = openFtpClients();


        // Fase 1: send the files to the servers
        for (int i = 0; i < contents.size(); i++){
            FTPClient ftpClient = ftpClients.get(i % ftpClients.size());
            System.out.println("Processing file on server: " + servers.get(i % ftpClients.size()));
            processFileOnServer(ftpClient, contents.get(i));
        }

        closeFtpClients(ftpClients);

        // Fase 2: send the ip of the other servers and tell the servers to start the map function 
        openSocketsAndBuffers();
        sendServerIPsAndStartMapFunction();

        // Fase 3: receive shuffle complete messages from servers
        receiveShuffleCompleteMessages();
    }


    private static List<FTPClient> openFtpClients() {
        List<FTPClient> ftpClients = new ArrayList<>();
        for (String server : servers) {
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(server, FTP_PORT);
                ftpClient.login(username, password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClients.add(ftpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ftpClients;
    }

    private static void closeFtpClients(List<FTPClient> ftpClients) {
        for (FTPClient ftpClient : ftpClients) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void openSocketsAndBuffers() {
        for (String server : servers) {
            try {
                Socket socket = new Socket(server, SOCKET_PORT);
                sockets.add(socket);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                readers.add(reader);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writers.add(writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendServerIPsAndStartMapFunction() {
        for (int i = 0; i < servers.size(); i++) {
            try {
                BufferedWriter writer = writers.get(i);
                // Enviar sinal de início
                writer.write("START_IP_SENDING\n");
                writer.flush();
                // Envia o próprio IP
                writer.write(servers.get(i) + "\n");
                writer.flush();
                int numServers = servers.size();
                // Enviar o número de servidores
                writer.write(numServers + "\n");
                writer.flush();
    
                // Enviar os IPs dos servidores
                for (int j = 0; j < servers.size(); j++) { 
                    writer.write(servers.get(j) + "\n");
                    writer.flush();
                }
    
                // Enviar sinal de que terminamos de enviar os IPs
                writer.write("END_OF_IPS\n");
                writer.flush();
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("IPs sent to servers.");
    }

    private static void receiveShuffleCompleteMessages() {
        for (BufferedReader reader : readers) {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("SHUFFLE_COMPLETE")) {
                        System.out.println("Shuffle complete received from server.");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processFileOnServer(FTPClient ftpClient, String content) {
        try {
            if (!fileExistsOnServer(ftpClient)) {
                uploadFileToServer(ftpClient, content);
            } else {
                appendLineToServerFile(ftpClient, content);
                displayFileContentFromServer(ftpClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean fileExistsOnServer(FTPClient ftpClient) throws IOException {
        FTPFile[] files = ftpClient.listFiles();
        for (FTPFile file : files) {
            if (file.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    private static void uploadFileToServer(FTPClient ftpClient, String content) throws IOException {
        // Create a new empty file
        ftpClient.storeFile(fileName,  new ByteArrayInputStream(new byte[0]));

        // Add the first line to the file
        ByteArrayInputStream inputStream = new ByteArrayInputStream((content + "\n").getBytes());
        ftpClient.appendFile(fileName, inputStream);

        int errorCode = ftpClient.getReplyCode();
        if (errorCode != 226) {
            System.out.println("File upload failed. FTP Error code: " + errorCode);
        } else {
            System.out.println("File uploaded successfully.");
        }
    }

    private static void appendLineToServerFile(FTPClient ftpClient, String content) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream((content + "\n").getBytes());
        ftpClient.appendFile(fileName, inputStream);
    }

    private static void displayFileContentFromServer(FTPClient ftpClient) throws IOException {
        InputStream inputStream = ftpClient.retrieveFileStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
        ftpClient.completePendingCommand();
    }

    private static List<String> readFileContents() {
        List<String> contents = new ArrayList<>();
        try {
            contents = Files.readAllLines(Paths.get(localfileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }
}