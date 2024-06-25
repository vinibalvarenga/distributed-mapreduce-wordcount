package rs.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rs.handlers.Handler;
import rs.utils.WordCountUtils;

public class SocketServerManager {
    private final int socketPort;
    private static final String FILE_PATH = System.getProperty("java.io.tmpdir") + "/alvarenga-23/random_lines.txt";
    private static List<String> knownServers = new ArrayList<>();
    private static String myIp;
    private static int myIndex;
    private Map<String, Integer> reduce_one;
    private boolean continueRunning = true;

    private FTPServerManager ftpServerManager;


    public SocketServerManager(int socketPort) {
        this.socketPort = socketPort;
    }

    public void setFtpServerManager(FTPServerManager ftpServerManager) {
        this.ftpServerManager = ftpServerManager;
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
            Handler handler = new Handler(FILE_PATH, ftpServerManager);
            String line = in.readLine();
            // System.out.println("Line received: " + line);
            if(line == null) {
                out.println("Error: client closed connection");
                continueRunning = false;
                return;
            }
            if (line.equals("START_IP_SENDING")) {
               // System.out.println("Received START_IP_SENDING");
                myIp = in.readLine();
                //System.out.println("Received my IP: " + myIp);
                int numServers = Integer.parseInt(in.readLine());
                for (int i = 0; i < numServers; i++) {
                    line = in.readLine(); // Read server IP
                    knownServers.add(line);
                    //System.out.println("Received IP: " + line);
                }
                line = in.readLine();
                if (line.equals("END_OF_IPS")) {
                    out.println(String.join(",", knownServers));
                  //  System.out.println("END_OF_IPS received");
                    Map<String, Integer> wordCount = handler.mapHandler();
                    handler.shuffle_one( wordCount, knownServers, myIp);
                    out.println("SHUFFLE_FINISHED");
                    return;
                }
            } else if(line.equals("START_REDUCE_ONE")){
              //  System.out.println("Received START_REDUCE_ONE");
                reduce_one = handler.reduce_one();
              //  System.out.println("reduce one: " + reduce_one);
                String reduce_intervals = WordCountUtils.takeReduceIntervals(reduce_one);
             //   System.out.println("FINISHED_REDUCE_ONE");
                out.println("FINISHED_REDUCE_ONE");
                out.println(reduce_intervals);
                return;
            } else if(line.equals("START_GROUP")){
             //   System.out.println("Received START_GROUP");
                myIndex = Integer.parseInt(in.readLine());
           //     System.out.println("Received server index: " + myIndex);
                List<List<Integer>> groupRanges = handler.group(in, out);
            //    System.out.println("FINISHED_GROUP");
                handler.shuffle_two(groupRanges, reduce_one, myIp, knownServers);
                out.println("FINISHED_SHUFFLE_TWO");
                return;
            } else if(line.equals("START_REDUCE_TWO")){
             //   System.out.println("Received START_REDUCE_TWO");
                List<Entry<String, Integer>> reduce_two = handler.reduce_two();
             //   System.out.println("reduce two: " + reduce_two);
             //   System.out.println("FINISHED_REDUCE_TWO");
                out.println("FINISHED_REDUCE_TWO");
                //out.println(WordCountUtils.takeReduceIntervals(reduce_two));
                return;
            }
            else {
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
