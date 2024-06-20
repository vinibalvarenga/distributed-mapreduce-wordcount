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
    private boolean continueRunning = true;


    public SocketServerManager(int socketPort) {
        this.socketPort = socketPort;
    }

    public void startSocketServer() {
        try (ServerSocket serverSocket = new ServerSocket(socketPort)) {
            System.out.println("Socket Server started on port " + socketPort);
            while (continueRunning) {
                Socket socket = serverSocket.accept(); 
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while(continueRunning){handleRequest(in, out);}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(BufferedReader in, PrintWriter out) {
        try {
            String line = in.readLine();
            System.out.println("Line received: " + line);
            if(line == null) {
                out.println("Error: client closed connection");
                continueRunning = false;
                return;
            }
            if (line.equals("START_IP_SENDING")) {
               // System.out.println("Received START_IP_SENDING");
                myIp = in.readLine();
                //System.out.println("Received my IP: " + myIp);
                line = in.readLine(); // Read the number of servers
                int numServers = Integer.parseInt(line);
                for (int i = 0; i < numServers; i++) {
                    line = in.readLine(); // Read server IP
                    knownServers.add(line);
                    //System.out.println("Received IP: " + line);
                }
                line = in.readLine();
                if (line.equals("END_OF_IPS")) {
                    out.println(String.join(",", knownServers));
                    System.out.println("END_OF_IPS received");
                    Handler handler = new Handler(FILE_PATH);
                    Map<String, Integer> wordCount = handler.mapHandler();
                    handler.shuffle( wordCount, knownServers, myIp);
                    out.println("SHUFFLE_FINISHED");
                    return;
                }
            } else if(line.equals("START_REDUCE_ONE")){
                System.out.println("Received START_REDUCE_ONE");
                Handler fileHandler = new Handler(FILE_PATH);
                String reduce = fileHandler.reduce_one();
                System.out.println("FINISHED_REDUCE_ONE");
                out.println("FINISHED_REDUCE_ONE");
                out.println(reduce);
                return;
            } else {
                out.println("Error interpreting the request");
                continueRunning = false;
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println("Error interpreting the request");
    }
}
