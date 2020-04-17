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
import edu.aau.se2.server.logic.DiceHelper;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.NextTurnMessage;
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

    public MainServer(boolean debug_lobbiesEnabled) {
        this.debug_lobbiesEnabled = debug_lobbiesEnabled;

        // TODO: Remove after lobbies are truly implemented
        ds = DataStore.getInstance();
        ds.createLobby();

        setupLogger();

        server = new NetworkServerKryo();
        SerializationRegister.registerClassesForComponent(server);
        server.registerCallback(arg -> {
            try {
                if (arg instanceof ReadyMessage) {
                    log.info("Received ReadyMessage");
                    handleReadyMessage((ReadyMessage) arg);
                } else if (arg instanceof ArmyPlacedMessage) {
                    log.info("Received ArmyPlacedMessage");
                    handleArmyPlaced((ArmyPlacedMessage) arg);
                } else if (arg instanceof CreateLobbyMessage) {
                    log.info("Received CreateLobbyMessage");
                    handleCreateLobby((CreateLobbyMessage) arg);
                } else if (arg instanceof CardExchangeMessage) {
                    log.info("Received CardExchangeMessage");
                    handleCardExchangeMessage((CardExchangeMessage) arg);
                } else if (arg instanceof NextTurnMessage) {
                    log.info("Received NextTurnMessage");
                    handleNextTurnMessage((NextTurnMessage) arg);
                }
            }
            catch (Exception ex) {
                log.log(Level.SEVERE, "Exception: " + ex.getMessage(), ex);
            }
        });
    }

    private synchronized void handleNextTurnMessage(NextTurnMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        // only player to act can trigger next turn
        if (lobby.getPlayerToAct().getUid() == msg.getFromPlayerID()) {
            lobby.nextPlayersTurn();
            ds.updateLobby(lobby);

            log.info("Broadcasting NextTurnMessage");
            if (debug_lobbiesEnabled) {
                server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                        lobby.getPlayerToAct().getUid()), lobby.getPlayers());
            }
            else {
                server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                        lobby.getPlayerToAct().getUid()));
            }
        }
    }

    private synchronized void handleCardExchangeMessage(CardExchangeMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        // only handle if msg is from player to act
        if (msg.getFromPlayerID() == lobby.getPlayerToAct().getUid()) {
            // generate new armies for player
            lobby.giveNewArmiesToPlayer(msg.getFromPlayerID());
            ds.updateLobby(lobby);

            log.info("Broadcasting NewArmiesMessage");
            if (debug_lobbiesEnabled) {
                server.broadcastMessage(new NewArmiesMessage(lobby.getLobbyID(), msg.getFromPlayerID(),
                        lobby.getPlayerToAct().getArmyReserveCount()), lobby.getPlayers());
            }
            else {
                server.broadcastMessage(new NewArmiesMessage(lobby.getLobbyID(), msg.getFromPlayerID(),
                        lobby.getPlayerToAct().getArmyReserveCount()));
            }
        }
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
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        // only player to act can place armies & only if he has enough armies to place remaining
        if (lobby.isStarted() &&
                msg.getFromPlayerID() == lobby.getPlayerToAct().getUid() &&
                lobby.getPlayerToAct().getArmyReserveCount() >= msg.getArmyCountPlaced()) {
            if (!lobby.areInitialArmiesPlaced()) {
                handleInitialArmyPlaced(msg);
            }
            else {
                handleTurnArmyPlaced(msg);
            }
        }
    }

    /**
     * Handles army placed during a normal turn
     * @param msg ArmyPlacedMessage
     */
    private synchronized void handleTurnArmyPlaced(ArmyPlacedMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        Territory t = lobby.getTerritoryByID(msg.getOnTerritoryID());
        if (t.getOccupierPlayerID() == msg.getFromPlayerID()) {
            t.setOccupierPlayerID(msg.getFromPlayerID());
            t.addToArmyCount(msg.getArmyCountPlaced());

            Player curPlayer = lobby.getPlayerToAct();
            curPlayer.addToArmyReserveCount(msg.getArmyCountPlaced()*-1);
            ds.updateLobby(lobby);

            msg.setArmyCountRemaining(curPlayer.getArmyReserveCount());

            if (debug_lobbiesEnabled) {
                server.broadcastMessage(msg, lobby.getPlayers());
            }
            else {
                server.broadcastMessage(msg);
            }
        }
    }

    /**
     * Handles army placed during initial army placing phase
     * @param msg ArmyPlacedMessage
     */
    private synchronized void handleInitialArmyPlaced(ArmyPlacedMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
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
            lobby.nextPlayersTurn();
            ds.updateLobby(lobby);

            log.info("Broadcasting ArmyPlacedMessage");
            if (debug_lobbiesEnabled) {
                server.broadcastMessage(msg, lobby.getPlayers());
                if (lobby.areInitialArmiesPlaced()) {
                    log.info("All initial armies placed - Broadcasting NextTurnMessage");
                    server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                            lobby.getPlayerToAct().getUid()), lobby.getPlayers());
                }
            }
            else {
                server.broadcastMessage(msg);
                if (lobby.areInitialArmiesPlaced()) {
                    log.info("All initial armies placed - Broadcasting NextTurnMessage");
                    server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                            lobby.getPlayerToAct().getUid()));
                }
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
                    lobby.getPlayers().get(0).getArmyReserveCount());
            log.info("Broadcasting StartGameMessage");
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
        log.info("Broadcasting InitialArmyPlacingMessage");
        if (debug_lobbiesEnabled) {
            server.broadcastMessage(new InitialArmyPlacingMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                    lobby.getTurnOrder()), lobby.getPlayers());
        }
        else {
            server.broadcastMessage(new InitialArmyPlacingMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                    lobby.getTurnOrder()));
        }
    }
}
