package rs;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<List<Integer>> initializeReducedRanges(int size) {
        List<List<Integer>> reducedRanges = new ArrayList<>(2);
        reducedRanges.add(new ArrayList<>(size)); // Primeira lista para os valores minimos
        reducedRanges.add(new ArrayList<>(size)); // Segunda lista para os valores maximos
        return reducedRanges;
    }
    
    public static void extractIntegersAndAddToList(String reduce, List<List<Integer>> reducedRanges, int serverIndex) {
        // Extrai os inteiros da string
        String[] parts = reduce.substring(1, reduce.length() - 1).split(", ");
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);
    
        // Adiciona os inteiros às respectivas listas
        reducedRanges.get(0).add(first);
        reducedRanges.get(1).add(second);
    }

    public static List<List<Integer>> calculateGroupRanges(int fmin, int fmax, int numServers) {
        int rangeSize = (fmax - fmin) / numServers;
        List<List<Integer>> ranges = new ArrayList<>();
    
        List<Integer> minValues = new ArrayList<>();
        List<Integer> maxValues = new ArrayList<>();
    
        int minValue = fmin;
        int maxValue = fmin + rangeSize;
        minValues.add(minValue);
        maxValues.add(maxValue);
        for (int i = 1; i < numServers; i++) {
            minValue = maxValue + 1;
            maxValue = (minValue + rangeSize) <= fmax ? minValue + rangeSize : fmax;

            minValues.add(minValue);
            maxValues.add(maxValue);
        }
    
        ranges.add(minValues);
        ranges.add(maxValues);
    
        return ranges;
    }
}