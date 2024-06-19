package rs;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileHandler {
    private final String filePath;
    private static final int FTP_PORT = 3456;
    private static final String USERNAME = "toto";
    private static final String PASSWORD = "tata";

    public FileHandler(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, Integer> mapHandler() {
        System.out.println("[Starting Map]");
        File file = new File(filePath);
        Map<String, Integer> wordCount = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Map<String, Integer> lineWordCount = WordCountUtils.mapFunction(line);
                WordCountUtils.mergeWordCounts(wordCount, lineWordCount);
            }
           // System.out.println(wordCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCount;
    }

    public void shuffle(Map<String, Integer> wordCount, List<String> knownServers, String fromNodeIp) {
        System.out.println("[Shuffling] words to servers");
        List<Map<String, Integer>> serverWordCounts = WordCountUtils.splitWordCounts(wordCount, knownServers.size());
        sendWordCountsToServers(fromNodeIp, serverWordCounts, knownServers);
        System.out.println("[Shuffling] finished");
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
         //   System.out.println("Sending Word count to server" + toNodeIP + " : \n" + wordCountString);
            InputStream inputStream = new ByteArrayInputStream(wordCountString.getBytes(StandardCharsets.UTF_8));
            boolean done = ftpClient.storeFile(remoteFile, inputStream);
            inputStream.close();
            if (done) {
                InputStream retrieveFileStream = ftpClient.retrieveFileStream(remoteFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(retrieveFileStream));
             //   String line;
               // System.out.println("Word count file: " + remoteFile + " is uploaded successfully to server " + toNodeIP + ". Word count file content:\n");
               // while ((line = reader.readLine()) != null) {
               //     System.out.println(line);
             //   }
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
    
    public String reduce_one() {
        Map<String, Integer> wordCounts = new HashMap<>();
        String filePattern = "/wordCount-From-.*-To-.*\\.txt";
        try (Stream<Path> paths = Files.walk(Paths.get("."))) {
            paths.filter(Files::isRegularFile)
                .filter(p -> Pattern.matches(filePattern, p.toString()))
                .forEach(filePath -> processFile(filePath, wordCounts));
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return formatMinMaxCounts(wordCounts);
    }

    private void processFile(Path filePath, Map<String, Integer> wordCounts) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, wordCounts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void processLine(String line, Map<String, Integer> wordCounts) {
        String[] parts = line.split(": ");
        String word = parts[0];
        int count = Integer.parseInt(parts[1]);
        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + count);
    }

    private String formatMinMaxCounts(Map<String, Integer> wordCounts) {
        if (wordCounts.isEmpty()) {
            return "[0, 0]";
        } else {
            int min = Collections.min(wordCounts.values());
            int max = Collections.max(wordCounts.values());
            return "[" + min + ", " + max + "]";
        }
    }

    private String generateRemoteFileName(String fromNodeIp, String toNodeIp) {
        return "/wordCount-From-" + fromNodeIp.replace(".", "_") + "-To-" + toNodeIp.replace(".", "_") + ".txt";
    }
}
