package rs.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rs.managers.FTPServerManager;

public class Handler {
    private final String filePath;
    private final MapHandler mapHandler;
    private final ShuffleHandler shuffleHandler;
    private final ReduceHandler reduceHandler;
    private final GroupHandler groupHandler;
    private final FTPServerManager ftpServerManager;

    public Handler(String filePath, FTPServerManager ftpServerManager) {
        this.filePath = filePath;
        this.mapHandler = new MapHandler(filePath);
        this.shuffleHandler = new ShuffleHandler();
        this.reduceHandler = new ReduceHandler();
        this.groupHandler = new GroupHandler();
        this.ftpServerManager = ftpServerManager;
    }

    public Map<String, Integer> mapHandler() {
        return mapHandler.process();
    }

    public void shuffle_one(Map<String, Integer> wordCount, List<String> knownServers, String fromNodeIp) {
        shuffleHandler.shuffle_one(wordCount, knownServers, fromNodeIp, ftpServerManager);
    }

    public Map<String, Integer> reduce_one() {
        return reduceHandler.reduce("firstWordCount");
    }

    public List<List<Integer>> group(BufferedReader in, PrintWriter out) throws IOException {
        return groupHandler.group(in, out);
    }

    public void shuffle_two(List<List<Integer>> groupRanges, Map<String, Integer> reduce_one, String myIP, List<String> knownServers) {
        shuffleHandler.shuffle_two(groupRanges, reduce_one, myIP, knownServers, ftpServerManager);
    }

    public List<Entry<String, Integer>> reduce_two() {
        List<Entry<String, Integer>> reduce_two = ReduceHandler.sortWordCounts(reduceHandler.reduce("groupWordCount"));
        reduceHandler.writeSortedWordCountsToFile(reduce_two);
        return reduce_two;
    }
}
