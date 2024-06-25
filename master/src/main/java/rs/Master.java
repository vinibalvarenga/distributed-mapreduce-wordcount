package rs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.net.ftp.FTPClient;

import rs.managers.FTPManager;
import rs.managers.SocketManager;
import rs.utils.FileSplitter;
import rs.utils.Utils;

public class Master {
    //private static final String localfileName = "./master/random_lines.txt";
    private static final  String localfileName = "/cal/commoncrawl/CC-MAIN-20230320114206-20230320144206-00516.warc.wet";
    private static final String fileName = "random_lines.txt";
    private static final List<String> servers = Arrays.asList("tp-m5-00", "tp-m5-01", "tp-m5-02");

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
                socketManager.receiveShuffleOneCompleteMessages(serverIndex);
            });
        }

        CompletableFuture.allOf(futures).join();

        // First reduce phase

        CompletableFuture<?>[] futures2 = new CompletableFuture<?>[servers.size()];

        for (int i = 0; i < servers.size(); i++) {
            int serverIndex = i;

            futures2[serverIndex] = CompletableFuture.runAsync(() -> {
                System.out.println("Starting reduce one phase for server " + servers.get(serverIndex));
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
                socketManager.receiveShuffleTwoCompleteMessage(serverIndex);
            });
        }

        CompletableFuture.allOf(futures3).join();

        // Second reduce phase

        CompletableFuture<?>[] futures4 = new CompletableFuture<?>[servers.size()];

        for (int i = 0; i < servers.size(); i++) {
            int serverIndex = i;

            futures4[serverIndex] = CompletableFuture.runAsync(() -> {
                System.out.println("Starting reduce two phase for server " + servers.get(serverIndex));
                socketManager.reduce_two(serverIndex);
                // Utils.extractIntegersAndAddToList(reduce, reducedRanges, serverIndex);
            });
        }

        CompletableFuture.allOf(futures4).join();

        ftpManager.closeFtpClients(ftpClients);
        socketManager.closeSocketsAndBuffers();

    }

}
