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
import edu.aau.se2.server.networking.Callback;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.BaseMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer {
    private static final String TAG = "Server";

    private static final int TEMP_LOBBY_ID = 0;
    private static final int SERVER_PLAYER_ID = 0;

    public static void main(String[] args) {
        try {
            new MainServer().start();
        } catch (IOException e) {
            e.printStackTrace();
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
        server.registerCallback(new Callback<BaseMessage>() {
            @Override
            public void callback(BaseMessage arg) {
                if (arg instanceof ReadyMessage) {
                    log.log(Level.ALL, "Received ReadyMessage");
                    handleReadyMessage((ReadyMessage) arg);
                }
                else if (arg instanceof ArmyPlacedMessage) {
                    log.log(Level.ALL, "Received ArmyPlacedMessage");
                    handleArmyPlaced((ArmyPlacedMessage) arg);
                }
            }
        });
    }

    private void setupLogger() {
        log = Logger.getLogger(TAG);
        Handler handlerObj = new ConsoleHandler();
        handlerObj.setLevel(Level.ALL);
        log.addHandler(handlerObj);
        log.setLevel(Level.ALL);
        log.setUseParentHandlers(false);

    }

    public void start() throws IOException {
        server.start();
    }
    public void stop() {
        server.stop();
    }

    private synchronized void handleArmyPlaced(ArmyPlacedMessage msg) {
        Lobby lobby = ds.getLobbyByID(TEMP_LOBBY_ID);
        if (lobby.isStarted()) {
            // only player to act can place armies & only if he has enough armies to place remaining
            if (msg.getFromPlayerID() == lobby.getCurrentPlayer().getUid() &&
                    lobby.getCurrentPlayer().getArmyReserveCount() >= msg.getArmyCountPlaced()) {
                if (!lobby.areInitialArmiesPlaced()) {
                    handleInitialArmyPlaced(msg);
                }
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
                Thread.sleep(1000);
                lobby.setTurnOrder(DiceHelper.getRandomTurnOrder(lobby.getPlayers()));
                log.log(Level.ALL, "Broadcasting InitialArmyPlacingMessage");
                server.broadcastMessage(new InitialArmyPlacingMessage(TEMP_LOBBY_ID, SERVER_PLAYER_ID,
                        lobby.getTurnOrder()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
