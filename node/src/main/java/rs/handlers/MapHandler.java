package rs.handlers;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import rs.utils.WordCountUtils;

public class MapHandler {
    private final String filePath;

    public MapHandler(String filePath) {
        this.filePath = filePath;
    }

    public void removeUnwantedCharacters() {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Remove os caracteres ":" e "=" substituindo por uma string vazia
                String modifiedLine = line.replace(":", "").replace("=", "");
                contentBuilder.append(modifiedLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        overwriteFileWithModifiedContent(contentBuilder.toString());
    }

    private void overwriteFileWithModifiedContent(String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> processFileAndCountWords() {
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

    public Map<String, Integer> process() {
        removeUnwantedCharacters(); // Primeiro, remove os caracteres indesejados
        return processFileAndCountWords(); // Depois, processa o arquivo e conta as palavras
    }
}