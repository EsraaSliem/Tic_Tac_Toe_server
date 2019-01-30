package model;

import client.server.remote.interfaces.UserModel;
import utils.Constants;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author Mohamed Ramadan
 * @author Amer Shaker
 */
public class DatabaseHandler {

    private static DatabaseHandler instance;
    private String ipAddress;
    private int portNumber;
    private String databaseName;
    private String userName;
    private String password;
    private boolean autoCommit;

    // Connection and Statement for creating queries
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    private DatabaseHandler(String databaseName, String userName, String password) {
        this("localhost", 3306, databaseName, userName, password, true);
    }

    private DatabaseHandler(String ipAddress, int portNumber, String databaseName, String userName, String password, boolean autoCommit) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.databaseName = databaseName;
        this.userName = userName;
        this.password = password;
        this.autoCommit = autoCommit;

        // Initialize database connection and create a Statement object
        initializeDB();
    }

    private void initializeDB() {
        try {
            // Load the JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");

            // Establish a connection
            connection = DriverManager.getConnection("jdbc:mysql://" + ipAddress + ":" + portNumber + "/" + databaseName + "", userName, password);
            connection.setAutoCommit(autoCommit);
            System.out.println("Database connected");

            // Create a statement
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    // Close the connection
    public void closeConnection() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }

    public UserModel login(String emailAddress, String password) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM " + Constants.USER_TABLE
                + " WHERE " + Constants.EMAIL_ADDRESS + " = '" + emailAddress
                + "' AND " + Constants.PASSWORD + " = '" + password + "'");
        UserModel user = null;
        if (resultSet.next()) {
            user = new UserModel(resultSet.getString(Constants.IP_ADDRESS),
                    resultSet.getString(Constants.USER_NAME),
                    resultSet.getString(Constants.PASSWORD),
                    resultSet.getString(Constants.EMAIL_ADDRESS),
                    resultSet.getInt(Constants.WINED_MATCHES),
                    resultSet.getInt(Constants.LOSTED_MATCHES),
                    resultSet.getInt(Constants.SCORE),
                    resultSet.getInt(Constants.IS_ONLINE) == 1);
        }
        resultSet.afterLast();
        return user;
    }

    public boolean addNewUser(String emailAddress, String userName, String password, String ipAddressString) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + Constants.USER_TABLE
                + " (" + Constants.EMAIL_ADDRESS
                + ", " + Constants.USER_NAME
                + ", " + Constants.PASSWORD
                + ", " + Constants.IP_ADDRESS
                + ", " + Constants.IS_ONLINE
                + ", " + Constants.SCORE
                + ", " + Constants.WINED_MATCHES
                + ", " + Constants.LOSTED_MATCHES + ") VALUES (?, ?, ?, ?, 0, 0, 0, 0);");
        preparedStatement.setString(1, emailAddress);
        preparedStatement.setString(2, userName);
        preparedStatement.setString(3, password);
        preparedStatement.setString(4, ipAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean deleteUser(String emailAddress) throws SQLException {
        resultSet = statement.executeQuery("DELETE FROM " + Constants.USER_TABLE
                + " WHERE " + Constants.EMAIL_ADDRESS + " = '" + emailAddress + "'");
        return resultSet.next();
    }

    public UserModel getUserData(String emailAddress) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM " + Constants.USER_TABLE
                + " WHERE " + Constants.EMAIL_ADDRESS + " = '" + emailAddress + "'");
        UserModel user = null;
        if (resultSet.next()) {
            user = new UserModel(resultSet.getString(Constants.IP_ADDRESS),
                    resultSet.getString(Constants.USER_NAME),
                    resultSet.getString(Constants.PASSWORD),
                    resultSet.getString(Constants.EMAIL_ADDRESS),
                    resultSet.getInt(Constants.WINED_MATCHES),
                    resultSet.getInt(Constants.LOSTED_MATCHES),
                    resultSet.getInt(Constants.SCORE),
                    resultSet.getInt(Constants.IS_ONLINE) == 1);
        }
        resultSet.afterLast();
        return user;
    }

    public ArrayList<UserModel> getUsersData() throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM " + Constants.USER_TABLE
                + " ORDER BY " + Constants.SCORE + " DESC");
        ArrayList<UserModel> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(new UserModel(resultSet.getString(Constants.IP_ADDRESS),
                    resultSet.getString(Constants.USER_NAME),
                    resultSet.getString(Constants.PASSWORD),
                    resultSet.getString(Constants.EMAIL_ADDRESS),
                    resultSet.getInt(Constants.WINED_MATCHES),
                    resultSet.getInt(Constants.LOSTED_MATCHES),
                    resultSet.getInt(Constants.SCORE),
                    resultSet.getInt(Constants.IS_ONLINE) == 1));
        }
        resultSet.afterLast();
        return users;
    }

    boolean updateColumn(String emailAddress, String columnName, String columnValue) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + columnName + " = ? WHERE " + Constants.EMAIL_ADDRESS + " = ?");
        preparedStatement.setString(1, columnValue);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    boolean updateColumn(String emailAddress, String columnName, int columnValue) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + columnName + " = ? WHERE " + Constants.EMAIL_ADDRESS + " = ?");
        preparedStatement.setInt(1, columnValue);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean updateUserName(String emailAddress, String userName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.USER_NAME + " = " + "? WHERE " + Constants.EMAIL_ADDRESS + " = " + "?");
        preparedStatement.setString(1, userName);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean updatePassword(String emailAddress, String password) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.PASSWORD + " = " + "? WHERE " + Constants.EMAIL_ADDRESS + " = " + "?");
        preparedStatement.setString(1, password);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean updateUserScore(String emailAddress, int score) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.SCORE + " = " + "? WHERE " + Constants.EMAIL_ADDRESS + " = " + "?");
        preparedStatement.setInt(1, score);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean updateWinedMatches(String emailAddress, int numberOfWinedMatches) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.WINED_MATCHES + " = ? WHERE " + Constants.EMAIL_ADDRESS + " = ?");
        preparedStatement.setInt(1, numberOfWinedMatches);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean updateLostedMatches(String emailAddress, int numberOfLostedMatches) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.LOSTED_MATCHES + " = ? WHERE " + Constants.EMAIL_ADDRESS + " = ?");
        preparedStatement.setInt(1, numberOfLostedMatches);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean updateIPAddress(String emailAddress, String ipAddress) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.IP_ADDRESS + " = ? WHERE " + Constants.EMAIL_ADDRESS + " = ?");
        preparedStatement.setString(1, ipAddress);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public boolean updateUserStatue(String emailAddress, int status) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.IS_ONLINE + " = ? WHERE " + Constants.EMAIL_ADDRESS + " = ?");
        preparedStatement.setInt(1, status);
        preparedStatement.setString(2, emailAddress);
        return preparedStatement.executeUpdate() > 0;
    }

    public ArrayList<UserModel> getOnlineUsers() throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM " + Constants.USER_TABLE
                + " WHERE " + Constants.IS_ONLINE + " = " + 1 + " ORDER BY " + Constants.SCORE + " DESC");
        ArrayList<UserModel> onlineUsers = new ArrayList<>();
        while (resultSet.next()) {
            onlineUsers.add(new UserModel(resultSet.getString(Constants.IP_ADDRESS),
                    resultSet.getString(Constants.USER_NAME),
                    resultSet.getString(Constants.PASSWORD),
                    resultSet.getString(Constants.EMAIL_ADDRESS),
                    resultSet.getInt(Constants.WINED_MATCHES),
                    resultSet.getInt(Constants.LOSTED_MATCHES),
                    resultSet.getInt(Constants.SCORE),
                    resultSet.getInt(Constants.IS_ONLINE) == 1));
        }
        resultSet.afterLast();
        return onlineUsers;
    }

    public ArrayList<UserModel> getOfflineUsers() throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM " + Constants.USER_TABLE
                + " WHERE " + Constants.IS_ONLINE + " = " + 0 + " ORDER BY " + Constants.SCORE + " DESC");
        ArrayList<UserModel> onlineUsers = new ArrayList<>();
        while (resultSet.next()) {
            onlineUsers.add(new UserModel(resultSet.getString(Constants.IP_ADDRESS),
                    resultSet.getString(Constants.USER_NAME),
                    resultSet.getString(Constants.PASSWORD),
                    resultSet.getString(Constants.EMAIL_ADDRESS),
                    resultSet.getInt(Constants.WINED_MATCHES),
                    resultSet.getInt(Constants.LOSTED_MATCHES),
                    resultSet.getInt(Constants.SCORE),
                    resultSet.getInt(Constants.IS_ONLINE) == 1));
        }
        resultSet.afterLast();
        return onlineUsers;
    }

    public boolean setUsersOffline() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + Constants.USER_TABLE
                + " SET " + Constants.IS_ONLINE + " = ? WHERE " + Constants.IS_ONLINE + " = 1");
        preparedStatement.setInt(1, 0);
        return preparedStatement.executeUpdate() > 0;
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            synchronized (DatabaseHandler.class) {
                if (instance == null) {
                    instance = new DatabaseHandler(Constants.DATABASE_NAME,
                            Constants.DATABASE_USER_NAME,
                            Constants.DATABASE_PASSWORD);
                }
            }
        }
        return instance;
    }

    public int getUserScore(String email) throws SQLException {
        System.out.println(""+email);
        int score = 0;
        resultSet = statement.executeQuery("SELECT " + Constants.SCORE + " FROM " + Constants.USER_TABLE
                + " WHERE " + Constants.EMAIL_ADDRESS + " = '" + email + "'");

        while (resultSet.next()) {
            score = resultSet.getInt(Constants.SCORE);
            System.err.println("entered ");
        }
        System.out.println(""+score);
        return score;
    }

}
