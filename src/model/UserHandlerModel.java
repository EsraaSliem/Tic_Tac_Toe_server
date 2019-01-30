package model;

import client.server.remote.interfaces.ClientInterface;
import client.server.remote.interfaces.Step;
import client.server.remote.interfaces.UserAccountHandler;
import client.server.remote.interfaces.UserModel;
import controller.ServerController;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import utils.Constants;
import utils.Utility;

/**
 * @author Amer Shaker
 */
public class UserHandlerModel extends UnicastRemoteObject implements UserAccountHandler {

    private ServerController serverController;
    private DatabaseHandler databaseHandler;
    public static List<UserModel> users = new ArrayList<>();
    public static HashMap<String, ClientInterface> clients = new HashMap();
    private String userName;

    public UserHandlerModel() throws RemoteException {
        serverController = ServerController.getInstance();
        databaseHandler = DatabaseHandler.getInstance();
    }

    @Override
    public UserModel login(ClientInterface client, String emailAddress, String password) throws RemoteException {
        UserModel user = null;

        try {
            if (databaseHandler != null) {
                user = databaseHandler.login(emailAddress, password);
                userName = user.getUserName();
            }

            if (user != null && serverController != null) {

                if (user.isOnline()) {
                    return null;
                }

                databaseHandler.updateUserStatue(emailAddress, 1);
                user.setOnline(true);

                int userIndex = Utility.getUserIndex(ServerController.offlineUsers, user.getEmailAddress());
                ServerController.offlineUsers.remove(userIndex);

                int updatedUserIndex = Utility.getUserIndex(ServerController.allUsersData, user.getEmailAddress());
                UserModel updatedUser = ServerController.allUsersData.get(updatedUserIndex);
                updatedUser.setOnline(true);

                notifyClientsOnlineListChanges(user, true);

                users.add(user);
                clients.put(user.getEmailAddress(), client);
                serverController.refreshListView();

                Platform.runLater(() -> {
                    Utility.showNotification("Online Player", userName + " is Logged in");
                    AudioClip audio = new AudioClip(getClass().getResource("/sounds/notification-sound.mp3").toString());
                    audio.play();
                });

                System.out.println("User Name: " + user.getUserName()
                        + " Email Address: " + user.getEmailAddress() + " is Logged in");
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }

        return user;
    }

    @Override
    public boolean signUp(UserModel user) throws RemoteException {
        boolean isSuccess = false;
        if (user != null) {
            try {
                isSuccess = databaseHandler.addNewUser(user.getEmailAddress(),
                        user.getUserName(),
                        user.getPassword(),
                        user.getIpAddress());

                System.out.println("User Name: " + user.getUserName()
                        + " Email Address: " + user.getEmailAddress() + " is Signed up");
            } catch (SQLIntegrityConstraintViolationException ex) {
                System.err.println("Duplicate entry " + user.getEmailAddress() + " for key 'PRIMARY'");
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return isSuccess;
    }

    @Override
    public boolean logOut(String emailAddress) throws RemoteException {
        boolean isSuccess = false;
        try {
            isSuccess = databaseHandler.updateUserStatue(emailAddress, 0);

            UserModel user = getUserData(emailAddress);

            if (user != null) {
                user.setOnline(false);
                ServerController.offlineUsers.add(user);

                int user2 = Utility.getUserIndex(ServerController.allUsersData, user.getEmailAddress());
                UserModel user2Data = ServerController.allUsersData.get(user2);
                user2Data.setOnline(false);

                int userIndex = Utility.getUserIndex((ArrayList<UserModel>) users, emailAddress);
                users.remove(userIndex);
                clients.remove(emailAddress);

                notifyClientsOnlineListChanges(user, false);

                if (isSuccess && serverController != null) {
                    serverController.refreshListView();

                    Platform.runLater(() -> {
                        Utility.showNotification("Player Log out", user.getUserName() + " is Logged out");
                        AudioClip audio = new AudioClip(getClass().getResource("/sounds/notification-sound.mp3").toString());
                        audio.play();
                    });

                    System.out.println(emailAddress + " is Loged Out");
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return isSuccess;
    }

    @Override
    public List<UserModel> getOnlinePlayers() throws RemoteException {
        ArrayList<UserModel> onlineUsers = null;
        try {
            onlineUsers = databaseHandler.getOnlineUsers();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return onlineUsers;
    }

    @Override
    public int requestGame(UserModel player1, UserModel player2) throws RemoteException {
        
        int isSuccess ;
        ClientInterface firstClient = clients.get(player1.getEmailAddress());
        ClientInterface secondClient = clients.get(player2.getEmailAddress());

        isSuccess = secondClient.requestGame(player1, player2);
        //o playing 1 accept  2 refused
        if (isSuccess==1) {
            player1.setStatus(Constants.IS_PLAYING);
            player2.setStatus(Constants.IS_PLAYING);
            firstClient.startGame(player1, player2);
            secondClient.startGame(player1, player2);
            System.out.println("Player 2 accepted");
            return 1;
        } else if(isSuccess==2) {
            System.err.println("Player 2 refused request");
            return 2;
        }
        else if(isSuccess==0){
            System.out.println("Player 2 is busy");
            return 0;
        }
        return isSuccess;
        
    }

    @Override
    public void transmitMove(Step step) throws RemoteException {
        ClientInterface firstPlayer = clients.get(step.getCurrentPlayer());
        ClientInterface secondPlayer = clients.get(step.getPlayer());

        if (firstPlayer != null && secondPlayer != null) {
            firstPlayer.drawMove(step);
            secondPlayer.drawMove(step);
        }
    }

    @Override
    public void sendMessage(UserModel player1, UserModel player2, String message) throws RemoteException {
        ClientInterface firstPlayer = clients.get(player1.getEmailAddress());
        ClientInterface secondPlayer = clients.get(player2.getEmailAddress());

        if (firstPlayer != null && secondPlayer != null) {
            firstPlayer.receiverMessage(player1, message);
            secondPlayer.receiverMessage(player1, message);
        }
    }

    @Override
    public boolean isServerUponRunning() throws RemoteException {
        return ServerModel.getInstance().isRunning();
    }

    @Override
    public int increaseWinnerScore(String emailAddress) throws RemoteException {
        int userScore = 0;
        try {
            if (emailAddress != null) {
                UserModel user = databaseHandler.getUserData(emailAddress);
                
                if (user != null) {
                    userScore = (int) (user.getScore() + 10);
                    databaseHandler.updateUserScore(emailAddress, userScore);
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return userScore;
    }

    @Override
    public void closeGame(UserModel player1, UserModel player2) throws RemoteException {
        if (player1 != null && player2 != null) {
            clients.get(player2.getEmailAddress()).closeGame();

            // Change players status
            player1.setStatus(Constants.AVAILABLE);
            player2.setStatus(Constants.AVAILABLE);
        }
    }

    private UserModel getUserData(String emailAddress) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmailAddress().equals(emailAddress)) {
                return users.get(i);
            }
        }
        return null;
    }

    public static void notifyClients() {
        if (clients != null) {
            try {
                for (Map.Entry<String, ClientInterface> client : clients.entrySet()) {
                    client.getValue().serverLogOut();
                }
            } catch (RemoteException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    public static void notifyClientsOnlineListChanges(UserModel user, boolean isLoggedIn) {
        if (clients != null) {
            try {
                for (Map.Entry<String, ClientInterface> client : clients.entrySet()) {
                    client.getValue().refreshOnlineUsersList(user, isLoggedIn);
                }
            } catch (RemoteException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    @Override
    public int getUpdatedScore(String emailAddress) throws RemoteException {
        int score=0;
         System.out.println(""+emailAddress);
        try {
            
          score=databaseHandler.getUserScore(emailAddress);
            System.out.println(""+score);
        } catch (SQLException ex) {
            Logger.getLogger(UserHandlerModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return score;
    }
}
