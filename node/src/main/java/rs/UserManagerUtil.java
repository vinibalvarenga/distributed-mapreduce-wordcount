package rs;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserManagerUtil {
    public static UserManager createUserManager(String username, String password, String homeDirectory) {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        File userFile = new File("users.properties");
        createFileIfNotExists(userFile);

        userManagerFactory.setFile(userFile);
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());

        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = createUser(username, password, homeDirectory);
        saveUser(userManager, user);

        return userManager;
    }

    private static BaseUser createUser(String username, String password, String homeDirectory) {
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);

        createDirectoryIfNotExists(homeDirectory);

        user.setHomeDirectory(homeDirectory);
        user.setAuthorities(getUserAuthorities());

        return user;
    }

    private static List<Authority> getUserAuthorities() {
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        return authorities;
    }

    private static void saveUser(UserManager userManager, BaseUser user) {
        try {
            userManager.save(user);
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    private static void createFileIfNotExists(File file) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("File created: " + file.getName());
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory.getAbsolutePath());
            } else {
                System.out.println("Failed to create directory.");
            }
        }
    }
}
