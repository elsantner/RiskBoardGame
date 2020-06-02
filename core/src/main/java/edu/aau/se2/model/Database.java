package edu.aau.se2.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.PlayerDeviceNameListener;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.networking.NetworkClient;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.game.ArmyMovedMessage;
import edu.aau.se2.server.networking.dto.game.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.game.AttackResultMessage;
import edu.aau.se2.server.networking.dto.game.AttackStartedMessage;
import edu.aau.se2.server.networking.dto.game.AttackingPhaseFinishedMessage;
import edu.aau.se2.server.networking.dto.game.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.game.DefenderDiceCountMessage;
import edu.aau.se2.server.networking.dto.game.DiceResultMessage;
import edu.aau.se2.server.networking.dto.game.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.game.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.game.NewCardMessage;
import edu.aau.se2.server.networking.dto.game.NextTurnMessage;
import edu.aau.se2.server.networking.dto.game.OccupyTerritoryMessage;
import edu.aau.se2.server.networking.dto.game.RefreshCardsMessage;
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
import edu.aau.se2.server.networking.dto.prelobby.ChangeNicknameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.server.networking.kryonet.NetworkConstants;
import edu.aau.se2.utils.LoggerConfigurator;
import edu.aau.se2.view.DefaultNameProvider;
import edu.aau.se2.view.game.OnBoardInteractionListener;

public class Database implements OnBoardInteractionListener, NetworkClient.OnConnectionChangedListener {
    private static final String TAG = "Database";

    private static Database instance = null;
    protected static String serverAddress = NetworkConstants.SERVER_IP;

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

    private Logger log;
    private NetworkClientKryo client;
    private boolean isConnected;
    private ListenerManager listenerManager;

    private Player thisPlayer;
    private int currentArmyReserve;
    private Phase currentPhase;
    private Lobby lobby;
    private String deviceName;
    private DefaultNameProvider defaultNameProvider;
    private Preferences prefs = Gdx.app.getPreferences("profile");

    protected Database() {
        resetLobby();
        isConnected = false;

        this.client = new NetworkClientKryo();
        client.registerConnectionListener(this);
        SerializationRegister.registerClassesForComponent(client);
        registerClientCallback();
        log = LoggerConfigurator.getConfiguredLogger(TAG, Level.INFO);
        this.listenerManager = new ListenerManager();
    }

    public enum Phase {
        PLACING, ATTACKING, MOVING, NONE
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
        lobby = new Lobby(-1);
        currentPhase = Phase.NONE;
        currentArmyReserve = 0;
    }

