package rs;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;

public class MyFTPServer {
    public static void main(String[] args) {
    FtpServerFactory serverFactory = new FtpServerFactory();
    int port = 3456; // Replace 3456 with the desired port number

    ListenerFactory listenerFactory = new ListenerFactory();
    listenerFactory.setPort(port);

    serverFactory.addListener("default", listenerFactory.createListener());

    // Set the user manager
    MyUserManager userManager = new MyUserManager("admin", new ClearTextPasswordEncryptor());
    serverFactory.setUserManager(userManager);

    FtpServer server = serverFactory.createServer();

    // start the server
    try {
        server.start();
        System.out.println("FTP Server started on port " + port);
    } catch (FtpException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    }
}