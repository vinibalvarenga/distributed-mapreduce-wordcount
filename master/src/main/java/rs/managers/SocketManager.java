package rs.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SocketManager {
    private static final int SOCKET_PORT = 3457;
    private List<String> servers;
    private List<Socket> sockets = new ArrayList<>();
    private List<BufferedReader> readers = new ArrayList<>();
    private List<BufferedWriter> writers = new ArrayList<>();

    public SocketManager(List<String> servers) {
        this.servers = servers;
    }

    public void openSocketsAndBuffers() {
        for (String server : servers) {
            try {
                Socket socket = new Socket(server, SOCKET_PORT);
                sockets.add(socket);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                readers.add(reader);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writers.add(writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendServerIPsAndStartMapFunction(int serverIndex) {
        try {
            BufferedWriter writer = writers.get(serverIndex);

            writer.write("START_IP_SENDING\n");
            writer.flush();
            // Send the IP address of the server
            writer.write(servers.get(serverIndex) + "\n");
            writer.flush();
            // Send the number of servers
            writer.write(servers.size() + "\n");
            writer.flush();

            for (String server : servers) {
                writer.write(server + "\n");
                writer.flush();
            }

            writer.write("END_OF_IPS\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveShuffleOneCompleteMessages(int serverIndex) {
        try {
            BufferedReader reader = readers.get(serverIndex);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("SHUFFLE_FINISHED")) {
                    System.out.println("Shuffle complete received from server " + servers.get(serverIndex) + ".");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String reduce_one(int serverIndex) {
        try {
            BufferedWriter writer = writers.get(serverIndex);
            writer.write("START_REDUCE_ONE\n");
            writer.flush();

            BufferedReader reader = readers.get(serverIndex);
            String line;
            while ((line = reader.readLine()) != "FINISHED_REDUCE_ONE") {
                System.out.println("Line received: " + line);
                line = reader.readLine();
                System.out.println(
                        "Reduce one complete received from server " + servers.get(serverIndex) + " , result: " + line);
                return line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void group(int serverIndex, List<List<Integer>> groupRanges) {
        try {
            BufferedWriter writer = writers.get(serverIndex);
            writer.write("START_GROUP\n");
            writer.flush();

            // Send the server index
            writer.write(serverIndex + "\n");
            writer.flush();

            // Convert groupRanges to a string and send it
            for (List<Integer> range : groupRanges) {
                String rangeStr = range.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                writer.write(rangeStr + "\n");
                writer.flush();
            }

            writer.write("FINISHED_GROUP\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reduce_two(int serverIndex) {
        try {
            BufferedWriter writer = writers.get(serverIndex);
            writer.write("START_REDUCE_TWO\n");
            writer.flush();

            BufferedReader reader = readers.get(serverIndex);
            String line;
            while ((line = reader.readLine()) != "FINISHED_REDUCE_TWO") {
                System.out.println("Line received: " + line);
                System.out.println(
                        "Reduce two complete received from server " + servers.get(serverIndex) + " , result: " + line);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();}
    }

    public void closeSocketsAndBuffers() {
        for (int i = 0; i < servers.size(); i++) {
            try {
                readers.get(i).close();
                writers.get(i).close();
                sockets.get(i).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveShuffleTwoCompleteMessage(int serverIndex) {
        try {
            BufferedReader reader = readers.get(serverIndex);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("FINISHED_SHUFFLE_TWO")) {
                    System.out.println("Shuffle two complete received from server " + servers.get(serverIndex) + ".");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
