package model;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.Instant;
import utils.Constants;

/**
 *
 * @author Amer Shaker
 */
public class ServerModel {

    private static ServerModel instance;
    private Registry registry;
    private Instant start;
    private String session;
    private boolean isRunning;

    private ServerModel() {

    }

    public String getSession() {
        if (start != null) {
            long seconds = Duration.between(start, Instant.now()).getSeconds();
            session = String.format(Constants.TIME_FORMAT, seconds / 3600, (seconds % 3600) / 60, (seconds % 60));;
        }
        return session;
    }

    public void start() {
        try {
            System.out.println("Server Started");
            registry = LocateRegistry.createRegistry(Constants.PORT);
            registry.rebind(Constants.ACCOUNT_SERVICE, new UserHandlerModel());

            // Server start time
            start = Instant.now();
            isRunning = true;
        } catch (RemoteException ex) {
            System.err.println("Port already in use: " + Constants.PORT);
        }
    }

    public void shutDown() {
        try {
            registry.unbind(Constants.ACCOUNT_SERVICE);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (RemoteException | NotBoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public static ServerModel getInstance() {
        if (instance == null) {
            synchronized (ServerModel.class) {
                if (instance == null) {
                    instance = new ServerModel();
                }
            }
        }
        return instance;
    }
}
