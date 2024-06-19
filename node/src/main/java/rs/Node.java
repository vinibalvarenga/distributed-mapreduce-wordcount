package rs;

import org.apache.ftpserver.FtpServer;
import org.apache.log4j.PropertyConfigurator;

public class Node {
    private static final int FTP_PORT = 3456;
    private static final int SOCKET_PORT = 3457;

    public static void main(String[] args) {
        PropertyConfigurator.configure(Node.class.getResource("/log4J.properties"));

        FTPServerManager ftpServerManager = new FTPServerManager(FTP_PORT);
        FtpServer ftpServer = ftpServerManager.createAndStartFTPServer();

        SocketServerManager socketServerManager = new SocketServerManager(SOCKET_PORT);
        socketServerManager.startSocketServer();
    }
}
