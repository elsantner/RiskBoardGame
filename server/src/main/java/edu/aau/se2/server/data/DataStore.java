package edu.aau.se2.server.data;

import java.util.TreeMap;

public class DataStore {
    private static DataStore instance;

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private TreeMap<Integer, Lobby> lobbies;
    private TreeMap<Integer, Player> playersOnline;
    private int nextLobbyID;
    private int nextPlayerID;

    protected DataStore() {
        lobbies = new TreeMap<>();
        playersOnline = new TreeMap<>();
        nextLobbyID = 0;
        nextPlayerID = 1;
    }

    public synchronized boolean isPlayerHostingLobby(int playerID) {
        for (Lobby l: lobbies.values()) {
            if (l.getHost() != null && l.getHost().getUid() == playerID) {
                return true;
            }
        }
        return false;
    }

    public synchronized Lobby createLobby(Player host) {
        Lobby l = new Lobby(getNextLobbyID());
        l.setHost(host);
        lobbies.put(l.getLobbyID(), l);
        return l;
    }

    public synchronized Lobby createLobby() {
        Lobby l = new Lobby(getNextLobbyID());
        lobbies.put(l.getLobbyID(), l);
        return l;
    }

    public synchronized void updateLobby(Lobby l) {
        if (!lobbies.containsKey(l.getLobbyID())) {
            throw new IllegalArgumentException("lobby cannot be updated because it does not exist");
        }
        lobbies.put(l.getLobbyID(), l);
    }

    private synchronized int getNextLobbyID() {
        return nextLobbyID++;
    }

    public synchronized Player newPlayer() {
        int playerID = getNextPlayerID();
        Player p = new Player(playerID, "Player" + playerID);
        playersOnline.put(p.getUid(), p);
        return p;
    }

    public synchronized Player getPlayerByID(int playerID) {
        return playersOnline.get(playerID);
    }

    private synchronized int getNextPlayerID() {
        return nextPlayerID++;
    }

    /**
     * Gets the lobby with the provided lobbyID
     * @param lobbyID ID of lobby to get
     * @return Lobby if it exists, otherwise null.
     */
    public Lobby getLobbyByID(int lobbyID) {
        return lobbies.get(lobbyID);
    }

    public synchronized void removePlayer(Integer disconnectedPlayerID) {
        if (disconnectedPlayerID != null) {
            playersOnline.remove(disconnectedPlayerID);
            for (Lobby l: lobbies.values()) {
                l.removePlayer(disconnectedPlayerID);
                updateLobby(l);
                // TODO: handle client messaging
            }
        }
    }
}
