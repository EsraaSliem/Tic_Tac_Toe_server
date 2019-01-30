package utils;

/**
 * @author Amer Shaker
 */
public interface Constants {

    public final static String SERVER_IP_ADDRESS = "127.0.0.1";
    public final static int PORT = 5000;

    // Server Services
    public final static String ACCOUNT_SERVICE = "Account";

    // User Status
    public final static String AVAILABLE = "Available";
    public final static String IS_PLAYING = "is Playing";

    // Database Constants
    // Database Name
    public final static String DATABASE_NAME = "client";
    public final static String DATABASE_USER_NAME = "root";
    public final static String DATABASE_PASSWORD = "";

    // Database Tables
    public final static String USER_TABLE = "client";

    // User Table Columns
    public final static String EMAIL_ADDRESS = "EMAIL";
    public final static String USER_NAME = "USER_NAME";
    public final static String PASSWORD = "PASSWORD";
    public final static String IP_ADDRESS = "IP_ADDRESS";
    public final static String IS_ONLINE = "IS_ONLINE";
    public final static String SCORE = "SCORE";
    public final static String WINED_MATCHES = "WINED_MATCHES";
    public final static String LOSTED_MATCHES = "LOSTED_MATCHES";

    public final static String TIME_FORMAT = "%02d:%02d:%02d";
    public final static String DEFAULT_TIME = "00:00:00";
}
