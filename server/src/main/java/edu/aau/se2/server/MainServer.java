package edu.aau.se2.server;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.logic.DiceHelper;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer {
    private static final String TAG = "Server";

    private static final int TEMP_LOBBY_ID = 0;
    private static final int SERVER_PLAYER_ID = 0;

    public static void main(String[] args) {
        try {
            new MainServer(true).start();
        } catch (IOException e) {
            Logger.getLogger(TAG).log(Level.SEVERE, "Error starting server", e);
        }
    }

    private NetworkServerKryo server;
    private DataStore ds;
    private Logger log;

    // TODO: Remove debug_lobbiesEnabled once join lobbies in implemented
    private boolean debug_lobbiesEnabled;

    public MainServer(boolean debug_lobbiesEnabled) {
        this.debug_lobbiesEnabled = debug_lobbiesEnabled;

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
            else if (arg instanceof CreateLobbyMessage) {
                log.log(Level.INFO, "Received CreateLobbyMessage");
                handleCreateLobby((CreateLobbyMessage) arg);
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

    private synchronized void handleCreateLobby(CreateLobbyMessage msg) {
        Player player = ds.getPlayerByID(msg.getPlayerID());
        if (player != null && !ds.isPlayerHostingLobby(msg.getPlayerID())) {
            Lobby newLobby = ds.createLobby(player);

            server.broadcastMessage(new JoinedLobbyMessage(newLobby.getLobbyID(), SERVER_PLAYER_ID,
                    newLobby.getPlayers(), newLobby.getHost()), newLobby.getHost());
        }
    }

    private synchronized void handleArmyPlaced(ArmyPlacedMessage msg) {
        Lobby lobby = ds.getLobbyByID(TEMP_LOBBY_ID);
        // only player to act can place armies & only if he has enough armies to place remaining
        if (lobby.isStarted() &&
                msg.getFromPlayerID() == lobby.getPlayerToAct().getUid() &&
                lobby.getPlayerToAct().getArmyReserveCount() >= msg.getArmyCountPlaced()) {
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
            Player curPlayer = lobby.getPlayerToAct();
            curPlayer.addToArmyReserveCount(msg.getArmyCountPlaced()*-1);
            msg.setArmyCountRemaining(curPlayer.getArmyReserveCount());
            lobby.nextPlayerTurn();
            log.log(Level.INFO, "Broadcasting ArmyPlacedMessage");
            if (debug_lobbiesEnabled) {
                server.broadcastMessage(msg, lobby.getPlayers());
            }
            else {
                server.broadcastMessage(msg);
            }
        }
    }

    private synchronized void handleReadyMessage(ReadyMessage msg) {
        Lobby lobby = ds.getLobbyByID(TEMP_LOBBY_ID);
        // TODO: remove once joinLobby is implemented
        lobby.addPlayer(ds.getPlayerByID(msg.getFromPlayerID()));

        lobby.setPlayerReady(msg.getFromPlayerID(), msg.isReady());
        ds.updateLobby(lobby);

        if (!lobby.isStarted() && lobby.canStartGame()) {
            lobby.setupForGameStart();
            lobby.setStarted(true);
            ds.updateLobby(lobby);
            // start game
            StartGameMessage sgm = new StartGameMessage(msg.getLobbyID(), SERVER_PLAYER_ID, lobby.getPlayers(),
                    ArmyCountHelper.getStartCount(lobby.getPlayers().size()));
            log.log(Level.INFO, "Broadcasting StartGameMessage");
            if (debug_lobbiesEnabled) {
                server.broadcastMessage(sgm, lobby.getPlayers());
            }
            else {
                server.broadcastMessage(sgm);
            }

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
        log.log(Level.INFO, "Broadcasting InitialArmyPlacingMessage");
        if (debug_lobbiesEnabled) {
            server.broadcastMessage(new InitialArmyPlacingMessage(TEMP_LOBBY_ID, SERVER_PLAYER_ID,
                    lobby.getTurnOrder()), lobby.getPlayers());
        }
        else {
            server.broadcastMessage(new InitialArmyPlacingMessage(TEMP_LOBBY_ID, SERVER_PLAYER_ID,
                    lobby.getTurnOrder()));
        }
    }
}
