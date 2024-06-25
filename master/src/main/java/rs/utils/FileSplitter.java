package rs.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileSplitter {
    public static List<String> readFileContents(String localfileName) {
        List<String> contents = new ArrayList<>();
        try {
            contents = Files.readAllLines(Paths.get(localfileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static List<List<String>> allocateSplitsToServers(List<String> contents, int numServers) {
        List<List<String>> serverSplits = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            serverSplits.add(new ArrayList<>());
        }

        for (int i = 0; i < contents.size(); i++) {
            serverSplits.get(i % numServers).add(contents.get(i));
        }

        return serverSplits;
    }
}
