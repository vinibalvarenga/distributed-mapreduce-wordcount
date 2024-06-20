package rs;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MapHandler {
    private final String filePath;

    public MapHandler(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, Integer> process() {
        System.out.println("[Starting Map]");
        File file = new File(filePath);
        Map<String, Integer> wordCount = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Map<String, Integer> lineWordCount = WordCountUtils.mapFunction(line);
                WordCountUtils.mergeWordCounts(wordCount, lineWordCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCount;
    }
}
