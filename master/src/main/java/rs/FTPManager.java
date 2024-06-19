package rs;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FTPManager {
    private static final int FTP_PORT = 3456;
    private static final String username = "toto";
    private static final String password = "tata";
    private List<String> servers;

    public FTPManager(List<String> servers) {
        this.servers = servers;
    }

    public List<FTPClient> openFtpClients() {
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

    public void sendFileToServer(FTPClient ftpClient, List<String> content, String fileName) {
        try {
            String fileContent = String.join("\n", content);
            InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
            ftpClient.deleteFile(fileName);
            boolean success = ftpClient.storeFile(fileName, inputStream);

            if (!success) {
                System.out.println("File upload failed. FTP Error code: " + ftpClient.getReplyCode());
            } else {
                System.out.println("File uploaded successfully to server " + ftpClient.getRemoteAddress().getHostName() + ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void closeFtpClients(List<FTPClient> ftpClients) {
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
