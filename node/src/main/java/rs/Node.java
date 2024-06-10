package rs;

import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.log4j.PropertyConfigurator;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private static final int FTP_PORT = 3456;
    private static final int SOCKET_PORT = 3457;
    private static final String USERNAME = "toto";
    private static final String PASSWORD = "tata";
    private static String my_ip;
    private static final List<String> knownServers = new ArrayList<>();
    private static final String homeDirectory = System.getProperty("java.io.tmpdir") + "/alvarenga-23/";
    private static final String filePath = homeDirectory + "random_lines.txt";

    public static void main(String[] args) {
        PropertyConfigurator.configure(Node.class.getResource("/log4J.properties"));

        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(FTP_PORT);
        serverFactory.addListener("default", listenerFactory.createListener());

        UserManager userManager = createUserManager();
        serverFactory.setUserManager(userManager);

        FtpServer server = serverFactory.createServer();

        startFTPServer(server);

        startSocketServer();
    }

    private static UserManager createUserManager() {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        File userFile = new File("users.properties");
        createFileIfNotExists(userFile);

        userManagerFactory.setFile(userFile);
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());

        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = createUser();
        saveUser(userManager, user);

        return userManager;
    }

    private static BaseUser createUser() {
        BaseUser user = new BaseUser();
        user.setName(USERNAME);
        user.setPassword(PASSWORD);

        createDirectoryIfNotExists(homeDirectory);

        user.setHomeDirectory(homeDirectory);
        user.setAuthorities(getUserAuthorities());

        return user;
    }

    private static List<Authority> getUserAuthorities() {
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        return authorities;
    }

    private static void saveUser(UserManager userManager, BaseUser user) {
        try {
            userManager.save(user);
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    private static void createFileIfNotExists(File file) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("File created: " + file.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    private static void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory.getAbsolutePath());
            } else {
                System.out.println("Failed to create directory.");
            }
        }
    }

    private static void startFTPServer(FtpServer server) {
        try {
            server.start();
            System.out.println("FTP Server started on port " + FTP_PORT);
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    private static void startSocketServer() {
        try (ServerSocket serverSocket = new ServerSocket(SOCKET_PORT)) {
            System.out.println("Socket Server started on port " + SOCKET_PORT);
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    handleRequest(in, out);
                    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(BufferedReader in, PrintWriter out) {
        try {
            String line = in.readLine();
            if (line.equals("START_IP_SENDING")) {
                my_ip = in.readLine();
                System.out.println("Received my IP: " + my_ip);
                line = in.readLine(); // Lê o número de servidores
                int numServers = Integer.parseInt(line);
                for (int i = 0; i < numServers; i++) {
                    line = in.readLine(); // Lê o IP do servidor
                    knownServers.add(line);
                    System.out.println("Received IP: " + line);
                }
                line = in.readLine();
                if (line.equals("END_OF_IPS")) {
                    out.println(String.join(",", knownServers));
                    System.out.println("Known servers to node received");
                    System.out.println("Starting Map Function");
                    Map<String, Integer> wordCount = mapHandler(filePath);
                    shuffle(wordCount);
                    out.println("SHUFFLE_FINISHED");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("Erro ao interpretar a solicitação");
    }

    private static Map<String, Integer> mapHandler(String filePath) {
        File file = new File(filePath);
        Map<String, Integer> wordCount = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Aqui você pode fazer algo com wordCount, como imprimir no console ou armazenar para uso posterior.
                Map<String, Integer> lineWordCount = mapFunction(line);
                mergeWordCounts(wordCount, lineWordCount);
            }
            System.out.println(wordCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCount;
    }

    private static Map<String, Integer> mapFunction(String line) {
        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = line.split(" ");
        for (String word : words) {
            wordCount.put(word, 1);
        }
        return wordCount;
    }
    
    private static void mergeWordCounts(Map<String, Integer> totalWordCount, Map<String, Integer> lineWordCount) {
        for (Map.Entry<String, Integer> entry : lineWordCount.entrySet()) {
            if (!totalWordCount.containsKey(entry.getKey())) {
                totalWordCount.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void shuffle(Map<String, Integer> wordCount){
        System.out.println("[Shuffling] words to servers");
        List<Map<String, Integer>> serverWordCounts = new ArrayList<>();
        for (int i = 0; i < knownServers.size(); i++) {
            serverWordCounts.add(new HashMap<>());
        }
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            int serverIndex = Math.abs(word.hashCode()) % knownServers.size();
            Map<String, Integer> serverWordCount = serverWordCounts.get(serverIndex);
            serverWordCount.put(word, count);
        }
        sendWordCountsToServers(serverWordCounts);
    }

    private static void sendWordCountsToServers(List<Map<String, Integer>> serverWordCounts) {
        for (int i = 0; i < knownServers.size(); i++) {
            String serverIp = knownServers.get(i);
            Map<String, Integer> wordCount = serverWordCounts.get(i);
            sendWordCountToServer(serverIp, wordCount);
        }

    }

    private static void sendWordCountToServer(String serverIp, Map<String, Integer> wordCount) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(serverIp, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteFile = "/wordCount.txt";
            InputStream inputStream = new ByteArrayInputStream(mapToString(wordCount).getBytes(StandardCharsets.UTF_8));
            boolean done = ftpClient.storeFile(remoteFile, inputStream);
            inputStream.close();
            if (done) {
                System.out.println("Word count file is uploaded successfully to server " + serverIp);
            }
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String mapToString(Map<String, Integer> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }
   
}