package edu.aau.se2.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.listener.OnArmiesMovedListener;
import edu.aau.se2.model.listener.OnArmyReserveChangedListener;
import edu.aau.se2.model.listener.OnAttackUpdatedListener;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.model.listener.OnErrorListener;
import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.model.listener.OnJoinedLobbyListener;
import edu.aau.se2.model.listener.OnLeftLobbyListener;
import edu.aau.se2.model.listener.OnLobbyListChangedListener;
import edu.aau.se2.model.listener.OnNextTurnListener;
import edu.aau.se2.model.listener.OnPhaseChangedListener;
import edu.aau.se2.model.listener.OnPlayersChangedListener;
import edu.aau.se2.model.listener.OnTerritoryUpdateListener;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.NetworkClient;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.game.ArmyMovedMessage;
import edu.aau.se2.server.networking.dto.game.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.game.AttackResultMessage;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.dto.game.AttackingPhaseFinishedMessage;
import edu.aau.se2.server.networking.dto.game.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.game.DiceResultMessage;
import edu.aau.se2.server.networking.dto.game.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.game.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.dto.game.OccupyTerritoryMessage;
import edu.aau.se2.server.networking.dto.game.StartGameMessage;
import edu.aau.se2.server.networking.dto.lobby.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.ErrorMessage;
import edu.aau.se2.server.networking.dto.lobby.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.LeftLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.PlayersChangedMessage;
import edu.aau.se2.server.networking.dto.lobby.ReadyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestJoinLobbyMessage;
import edu.aau.se2.server.networking.dto.lobby.RequestLeaveLobby;
import edu.aau.se2.server.networking.dto.prelobby.ConnectedMessage;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;
import edu.aau.se2.server.networking.dto.prelobby.RequestLobbyListMessage;
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
    private OnLobbyListChangedListener lobbyListChangedListener;
    private OnLeftLobbyListener onLeftLobbyListener;
    private OnErrorListener errorListener;
    private OnPhaseChangedListener phaseChangedListener;
    private OnArmiesMovedListener armiesMovedListener;
    private OnAttackUpdatedListener attackUpdatedListener;

    private Player thisPlayer;
    private TreeMap<Integer, Player> currentPlayers;
    private List<Integer> turnOrder;
    private int currentTurnIndex;
    private int currentLobbyID;
    private Territory[] territoryData;
    private boolean initialArmyPlacementFinished;
    private int currentTurnPlayerID;
    private boolean hasPlayerReceivedArmiesThisTurn;

    private int currentArmyReserve;
    private Phase currentPhase;

    private Attack currentAttack;

    protected Database() {
        resetLobby();
        isConnected = false;

        this.client = new NetworkClientKryo();
        client.registerConnectionListener(this);
        SerializationRegister.registerClassesForComponent(client);
        registerClientCallback();
        setupLogger();
    }

    public enum Phase {
        PLACING, ATTACKING, MOVING, NONE
    }

    private void setupLogger() {
        log = Logger.getLogger(TAG);
        if (log.getHandlers().length == 0) {
            Handler handlerObj = new ConsoleHandler();
            handlerObj.setLevel(Level.INFO);
            log.addHandler(handlerObj);
        }
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
        currentPhase = Phase.NONE;
        currentArmyReserve = 0;
        initTerritoryData();
    }

    private void initTerritoryData() {
        territoryData = new Territory[42];
        for (int i=0; i<territoryData.length; i++) {
            territoryData[i] = new Territory(i);
        }
    }

    public void setArmiesMovedListener(OnArmiesMovedListener l) { this.armiesMovedListener = l; }
    public void setPhaseChangedListener(OnPhaseChangedListener l) { this.phaseChangedListener = l; }
    public void setErrorListener(OnErrorListener l) {
        this.errorListener = l;
    }
    public void setLeftLobbyListener(OnLeftLobbyListener l) {
        this.onLeftLobbyListener = l;
    }
    public void setLobbyListChangedListener(OnLobbyListChangedListener l) { this.lobbyListChangedListener = l; }
    public void setConnectionChangedListener(OnConnectionChangedListener l) { this.connectionChangedListener = l; }
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
    public void setAttackUpdatedListener(OnAttackUpdatedListener l) { this.attackUpdatedListener = l; }

    private void registerClientCallback() {
        this.client.registerCallback(msg -> {
            log.info("[Client] Received " + msg.getClass().getSimpleName());
            if (msg instanceof ConnectedMessage) {
                handleConnectedMessage((ConnectedMessage) msg);
            } else if (msg instanceof StartGameMessage) {
                handleStartGameMessage((StartGameMessage) msg);
            } else if (msg instanceof InitialArmyPlacingMessage) {
                handleInitialArmyPlacingMessage((InitialArmyPlacingMessage) msg);
            } else if (msg instanceof ArmyPlacedMessage) {
                handleArmyPlacedMessage((ArmyPlacedMessage) msg);
            } else if (msg instanceof PlayersChangedMessage) {
                handlePlayersChangedMessage((PlayersChangedMessage) msg);
            } else if (msg instanceof JoinedLobbyMessage) {
                handleJoinedLobbyMessage((JoinedLobbyMessage) msg);
            } else if (msg instanceof LobbyListMessage) {
                handleLobbyListMessage((LobbyListMessage) msg);
            } else if (msg instanceof LeftLobbyMessage) {
                handleLeftLobbyMessage((LeftLobbyMessage) msg);
            } else if (msg instanceof NextTurnMessage) {
                handleNextTurnMessage((NextTurnMessage) msg);
            } else if (msg instanceof NewArmiesMessage) {
                handleNewArmiesMessage((NewArmiesMessage) msg);
            } else if (msg instanceof ArmyMovedMessage) {
                handleArmyMovedMessage((ArmyMovedMessage) msg);
            } else if (msg instanceof ErrorMessage) {
                handleErrorMessage((ErrorMessage) msg);
            } else if (msg instanceof AttackingPhaseFinishedMessage) {
                handleAttackingPhaseFinishedMessage();
            } else if (msg instanceof AttackStartedMessage) {
                handleAttackStartedMessage((AttackStartedMessage) msg);
            } else if (msg instanceof AttackResultMessage) {
                handleAttackResultMessage((AttackResultMessage) msg);
            } else if (msg instanceof OccupyTerritoryMessage) {
                handleOccupyTerritoryMessage((OccupyTerritoryMessage) msg);
            }
        });
    }

    private void handleOccupyTerritoryMessage(OccupyTerritoryMessage msg) {
        // update territory state
        territoryData[msg.getFromTerritoryID()].subFromArmyCount(msg.getArmyCount());
        territoryData[msg.getTerritoryID()].setArmyCount(msg.getArmyCount());
        territoryData[msg.getTerritoryID()].setOccupierPlayerID(msg.getFromPlayerID());

        notifyTerritoryUpdateListener(territoryData[msg.getFromTerritoryID()]);
        notifyTerritoryUpdateListener(territoryData[msg.getTerritoryID()]);

        if (attackUpdatedListener != null) {
            attackUpdatedListener.attackFinished();
        }
    }

    // TODO: add actual logic (--> Carina)
    private void handleAttackResultMessage(AttackResultMessage msg) {
        this.currentAttack.setOccupyRequired(msg.isOccupyRequired());
        if (attackUpdatedListener != null) {
            attackUpdatedListener.attackUpdated();
        }
        if (!msg.isOccupyRequired()) {
            if (attackUpdatedListener != null) {
                attackUpdatedListener.attackFinished();
            }
        }
    }

    private void handleAttackStartedMessage(AttackStartedMessage msg) {
        currentAttack = new Attack(msg.getFromTerritoryID(), msg.getToTerritoryID(), msg.getDiceCount());
        if (attackUpdatedListener != null) {
            attackUpdatedListener.attackStarted();
        }
    }

    private void handleAttackingPhaseFinishedMessage() {
        if (currentPhase != Phase.MOVING) {
            setCurrentPhase(Phase.MOVING);
        }
    }

    private void handleArmyMovedMessage(ArmyMovedMessage msg) {
        // update territory state
        territoryData[msg.getFromTerritoryID()].subFromArmyCount(msg.getArmyCountMoved());
        territoryData[msg.getToTerritoryID()].addToArmyCount(msg.getArmyCountMoved());

        if (armiesMovedListener != null) {
            armiesMovedListener.armiesMoved(msg.getFromPlayerID(), msg.getFromTerritoryID(),
                    msg.getToTerritoryID(), msg.getArmyCountMoved());
        }
        notifyTerritoryUpdateListener(territoryData[msg.getFromTerritoryID()]);
        notifyTerritoryUpdateListener(territoryData[msg.getToTerritoryID()]);
    }

    private void notifyTerritoryUpdateListener(Territory t) {
        if (territoryUpdateListener != null) {
            territoryUpdateListener.territoryUpdated(t.getId(),
                    t.getArmyCount(),
                    currentPlayers.get(t.getOccupierPlayerID()).getColorID());
        }
    }

    private void handleErrorMessage(ErrorMessage msg) {
        if (errorListener != null) {
            errorListener.onError(msg.getErrorCode());
        }
    }

    private void handleLeftLobbyMessage(LeftLobbyMessage msg) {
        resetLobby();
        if (onLeftLobbyListener != null) {
            onLeftLobbyListener.leftLobby(msg.isWasClosed());
        }
    }

    private void handleLobbyListMessage(LobbyListMessage msg) {
        List<LobbyListMessage.LobbyData> lobbyList = msg.getLobbies();
        if (lobbyListChangedListener != null) {
            lobbyListChangedListener.lobbyListChanged(lobbyList);
        }
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
        setCurrentPhase(Phase.PLACING);

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

        notifyTerritoryUpdateListener(territoryData[msg.getOnTerritoryID()]);

        // if this message is part of initial army placement, initiate next turn
        if (!initialArmyPlacementFinished) {
            nextPlayersTurn();
            if (nextTurnListener != null) {
                nextTurnListener.isPlayersTurnNow(currentTurnPlayerID,
                        thisPlayer.getUid() == currentTurnPlayerID);
            }
        }
        else {
            if (msg.getArmyCountRemaining() == 0) {
                setCurrentPhase(Phase.ATTACKING);
            }
        }
    }

    private synchronized void handleStartGameMessage(StartGameMessage msg) {
        setCurrentArmyReserve(msg.getStartArmyCount(), true);
        if (gameStartListener != null) {
            for (Player p: msg.getPlayers()) {
                currentPlayers.put(p.getUid(), p);
            }
            gameStartListener.onGameStarted(msg.getPlayers(), msg.getStartArmyCount());
        }
    }

    private synchronized void handleDiceResultMessage(DiceResultMessage msg) {
        // TODO show result
        log.info("Received DiceResultMessage");
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
        return getCurrentPlayerToAct() != null && getCurrentPlayerToAct().getUid() == thisPlayer.getUid();
    }

    public boolean isThisPlayerDefender() {
        if (currentAttack == null) return false;
        return territoryData[currentAttack.getToTerritoryID()].getOccupierPlayerID() == thisPlayer.getUid();
    }

    public Attack getAttack() {
        return currentAttack;
    }

    public void setPlayerReady(boolean ready) {
        log.info("Sending ReadyMessage");
        client.sendMessage(new ReadyMessage(currentLobbyID, thisPlayer.getUid(), ready));
    }

    public void togglePlayerReady() {
        log.info("Sending ReadyMessage");
        Player player = currentPlayers.get(thisPlayer.getUid());
        if (player != null) {
            client.sendMessage(new ReadyMessage(currentLobbyID, thisPlayer.getUid(),
                    !player.isReady()));
        }
    }

    @Override
    public void armyPlaced(int territoryID, int count) {
        if (!isInitialArmyPlacementFinished() || currentPhase == Phase.PLACING) {
            log.info("Sending ArmyPlacedMessage");
            client.sendMessage(new ArmyPlacedMessage(currentLobbyID, thisPlayer.getUid(), territoryID, count));
        }
    }

    @Override
    public void armyMoved(int fromTerritoryID, int toTerritoryID, int count) {
        if (currentPhase == Phase.MOVING) {
            log.info("Sending ArmyMovedMessage");
            client.sendMessage(new ArmyMovedMessage(currentLobbyID, thisPlayer.getUid(),
                    fromTerritoryID, toTerritoryID, count));
        }
    }

    public void sendAttackerResults(List<Integer> results, boolean cheated) {
        log.info("Sending AttackerResults");
        client.sendMessage(new DiceResultMessage(currentLobbyID, thisPlayer.getUid(), results, cheated));
    }

    @Override
    public void attackStarted(int fromTerritoryID, int onTerritoryID, int count) {
        if (currentPhase == Phase.ATTACKING) {
            log.info("Sending AttackStartedMessage");
            client.sendMessage(new AttackStartedMessage(currentLobbyID, thisPlayer.getUid(), fromTerritoryID, onTerritoryID, count));
        }
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

    public synchronized List<Player> getCurrentPlayers() {
        return new ArrayList<>(currentPlayers.values());
    }

    public synchronized void triggerLobbyListUpdate() {
        client.sendMessage(new RequestLobbyListMessage(thisPlayer.getUid()));
    }

    public synchronized void joinLobby(int lobbyID) {
        client.sendMessage(new RequestJoinLobbyMessage(lobbyID, thisPlayer.getUid()));
    }

    public synchronized void leaveLobby() {
        if (currentLobbyID != -1) {
            client.sendMessage(new RequestLeaveLobby(currentLobbyID, thisPlayer.getUid()));
        }
    }

    public synchronized void returnToMainMenu() {
        if (onLeftLobbyListener != null)
            onLeftLobbyListener.leftLobby(false);
    }

    public int getCurrentLobbyID() {
        return currentLobbyID;
    }

    public void finishAttackingPhase() {
        client.sendMessage(new AttackingPhaseFinishedMessage(currentLobbyID, thisPlayer.getUid()));
    }

    private void setCurrentPhase(Phase phase) {
        this.currentPhase = phase;
        if (phaseChangedListener != null) {
            phaseChangedListener.phaseChanged(phase);
        }
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public Territory getTerritoryByID(int territoryID) {
        try {
            return territoryData[territoryID];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("territory with id " + territoryID + " does not exist");
        }
    }

    public void closeConnection() {
        client.disconnect();
    }

    public Player getPlayerByTerritoryID(int territoryID) {
        return currentPlayers.get(territoryData[territoryID].getOccupierPlayerID());
    }

    public void occupyTerritory(int territoryID, int fromTerritoryID, int armyCount) {
        client.sendMessage(new OccupyTerritoryMessage(currentLobbyID, thisPlayer.getUid(), territoryID, fromTerritoryID, armyCount));
    }
}
