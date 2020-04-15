package edu.aau.se2.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.model.listener.OnPlayerReadyListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public class Database {
    private static Database instance = null;
    private static String serverAddress = "10.0.2.2";

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Sets the server address. This can only be done before a instance of Database is created.
     * Note: This method is designed to be used for testing purposes only!
     * @param serverAddress New server address
     */
    public static void setServerAddress(String serverAddress) throws IllegalStateException {
        if (instance != null) {
            throw new IllegalStateException("can only set server address before ever calling getInstance()");
        }
        Database.serverAddress = serverAddress;
    }

    private NetworkClientKryo client;
    private OnGameStartListener gameStartListener;
    private OnPlayerReadyListener playerReadyListener;
    private Player thisPlayer;

    private int currentArmyReserve = 0;

    private Database() {
        Random rand = new Random();
        thisPlayer = new Player(rand.nextInt(), "Player " + rand.nextInt());

        this.client = new NetworkClientKryo();
        SerializationRegister.registerClassesForComponent(client);
        registerClientCallback();
        try {
            this.client.connect(serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnGameStartListener(OnGameStartListener l) {
        this.gameStartListener = l;
    }
    public void setOnPlayerReadyListener(OnPlayerReadyListener l) {
        this.playerReadyListener = l;
    }

    private void registerClientCallback() {
        this.client.registerCallback(msg -> {
            if (msg instanceof StartGameMessage) {
                handleStartGameMessage((StartGameMessage) msg);
            }
            else if (msg instanceof ReadyMessage) {
                handleReadyMessage((ReadyMessage) msg);
            }
        });
    }

    private void handleReadyMessage(ReadyMessage msg) {
        if (playerReadyListener != null) {
            playerReadyListener.playerReady(msg.getFromPlayerID(), msg.isReady());
        }
    }

    private void handleStartGameMessage(StartGameMessage msg) {
        currentArmyReserve = msg.getStartArmyCount();
        if (gameStartListener != null) {
            gameStartListener.onGameStarted(msg.getPlayers(), msg.getStartArmyCount());
        }
    }

    public int getCurrentArmyReserve() {
        return currentArmyReserve;
    }

    public void setPlayerReady(boolean ready) {
        client.sendMessage(new ReadyMessage(0, thisPlayer.getUid(), ready));
    }
}
