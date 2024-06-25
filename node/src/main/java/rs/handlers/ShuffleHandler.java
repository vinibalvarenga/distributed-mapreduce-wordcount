package rs.handlers;

import rs.managers.FTPServerManager;
import rs.utils.WordCountUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShuffleHandler {
    public void shuffle_one(Map<String, Integer> wordCount, List<String> knownServers, String fromNodeIp,
            FTPServerManager ftpServerManager) {
    //    System.out.println("[Shuffling] words to servers");
        List<Map<String, Integer>> serverWordCounts = WordCountUtils.splitWordCounts(wordCount, knownServers.size());
        ftpServerManager.sendDataToServers(fromNodeIp, "firstWordCount",serverWordCounts, knownServers, true);
    //    System.out.println("[Shuffling] finished");
    }

    public void shuffle_two(List<List<Integer>> groupRanges, Map<String, Integer> reduce_one, String myIP,
            List<String> knownServers, FTPServerManager ftpServerManager) {
    //    System.out.println("[Shuffling] groups to servers");
        List<List<Map.Entry<String, Integer>>> serverEntries = new ArrayList<>();
        for (int i = 0; i < knownServers.size(); i++) {
            serverEntries.add(new ArrayList<>());
        }

        for (Map.Entry<String, Integer> entry : reduce_one.entrySet()) {
            for (int i = 0; i < groupRanges.get(0).size(); i++) {
                int min = groupRanges.get(0).get(i);
                int max = groupRanges.get(1).get(i);
                if (entry.getValue() >= min && entry.getValue() <= max) {
                    serverEntries.get(i).add(entry);
                    break;
                }
            }
        }

      //  System.out.println("Server entries: " + serverEntries);

        ftpServerManager.sendDataToServers(myIP, "groupWordCount",serverEntries, knownServers, false);
      //  System.out.println("[Shuffling] finished");
    }

}