package edu.aau.se2.model;

import com.badlogic.gdx.graphics.Color;
import java.io.IOException;
import java.util.ArrayList;

import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;

public class Database {
    private static Database instance = null;
    private static final String SERVER_ADDRESS = "10.0.2.2";

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private NetworkClientKryo client;
    private OnGameStartListener gameStartListener;
    private int currentArmyReserve = 0;

    private Database() {
        this.client = new NetworkClientKryo();
        registerClientClasses();
        registerClientCallback();
        try {
            this.client.connect(SERVER_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnGameStartListener(OnGameStartListener l) {
        this.gameStartListener = l;
    }

    private void registerClientCallback() {
        this.client.registerCallback(msg -> {
            if (msg instanceof StartGameMessage) {
                handleStartGameMessage((StartGameMessage) msg);
            }
        });
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

    private void registerClientClasses() {
        client.registerClass(Player.class);
        client.registerClass(ArrayList.class);
        client.registerClass(StartGameMessage.class);
    }
}
