package rs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.net.ftp.FTPClient;

import rs.managers.FTPManager;
import rs.managers.SocketManager;
import rs.utils.FileSplitter;
import rs.utils.MultipleTimer;
import rs.utils.PrintCSV;
import rs.utils.Utils;

public class Master {
    // private static final String localfileName = "./master/random_lines.txt";
    private static final String localfileName = "/cal/commoncrawl/CC-MAIN-20230320114206-20230320144206-00516.warc.wet";
    private static final String fileName = "random_lines.txt";
    // private static final List<String> servers = Arrays.asList("tp-m5-00",
    // "tp-m5-01", "tp-m5-02");

    public static void main(String[] args) {

        if (args.length > 0) {
            List<String> servers = Arrays.asList(args[0].split(","));
            System.out.println("Servers: " + servers);
            List<String> contents = FileSplitter.readFileContents(localfileName);
            List<List<String>> serverSplits = FileSplitter.allocateSplitsToServers(contents, servers.size());

            FTPManager ftpManager = new FTPManager(servers);
            List<FTPClient> ftpClients = ftpManager.openFtpClients();
            SocketManager socketManager = new SocketManager(servers);
            socketManager.openSocketsAndBuffers();

            List<List<Integer>> reducedRanges = Utils.initializeReducedRanges(servers.size());

            // Communication timers
            MultipleTimer send_splits_timer = new MultipleTimer(servers.size()),
            shuffle_one_timer = new MultipleTimer(servers.size()),
            shuffle_two_timer = new MultipleTimer(servers.size());
            
            
            // Computation timers
            
            MultipleTimer map_timer = new MultipleTimer(servers.size()),
            reduce_one_timer = new MultipleTimer(servers.size()),
            group_timer = new MultipleTimer(servers.size()),
            reduce_two_timer = new MultipleTimer(servers.size());
            
            // Synchronization timers
            MultipleTimer send_ip_timer = new MultipleTimer(servers.size());
            

            long communication_time, computation_time, synchronization_time;


            // Send files, Map phase and Shuffle phases

            CompletableFuture<?>[] futures = new CompletableFuture<?>[servers.size()];
            for (int i = 0; i < servers.size(); i++) {
                int serverIndex = i;

                futures[serverIndex] = CompletableFuture.runAsync(() -> {
                    send_splits_timer.start(serverIndex);
                    FTPClient ftpClient = ftpClients.get(serverIndex);
                    List<String> serverContent = serverSplits.get(serverIndex);
                    ftpManager.sendFileToServer(ftpClient, serverContent, fileName);
                    send_splits_timer.stop(serverIndex);
                }).thenRunAsync(() -> {
                    send_ip_timer.start(serverIndex);
                    socketManager.sendServersIPs(serverIndex);
                    send_ip_timer.stop(serverIndex);

                    map_timer.start(serverIndex);
                    socketManager.sendMap(serverIndex);
                    map_timer.stop(serverIndex);

                    shuffle_one_timer.start(serverIndex);
                    socketManager.receiveShuffleOneCompleteMessages(serverIndex);
                    shuffle_one_timer.stop(serverIndex);
                });
            }

            CompletableFuture.allOf(futures).join();

            // First reduce phase

            CompletableFuture<?>[] futures2 = new CompletableFuture<?>[servers.size()];

            for (int i = 0; i < servers.size(); i++) {
                int serverIndex = i;

                futures2[serverIndex] = CompletableFuture.runAsync(() -> {
                    System.out.println("Starting reduce one phase for server " + servers.get(serverIndex));
                    
                    reduce_one_timer.start(serverIndex);
                    String reduce = socketManager.reduce_one(serverIndex);
                    reduce_one_timer.stop(serverIndex);

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
                    
                    group_timer.start(serverIndex);
                    socketManager.group(serverIndex, groupRanges);
                    group_timer.stop(serverIndex);

                    shuffle_two_timer.start(serverIndex);
                    socketManager.receiveShuffleTwoCompleteMessage(serverIndex);
                    shuffle_two_timer.stop(serverIndex);
                });
            }

            CompletableFuture.allOf(futures3).join();

            // Second reduce phase

            CompletableFuture<?>[] futures4 = new CompletableFuture<?>[servers.size()];

            for (int i = 0; i < servers.size(); i++) {
                int serverIndex = i;

                futures4[serverIndex] = CompletableFuture.runAsync(() -> {
                    System.out.println("Starting reduce two phase for server " + servers.get(serverIndex));
                    reduce_two_timer.start(serverIndex);
                    socketManager.reduce_two(serverIndex);
                    reduce_two_timer.stop(serverIndex);
                });
            }

            CompletableFuture.allOf(futures4).join();

            CompletableFuture<?>[] futures5 = new CompletableFuture<?>[servers.size()];

            for (int i = 0; i < servers.size(); i++) {
                int serverIndex = i;

                futures5[serverIndex] = CompletableFuture.runAsync(() -> {
                    System.out.println("Resetting server " + servers.get(serverIndex));
                    socketManager.sendResetServer(serverIndex);
                });
            }

            CompletableFuture.allOf(futures5).join();

            ftpManager.closeFtpClients(ftpClients);
            socketManager.closeSocketsAndBuffers();

            communication_time = send_splits_timer.getLongestElapsedTime() + shuffle_one_timer.getLongestElapsedTime() + shuffle_two_timer.getLongestElapsedTime();
            computation_time = map_timer.getLongestElapsedTime() + reduce_one_timer.getLongestElapsedTime() + group_timer.getLongestElapsedTime() + reduce_two_timer.getLongestElapsedTime();
            synchronization_time = send_ip_timer.getLongestElapsedTime();

            System.out.println("Communication time: " + communication_time);
            System.out.println("Computation time: " + computation_time);
            System.out.println("Synchronization time: " + synchronization_time);

            PrintCSV.printResultsToCSV(servers.size(), communication_time, computation_time, synchronization_time);

        } else {
            System.out.println("Please provide the list of servers as an argument");
        }
    }
}
