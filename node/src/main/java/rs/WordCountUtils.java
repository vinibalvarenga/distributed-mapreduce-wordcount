package rs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCountUtils {
    public static Map<String, Integer> mapFunction(String line) {
        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = line.split(" ");
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
        return wordCount;
    }

    public static void mergeWordCounts(Map<String, Integer> totalWordCount, Map<String, Integer> lineWordCount) {
        for (Map.Entry<String, Integer> entry : lineWordCount.entrySet()) {
            totalWordCount.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }

    public static List<Map<String, Integer>> splitWordCounts(Map<String, Integer> wordCount, int numberOfServers) {
        List<Map<String, Integer>> serverWordCounts = new ArrayList<>();
        for (int i = 0; i < numberOfServers; i++) {
            serverWordCounts.add(new HashMap<>());
        }
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            int serverIndex = Math.abs(word.hashCode()) % numberOfServers;
            serverWordCounts.get(serverIndex).put(word, count);
        }
        return serverWordCounts;
    }

    public static String mapToString(Map<String, Integer> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }

    public static String takeReduceIntervals(Map<String, Integer> wordCounts) {
        if (wordCounts.isEmpty()) {
            return "[0, 0]";
        } else {
            int min = Collections.min(wordCounts.values());
            int max = Collections.max(wordCounts.values());
            return "[" + min + ", " + max + "]";
        }
    }
}
