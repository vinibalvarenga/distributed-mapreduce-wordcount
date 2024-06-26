package rs.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintCSV {
    public static void printResultsToCSV(int numberOfServers, long communicationTime, long computationTime, long synchronizationTime) {
    String filePath = "results.csv";
    File file = new File(filePath);
    boolean fileExists = file.exists();
    
    try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
        if (!fileExists) {
            // Se o arquivo não existir, escreva os cabeçalhos das colunas
            out.println("Number of Servers,Communication Time,Computation Time,Synchronization Time");
        }
        // Escreva os dados
        out.println(numberOfServers + "," + communicationTime + "," + computationTime + "," + synchronizationTime);
    } catch (IOException e) {
        System.err.println("Ocorreu um erro ao escrever no arquivo: " + e.getMessage());
    }
}
}
