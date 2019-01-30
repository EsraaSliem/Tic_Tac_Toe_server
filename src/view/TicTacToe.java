package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Amer Shaker
 */
public class TicTacToe extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ServerView.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("Tic Tac Toe");
        stage.getIcons().add(new Image("images/tic-tac-toe.png"));
        stage.setResizable(false);
        stage.setScene(scene);

        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
