package edu.aau.se2.server.networking;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import edu.aau.se2.server.MainServer;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.logic.ArmyCountHelper;
import edu.aau.se2.server.logic.DiceHelper;
import edu.aau.se2.server.networking.dto.prelobby.ConnectedMessage;
import edu.aau.se2.server.networking.kryonet.NetworkClientKryo;
import edu.aau.se2.server.networking.kryonet.NetworkServerKryo;

public class MainServerTestable extends MainServer {

    public MainServerTestable() {
        super();
        ds = new DataStoreTestable();
        ds.setLostConnectionListener(this);
        server = new NetworkServerKryo(ds);
        SerializationRegister.registerClassesForComponent(server);
        registerCallbacks();
    }

    /**
     * Starts game and places initial armies.
     * This requires the given lobby to be setup before.
     * @param lobbyID ID of lobby to setup game for.
     * @return Turn order of clients.
     */
    public List<Integer> setupGame(int lobbyID) {
        Lobby l = ds.getLobbyByID(lobbyID);
        if (l == null) {
            throw new IllegalArgumentException("no such lobby");
        }

        l.setupForGameStart();
        l.setStarted(true);
        l.setTurnOrder(DiceHelper.getRandomTurnOrder(l.getPlayers()));

        List<Player> players = l.getPlayers();
        int playerIndex = 0;

        for (int i = 0; i < ArmyCountHelper.getStartCount(players.size()) * players.size(); i++) {
            int territoryIndex = i % 42;
            if (l.getTerritoryByID(territoryIndex).getOccupierPlayerID() == -1) {
                l.getTerritoryByID(territoryIndex).setOccupierPlayerID(players.get(playerIndex).getUid());
            }
            l.getTerritoryByID(territoryIndex).addToArmyCount(1);
            playerIndex = ++playerIndex % players.size();
        }

        return l.getTurnOrder();
    }

    /**
     * Creates a lobby (host = clients[0]) and lets all clients join.
     * @param clients Clients to setup lobby for
     * @return lobby ID
     */
    public int setupLobby(List<NetworkClientKryo> clients) {
        if (!this.server.isRunning()) {
            throw new IllegalStateException("must start server before setting up lobby");
        }
        if (clients.size() > 6) {
            throw new IllegalArgumentException("too many clients");
        }

        List<Player> players = ((DataStoreTestable)ds).getPlayers();
        Lobby l = ds.createLobby(players.get(0));
        for (int i=1; i<players.size(); i++) {
            l.join(players.get(i));
        }
        return l.getLobbyID();
    }

    /**
     * Connects all given clients to the server.
     * @param clients Clients to connect to server.
     * @param timeoutMS Maximum waiting time for for finish in MS.
     * @return Map of Client --> Player
     * @throws IOException If connection fails.
     * @throws TimeoutException If no finish within timeout.
     */
    public Map<NetworkClientKryo, Player> connect(List<NetworkClientKryo> clients, int timeoutMS) throws IOException, TimeoutException {
        if (!this.server.isRunning()) {
            throw new IllegalStateException("must start server before connecting clients");
        }

        Map<NetworkClientKryo, Player> players = new HashMap<>();

        for (NetworkClientKryo c : clients) {
            c.registerCallback(argument -> {
                if (argument instanceof ConnectedMessage) {
                    players.put(c, ((ConnectedMessage) argument).getPlayer());
                }
            });
            c.connect("localhost");
        }
        // wait for all clients to connect
        wait(() -> players.size() < clients.size(), timeoutMS);
        return players;
    }

    /**
     * Sets up an attack in occupy required phase.
     * Requires started game.
     * @param lobbyID Concerned Lobby
     * @param fromTerritory Attacker territory
     * @param toTerritory Defender Territory.
     */
    public void setupPreOccupy(int lobbyID, int fromTerritory, int toTerritory) {
        Lobby l = ds.getLobbyByID(lobbyID);
        l.setCurrentAttack(new Attack(fromTerritory, toTerritory, 2));
        l.getCurrentAttack().setOccupyRequired(true);
        l.getTerritoryByID(toTerritory).setArmyCount(0);
        ds.updateLobby(l);
    }

    public DataStoreTestable getDataStore() {
        return (DataStoreTestable) ds;
    }

    private void wait(WaitingCondition condition, int timeoutMS) throws TimeoutException {
        long startTime = System.currentTimeMillis();
        while (!condition.isDone()) {
            if ((System.currentTimeMillis() - startTime) > timeoutMS) {
                throw new TimeoutException();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {/*goto loop header*/}
        }
    }

    private interface WaitingCondition {
        boolean isDone();
    }
}
