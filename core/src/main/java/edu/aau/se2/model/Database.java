package edu.aau.se2.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.listener.OnArmyReserveChangedListener;
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
import edu.aau.se2.server.networking.dto.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.ConnectedMessage;
import edu.aau.se2.server.networking.dto.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.NextTurnMessage;
import edu.aau.se2.server.networking.dto.PlayersChangedMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.server.networking.kryonet.NetworkConstants;
import edu.aau.se2.view.game.OnBoardInteractionListener;

public class Database implements OnBoardInteractionListener, NetworkClient.OnConnectionChangedListener {
    private static final String TAG = "Database";

    private static Database instance = null;
    private static String serverAddress = NetworkConstants.SERVER_IP;

    /**
     * Gets the singleton instance of Database.
     * @return Database instance, or null if connection error occurred.
     */
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
    public static void setServerAddress(String serverAddress) {
        if (instance != null) {
            throw new IllegalStateException("can only set server address before ever calling getInstance()");
        }
        Database.serverAddress = serverAddress;
    }

    private Logger log;
    private NetworkClientKryo client;
    private boolean isConnected;
    private OnGameStartListener gameStartListener;
    private OnPlayersChangedListener playersChangedListener;
    private OnTerritoryUpdateListener territoryUpdateListener;
    private OnNextTurnListener nextTurnListener;
    private OnJoinedLobbyListener joinedLobbyListener;
    private OnConnectionChangedListener connectionChangedListener;
    private OnArmyReserveChangedListener armyReserveChangedListener;

    private Player thisPlayer;
    private TreeMap<Integer, Player> currentPlayers;
    private List<Integer> turnOrder;
    private int currentTurnIndex;
    private int currentLobbyID;
    private Territory[] territoryData;
    private boolean initialArmyPlacementFinished;
    private int currentTurnPlayerID;
    private boolean hasPlayerReceivedArmiesThisTurn;

    private int currentArmyReserve = 0;

    protected Database() {
        resetLobby();
        isConnected = false;

        this.client = new NetworkClientKryo();
        client.registerConnectionListener(this);
        SerializationRegister.registerClassesForComponent(client);
        registerClientCallback();
        setupLogger();
    }

    private void setupLogger() {
        log = Logger.getLogger(TAG);
        Handler handlerObj = new ConsoleHandler();
        handlerObj.setLevel(Level.INFO);
        log.addHandler(handlerObj);
        log.setLevel(Level.INFO);
        log.setUseParentHandlers(false);
    }

    public void connectIfNotConnected() throws IOException {
        if (!isConnected) {
            connect();
        }
    }

