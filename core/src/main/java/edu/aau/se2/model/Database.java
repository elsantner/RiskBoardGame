package edu.aau.se2.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPlayerReadyListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.view.game.OnBoardInteractionListener;

public class Database implements OnBoardInteractionListener {
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
    private OnTerritoryUpdateListener territoryUpdateListener;
    private OnNextTurnListener nextTurnListener;

    private Player thisPlayer;
    private TreeMap<Integer, Player> currentPlayers;
    private ArrayList<Integer> turnOrder;
    private int currentTurnIndex;
    private int currentLobbyID;
    private Territory[] territoryData;
    private boolean initalArmyPlacementFinished;

    private int currentArmyReserve = 0;

    protected Database() {
        Random rand = new Random();
        thisPlayer = new Player(rand.nextInt(), "Player " + rand.nextInt());
        resetLobby();

        this.client = new NetworkClientKryo();
        SerializationRegister.registerClassesForComponent(client);
        registerClientCallback();
        try {
            this.client.connect(serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets all lobby-related data internally.
     */
    public void resetLobby() {
        currentPlayers = new TreeMap<>();
        turnOrder = null;
        currentTurnIndex = 0;
        initalArmyPlacementFinished = false;
        currentLobbyID = -1;
        initTerritoryData();
    }

    private void initTerritoryData() {
        territoryData = new Territory[42];
        for (int i=0; i<territoryData.length; i++) {
            territoryData[i] = new Territory(i+1);
        }
    }

    public void setOnGameStartListener(OnGameStartListener l) {
        this.gameStartListener = l;
    }
    public void setOnPlayerReadyListener(OnPlayerReadyListener l) {
        this.playerReadyListener = l;
    }
    public void setOnTerritoryUpdateListener(OnTerritoryUpdateListener l) {
        this.territoryUpdateListener = l;
    }
    public void setOnNextTurnListener(OnNextTurnListener l) {
        this.nextTurnListener = l;
    }

    private void registerClientCallback() {
        this.client.registerCallback(msg -> {
            if (msg instanceof StartGameMessage) {
                System.out.println("Received StartGameMessage");
                handleStartGameMessage((StartGameMessage) msg);
            }
            else if (msg instanceof ReadyMessage) {
                handleReadyMessage((ReadyMessage) msg);
            }
            else if (msg instanceof InitialArmyPlacingMessage) {
                handleInitialArmyPlacingMessage((InitialArmyPlacingMessage) msg);
            }
            else if (msg instanceof ArmyPlacedMessage) {
                handleArmyPlacedMessage((ArmyPlacedMessage) msg);
            }
        });
    }

    private void handleInitialArmyPlacingMessage(InitialArmyPlacingMessage msg) {
        turnOrder = msg.getPlayerOrder();
        if (nextTurnListener != null) {
            nextTurnListener.isPlayersTurnNow(turnOrder.get(0), thisPlayer.getUid() == turnOrder.get(0));
        }
    }

    private void handleArmyPlacedMessage(ArmyPlacedMessage msg) {
        if (territoryUpdateListener != null) {
            // adjust remaining army count if this player placed armies
            if (msg.getFromPlayerID() == thisPlayer.getUid()) {
                currentArmyReserve = msg.getArmyCountRemaining();
            }
            // update territory state
            territoryData[msg.getOnTerritoryID()-1].addToArmyCount(msg.getArmyCountPlaced());
            territoryData[msg.getOnTerritoryID()-1].setOccupierPlayerID(msg.getFromPlayerID());

            territoryUpdateListener.territoryUpdated(msg.getOnTerritoryID(),
                    territoryData[msg.getOnTerritoryID()-1].getArmyCount(),
                    currentPlayers.get(msg.getFromPlayerID()).getColorID());

            // if this message is part of initial army placement, initiate next turn
            if (!initalArmyPlacementFinished) {
                nextPlayersTurn();
                if (nextTurnListener != null) {
                    nextTurnListener.isPlayersTurnNow(turnOrder.get(currentTurnIndex),
                            thisPlayer.getUid() == turnOrder.get(currentTurnIndex));
                }
            }
        }
    }

    private void handleReadyMessage(ReadyMessage msg) {
        if (playerReadyListener != null) {
            playerReadyListener.playerReady(msg.getFromPlayerID(), msg.isReady());
        }
    }

    private void handleStartGameMessage(StartGameMessage msg) {
        currentArmyReserve = msg.getStartArmyCount();
        if (gameStartListener != null) {
            currentArmyReserve = msg.getStartArmyCount();
            for (Player p: msg.getPlayers()) {
                currentPlayers.put(p.getUid(), p);
            }
            gameStartListener.onGameStarted(msg.getPlayers(), msg.getStartArmyCount());
        }
    }

    private void nextPlayersTurn() {
        currentTurnIndex = (currentTurnIndex + 1) % currentPlayers.size();
    }

    public int getCurrentArmyReserve() {
        return currentArmyReserve;
    }

    public Player getPlayerByID(int playerID) {
        return currentPlayers.get(playerID);
    }

    public Player getCurrentPlayerToAct() {
        return currentPlayers.get(turnOrder.get(currentTurnIndex));
    }

    public Player getThisPlayer() {
        return thisPlayer;
    }

    public boolean isThisPlayersTurn() {
        return getCurrentPlayerToAct().getUid() == thisPlayer.getUid();
    }

    public void setPlayerReady(boolean ready) {
        client.sendMessage(new ReadyMessage(0, thisPlayer.getUid(), ready));
    }

    @Override
    public void armyPlaced(int territoryID, int count) {
        client.sendMessage(new ArmyPlacedMessage(currentLobbyID, thisPlayer.getUid(), territoryID, count));
    }

    @Override
    public void armyMoved(int fromTerritoryID, int toTerritoryID, int count) {

    }

    @Override
    public void attackStarted(int fromTerritoryID, int onTerritoryID) {

    }
}