    public ListenerManager getListeners() {
        return this.listenerManager;
    }

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
            } else if (msg instanceof NewCardMessage) {
                handleNewCardMessage((NewCardMessage) msg);
            } else if (msg instanceof NewArmiesMessage) {
                handleNewArmiesMessage((NewArmiesMessage) msg);
            } else if (msg instanceof ArmyMovedMessage) {
                handleArmyMovedMessage((ArmyMovedMessage) msg);
            } else if (msg instanceof RefreshCardsMessage) {
                handleRefreshCardsMessage((RefreshCardsMessage) msg);
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
            } else if (msg instanceof DiceResultMessage) {
                handleDiceResultMessage((DiceResultMessage) msg);
            } else if (msg instanceof DefenderDiceCountMessage) {
                handleDefenderDiceCountMessage((DefenderDiceCountMessage) msg);
            } else if (msg instanceof ChangeNicknameMessage) {
                handleChangeNicknameMessage((ChangeNicknameMessage) msg);
            }
        });
    }


    public synchronized void setPlayerNickname(String nickname) {
        client.sendMessage(new ChangeNicknameMessage(thisPlayer.getUid(), nickname));
    }

    private void handleChangeNicknameMessage(ChangeNicknameMessage msg) {
        listenerManager.notifyNicknameChangeListener(msg.getNickname());
        if(prefs.getString("name") == null){
            client.sendMessage(new ChangeNicknameMessage(thisPlayer.getUid(), "Player"));
        } else {
            client.sendMessage(new ChangeNicknameMessage(thisPlayer.getUid(), prefs.getString("name")));
        }
    }

    private void handleDefenderDiceCountMessage(DefenderDiceCountMessage msg) {
        if (lobby.attackRunning()) {
            lobby.getCurrentAttack().setDefenderDiceCount(msg.getDiceCount());
            listenerManager.notifyAttackUpdatedListener();
        }
    }

    private void handleOccupyTerritoryMessage(OccupyTerritoryMessage msg) {
        // update territory state
        lobby.getTerritoryByID(msg.getFromTerritoryID()).subFromArmyCount(msg.getArmyCount());
        lobby.getTerritoryByID(msg.getTerritoryID()).setArmyCount(msg.getArmyCount());
        lobby.getTerritoryByID(msg.getTerritoryID()).setOccupierPlayerID(msg.getFromPlayerID());

        notifyTerritoryUpdateListener(lobby.getTerritoryByID(msg.getFromTerritoryID()));
        notifyTerritoryUpdateListener(lobby.getTerritoryByID(msg.getTerritoryID()));
        lobby.setCurrentAttack(null);
        listenerManager.notifyAttackFinishedListener();
    }

    private void handleAttackResultMessage(AttackResultMessage msg) {
        log.info("Attacker armies lost: " + msg.getArmiesLostAttacker());
        log.info("Defender armies lost: " + msg.getArmiesLostDefender());

        Attack attack = lobby.getCurrentAttack();
        attack.setArmiesLostAttacker(msg.getArmiesLostAttacker());
        attack.setArmiesLostDefender(msg.getArmiesLostDefender());
        attack.setCheated(msg.isCheated());
        attack.setOccupyRequired(msg.isOccupyRequired());
        lobby.getTerritoryByID(attack.getFromTerritoryID()).subFromArmyCount(msg.getArmiesLostAttacker());
        lobby.getTerritoryByID(attack.getToTerritoryID()).subFromArmyCount(msg.getArmiesLostDefender());
        notifyTerritoryUpdateListener(lobby.getTerritoryByID(attack.getFromTerritoryID()));
        notifyTerritoryUpdateListener(lobby.getTerritoryByID(attack.getToTerritoryID()));

        listenerManager.notifyAttackUpdatedListener();
        if (!msg.isOccupyRequired()) {
            lobby.setCurrentAttack(null);
            listenerManager.notifyAttackFinishedListener();
        }
    }

    private void handleAttackStartedMessage(AttackStartedMessage msg) {
        lobby.setCurrentAttack(new Attack(msg.getFromTerritoryID(), msg.getToTerritoryID(), msg.getDiceCount()));
        listenerManager.notifyAttackStartedListener();
    }

    private void handleAttackingPhaseFinishedMessage() {
        if (currentPhase != Phase.MOVING) {
            setCurrentPhase(Phase.MOVING);
        }
    }

    private void handleArmyMovedMessage(ArmyMovedMessage msg) {
        // update territory state
        lobby.getTerritoryByID(msg.getFromTerritoryID()).subFromArmyCount(msg.getArmyCountMoved());
        lobby.getTerritoryByID(msg.getToTerritoryID()).addToArmyCount(msg.getArmyCountMoved());
        listenerManager.notifyArmiesMovedListener(msg.getFromPlayerID(), msg.getFromTerritoryID(), msg.getToTerritoryID(), msg.getArmyCountMoved());

        notifyTerritoryUpdateListener(lobby.getTerritoryByID(msg.getFromTerritoryID()));
        notifyTerritoryUpdateListener(lobby.getTerritoryByID(msg.getToTerritoryID()));
    }

    private void notifyTerritoryUpdateListener(Territory t) {
        listenerManager.notifyTerritoryUpdateListener(t.getId(), t.getArmyCount(),
                lobby.getPlayerByID(t.getOccupierPlayerID()).getColorID());
    }

    private void handleErrorMessage(ErrorMessage msg) {
        listenerManager.notifyErrorListener(msg.getErrorCode());
    }

    private void handleLeftLobbyMessage(LeftLobbyMessage msg) {
        resetLobby();
        listenerManager.notifyLeftLobbyListener(msg.isWasClosed());
    }

    private void handleLobbyListMessage(LobbyListMessage msg) {
        listenerManager.notifyLobbyListChangedListener(msg.getLobbies());
    }

    private synchronized void handleNewArmiesMessage(NewArmiesMessage msg) {
        lobby.getPlayerByID(msg.getFromPlayerID()).setArmyReserveCount(msg.getNewArmyCount());
        if (thisPlayer.getUid() == msg.getFromPlayerID()) {
            setCurrentArmyReserve(msg.getNewArmyCount(), true);
            lobby.setCurrentPlayerToActReceivedNewArmies(true);
        }

        // add bonus armies to the correct territory, update for all players
        if (msg.getTerritoryIdForBonusArmies() != -1) {
            lobby.getTerritoryByID(msg.getTerritoryIdForBonusArmies()).addToArmyCount(2);
            listenerManager.notifyTerritoryUpdateListener(msg.getTerritoryIdForBonusArmies(),
                    lobby.getTerritoryByID(msg.getTerritoryIdForBonusArmies()).getArmyCount(),
                    lobby.getPlayerByID(msg.getFromPlayerID()).getColorID());
        }
    }

    private synchronized void handleNextTurnMessage(NextTurnMessage msg) {
        lobby.setInitialArmiesPlaced();
        lobby.nextPlayersTurn();
        setCurrentPhase(Phase.PLACING);

        listenerManager.notifyNextTurnListener(lobby.getPlayerToAct().getUid(),
                thisPlayer.getUid() == lobby.getPlayerToAct().getUid());
        if (isThisPlayersTurn() && !thisPlayer.isAskForCardExchange()) {
            lobby.setCurrentPlayerToActReceivedNewArmies(false);
            exchangeCards(false);
        }
    }

    private synchronized void handleNewCardMessage(NewCardMessage msg) {
        // adds the new card to the players cards, shown by CardStage
        if (msg.isAskForCardExchange()) {
            this.thisPlayer.setAskForCardExchange(true);
        }
        listenerManager.notifySingleNewCardListener(msg.getCardName());
    }

    private void handleRefreshCardsMessage(RefreshCardsMessage msg) {
        // after set trade-in displayed cards need to be refreshed
        listenerManager.notifyRefreshCardListener(msg.getCardNames());
    }

    private synchronized void handleJoinedLobbyMessage(JoinedLobbyMessage msg) {
        lobby.setLobbyID(msg.getLobbyID());
        setCurrentPlayers(msg.getPlayers());
        listenerManager.notifyJoinedLobbyListener(msg.getLobbyID(), msg.getHost(), msg.getPlayers());
    }

    private synchronized void handleConnectedMessage(ConnectedMessage msg) {
        thisPlayer = msg.getPlayer();
        isConnected = true;
        listenerManager.notifyConnectedListener(thisPlayer);
    }

    private synchronized void setCurrentPlayers(List<Player> players) {
        lobby.clearPlayers();
        for (Player p : players) {
            lobby.join(p);
        }
    }

    private synchronized void handlePlayersChangedMessage(PlayersChangedMessage msg) {
        setCurrentPlayers(msg.getPlayers());
        listenerManager.notifyPlayersChangedListener(lobby.getPlayers());
    }

    private synchronized void handleInitialArmyPlacingMessage(InitialArmyPlacingMessage msg) {
        lobby.setTurnOrder(msg.getPlayerOrder());
        listenerManager.notifyNextTurnListener(lobby.getPlayerToAct().getUid(),
                thisPlayer.getUid() == lobby.getPlayerToAct().getUid());
    }

    private synchronized void handleArmyPlacedMessage(ArmyPlacedMessage msg) {
        // adjust remaining army count if this player placed armies
        if (msg.getFromPlayerID() == thisPlayer.getUid()) {
            setCurrentArmyReserve(msg.getArmyCountRemaining(), false);
        }
        // update remaining army count on player
        lobby.getPlayerByID(msg.getFromPlayerID()).setArmyReserveCount(msg.getArmyCountRemaining());
        // update territory state
        lobby.getTerritoryByID(msg.getOnTerritoryID()).addToArmyCount(msg.getArmyCountPlaced());
        lobby.getTerritoryByID(msg.getOnTerritoryID()).setOccupierPlayerID(msg.getFromPlayerID());

        notifyTerritoryUpdateListener(lobby.getTerritoryByID(msg.getOnTerritoryID()));

        // if this message is part of initial army placement, initiate next turn
        if (!lobby.areInitialArmiesPlaced()) {
            lobby.nextPlayersTurn();
            listenerManager.notifyNextTurnListener(lobby.getPlayerToAct().getUid(),
                    thisPlayer.getUid() == lobby.getPlayerToAct().getUid());
        }
        else {
            if (msg.getArmyCountRemaining() == 0) {
                setCurrentPhase(Phase.ATTACKING);
            }
        }
    }

    private synchronized void handleStartGameMessage(StartGameMessage msg) {
        setCurrentArmyReserve(msg.getStartArmyCount(), true);
        // set Player colors
        for (Player p : msg.getPlayers()) {
            lobby.updatePlayer(p);
        }
        listenerManager.notifyGameStartListener(msg.getPlayers(), msg.getStartArmyCount());
    }

    private synchronized void handleDiceResultMessage(DiceResultMessage msg) {
        if (lobby.attackRunning() && msg.isFromAttacker()) {
            lobby.getCurrentAttack().setAttackerDiceResults(msg.getResults());
        } else if(lobby.attackRunning()) {
            lobby.getCurrentAttack().setDefenderDiceResults(msg.getResults());
        }
        listenerManager.notifyAttackUpdatedListener();
    }

    public int getCurrentArmyReserve() {
        return currentArmyReserve;
    }

    public Player getThisPlayer() {
        return thisPlayer;
    }

    public boolean isThisPlayersTurn() {
        return lobby.getPlayerToAct() != null && lobby.getPlayerToAct().getUid() == thisPlayer.getUid();
    }

    public boolean isThisPlayerDefender() {
        if (lobby.getCurrentAttack() == null) return false;
        return lobby.getTerritoryByID(lobby.getCurrentAttack().getToTerritoryID()).getOccupierPlayerID() == thisPlayer.getUid();
    }

    public void setPlayerReady(boolean ready) {
        client.sendMessage(new ReadyMessage(lobby.getLobbyID(), thisPlayer.getUid(), ready));
    }

    public void sendDefenderDiceCount(int result) {
        if (currentPhase == Phase.ATTACKING && lobby.attackRunning() && isThisPlayerDefender()) {
            client.sendMessage(new DefenderDiceCountMessage(lobby.getLobbyID(), thisPlayer.getUid(), result));
        }
    }

    public void togglePlayerReady() {
        Player player = lobby.getPlayerByID(thisPlayer.getUid());
        if (player != null) {
            client.sendMessage(new ReadyMessage(lobby.getLobbyID(), thisPlayer.getUid(),
                    !player.isReady()));
        }
    }

    @Override
    public void armyPlaced(int territoryID, int count) {
        if (!lobby.areInitialArmiesPlaced() || currentPhase == Phase.PLACING) {
            client.sendMessage(new ArmyPlacedMessage(lobby.getLobbyID(), thisPlayer.getUid(), territoryID, count));
        }
    }

    @Override
    public void armyMoved(int fromTerritoryID, int toTerritoryID, int count) {
        if (currentPhase == Phase.MOVING) {
            client.sendMessage(new ArmyMovedMessage(lobby.getLobbyID(), thisPlayer.getUid(),
                    fromTerritoryID, toTerritoryID, count));
        }
    }

    public void sendAttackerResults(List<Integer> results, boolean cheated) {
        client.sendMessage(new DiceResultMessage(lobby.getLobbyID(), thisPlayer.getUid(), results, cheated, true));
    }

    public void sendDefenderResults(List<Integer> results) {
        client.sendMessage(new DiceResultMessage(lobby.getLobbyID(), thisPlayer.getUid(), results, false, false));
    }

    @Override
    public void attackStarted(int fromTerritoryID, int onTerritoryID, int count) {
        if (currentPhase == Phase.ATTACKING) {
            client.sendMessage(new AttackStartedMessage(lobby.getLobbyID(), thisPlayer.getUid(), fromTerritoryID, onTerritoryID, count));
        }
    }

    public void hostLobby() {
        client.sendMessage(new CreateLobbyMessage(thisPlayer.getUid()));
    }

    @Override
    public void connected() {
        // handled in ConnectedMessage
    }

    @Override
    public void disconnected() {
        isConnected = false;
        listenerManager.notifyDisconnectedListener();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void exchangeCards(boolean exchangeCards) {
        if (exchangeCards) {
            thisPlayer.setExchangeCards(true);
            thisPlayer.setAskForCardExchange(false);
        }

        client.sendMessage(new CardExchangeMessage(lobby.getLobbyID(), thisPlayer.getUid(), exchangeCards));
    }

    private synchronized void setCurrentArmyReserve(int newValue, boolean isInitialCount) {
        this.currentArmyReserve = newValue;
        lobby.getPlayerByID(thisPlayer.getUid()).setArmyReserveCount(newValue);
        listenerManager.notifyArmyReserveChangedListener(this.currentArmyReserve, isInitialCount);
    }

    public synchronized void finishTurn() {
        if (!(isThisPlayersTurn() && lobby.hasCurrentPlayerToActReceivedNewArmies() && currentArmyReserve == 0)) {
            throw new IllegalStateException("can only finish own turn after all army reserves have been placed");
        }
        client.sendMessage(new NextTurnMessage(lobby.getLobbyID(), thisPlayer.getUid()));
    }

    public synchronized void triggerLobbyListUpdate() {
        client.sendMessage(new RequestLobbyListMessage(thisPlayer.getUid()));
    }

    public synchronized void joinLobby(int lobbyID) {
        client.sendMessage(new RequestJoinLobbyMessage(lobbyID, thisPlayer.getUid()));
    }

    public synchronized void leaveLobby() {
        if (lobby.getLobbyID() != -1) {
            client.sendMessage(new RequestLeaveLobby(lobby.getLobbyID(), thisPlayer.getUid()));
        }
    }

    public synchronized void returnToMainMenu() {
        listenerManager.notifyLeftLobbyListener(false);
    }

    public void finishAttackingPhase() {
        client.sendMessage(new AttackingPhaseFinishedMessage(lobby.getLobbyID(), thisPlayer.getUid()));
    }

    private void setCurrentPhase(Phase phase) {
        this.currentPhase = phase;
        listenerManager.notifyPhaseChangedListener(phase);
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void closeConnection() {
        client.disconnect();
    }

    public void occupyTerritory(int territoryID, int fromTerritoryID, int armyCount) {
        client.sendMessage(new OccupyTerritoryMessage(lobby.getLobbyID(), thisPlayer.getUid(), territoryID, fromTerritoryID, armyCount));
    }

    public Territory[] getMyTerritories() {
        return lobby.getTerritoriesOccupiedByPlayer(thisPlayer.getUid());
    }

    public Lobby getLobby() {
        return lobby;
    }



}
