package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.CreateLobby;
import edu.aau.se2.server.networking.dto.TextMessage;
import edu.aau.se2.server.networking.dto.UserList;
import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.logic.DiceHelper;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;
import edu.aau.se2.server.networking.kryonet.RegisterClasses;

import static java.lang.Thread.sleep;

public class MainServer {
    private static NetworkServerKryo server;
    private static int lobbyNumber = 0;
    private static ArrayList<Lobby> lobbys;
    private static final String TAG = "Server";

    private static final int TEMP_LOBBY_ID = 0;
    private static final int SERVER_PLAYER_ID = 0;

    public static void main(String[] args) {
        lobbys = new ArrayList<>();
        try {
            new MainServer().start();
        } catch (IOException e) {
            Logger.getLogger(TAG).log(Level.SEVERE, "Error starting server", e);
        }
    }

    private NetworkServerKryo server;
    private DataStore ds;
    private Logger log;

    public MainServer() {
        // TODO: Remove after lobbies are truly implemented
        ds = DataStore.getInstance();
        ds.createLobby();
        setupLogger();

        server = new NetworkServerKryo();
        SerializationRegister.registerClassesForComponent(server);
        server.registerCallback(arg -> {
            if (arg instanceof ReadyMessage) {
                log.log(Level.INFO, "Received ReadyMessage");
                handleReadyMessage((ReadyMessage) arg);
            }
            else if (arg instanceof ArmyPlacedMessage) {
                log.log(Level.INFO, "Received ArmyPlacedMessage");
                handleArmyPlaced((ArmyPlacedMessage) arg);
            }
            else if (arg instanceof CreateLobby) {
                hostLobby(((CreateLobby) arg).getUserName());
            }
        });
    }

    private void setupLogger() {
        log = Logger.getLogger(TAG);
        Handler handlerObj = new ConsoleHandler();
        handlerObj.setLevel(Level.INFO);
        log.addHandler(handlerObj);
        log.setLevel(Level.INFO);
        log.setUseParentHandlers(false);

    }

    public void start() throws IOException {
        server.start();
    }
    public void stop() {
        server.stop();
    }

    private void hostLobby(String userName) {
        lobbys.add(new edu.aau.se2.server.Lobby(userName + " lobby: " + lobbyNumber, lobbyNumber));

        // only for testing
        lobbys.get(lobbyNumber).addUser(new User("User 2: lobby:" + lobbyNumber));
        lobbys.get(lobbyNumber).addUser(new User("User 3: lobby:" + lobbyNumber));
        lobbys.get(lobbyNumber).getUser(1).setReady(true);

        server.broadcastLobbyMessage((lobbyNumber), new TextMessage("Lobby " + lobbyNumber + " erstellt!"));
        server.broadcastLobbyMessage((lobbyNumber), new UserList((ArrayList<User>) lobbys.get(lobbyNumber).getUsers()));


        // to long sleep, makes other devices disconnect (they need "keepalive" signal)
        try {
            sleep(2000);
        } catch (Exception e) {
            log.severe(e.getMessage());
        }

        lobbys.get(lobbyNumber).setUser(1, new User("User 4: lobby:" + lobbyNumber));
        lobbys.get(lobbyNumber).addUser(new User("User 5: lobby:" + lobbyNumber, true));
        lobbys.get(lobbyNumber).getUser(2).setReady(true);

        server.broadcastLobbyMessage((lobbyNumber), new UserList((ArrayList<User>) lobbys.get(lobbyNumber).getUsers()));
        lobbyNumber++;
    }

    private synchronized void handleArmyPlaced(ArmyPlacedMessage msg) {
        Lobby lobby = ds.getLobbyByID(TEMP_LOBBY_ID);
        // only player to act can place armies & only if he has enough armies to place remaining
        if (lobby.isStarted() &&
                msg.getFromPlayerID() == lobby.getCurrentPlayer().getUid() &&
                lobby.getCurrentPlayer().getArmyReserveCount() >= msg.getArmyCountPlaced()) {
            if (!lobby.areInitialArmiesPlaced()) {
                handleInitialArmyPlaced(msg);
            }
            else {
                // TODO: Handle normal army placed
            }
        }
    }

    private synchronized void handleInitialArmyPlaced(ArmyPlacedMessage msg) {
        Lobby lobby = ds.getLobbyByID(TEMP_LOBBY_ID);
        Territory t = lobby.getTerritoryByID(msg.getOnTerritoryID());
        // during initial army placing phase, player can on place armies on unoccupied territories
        // or, if all territories are already occupied, on own territories
        if ((!lobby.allTerritoriesOccupied() && t.isNotOccupied()) ||
                (lobby.allTerritoriesOccupied() && t.getOccupierPlayerID() == msg.getFromPlayerID())) {

            t.setOccupierPlayerID(msg.getFromPlayerID());
            t.addToArmyCount(msg.getArmyCountPlaced());
            Player curPlayer = lobby.getCurrentPlayer();
            curPlayer.addToArmyReserveCount(msg.getArmyCountPlaced()*-1);
            msg.setArmyCountRemaining(curPlayer.getArmyReserveCount());
            lobby.nextPlayerTurn();
            log.log(Level.ALL, "Broadcasting ArmyPlacedMessage");
            server.broadcastMessage(msg);
        }
    }

    private synchronized void handleReadyMessage(ReadyMessage msg) {
        Lobby lobby = ds.getLobbyByID(TEMP_LOBBY_ID);
        // TODO: remove once joinLobby is implemented
        lobby.addPlayer(new Player(msg.getFromPlayerID(), "Player" + msg.getFromPlayerID()));

        lobby.setPlayerReady(msg.getFromPlayerID(), msg.isReady());

        if (!lobby.isStarted() && lobby.canStartGame()) {
            lobby.setupForGameStart();
            lobby.setStarted(true);
            // start game
            StartGameMessage sgm = new StartGameMessage(msg.getLobbyID(), SERVER_PLAYER_ID, lobby.getPlayers(),
                    ArmyCountHelper.getStartCount(lobby.getPlayers().size()));
            log.log(Level.ALL, "Broadcasting StartGameMessage");
            server.broadcastMessage(sgm);

            // TODO: replace once "dice to decide starter" is implemented
            // send turn order and initiate initial army placing
            try {
                synchronized(lobby) {
                    lobby.wait(1000);
                }
                broadcastInitialArmyPlacingMessage(lobby);
            } catch (InterruptedException e) {
                broadcastInitialArmyPlacingMessage(lobby);
            }
        }
    }

    private synchronized void broadcastInitialArmyPlacingMessage(Lobby lobby) {
        lobby.setTurnOrder(DiceHelper.getRandomTurnOrder(lobby.getPlayers()));
        log.log(Level.ALL, "Broadcasting InitialArmyPlacingMessage");
        server.broadcastMessage(new InitialArmyPlacingMessage(TEMP_LOBBY_ID, SERVER_PLAYER_ID,
                lobby.getTurnOrder()));
    }
}
