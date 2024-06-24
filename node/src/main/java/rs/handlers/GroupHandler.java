package rs.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GroupHandler {
    public List<List<Integer>> group(BufferedReader in, PrintWriter out) throws IOException {
        String line;
        List<List<Integer>> groupRanges = new ArrayList<>();
        while (!(line = in.readLine()).equals("FINISHED_GROUP")) {
            List<Integer> range = Arrays.stream(line.split(","))
                                        .map(Integer::parseInt)
                                        .collect(Collectors.toList());
            groupRanges.add(range);
        }
        System.out.println("Received group ranges: " + groupRanges);
        return groupRanges;
    }
}