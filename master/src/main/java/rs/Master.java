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
import java.util.concurrent.CompletableFuture;

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
        List<List<String>> serverSplits = allocateSplitsToServers(contents, servers.size());
        List<FTPClient> ftpClients = openFtpClients();

        CompletableFuture<?>[] futures = new CompletableFuture<?>[servers.size()];
        for (int i= 0; i < servers.size(); i++) {
            int serverIndex = i;

            // Macro phase 1: split

            futures[serverIndex] = CompletableFuture.runAsync(() -> {
                // send the files (splits) to the servers
                FTPClient ftpClient = ftpClients.get(serverIndex);
                List<String> serverContent = serverSplits.get(serverIndex);
                sendFileToServer(ftpClient, serverContent);
            }).thenRunAsync(() -> {
                // send the ip of the other servers and tell the servers to start the map function 
                openSocketsAndBuffers();
                sendServerIPsAndStartMapFunction(serverIndex);
                receiveShuffleCompleteMessages(readers.get(serverIndex));
            });
        }
        
        CompletableFuture.allOf(futures).join();
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

    private static void sendServerIPsAndStartMapFunction(int i) {
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
        System.out.println("IPs sent to servers.");
    }

    private static void receiveShuffleCompleteMessages(BufferedReader reader) {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("SHUFFLE_FINISHED")) {
                        System.out.println("Shuffle complete received from server.");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static void sendFileToServer(FTPClient ftpClient, List<String> content) {
        try {
            // Transforma a lista de strings em um único string onde cada item da lista é uma linha
            String fileContent = String.join("\n", content);
    
            // Converte o string para um InputStream
            InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    
            // Envia o arquivo para o servidor, sobrescrevendo se já existir
            ftpClient.deleteFile(fileName);
            boolean success = ftpClient.storeFile(fileName, inputStream);
    
            if (!success) {
                System.out.println("File upload failed. FTP Error code: " + ftpClient.getReplyCode());
            } else {
                System.out.println("File uploaded successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    private static List<List<String>> allocateSplitsToServers(List<String> contents, int numServers) {
        List<List<String>> serverSplits = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            serverSplits.add(new ArrayList<>());
        }

        for (int i = 0; i < contents.size(); i++) {
            serverSplits.get(i % numServers).add(contents.get(i));
        }

        return serverSplits;
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

}

