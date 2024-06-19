package rs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SocketServerManager {
    private final int socketPort;
    private static final String FILE_PATH = System.getProperty("java.io.tmpdir") + "/alvarenga-23/random_lines.txt";
    private static List<String> knownServers = new ArrayList<>();
    private static String myIp;

    public SocketServerManager(int socketPort) {
        this.socketPort = socketPort;
    }

    public void startSocketServer() {
        try (ServerSocket serverSocket = new ServerSocket(socketPort)) {
            System.out.println("Socket Server started on port " + socketPort);
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    handleRequest(in, out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(BufferedReader in, PrintWriter out) {
        try {
            String line = in.readLine();
            System.out.println("Received request: " + line);
            if (line.equals("START_IP_SENDING")) {
                System.out.println("Received START_IP_SENDING");
                myIp = in.readLine();
                System.out.println("Received my IP: " + myIp);
                line = in.readLine(); // Read the number of servers
                int numServers = Integer.parseInt(line);
                for (int i = 0; i < numServers; i++) {
                    line = in.readLine(); // Read server IP
                    knownServers.add(line);
                    System.out.println("Received IP: " + line);
                }
                line = in.readLine();
                if (line.equals("END_OF_IPS")) {
                    out.println(String.join(",", knownServers));
                    System.out.println("END_OF_IPS received");
                    FileHandler fileHandler = new FileHandler(FILE_PATH);
                    Map<String, Integer> wordCount = fileHandler.mapHandler();
                    fileHandler.shuffle( wordCount, knownServers, myIp);
                    out.println("SHUFFLE_FINISHED");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("Error interpreting the request");
    }
}
