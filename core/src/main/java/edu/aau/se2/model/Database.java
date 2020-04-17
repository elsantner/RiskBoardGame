package edu.aau.se2.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.model.listener.OnJoinedLobbyListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPlayersChangedListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.NetworkClient;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.ConnectedMessage;
import edu.aau.se2.server.networking.dto.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.PlayersChangedMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.server.networking.kryonet.NetworkConstants;
import edu.aau.se2.view.game.OnBoardInteractionListener;

public class Database implements OnBoardInteractionListener, NetworkClient.OnConnectionChangedListener {
    private static Database instance = null;
    private static String serverAddress = NetworkConstants.SERVER_IP;

    /**
     * Gets the singleton instance of Database.
     * @return Database instance, or null if connection error occurred.
     */
    public static synchronized Database getInstance() {
        if (instance == null) {
            try {
                instance = new Database();
            } catch (IOException e) {
                instance = null;
            }
        }
        return instance;
    }

    /**
     * Sets the server address. This can only be done before a instance of Database is created.
     * Note: This method is designed to be used for testing purposes only!
     * @param serverAddress New server address
     */
    public static void setServerAddress(String serverAddress) {
        if (instance != null) {
            throw new IllegalStateException("can only set server address before ever calling getInstance()");
        }
        Database.serverAddress = serverAddress;
    }

    private NetworkClientKryo client;
    private boolean isConnected;
    private OnGameStartListener gameStartListener;
    private OnPlayersChangedListener playersChangedListener;
    private OnTerritoryUpdateListener territoryUpdateListener;
    private OnNextTurnListener nextTurnListener;
    private OnJoinedLobbyListener joinedLobbyListener;
    private OnConnectionChangedListener connectionChangedListener;

    private Player thisPlayer;
    private TreeMap<Integer, Player> currentPlayers;
    private List<Integer> turnOrder;
    private int currentTurnIndex;
    private int currentLobbyID;
    private Territory[] territoryData;
    private boolean initialArmyPlacementFinished;

    private int currentArmyReserve = 0;

    protected Database() throws IOException {
        resetLobby();
        isConnected = false;

        this.client = new NetworkClientKryo();
        client.registerConnectionListener(this);
        SerializationRegister.registerClassesForComponent(client);
        registerClientCallback();
        this.client.connect(serverAddress);
    }

    /**
     * Resets all lobby-related data internally.
     */
    public void resetLobby() {
        currentPlayers = new TreeMap<>();
        turnOrder = null;
        currentTurnIndex = 0;
        initialArmyPlacementFinished = false;
        currentLobbyID = -1;
        initTerritoryData();
    }

    private void initTerritoryData() {
        territoryData = new Territory[42];
        for (int i=0; i<territoryData.length; i++) {
            territoryData[i] = new Territory(i+1);
        }
    }

    public void setConnectionChangedListener(OnConnectionChangedListener l) {
        this.connectionChangedListener = l;
        if (isConnected) {
            connectionChangedListener.connected(thisPlayer);
        }
        else {
            connectionChangedListener.disconnected();
        }
    }
    public void setGameStartListener(OnGameStartListener l) {
        this.gameStartListener = l;
    }
    public void setPlayersChangedListener(OnPlayersChangedListener l) {
        this.playersChangedListener = l;
    }
    public void setTerritoryUpdateListener(OnTerritoryUpdateListener l) {
        this.territoryUpdateListener = l;
    }
    public void setNextTurnListener(OnNextTurnListener l) {
        this.nextTurnListener = l;
    }

    public void setJoinedLobbyListener(OnJoinedLobbyListener l) {
        this.joinedLobbyListener = l;
    }

