package edu.aau.se2.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Card;
import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.data.PlayerLostConnectionListener;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.server.logic.DiceHelper;
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
import edu.aau.se2.server.networking.dto.prelobby.ChangeNicknameMessage;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;
import edu.aau.se2.server.networking.dto.prelobby.RequestLobbyListMessage;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServer implements PlayerLostConnectionListener {
    private static final String TAG = "Server";

    protected static final int SERVER_PLAYER_ID = 0;

    public static void main(String[] args) {
        try {
            new MainServer().start();
        } catch (IOException e) {
            Logger.getLogger(TAG).log(Level.SEVERE, "Error starting server", e);
        }
    }

    protected NetworkServerKryo server;
    protected DataStore ds;
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
        server = new NetworkServerKryo(ds);
        SerializationRegister.registerClassesForComponent(server);
        registerCallbacks();
    }

    protected void registerCallbacks() {
        server.registerCallback(arg -> {
            try {
                log.info("Received " + arg.getClass().getSimpleName());
                if (arg instanceof ReadyMessage) {
                    handleReadyMessage((ReadyMessage) arg);
                } else if (arg instanceof ArmyPlacedMessage) {
                    handleArmyPlacedMessage((ArmyPlacedMessage) arg);
                } else if (arg instanceof CreateLobbyMessage) {
                    handleCreateLobbyMessage((CreateLobbyMessage) arg);
                } else if (arg instanceof CardExchangeMessage) {
                    handleCardExchangeMessage((CardExchangeMessage) arg);
                } else if (arg instanceof NextTurnMessage) {
                    handleNextTurnMessage((NextTurnMessage) arg);
                } else if (arg instanceof RequestLobbyListMessage) {
                    handleRequestLobbyListMessage((RequestLobbyListMessage) arg);
                } else if (arg instanceof RequestJoinLobbyMessage) {
                    handleRequestJoinLobbyMessage((RequestJoinLobbyMessage) arg);
                } else if (arg instanceof RequestLeaveLobby) {
                    handleRequestLeaveLobby((RequestLeaveLobby) arg);
                } else if (arg instanceof ArmyMovedMessage) {
                    handleArmyMovedMessage((ArmyMovedMessage) arg);
                } else if (arg instanceof AttackingPhaseFinishedMessage) {
                    handleAttackingPhaseFinishedMessage((AttackingPhaseFinishedMessage) arg);
                } else if (arg instanceof DiceResultMessage) {
                    handleDiceResultMessage((DiceResultMessage) arg);
                } else if (arg instanceof AttackStartedMessage) {
                    handleAttackStartedMessage((AttackStartedMessage) arg);
                } else if (arg instanceof OccupyTerritoryMessage) {
                    handleOccupyTerritoryMessage((OccupyTerritoryMessage) arg);
                } else if (arg instanceof DefenderDiceCountMessage) {
                    handleDefenderDiceCountMessage((DefenderDiceCountMessage) arg);
                } else if (arg instanceof ChangeNicknameMessage) {
                    handleChangelNicknameMessage((ChangeNicknameMessage) arg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                log.log(Level.SEVERE, "Exception: " + ex.getMessage(), ex);
            }
        });
    }

    public void handleChangelNicknameMessage(ChangeNicknameMessage msg) {
        Player player = ds.getPlayerByID(msg.getFromPlayerID());
        String name = player.getNickname();
        String changedName = msg.getNickname();
        System.out.println("###handleChangelNicknameMessage new: " + changedName);
        player.setNickname(changedName);
        //TODO: broadcast message to all players
    }

    private synchronized void handleDefenderDiceCountMessage(DefenderDiceCountMessage msg) {
        Lobby l = ds.getLobbyByID(msg.getLobbyID());

        if (l.attackRunning() && l.getDefender().getUid() == msg.getFromPlayerID() && l.getTerritoryByID(l.getCurrentAttack().getToTerritoryID()).getArmyCount() >= msg.getDiceCount() && msg.getDiceCount() <= 2) {
            l.getCurrentAttack().setDefenderDiceCount(msg.getDiceCount());
            server.broadcastMessage(msg, l.getPlayers());
        }
    }

    private synchronized void handleOccupyTerritoryMessage(OccupyTerritoryMessage msg) {
        Lobby l = ds.getLobbyByID(msg.getLobbyID());
        Territory territoryToOccupy = l.getTerritoryByID(msg.getTerritoryID());
        Territory fromTerritory = l.getTerritoryByID(msg.getFromTerritoryID());

        // if it's this players turn during an attack and army counts are fine
        if (l.isPlayersTurn(msg.getFromPlayerID()) && l.attackRunning() &&
                l.getCurrentAttack().isOccupyRequired() && territoryToOccupy.getArmyCount() == 0 &&
                fromTerritory.getArmyCount() > msg.getArmyCount()) {

            territoryToOccupy.setOccupierPlayerID(fromTerritory.getOccupierPlayerID());
            territoryToOccupy.setArmyCount(msg.getArmyCount());
            fromTerritory.subFromArmyCount(msg.getArmyCount());
            l.setCurrentAttack(null);
            ds.updateLobby(l);

            server.broadcastMessage(msg, l.getPlayers());
        }
    }

    private synchronized void handleAttackStartedMessage(AttackStartedMessage msg) {
        Lobby l = ds.getLobbyByID(msg.getLobbyID());

        if (l.isPlayersTurn(msg.getFromPlayerID()) &&
                l.isPlayersTerritory(l.getPlayerToAct().getUid(), msg.getFromTerritoryID()) &&
                !l.isPlayersTerritory(l.getPlayerToAct().getUid(), msg.getToTerritoryID()) &&
                l.getTerritoryByID(msg.getFromTerritoryID()).getArmyCount() > msg.getDiceCount() &&
                msg.getDiceCount() <= 3) {

            l.setCurrentAttack(new Attack(msg.getFromTerritoryID(), msg.getToTerritoryID(), msg.getDiceCount()));
            server.broadcastMessage(msg, l.getPlayers());
        }
    }

    private synchronized void handleDiceResultMessage(DiceResultMessage msg) {
        Lobby l = ds.getLobbyByID(msg.getLobbyID());

        // if it's attackers turn and attack running, broadcast message to lobby
        if (l.getPlayerToAct().getUid() == msg.getFromPlayerID() && l.attackRunning() && msg.getResults().size() == l.getCurrentAttack().getAttackerDiceCount()) {
            l.getCurrentAttack().setAttackerDiceResults(msg.getResults());
            l.getCurrentAttack().setCheated(msg.isCheated());
            server.broadcastMessage(new DiceResultMessage(msg), l.getPlayers());
        } else if (l.attackRunning() && l.getDefender().getUid() == msg.getFromPlayerID() && msg.getResults().size() == l.getCurrentAttack().getDefenderDiceCount()) {
            l.getCurrentAttack().setDefenderDiceResults(msg.getResults());
            server.broadcastMessage(msg, l.getPlayers());
            try {
                wait(4000);
            } catch (InterruptedException e) {
                log.severe(e.getMessage());
            }
            attackFinished(l.getLobbyID());
        }
    }

    private synchronized void attackFinished(int lobbyId) {
        Lobby l = ds.getLobbyByID(lobbyId);

        if (l.attackRunning()) {
            int armiesLostAttacker = 0;
            int armiesLostDefender = 0;

            List<Integer> attackerResults = l.getCurrentAttack().getAttackerDiceResults();
            List<Integer> defenderResults = l.getCurrentAttack().getDefenderDiceResults();

            attackerResults.sort(Integer::compareTo);
            Collections.sort(attackerResults, Collections.reverseOrder());
            defenderResults.sort(Integer::compareTo);
            Collections.sort(defenderResults, Collections.reverseOrder());

            for(int i = 0; i < Math.min(attackerResults.size(), defenderResults.size()); i++) {
                if (attackerResults.get(i) < defenderResults.get(i)) {
                    armiesLostAttacker++;
                } else {
                    armiesLostDefender++;
                }
            }

            Territory fromTerritory = l.getTerritoryByID(l.getCurrentAttack().getFromTerritoryID());
            Territory toTerritory = l.getTerritoryByID(l.getCurrentAttack().getToTerritoryID());

            fromTerritory.setArmyCount(Math.max(0, fromTerritory.getArmyCount() - armiesLostAttacker));
            toTerritory.setArmyCount(Math.max(0, toTerritory.getArmyCount() - armiesLostDefender));

            boolean occupyRequired = toTerritory.getArmyCount() == 0;
            l.getCurrentAttack().setOccupyRequired(occupyRequired);
            boolean wasCheated = l.getCurrentAttack().isCheated();

            if (!occupyRequired) {
                l.setCurrentAttack(null);
            }

            ds.updateLobby(l);

            server.broadcastMessage(new AttackResultMessage(lobbyId, l.getPlayerToAct().getUid(), armiesLostAttacker, armiesLostDefender, wasCheated, occupyRequired), l.getPlayers());
        }
    }

    private void handleAttackingPhaseFinishedMessage(AttackingPhaseFinishedMessage msg) {
        Lobby l = ds.getLobbyByID(msg.getLobbyID());
        // if it's players turn, broadcast message to lobby
        if (l.getPlayerToAct().getUid() == msg.getFromPlayerID()) {
            server.broadcastMessage(msg, l.getPlayers());
        }
    }

    private synchronized void handleArmyMovedMessage(ArmyMovedMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        Player player = ds.getPlayerByID(msg.getFromPlayerID());
        // if is players turn and he has placed all new armies
        if (lobby.getPlayerToAct().getUid() == player.getUid() &&
                lobby.hasCurrentPlayerToActPlacedNewArmies()) {

            Territory fromTerritory = lobby.getTerritoryByID(msg.getFromTerritoryID());
            Territory toTerritory = lobby.getTerritoryByID(msg.getToTerritoryID());
            // if player occupies both territories and there are more armies than being moved (at least 1 needs to remain)
            if (fromTerritory.getOccupierPlayerID() == player.getUid() &&
                    toTerritory.getOccupierPlayerID() == player.getUid() &&
                    fromTerritory.getArmyCount() > msg.getArmyCountMoved()) {

                fromTerritory.subFromArmyCount(msg.getArmyCountMoved());
                toTerritory.addToArmyCount(msg.getArmyCountMoved());
                lobby.nextPlayersTurn();
                ds.updateLobby(lobby);

                // broadcast message to all players & start next turn
                server.broadcastMessage(msg, lobby.getPlayers());
                server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                        lobby.getPlayerToAct().getUid()), lobby.getPlayers());
            }
        }
    }

    private synchronized void handleRequestLeaveLobby(RequestLeaveLobby msg) {
        Lobby lobbyToLeave = ds.getLobbyByID(msg.getLobbyID());
        Player playerToLeave = ds.getPlayerByID(msg.getFromPlayerID());
        try {
            lobbyToLeave.leave(playerToLeave);
            playerToLeave.reset();
            ds.updateLobby(lobbyToLeave);
            // if player could successfully leave the lobby, inform him and all remaining players
            server.broadcastMessage(new LeftLobbyMessage(), playerToLeave);
            server.broadcastMessage(new PlayersChangedMessage(lobbyToLeave.getLobbyID(),
                    SERVER_PLAYER_ID, lobbyToLeave.getPlayers()), lobbyToLeave.getPlayers());
        } catch (IllegalArgumentException ex) {
            // if player could not leave the lobby (host, game already started), close lobby and inform all players
            ds.removeLobby(lobbyToLeave.getLobbyID());
            lobbyToLeave.resetPlayers();
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
                errorCode = ErrorMessage.JOIN_LOBBY_ALREADY_JOINED;
            } else {
                lobbyToJoin.join(ds.getPlayerByID(msg.getFromPlayerID()));
                ds.updateLobby(lobbyToJoin);
            }
        } catch (NullPointerException ex) {
            errorCode = ErrorMessage.JOIN_LOBBY_UNKNOWN;
        } catch (IllegalStateException ex) {
            errorCode = ErrorMessage.JOIN_LOBBY_FULL;
        }

        if (errorCode == 0) {
            // if join lobby succeeded, inform all players in lobby
            server.broadcastMessage(new JoinedLobbyMessage(lobbyToJoin.getLobbyID(), SERVER_PLAYER_ID,
                    lobbyToJoin.getPlayers(), lobbyToJoin.getHost()), ds.getPlayerByID(msg.getFromPlayerID()));
            server.broadcastMessage(new PlayersChangedMessage(lobbyToJoin.getLobbyID(),
                    SERVER_PLAYER_ID, lobbyToJoin.getPlayers()), lobbyToJoin.getPlayers());
        } else {
            // if error happened, inform just the requesting player
            server.broadcastMessage(new ErrorMessage(errorCode), ds.getPlayerByID(msg.getFromPlayerID()));
        }
    }

    private synchronized void handleRequestLobbyListMessage(RequestLobbyListMessage msg) {
        List<LobbyListMessage.LobbyData> lobbyData = new ArrayList<>();
        for (Lobby l : ds.getJoinableLobbyList()) {
            lobbyData.add(new LobbyListMessage.LobbyData(l.getLobbyID(), l.getHost(), l.getPlayers().size()));
        }
        server.broadcastMessage(new LobbyListMessage(lobbyData), ds.getPlayerByID(msg.getFromPlayerID()));
    }

    private synchronized void handleNextTurnMessage(NextTurnMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        // only player to act can trigger next turn AND only if all new armies have been received and placed
        if (lobby.getPlayerToAct().getUid() == msg.getFromPlayerID() &&
                lobby.hasCurrentPlayerToActPlacedNewArmies()) {

            // now: at the end of turn give new random card to player
            // todo: only give card if player has occupied new territory
            int id = msg.getFromPlayerID();
            Card c = lobby.getCardDeck().getRandomCard(id);

            // test if there is a set for trading in (if yes -> ask player for trade at start of next turn)
            lobby.getPlayerToAct().setTradableSet(lobby.getCardDeck().getCardSet(id));
            boolean b = false;
            if (lobby.getPlayerToAct().getTradableSet().length == 3  ) {
                b = true;
            }


            if (c != null) { // if c is null, there are no cards left
                // send name of new Card to player of last turn
                server.broadcastMessage(new NewCardMessage(lobby.getLobbyID(), id, c.getCardName(), b), lobby.getPlayerToAct());
            }

            lobby.nextPlayersTurn();
            ds.updateLobby(lobby);

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

            // add armies, when player trades in set, +2 bonus armies on one territory if same id as card
            int territoryIdForBonusArmies = -1;
            if (msg.isExchangeSet()) {

                Player p = lobby.getPlayerToAct();
                p.setArmyReserveCount(p.getArmyReserveCount() + lobby.getCardDeck().tradeInSet(p.getTradableSet()));
                territoryIdForBonusArmies = lobby.getCardDeck().getTerritoryIDForBonusArmies(p.getTradableSet(), lobby.getTerritoriesOccupiedByPlayer(p.getUid()));

                if(territoryIdForBonusArmies != -1) {
                    Territory t = lobby.getTerritoryByID(territoryIdForBonusArmies);
                    t.addToArmyCount(2);
                }

                p.setTradableSet(null);

                server.broadcastMessage(new RefreshCardsMessage(lobby.getLobbyID(), p.getUid(), lobby.getCardDeck().getCardNamesOfPlayer(p.getUid())), lobby.getPlayerToAct());
            }
            ds.updateLobby(lobby);

            server.broadcastMessage(new NewArmiesMessage(lobby.getLobbyID(), msg.getFromPlayerID(),
                    lobby.getPlayerToAct().getArmyReserveCount(), territoryIdForBonusArmies), lobby.getPlayers());
        }
    }

    private synchronized void handleCreateLobbyMessage(CreateLobbyMessage msg) {
        Player player = ds.getPlayerByID(msg.getPlayerID());
        if (player != null && !ds.isPlayerInAnyLobby(msg.getPlayerID())) {
            Lobby newLobby = ds.createLobby(player);
            server.broadcastMessage(new JoinedLobbyMessage(newLobby.getLobbyID(), SERVER_PLAYER_ID,
                    newLobby.getPlayers(), newLobby.getHost()), newLobby.getHost());
            server.broadcastMessage(new PlayersChangedMessage(newLobby.getLobbyID(),
                    SERVER_PLAYER_ID, newLobby.getPlayers()), newLobby.getPlayers());
        }
    }

    private synchronized void handleArmyPlacedMessage(ArmyPlacedMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        // only player to act can place armies & only if he has enough armies to place remaining
        if (lobby.isStarted() &&
                msg.getFromPlayerID() == lobby.getPlayerToAct().getUid() &&
                lobby.getPlayerToAct().getArmyReserveCount() >= msg.getArmyCountPlaced()) {
            if (!lobby.areInitialArmiesPlaced()) {
                handleInitialArmyPlaced(msg);
            } else {
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
            curPlayer.addToArmyReserveCount(msg.getArmyCountPlaced() * -1);
            ds.updateLobby(lobby);

            msg.setArmyCountRemaining(curPlayer.getArmyReserveCount());

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
            curPlayer.addToArmyReserveCount(msg.getArmyCountPlaced() * -1);
            msg.setArmyCountRemaining(curPlayer.getArmyReserveCount());
            lobby.nextPlayersTurn();
            ds.updateLobby(lobby);

            server.broadcastMessage(msg, lobby.getPlayers());
            if (lobby.areInitialArmiesPlaced()) {
                log.info("All initial armies placed");
                server.broadcastMessage(new NextTurnMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                        lobby.getPlayerToAct().getUid()), lobby.getPlayers());
            }
        }
    }

    private synchronized void handleReadyMessage(ReadyMessage msg) {
        Lobby lobby = ds.getLobbyByID(msg.getLobbyID());
        lobby.setPlayerReady(msg.getFromPlayerID(), msg.isReady());
        ds.updateLobby(lobby);

        server.broadcastMessage(new PlayersChangedMessage(lobby.getLobbyID(),
                SERVER_PLAYER_ID, lobby.getPlayers()), lobby.getPlayers());

        synchronized (lobby) {
            if (!lobby.isStarted() && lobby.canStartGame()) {
                lobby.setupForGameStart();
                lobby.setStarted(true);
                ds.updateLobby(lobby);
                // start game
                StartGameMessage sgm = new StartGameMessage(msg.getLobbyID(), SERVER_PLAYER_ID, lobby.getPlayers(),
                        lobby.getPlayers().get(0).getArmyReserveCount());
                server.broadcastMessage(sgm, lobby.getPlayers());
                // TODO: replace once "dice to decide starter" is implemented
                // TODO: temporary solution to not have timing issue on client --> waiting on decision whether to implement DiceToDecideStart or not
                broadcastInitialArmyPlacingMessage(lobby);
            }
        }
    }

    private synchronized void broadcastInitialArmyPlacingMessage(Lobby lobby) {
        lobby.setTurnOrder(DiceHelper.getRandomTurnOrder(lobby.getPlayers()));
        server.broadcastMessage(new InitialArmyPlacingMessage(lobby.getLobbyID(), SERVER_PLAYER_ID,
                lobby.getTurnOrder()), lobby.getPlayers());
    }

    @Override
    public void playerLostConnection(Player player, Lobby playerLobby) {
        try {
            if (playerLobby != null) {
                playerLobby.leave(player);
                player.reset();
                ds.updateLobby(playerLobby);
                // if player could successfully leave the lobby, inform all remaining players
                server.broadcastMessage(new PlayersChangedMessage(playerLobby.getLobbyID(),
                        SERVER_PLAYER_ID, playerLobby.getPlayers()), playerLobby.getPlayers());
            }
        } catch (IllegalArgumentException ex) {
            // if player could not leave the lobby (host, game already started), close lobby and inform all remaining players
            ds.removeLobby(playerLobby.getLobbyID());
            playerLobby.resetPlayers();
            server.broadcastMessage(new LeftLobbyMessage(true), playerLobby.getPlayers());
        }
    }
}
