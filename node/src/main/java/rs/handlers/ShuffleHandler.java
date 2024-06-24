package rs.handlers;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import rs.managers.FTPServerManager;
import rs.utils.WordCountUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShuffleHandler {
    public void shuffle_one(Map<String, Integer> wordCount, List<String> knownServers, String fromNodeIp,
            FTPServerManager ftpServerManager) {
        System.out.println("[Shuffling] words to servers");
        List<Map<String, Integer>> serverWordCounts = WordCountUtils.splitWordCounts(wordCount, knownServers.size());
        ftpServerManager.sendDataToServers(fromNodeIp, "singleWordCount",serverWordCounts, knownServers, true);
        System.out.println("[Shuffling] finished");
    }

    public void shuffle_two(List<List<Integer>> groupRanges, Map<String, Integer> reduce_one, String myIP,
            List<String> knownServers, FTPServerManager ftpServerManager) {
        System.out.println("[Shuffling] groups to servers");
        List<List<Map.Entry<String, Integer>>> serverEntries = new ArrayList<>();
        for (int i = 0; i < knownServers.size(); i++) {
            serverEntries.add(new ArrayList<>());
        }

        for (Map.Entry<String, Integer> entry : reduce_one.entrySet()) {
            for (int i = 0; i < groupRanges.size(); i++) {
                int min = groupRanges.get(i).get(0);
                int max = groupRanges.get(i).get(1);
                if (entry.getValue() >= min && entry.getValue() <= max) {
                    serverEntries.get(i).add(entry);
                    break;
                }
            }
        }

        ftpServerManager.sendDataToServers(myIP, "groupWordCount",serverEntries, knownServers, false);
        System.out.println("[Shuffling] finished");
    }

}