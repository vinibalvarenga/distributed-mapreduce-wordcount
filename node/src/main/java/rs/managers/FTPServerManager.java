package rs.managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;

import rs.utils.UserManagerUtil;
import rs.utils.WordCountUtils;

import org.apache.ftpserver.ftplet.UserManager;

public class FTPServerManager {
    private final int ftpPort;
    private static final String USERNAME = "toto";
    private static final String PASSWORD = "tata";
    private static final String HOME_DIRECTORY = System.getProperty("java.io.tmpdir") + "/alvarenga-23/";
    private FtpServer server;

    public FTPServerManager(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public void createAndStartFTPServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(ftpPort);
        serverFactory.addListener("default", listenerFactory.createListener());

        UserManager userManager = UserManagerUtil.createUserManager(USERNAME, PASSWORD, HOME_DIRECTORY);
        serverFactory.setUserManager(userManager);

        server = serverFactory.createServer();
        try {
            server.start();
            System.out.println("FTP Server started on port " + ftpPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FTPClient setupFtpClient(String toNodeIp) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(toNodeIp, ftpPort);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (IOException ex) {
            System.out.println("Error setting up FTP client: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public void sendDataToServers(String fromNodeIp, String file_prefix, List<?> data, List<String> knownServers,
            boolean isWordCount) {
        for (int i = 0; i < knownServers.size(); i++) {
            String toNodeIp = knownServers.get(i);
            String dataString = isWordCount ? WordCountUtils.mapToString((Map<String, Integer>) data.get(i))
                    : entriesToString((List<Map.Entry<String, Integer>>) data.get(i));
            sendToServer(fromNodeIp, toNodeIp, file_prefix, dataString);
        }
    }

    public void sendToServer(String fromNodeIp, String toNodeIp, String file_prefix, String dataString) {
        FTPClient ftpClient = setupFtpClient(toNodeIp);
        if (ftpClient != null) {
            try {
                String remoteFile = generateRemoteFileName(file_prefix, fromNodeIp, toNodeIp);
                InputStream inputStream = new ByteArrayInputStream(dataString.getBytes(StandardCharsets.UTF_8));
                if (!ftpClient.storeFile(remoteFile, inputStream)) {
                    System.out.println("Failed to upload the file to " + toNodeIp);
                }
                inputStream.close();
            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                disconnectFtpClient(ftpClient);
            }
        }
    }

    private String generateRemoteFileName(String file_prefix, String fromNodeIp, String toNodeIp) {
        return "/" + file_prefix + "-From-" + fromNodeIp.replace(".", "_") + "-To-" + toNodeIp.replace(".", "_")
                + ".txt";
    }

    private String entriesToString(List<Map.Entry<String, Integer>> entries) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : entries) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public void disconnectFtpClient(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void resetFTPServer(){
        try {
            // Passo 1: Verificar se o servidor está rodando e pará-lo
            if (server != null && !server.isStopped()) {
                server.stop();
                System.out.println("FTP Server stopped.");
            }
    
            // Passo 2 e 3: Recriar e reiniciar o servidor FTP com as configurações iniciais
            createAndStartFTPServer();
            System.out.println("FTP Server restarted.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error resetting FTP server: " + e.getMessage());
        }
    }
}
