package rs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class Handler {
    private final String filePath;
    private final MapHandler mapHandler;
    private final ShuffleHandler shuffleHandler;
    private final ReduceHandler reduceHandler;
    private final GroupHandler groupHandler;

    public Handler(String filePath) {
        this.filePath = filePath;
        this.mapHandler = new MapHandler(filePath);
        this.shuffleHandler = new ShuffleHandler();
        this.reduceHandler = new ReduceHandler();
        this.groupHandler = new GroupHandler();
    }

    public Map<String, Integer> mapHandler() {
        return mapHandler.process();
    }

    public void shuffle(Map<String, Integer> wordCount, List<String> knownServers, String fromNodeIp) {
        shuffleHandler.shuffle(wordCount, knownServers, fromNodeIp);
    }

    public Map<String, Integer> reduce_one() {
        return reduceHandler.reduce();
    }

    public List<List<Integer>> group(BufferedReader in, PrintWriter out) throws IOException {
        return groupHandler.group(in, out);
    }

    public void shuffle_two(List<List<Integer>> groupRanges, int myIndex, Map<String, Integer> reduce_one, String myIP, List<String> knownServers) {
        System.out.println("TODO: Implement shuffle_two");
    }
}
