package rs;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.net.ftp.FTPClient;

public class Master {
    private static final String localfileName = "./master/random_lines.txt";
    private static final String fileName = "random_lines.txt";
    private static final List<String> servers = Arrays.asList("tp-1a201-22", "tp-1a201-21", "tp-1a201-20");

    public static void main(String[] args) {
        List<String> contents = FileSplitter.readFileContents(localfileName);
        List<List<String>> serverSplits = FileSplitter.allocateSplitsToServers(contents, servers.size());
        FTPManager ftpManager = new FTPManager(servers);
        SocketManager socketManager = new SocketManager(servers);
        List<FTPClient> ftpClients = ftpManager.openFtpClients();
        socketManager.openSocketsAndBuffers();

        CompletableFuture<?>[] futures = new CompletableFuture<?>[servers.size()];
        for (int i = 0; i < servers.size(); i++) {
            int serverIndex = i;

            futures[serverIndex] = CompletableFuture.runAsync(() -> {
                FTPClient ftpClient = ftpClients.get(serverIndex);
                List<String> serverContent = serverSplits.get(serverIndex);
                ftpManager.sendFileToServer(ftpClient, serverContent, fileName);
            }).thenRunAsync(() -> {
                socketManager.sendServerIPsAndStartMapFunction(serverIndex);
                socketManager.receiveShuffleCompleteMessages(serverIndex);
            });
        }

        CompletableFuture.allOf(futures).join();
        ftpManager.closeFtpClients(ftpClients);
        socketManager.closeSocketsAndBuffers();
       
    }
}
