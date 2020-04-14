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

    private DataStore() {
        lobbies = new TreeMap<>();
    }

    public synchronized Lobby createLobby() {
        Lobby l = new Lobby(getNewLobbyID());
        lobbies.put(l.getLobbyID(), l);
        return l;
    }

    private int getNewLobbyID() {
        if (lobbies.isEmpty()) {
            return 0;
        }
        return lobbies.lastKey();
    }

    /**
     * Gets the lobby with the provided lobbyID
     * @param lobbyID ID of lobby to get
     * @return Lobby if it exists, otherwise null.
     */
    public Lobby getLobbyByID(int lobbyID) {
        return lobbies.get(lobbyID);
    }
}
