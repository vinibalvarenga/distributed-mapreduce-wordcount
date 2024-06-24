package rs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.net.ftp.FTPClient;

public class Master {
    private static final String localfileName = "./master/random_lines.txt";
    private static final String fileName = "random_lines.txt";
    private static final List<String> servers = Arrays.asList("tp-1a252-22", "tp-1a252-23", "tp-1a252-24");

    public static void main(String[] args) {
        List<String> contents = FileSplitter.readFileContents(localfileName);
        List<List<String>> serverSplits = FileSplitter.allocateSplitsToServers(contents, servers.size());
        FTPManager ftpManager = new FTPManager(servers);
        SocketManager socketManager = new SocketManager(servers);
        List<FTPClient> ftpClients = ftpManager.openFtpClients();   
        List<List<Integer>> reducedRanges = Utils.initializeReducedRanges(servers.size());

        socketManager.openSocketsAndBuffers();
       
        // Send files, Map phase and Shuffle phases

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

        // First reduce phase

        CompletableFuture<?>[] futures2 = new CompletableFuture<?>[servers.size()];

        for (int i = 0; i < servers.size(); i++) {
            int serverIndex = i;

            futures2[serverIndex] = CompletableFuture.runAsync(() -> {
                System.out.println("Starting reduce phase for server " + servers.get(serverIndex));
                String reduce = socketManager.reduce_one(serverIndex);
                Utils.extractIntegersAndAddToList(reduce, reducedRanges, serverIndex);
            });
        }

        
        CompletableFuture.allOf(futures2).join();
        
        int fmin = Collections.min(reducedRanges.get(0));
        int fmax = Collections.max(reducedRanges.get(1));
        List<List<Integer>> groupRanges = Utils.calculateGroupRanges(fmin, fmax, servers.size());

        System.out.println("Group ranges: " + groupRanges);

        // Group phase

        CompletableFuture<?>[] futures3 = new CompletableFuture<?>[servers.size()];

        for (int i = 0; i < servers.size(); i++) {
            int serverIndex = i;

            futures3[serverIndex] = CompletableFuture.runAsync(() -> {
                System.out.println("Starting group phase for server " + servers.get(serverIndex));
                socketManager.group(serverIndex, groupRanges);
                // wait for shuffle two
            });
        }

        CompletableFuture.allOf(futures3).join();

        ftpManager.closeFtpClients(ftpClients);
        socketManager.closeSocketsAndBuffers();
       
    }

   
}
