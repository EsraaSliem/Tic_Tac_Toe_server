package controller;

import client.server.remote.interfaces.UserModel;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.DatabaseHandler;
import model.ServerModel;
import model.UserHandlerModel;
import utils.Constants;

/**
 * @author Amer Shaker
 */
public class ServerController implements Initializable {

    @FXML
    private Label sessionLabel;
    @FXML
    private Label onlineUsersLabel;
    @FXML
    private JFXListView<UserModel> usersListView;
    @FXML
    private JFXListView<UserModel> onlineUsersListView;
    @FXML
    private JFXListView<UserModel> offlineUsersListView;

    private static ServerController instance;
    private ServerModel server;
    private DatabaseHandler databaseHandler;
    public static ArrayList<UserModel> allUsersData;
    public static ArrayList<UserModel> offlineUsers;

    /**
     * Initializes the controller class.
     */
    public ServerController() {
        instance = this;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Runnable updater = new Runnable() {

                    @Override
                    public void run() {
                        if (server != null) {
                            sessionLabel.setText(server.getSession());
                            onlineUsersLabel.setText(UserHandlerModel.users.size() + "");
                        }
                    }
                };

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        System.err.println(ex.getMessage());
                    }

                    // UI update is run on the Application thread
                    Platform.runLater(updater);
                }
            }

        });
        // don't let thread prevent JVM shutdown
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleStartAction(ActionEvent event) throws SQLException {
        if (server == null) {
            server = ServerModel.getInstance();
            server.start();

            databaseHandler = DatabaseHandler.getInstance();
            allUsersData = databaseHandler.getUsersData();
            offlineUsers = databaseHandler.getOfflineUsers();

            refreshListView();
        }
    }

    @FXML
    private void handleShutDownAction(ActionEvent event) {
        try {
            UserHandlerModel.notifyClients();

            if (server != null && databaseHandler != null) {
                databaseHandler.setUsersOffline();

                // Remove the users from users ListView
                allUsersData.clear();
                offlineUsers.clear();

                UserHandlerModel.users.clear();
                UserHandlerModel.clients.clear();

                refreshListView();

                server.shutDown();
                server = null;

                onlineUsersLabel.setText("0");
                sessionLabel.setText(Constants.DEFAULT_TIME);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private class UserListItem extends ListCell<UserModel> {

        private Pane pane;
        private HBox hBox;
        private VBox vBox;
        private Label userNameLabel;
        private HBox hBox0;
        private Label userScoreLabel;
        private Label userScoreValueLabel;
        private VBox vBox0;
        private Image online = new Image("images/online.png", 32, 32, false, false);
        private Image offline = new Image("images/offline.png", 32, 32, false, false);
        private ImageView imageView;

        public UserListItem() {
            pane = new Pane();
            pane.setPrefHeight(75.0);
            pane.setPrefWidth(300.0);
            pane.setStyle("-fx-background-color: #ffffff;");

            hBox = new HBox();
            hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            hBox.setPrefHeight(75.0);
            hBox.setPrefWidth(300.0);
            hBox.setPadding(new Insets(10.0));

            vBox = new VBox();
            vBox.setPrefHeight(55.0);
            vBox.setPrefWidth(200.0);

            userNameLabel = new Label();
            userNameLabel.setMaxWidth(200.0);
            userNameLabel.setPadding(new Insets(5.0));
            userNameLabel.setFont(new Font("System Bold", 12.0));

            hBox0 = new HBox();
            hBox0.setPrefHeight(100.0);
            hBox0.setPrefWidth(200.0);

            userScoreLabel = new Label();
            userScoreLabel.setMaxWidth(50.0);
            userScoreLabel.setMinWidth(50.0);
            userScoreLabel.setText("Score");
            userScoreLabel.setTextFill(javafx.scene.paint.Color.valueOf("#03a9f4"));
            userScoreLabel.setPadding(new Insets(5.0));
            userScoreLabel.setFont(new Font("System Bold", 12.0));

            userScoreValueLabel = new Label();
            userScoreValueLabel.setMaxWidth(140.0);
            userScoreValueLabel.setMinWidth(140.0);
            userScoreValueLabel.setPadding(new Insets(5.0));

            HBox.setMargin(userScoreValueLabel, new Insets(0.0, 0.0, 0.0, 10.0));
            HBox.setMargin(vBox, new Insets(0.0));

            vBox0 = new VBox();
            vBox0.setAlignment(javafx.geometry.Pos.CENTER);
            vBox0.setPrefHeight(55.0);
            vBox0.setPrefWidth(70.0);

            imageView = new ImageView();
            imageView.setFitHeight(16.0);
            imageView.setFitWidth(16.0);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);

            HBox.setMargin(vBox0, new Insets(0.0, 0.0, 0.0, 10.0));

            vBox.getChildren().add(userNameLabel);
            hBox0.getChildren().add(userScoreLabel);
            hBox0.getChildren().add(userScoreValueLabel);
            vBox.getChildren().add(hBox0);
            hBox.getChildren().add(vBox);
            vBox0.getChildren().add(imageView);
            hBox.getChildren().add(vBox0);
            pane.getChildren().add(hBox);
        }

        @Override
        protected void updateItem(UserModel user, boolean empty) {
            if (user != null && !empty) {
                userNameLabel.setText(user.getUserName());
                userScoreValueLabel.setText(user.getScore() + "");

                if (user.isOnline()) {
                    imageView.setImage(online);
                } else {
                    imageView.setImage(offline);
                }

                setGraphic(pane);
            }
        }
    }

    public void refreshListView() {
        Platform.runLater(() -> {
            usersListView.setItems(FXCollections.observableArrayList(allUsersData));
            usersListView.setCellFactory(param -> new UserListItem());

            onlineUsersListView.setItems(FXCollections.observableArrayList(UserHandlerModel.users));
            onlineUsersListView.setCellFactory(param -> new UserListItem());

            offlineUsersListView.setItems(FXCollections.observableArrayList(offlineUsers));
            offlineUsersListView.setCellFactory(param -> new UserListItem());
        });
    }

    public static ServerController getInstance() {
        if (instance == null) {
            synchronized (ServerController.class) {
                if (instance == null) {
                    instance = new ServerController();
                }
            }
        }
        return instance;
    }
}