    public void connect() throws IOException {
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

    public synchronized void setConnectionChangedListener(OnConnectionChangedListener l) {
        this.connectionChangedListener = l;
    }
    public void setGameStartListener(OnGameStartListener l) {
        this.gameStartListener = l;
    }
    public void setPlayersChangedListener(OnPlayersChangedListener l) { this.playersChangedListener = l; }
    public void setTerritoryUpdateListener(OnTerritoryUpdateListener l) { this.territoryUpdateListener = l; }
    public void setNextTurnListener(OnNextTurnListener l) {
        this.nextTurnListener = l;
    }
    public void setJoinedLobbyListener(OnJoinedLobbyListener l) {
        this.joinedLobbyListener = l;
    }
    public void setArmyReserveChangedListener(OnArmyReserveChangedListener l) { this.armyReserveChangedListener = l; }

    private void registerClientCallback() {
        this.client.registerCallback(msg -> {
            if (msg instanceof ConnectedMessage) {
                log.info("Received ConnectedMessage");
                handleConnectedMessage((ConnectedMessage) msg);
            }
            else if (msg instanceof StartGameMessage) {
                log.info("Received StartGameMessage");
                handleStartGameMessage((StartGameMessage) msg);
            }
            else if (msg instanceof InitialArmyPlacingMessage) {
                log.info("Received InitialArmyPlacingMessage");
                handleInitialArmyPlacingMessage((InitialArmyPlacingMessage) msg);
            }
            else if (msg instanceof ArmyPlacedMessage) {
                log.info("Received ArmyPlacedMessage");
                handleArmyPlacedMessage((ArmyPlacedMessage) msg);
            }
            else if (msg instanceof PlayersChangedMessage) {
                log.info("Received PlayersChangedMessage");
                handlePlayersChangedMessage((PlayersChangedMessage) msg);
            }
            else if (msg instanceof JoinedLobbyMessage) {
                log.info("Received JoinedLobbyMessage");
                handleJoinedLobbyMessage((JoinedLobbyMessage) msg);
            }
            else if (msg instanceof NextTurnMessage) {
                log.info("Received NextTurnMessage");
                handleNextTurnMessage((NextTurnMessage) msg);
            }
            else if (msg instanceof NewArmiesMessage) {
                log.info("Received NewArmiesMessage");
                handleNewArmiesMessage((NewArmiesMessage) msg);
            }
        });
    }

    private synchronized void handleNewArmiesMessage(NewArmiesMessage msg) {
        currentPlayers.get(msg.getFromPlayerID()).setArmyReserveCount(msg.getNewArmyCount());
        if (thisPlayer.getUid() == msg.getFromPlayerID()) {
            setCurrentArmyReserve(msg.getNewArmyCount(), true);
            hasPlayerReceivedArmiesThisTurn = true;
        }
    }

    private synchronized void handleNextTurnMessage(NextTurnMessage msg) {
        initialArmyPlacementFinished = true;
        currentTurnPlayerID = msg.getPlayerToActID();
        if (nextTurnListener != null) {
            nextTurnListener.isPlayersTurnNow(currentTurnPlayerID,
                    thisPlayer.getUid() == currentTurnPlayerID);
        }
        if (isThisPlayersTurn()) {
            hasPlayerReceivedArmiesThisTurn = false;
            exchangeCards();
        }
    }

    private synchronized void handleJoinedLobbyMessage(JoinedLobbyMessage msg) {
        this.currentLobbyID = msg.getLobbyID();
        setCurrentPlayers(msg.getPlayers());
        if (joinedLobbyListener != null) {
            joinedLobbyListener.joinedLobby(msg.getLobbyID(), msg.getHost(), msg.getPlayers());
        }
    }

    private synchronized void handleConnectedMessage(ConnectedMessage msg) {
        thisPlayer = msg.getPlayer();
        isConnected = true;
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
        currentTurnPlayerID = turnOrder.get(0);
        if (nextTurnListener != null) {
            nextTurnListener.isPlayersTurnNow(turnOrder.get(0), thisPlayer.getUid() == turnOrder.get(0));
        }
    }

    private synchronized void handleArmyPlacedMessage(ArmyPlacedMessage msg) {
        // adjust remaining army count if this player placed armies
        if (msg.getFromPlayerID() == thisPlayer.getUid()) {
            setCurrentArmyReserve(msg.getArmyCountRemaining(), false);
        }
        // update remaining army count on player
        currentPlayers.get(msg.getFromPlayerID()).setArmyReserveCount(msg.getArmyCountRemaining());
        // update territory state
        territoryData[msg.getOnTerritoryID()].addToArmyCount(msg.getArmyCountPlaced());
        territoryData[msg.getOnTerritoryID()].setOccupierPlayerID(msg.getFromPlayerID());

        if (territoryUpdateListener != null) {
            territoryUpdateListener.territoryUpdated(msg.getOnTerritoryID(),
                    territoryData[msg.getOnTerritoryID()].getArmyCount(),
                    currentPlayers.get(msg.getFromPlayerID()).getColorID());
        }
        // if this message is part of initial army placement, initiate next turn
        if (!initialArmyPlacementFinished) {
            nextPlayersTurn();
            if (nextTurnListener != null) {
                nextTurnListener.isPlayersTurnNow(currentTurnPlayerID,
                        thisPlayer.getUid() == currentTurnPlayerID);
            }
        }
    }

    private synchronized void handleStartGameMessage(StartGameMessage msg) {
        // TODO: Remove once join-lobby is implemented
        currentLobbyID = msg.getLobbyID();

        setCurrentArmyReserve(msg.getStartArmyCount(), true);
        if (gameStartListener != null) {
            for (Player p: msg.getPlayers()) {
                currentPlayers.put(p.getUid(), p);
            }
            gameStartListener.onGameStarted(msg.getPlayers(), msg.getStartArmyCount());
        }
    }

    /**
     * Sets the current player to act to the next player according to turn order.
     * WARNING: This method should only be used during initial army placing phase.
     * Use the player id communicated in the NextTurnMessage from server instead.
     */
    private synchronized void nextPlayersTurn() {
        currentTurnIndex = (currentTurnIndex + 1) % currentPlayers.size();
        currentTurnPlayerID = turnOrder.get(currentTurnIndex);
    }

    public int getCurrentArmyReserve() {
        return currentArmyReserve;
    }

    public Player getPlayerByID(int playerID) {
        return currentPlayers.get(playerID);
    }

    public Player getCurrentPlayerToAct() {
        return currentPlayers.get(currentTurnPlayerID);
    }

    public Player getThisPlayer() {
        return thisPlayer;
    }

    public boolean isThisPlayersTurn() {
        return getCurrentPlayerToAct().getUid() == thisPlayer.getUid();
    }

    public void setPlayerReady(boolean ready) {
        log.info("Sending ReadyMessage");
        client.sendMessage(new ReadyMessage(0, thisPlayer.getUid(), ready));
    }

    @Override
    public void armyPlaced(int territoryID, int count) {
        log.info("Sending ArmyPlacedMessage");
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
        log.info("Sending CreateLobbyMessage");
        client.sendMessage(new CreateLobbyMessage(thisPlayer.getUid()));
    }

    @Override
    public void connected() {
        // handled in ConnectedMessage
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

    // TODO: Change when cards are implemented
    public void exchangeCards() {
        log.info("Sending CardExchangeMessage");
        client.sendMessage(new CardExchangeMessage(currentLobbyID, thisPlayer.getUid()));
    }

    private synchronized void setCurrentArmyReserve(int newValue, boolean isInitialCount) {
        this.currentArmyReserve = newValue;
        if (armyReserveChangedListener != null) {
            armyReserveChangedListener.newArmyCount(this.currentArmyReserve, isInitialCount);
        }
    }

    public synchronized void finishTurn() {
        if (!(isThisPlayersTurn() && hasPlayerReceivedArmiesThisTurn && currentArmyReserve == 0)) {
            throw new IllegalStateException("can only finish own turn after all army reserves have been placed");
        }
        log.info("Sending NextTurnMessage");
        client.sendMessage(new NextTurnMessage(currentLobbyID, thisPlayer.getUid()));
    }

    public synchronized boolean isInitialArmyPlacementFinished() {
        return initialArmyPlacementFinished;
    }

    public List<Player> getCurrentPlayers() {
        return new ArrayList<>(currentPlayers.values());
    }
}