    private void registerClientCallback() {
        this.client.registerCallback(msg -> {
            if (msg instanceof ConnectedMessage) {
                handleConnectedMessage((ConnectedMessage) msg);
            }
            else if (msg instanceof StartGameMessage) {
                handleStartGameMessage((StartGameMessage) msg);
            }
            else if (msg instanceof InitialArmyPlacingMessage) {
                handleInitialArmyPlacingMessage((InitialArmyPlacingMessage) msg);
            }
            else if (msg instanceof ArmyPlacedMessage) {
                handleArmyPlacedMessage((ArmyPlacedMessage) msg);
            }
            else if (msg instanceof PlayersChangedMessage) {
                handlePlayersChangedMessage((PlayersChangedMessage) msg);
            }
            else if (msg instanceof JoinedLobbyMessage) {
                handleJoinedLobby((JoinedLobbyMessage) msg);
            }
        });
    }

    private void handleJoinedLobby(JoinedLobbyMessage msg) {
        this.currentLobbyID = msg.getLobbyID();
        setCurrentPlayers(msg.getPlayers());
        if (joinedLobbyListener != null) {
            joinedLobbyListener.joinedLobby(msg.getLobbyID(), msg.getHost(), msg.getPlayers());
        }
    }

    private void handleConnectedMessage(ConnectedMessage msg) {
        thisPlayer = msg.getPlayer();
        if (connectionChangedListener != null) {
            connectionChangedListener.connected(thisPlayer);
        }
    }

    private synchronized void setCurrentPlayers(List<Player> players) {
        currentPlayers.clear();
        for (Player p: players) {
            currentPlayers.put(p.getUid(), p);
        }
    }

    private synchronized void handlePlayersChangedMessage(PlayersChangedMessage msg) {
        setCurrentPlayers(msg.getPlayers());
        if (playersChangedListener != null) {
            playersChangedListener.playersChanged(new ArrayList<>(currentPlayers.values()));
        }
    }

    private synchronized void handleInitialArmyPlacingMessage(InitialArmyPlacingMessage msg) {
        turnOrder = msg.getPlayerOrder();
        if (nextTurnListener != null) {
            nextTurnListener.isPlayersTurnNow(turnOrder.get(0), thisPlayer.getUid() == turnOrder.get(0));
        }
    }

    private synchronized void handleArmyPlacedMessage(ArmyPlacedMessage msg) {
        if (territoryUpdateListener != null) {
            // adjust remaining army count if this player placed armies
            if (msg.getFromPlayerID() == thisPlayer.getUid()) {
                currentArmyReserve = msg.getArmyCountRemaining();
            }
            // update territory state
            territoryData[msg.getOnTerritoryID()].addToArmyCount(msg.getArmyCountPlaced());
            territoryData[msg.getOnTerritoryID()].setOccupierPlayerID(msg.getFromPlayerID());

            territoryUpdateListener.territoryUpdated(msg.getOnTerritoryID(),
                    territoryData[msg.getOnTerritoryID()].getArmyCount(),
                    currentPlayers.get(msg.getFromPlayerID()).getColorID());

            // if this message is part of initial army placement, initiate next turn
            if (!initialArmyPlacementFinished) {
                nextPlayersTurn();
                if (nextTurnListener != null) {
                    nextTurnListener.isPlayersTurnNow(turnOrder.get(currentTurnIndex),
                            thisPlayer.getUid() == turnOrder.get(currentTurnIndex));
                }
            }
        }
    }

    private synchronized void handleStartGameMessage(StartGameMessage msg) {
        currentArmyReserve = msg.getStartArmyCount();
        // TODO: Remove once join-lobby is implemented
        currentLobbyID = msg.getLobbyID();
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
        // currently unused as feature is not yet implemented
    }

    @Override
    public void attackStarted(int fromTerritoryID, int onTerritoryID) {
        // currently unused as feature is not yet implemented
    }

    public void hostLobby() {
        client.sendMessage(new CreateLobbyMessage(thisPlayer.getUid()));
    }

    @Override
    public void connected() {
        isConnected = true;
    }

    @Override
    public void disconnected() {
        isConnected = false;
        if (connectionChangedListener != null) {
            connectionChangedListener.disconnected();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
