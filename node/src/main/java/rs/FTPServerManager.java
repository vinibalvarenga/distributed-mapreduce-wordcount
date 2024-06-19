package rs;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ftplet.UserManager;


public class FTPServerManager {
    private final int ftpPort;
    private static final String USERNAME = "toto";
    private static final String PASSWORD = "tata";
    private static final String HOME_DIRECTORY = System.getProperty("java.io.tmpdir") + "/alvarenga-23/";

    public FTPServerManager(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public FtpServer createAndStartFTPServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(ftpPort);
        serverFactory.addListener("default", listenerFactory.createListener());

        UserManager userManager = UserManagerUtil.createUserManager(USERNAME, PASSWORD, HOME_DIRECTORY);
        serverFactory.setUserManager(userManager);

        FtpServer server = serverFactory.createServer();
        try {
            server.start();
            System.out.println("FTP Server started on port " + ftpPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server;
    }
}
