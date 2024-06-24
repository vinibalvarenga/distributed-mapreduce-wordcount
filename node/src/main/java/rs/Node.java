package rs;

import org.apache.log4j.PropertyConfigurator;

import rs.managers.FTPServerManager;
import rs.managers.SocketServerManager;

public class Node {
    private static final int FTP_PORT = 3456;
    private static final int SOCKET_PORT = 3457;

    public static void main(String[] args) {
        PropertyConfigurator.configure(Node.class.getResource("/log4J.properties"));

        FTPServerManager ftpServerManager = new FTPServerManager(FTP_PORT);
        ftpServerManager.createAndStartFTPServer();

        SocketServerManager socketServerManager = new SocketServerManager(SOCKET_PORT);
        socketServerManager.setFtpServerManager(ftpServerManager);
        socketServerManager.startSocketServer();
    }
}
