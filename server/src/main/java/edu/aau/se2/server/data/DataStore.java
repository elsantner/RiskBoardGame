package edu.aau.se2.server.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class DataStore {
    private static DataStore instance;

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    protected TreeMap<Integer, Lobby> lobbies;
    protected TreeMap<Integer, Player> playersOnline;
    private int nextLobbyID;
    private int nextPlayerID;
    private PlayerLostConnectionListener lostConnectionListener;
    private PlayerDeviceNameListener deviceNameListener;

    protected DataStore() {
        lobbies = new TreeMap<>();
        playersOnline = new TreeMap<>();
        nextLobbyID = 0;
        nextPlayerID = 1;
    }

    public synchronized boolean isPlayerInAnyLobby(int playerID) {
        for (Lobby l: lobbies.values()) {
            for (Player p: l.getPlayers()) {
                if (p.getUid() == playerID) {
                    return true;
                }
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

    public synchronized Lobby removeLobby(int lobbyID) {
        return lobbies.remove(lobbyID);
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

    public synchronized List<Lobby> getJoinableLobbyList() {
        List<Lobby> joinableLobbies = new ArrayList<>();
        for (Lobby l: lobbies.values()) {
            if (l.isJoinable()) {
                joinableLobbies.add(l);
            }
        }
        return joinableLobbies;
    }

    public synchronized void removePlayer(Integer disconnectedPlayerID) {
        if (disconnectedPlayerID != null) {
            Player p = playersOnline.remove(disconnectedPlayerID);
            Lobby curLobby;
            Lobby playersLobby = null;
            Iterator<Lobby> iterator = lobbies.values().iterator();
            while (iterator.hasNext() && playersLobby == null) {
                curLobby = iterator.next();
                if (curLobby.isPlayerJoined(disconnectedPlayerID)) {
                    playersLobby = curLobby;
                }
            }
            if (lostConnectionListener != null) {
                lostConnectionListener.playerLostConnection(p, playersLobby);
            }
        }
    }

    public void setLostConnectionListener(PlayerLostConnectionListener l) {
        this.lostConnectionListener = l;
    }

    public void setDeviceNameListener(PlayerDeviceNameListener listener) {
        this.deviceNameListener = listener;
    }

    public void setPlayerName(int playerID, String newName) {
        String nickname = playersOnline.get(playerID).getNickname();
        if(nickname != newName){
            playersOnline.get(playerID).setNickname(newName);
        }
    }

}
