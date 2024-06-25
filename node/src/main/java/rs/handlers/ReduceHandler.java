package rs.handlers;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ReduceHandler {
    public Map<String, Integer> reduce(String file_prefix) {
        Map<String, Integer> wordCounts = new HashMap<>();
        String filePattern = file_prefix + "-From-.*-To-.*\\.txt";
        try (Stream<Path> paths = Files.walk(Paths.get("."))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> Pattern.matches(filePattern, p.getFileName().toString()))
                 .forEach(filePath -> {
                    processFile(filePath, wordCounts, file_prefix);
                    System.out.println("wordCounts after processing " + filePath + ": " + wordCounts);
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCounts;
    }

    private void processFile(Path filePath, Map<String, Integer> wordCounts, String file_prefix) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, wordCounts, file_prefix);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line, Map<String, Integer> wordCounts, String file_prefix) {
        String[] parts;
        if (file_prefix.equals("firstWordCount")){
            parts = line.split(": ");
        } else {
            parts = line.split("=");
        }
        
        String word = parts[0];
        int count = Integer.parseInt(parts[1]);
        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + count);
    }


}
