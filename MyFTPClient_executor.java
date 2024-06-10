package rs;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyFTPClient_executor {

    private static final String localfileName = "./myftpclient/random_lines.txt";
    private static final String fileName = "random_lines.txt";
    private static final List<String> servers = Arrays.asList("tp-1a207-24", "tp-1a207-23", "tp-1a207-22");
    private static final int port = 3456;
    private static final String username = "toto";
    private static final String password = "tata";

    public static void main(String[] args) {
       
        List<String> contents = readFileContents();
        ExecutorService executor = Executors.newFixedThreadPool(servers.size());
        List<FTPClient> ftpClients = createFtpClients();

        distributeContents(contents, ftpClients, executor);

        shutdownAndDisconnect(executor, ftpClients);
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

    private static List<FTPClient> createFtpClients() {
        List<FTPClient> ftpClients = new ArrayList<>();
        for (String server : servers) {
            FTPClient ftpClient = new FTPClient();
            ftpClients.add(ftpClient);
            try {
                ftpClient.connect(server, port);
                ftpClient.login(username, password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("FTP clients created successfully.");
        return ftpClients;
    }

    private static void distributeContents(List<String> contents, List<FTPClient> ftpClients, ExecutorService executor) {
        for (int i = 0; i < contents.size(); i++) {
            FTPClient ftpClient = ftpClients.get(i % servers.size());
            String content = contents.get(i);
            executor.submit(() -> {
                try {
                    System.out.println("Uploading content to server: " + ftpClient.getRemoteAddress().getHostName());
                    handleContent(ftpClient, content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static void handleContent(FTPClient ftpClient, String content) throws IOException {
        System.out.println("Checking if file exists in the FTP server.");
        FTPFile[] files = ftpClient.listFiles();
        System.out.println("Files in the FTP server:");
        for (FTPFile file : files) {
            System.out.println(file.getName());
        }
        boolean fileExists = Arrays.stream(files).anyMatch(file -> file.getName().equals(fileName));

        if (!fileExists) {
            uploadContent(ftpClient, content);
        } else {
            displayFileContent(ftpClient);
        }
    }

    private static void uploadContent(FTPClient ftpClient, String content) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        ftpClient.storeFile(fileName, inputStream);
        int errorCode = ftpClient.getReplyCode();
        if (errorCode != 226) {
            System.out.println("File upload failed. FTP Error code: " + errorCode);
        } else {
            System.out.println("File uploaded successfully.");
        }
    }

    private static void displayFileContent(FTPClient ftpClient) throws IOException {
        InputStream inputStream = ftpClient.retrieveFileStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
        ftpClient.completePendingCommand();
    }

    private static void shutdownAndDisconnect(ExecutorService executor, List<FTPClient> ftpClients) {
        executor.shutdown();

        for (FTPClient ftpClient : ftpClients) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}