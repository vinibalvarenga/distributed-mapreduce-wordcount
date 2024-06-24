package rs;

import java.io.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ReduceHandler {
    public Map<String, Integer> reduce() {
        Map<String, Integer> wordCounts = new HashMap<>();
        String filePattern = "wordCount-From-.*-To-.*\\.txt";
        try (Stream<Path> paths = Files.walk(Paths.get("."))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> Pattern.matches(filePattern, p.getFileName().toString()))
                 .forEach(filePath -> {
                    processFile(filePath, wordCounts);
                    System.out.println("wordCounts after processing " + filePath + ": " + wordCounts);
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCounts;
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


}
