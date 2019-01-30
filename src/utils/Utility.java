package utils;

import client.server.remote.interfaces.UserModel;
import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 *
 * @author Amer Shaker
 */
public class Utility {

    public static void showNotification(String title, String message) {
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(message)
                .darkStyle()
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Pos.BOTTOM_RIGHT);
        notificationBuilder.showInformation();
    }

    /*public static void selectionSort(ArrayList<UserModel> usersList) {
        for (int i = 0; i < usersList.size() - 1; i++) {
            int currentMin = (int) usersList.get(i).getScore();
            int currentMinIndex = i;

            for (int j = i + 1; j < usersList.size(); j++) {
                if (currentMin > (int) usersList.get(j).getScore()) {
                    currentMin = usersList.get(j).ge
                }
            }
        }
    }*/
    public static int getUserIndex(ArrayList<UserModel> users, String emailAddress) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmailAddress().equalsIgnoreCase(emailAddress)) {
                return i;
            }
        }
        return -1;
    }
}
