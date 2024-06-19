package rs;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {
    private final String filePath;
    private static final int FTP_PORT = 3456;
    private static final String USERNAME = "toto";
    private static final String PASSWORD = "tata";

    public FileHandler(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, Integer> mapHandler() {
        System.out.println("Starting Map");
        File file = new File(filePath);
        Map<String, Integer> wordCount = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Map<String, Integer> lineWordCount = WordCountUtils.mapFunction(line);
                WordCountUtils.mergeWordCounts(wordCount, lineWordCount);
            }
            System.out.println(wordCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCount;
    }

    public void shuffle(Map<String, Integer> wordCount, List<String> knownServers, String fromNodeIp) {
        System.out.println("[Shuffling] words to servers");
        List<Map<String, Integer>> serverWordCounts = WordCountUtils.splitWordCounts(wordCount, knownServers.size());
        sendWordCountsToServers(fromNodeIp, serverWordCounts, knownServers);
    }

    private void sendWordCountsToServers(String fromNodeIp, List<Map<String, Integer>> serverWordCounts, List<String> knownServers) {
        for (int i = 0; i < knownServers.size(); i++) {
            String toNodeIp = knownServers.get(i);
            Map<String, Integer> wordCount = serverWordCounts.get(i);
            sendWordCountToServer(fromNodeIp, toNodeIp,wordCount);
        }
    }

    private void sendWordCountToServer(String fromNodeIp, String toNodeIP,Map<String, Integer> wordCount) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(toNodeIP, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteFile = generateRemoteFileName(fromNodeIp, toNodeIP);
            String wordCountString = WordCountUtils.mapToString(wordCount);
            System.out.println("Sending Word count to server" + toNodeIP + " : \n" + wordCountString);
            InputStream inputStream = new ByteArrayInputStream(wordCountString.getBytes(StandardCharsets.UTF_8));
            boolean done = ftpClient.storeFile(remoteFile, inputStream);
            inputStream.close();
            if (done) {
                InputStream retrieveFileStream = ftpClient.retrieveFileStream(remoteFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(retrieveFileStream));
                String line;
                System.out.println("Word count file: " + remoteFile + " is uploaded successfully to server " + toNodeIP + ". Word count file content:\n");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
                ftpClient.completePendingCommand(); // Important, to finalize the command
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

    private String generateRemoteFileName(String fromNodeIp, String toNodeIp) {
        return "/wordCount-From-" + fromNodeIp.replace(".", "_") + "To-" + toNodeIp.replace(".", "_") + ".txt";
    }
}
