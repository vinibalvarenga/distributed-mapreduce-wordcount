package rs;

import java.util.List;
import java.util.Map;

public class Handler {
    private final String filePath;
    private final MapHandler mapHandler;
    private final ShuffleHandler shuffleHandler;
    private final ReduceHandler reduceHandler;

    public Handler(String filePath) {
        this.filePath = filePath;
        this.mapHandler = new MapHandler(filePath);
        this.shuffleHandler = new ShuffleHandler();
        this.reduceHandler = new ReduceHandler();
    }

    public Map<String, Integer> mapHandler() {
        return mapHandler.process();
    }

    public void shuffle(Map<String, Integer> wordCount, List<String> knownServers, String fromNodeIp) {
        shuffleHandler.shuffle(wordCount, knownServers, fromNodeIp);
    }

    public String reduce_one() {
        return reduceHandler.reduce();
    }
}
