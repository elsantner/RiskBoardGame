package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.PlayerLostConnectionListener;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.DiceHelper;
import edu.aau.se2.server.networking.SerializationRegister;
import edu.aau.se2.server.networking.dto.ArmyPlacedMessage;
import edu.aau.se2.server.networking.dto.CardExchangeMessage;
import edu.aau.se2.server.networking.dto.CreateLobbyMessage;
import edu.aau.se2.server.networking.dto.ErrorMessage;
import edu.aau.se2.server.networking.dto.InitialArmyPlacingMessage;
import edu.aau.se2.server.networking.dto.JoinedLobbyMessage;
import edu.aau.se2.server.networking.dto.LeftLobbyMessage;
import edu.aau.se2.server.networking.dto.LobbyListMessage;
import edu.aau.se2.server.networking.dto.NewArmiesMessage;
import edu.aau.se2.server.networking.dto.NextTurnMessage;
import edu.aau.se2.server.networking.dto.PlayersChangedMessage;
import edu.aau.se2.server.networking.dto.ReadyMessage;
import edu.aau.se2.server.networking.dto.RequestJoinLobbyMessage;
import edu.aau.se2.server.networking.dto.RequestLeaveLobby;
import edu.aau.se2.server.networking.dto.RequestLobbyListMessage;
import edu.aau.se2.server.networking.dto.StartGameMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer implements PlayerLostConnectionListener {
    private static final String TAG = "Server";

    private static final int SERVER_PLAYER_ID = 0;

    public static void main(String[] args) {
        try {
            new MainServer().start();
        } catch (IOException e) {
            Logger.getLogger(TAG).log(Level.SEVERE, "Error starting server", e);
        }
    }

    private NetworkServerKryo server;
    private DataStore ds;
    private Logger log;

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

    public MainServer() {
        ds = DataStore.getInstance();
        ds.setLostConnectionListener(this);
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
                } else if (arg instanceof RequestLobbyListMessage) {
                    log.info("Received RequestLobbyListMessage");
                    handleRequestLobbyListMessage((RequestLobbyListMessage) arg);
                } else if (arg instanceof RequestJoinLobbyMessage) {
                    log.info("Received RequestJoinLobbyMessage");
                    handleRequestJoinLobbyMessage((RequestJoinLobbyMessage) arg);
                }else if (arg instanceof RequestLeaveLobby) {
                    log.info("Received RequestLeaveLobby");
                    handleRequestLeaveLobby((RequestLeaveLobby) arg);
                }
            }
            catch (Exception ex) {
                log.log(Level.SEVERE, "Exception: " + ex.getMessage(), ex);
            }
        });
    }

    private synchronized void handleRequestLeaveLobby(RequestLeaveLobby msg) {
        Lobby lobbyToLeave = ds.getLobbyByID(msg.getLobbyID());
        Player playerToLeave = ds.getPlayerByID(msg.getFromPlayerID());
        try {
            lobbyToLeave.leave(playerToLeave);
            ds.updateLobby(lobbyToLeave);
            // if player could successfully leave the lobby, inform him and all remaining players
            log.info("Broadcasting LeftLobbyMessage");
            server.broadcastMessage(new LeftLobbyMessage(), playerToLeave);
            log.info("Broadcasting PlayersChangedMessage");
            server.broadcastMessage(new PlayersChangedMessage(lobbyToLeave.getLobbyID(),
                    SERVER_PLAYER_ID, lobbyToLeave.getPlayers()), lobbyToLeave.getPlayers());
        }
        catch (IllegalArgumentException ex) {
            // if player could not leave the lobby (host, game already started), close lobby and inform all players
            ds.removeLobby(lobbyToLeave.getLobbyID());
            log.info("Broadcasting LeftLobbyMessage");
            server.broadcastMessage(new LeftLobbyMessage(true), lobbyToLeave.getPlayers());
        }
    }

    private synchronized void handleRequestJoinLobbyMessage(RequestJoinLobbyMessage msg) {
        int errorCode = 0;
        Lobby lobbyToJoin = null;
        try {
            lobbyToJoin = ds.getLobbyByID(msg.getLobbyID());
            if (lobbyToJoin == null) {
                errorCode = ErrorMessage.JOIN_LOBBY_CLOSED;
            } else if (ds.isPlayerInAnyLobby(msg.getFromPlayerID())) {
                errorCode =  ErrorMessage.JOIN_LOBBY_ALREADY_JOINED;
            } else {
                lobbyToJoin.join(ds.getPlayerByID(msg.getFromPlayerID()));
                ds.updateLobby(lobbyToJoin);
            }
        }
        catch (NullPointerException ex) {
            errorCode = ErrorMessage.JOIN_LOBBY_UNKNOWN;
        }
        catch (IllegalStateException ex) {
            errorCode = ErrorMessage.JOIN_LOBBY_FULL;
        }

        if (errorCode == 0) {
            // if join lobby succeeded, inform all players in lobby
            log.info("Broadcasting JoinedLobbyMessage");
            server.broadcastMessage(new JoinedLobbyMessage(lobbyToJoin.getLobbyID(), SERVER_PLAYER_ID,
                    lobbyToJoin.getPlayers(), lobbyToJoin.getHost()), ds.getPlayerByID(msg.getFromPlayerID()));
            log.info("Broadcasting PlayersChangedMessage");
            server.broadcastMessage(new PlayersChangedMessage(lobbyToJoin.getLobbyID(),
                    SERVER_PLAYER_ID, lobbyToJoin.getPlayers()), lobbyToJoin.getPlayers());
        }
        else {
            // if error happened, inform just the requesting player
            log.info("Broadcasting ErrorMessage");
            server.broadcastMessage(new ErrorMessage(errorCode), ds.getPlayerByID(msg.getFromPlayerID()));
        }
    }

    private synchronized void handleRequestLobbyListMessage(RequestLobbyListMessage msg) {
        List<LobbyListMessage.LobbyData> lobbyData = new ArrayList<>();
        for (Lobby l: ds.getJoinableLobbyList()) {
            lobbyData.add(new LobbyListMessage.LobbyData(l.getLobbyID(), l.getHost(), l.getPlayers().size()));
        }
        log.info("Broadcasting LobbyListMessage");
        server.broadcastMessage(new LobbyListMessage(lobbyData), ds.getPlayerByID(msg.getFromPlayerID()));
    }

    private synchronized void handleNextTurnMessage(NextTurnMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        // only player to act can trigger next turn AND only if all new armies have been received and placed
        if (lobby.getPlayerToAct().getUid() == msg.getFromPlayerID() &&
                lobby.hasCurrentPlayerToActReceivedNewArmies() &&
                lobby.getPlayerToAct().getArmyReserveCount() == 0) {

            lobby.nextPlayersTurn();
            ds.updateLobby(lobby);

            log.info("Broadcasting NextTurnMessage");
            server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                    lobby.getPlayerToAct().getUid()), lobby.getPlayers());
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
            server.broadcastMessage(new NewArmiesMessage(lobby.getLobbyID(), msg.getFromPlayerID(),
                    lobby.getPlayerToAct().getArmyReserveCount()), lobby.getPlayers());
        }
    }

    private synchronized void handleCreateLobby(CreateLobbyMessage msg) {
        Player player = ds.getPlayerByID(msg.getPlayerID());
        if (player != null && !ds.isPlayerInAnyLobby(msg.getPlayerID())) {
            Lobby newLobby = ds.createLobby(player);
            log.info("Broadcasting JoinedLobbyMessage");
            server.broadcastMessage(new JoinedLobbyMessage(newLobby.getLobbyID(), SERVER_PLAYER_ID,
                    newLobby.getPlayers(), newLobby.getHost()), newLobby.getHost());
            log.info("Broadcasting PlayersChangedMessage");
            server.broadcastMessage(new PlayersChangedMessage(newLobby.getLobbyID(),
                    SERVER_PLAYER_ID, newLobby.getPlayers()), newLobby.getPlayers());
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

            log.info("Broadcasting ArmyPlacedMessage");
            server.broadcastMessage(msg, lobby.getPlayers());
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
            server.broadcastMessage(msg, lobby.getPlayers());
            if (lobby.areInitialArmiesPlaced()) {
                log.info("All initial armies placed - Broadcasting NextTurnMessage");
                server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                        lobby.getPlayerToAct().getUid()), lobby.getPlayers());
            }
        }
    }

    private synchronized void handleReadyMessage(ReadyMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        lobby.setPlayerReady(msg.getFromPlayerID(), msg.isReady());
        ds.updateLobby(lobby);

        log.info("Broadcasting PlayersChangedMessage");
        server.broadcastMessage(new PlayersChangedMessage(lobby.getLobbyID(),
                SERVER_PLAYER_ID, lobby.getPlayers()), lobby.getPlayers());

        if (!lobby.isStarted() && lobby.canStartGame()) {
            lobby.setupForGameStart();
            lobby.setStarted(true);
            ds.updateLobby(lobby);
            // start game
            StartGameMessage sgm = new StartGameMessage(msg.getLobbyID(), SERVER_PLAYER_ID, lobby.getPlayers(),
                    lobby.getPlayers().get(0).getArmyReserveCount());
            log.info("Broadcasting StartGameMessage");
            server.broadcastMessage(sgm, lobby.getPlayers());

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
        server.broadcastMessage(new InitialArmyPlacingMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                lobby.getTurnOrder()), lobby.getPlayers());
    }

    @Override
    public void playerLostConnection(Player player, Lobby playerLobby) {
        try {
            if (playerLobby != null) {
                playerLobby.leave(player);
                ds.updateLobby(playerLobby);
                // if player could successfully leave the lobby, inform all remaining players
                log.info("Broadcasting PlayersChangedMessage");
                server.broadcastMessage(new PlayersChangedMessage(playerLobby.getLobbyID(),
                        SERVER_PLAYER_ID, playerLobby.getPlayers()), playerLobby.getPlayers());
            }
        }
        catch (IllegalArgumentException ex) {
            // if player could not leave the lobby (host, game already started), close lobby and inform all remaining players
            ds.removeLobby(playerLobby.getLobbyID());
            log.info("Broadcasting LeftLobbyMessage");
            server.broadcastMessage(new LeftLobbyMessage(true), playerLobby.getPlayers());
        }
    }
}
