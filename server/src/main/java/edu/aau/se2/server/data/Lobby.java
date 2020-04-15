package edu.aau.se2.server.data;

import java.util.ArrayList;
import java.util.TreeMap;

import edu.aau.se2.server.logic.ArmyCountHelper;

/**
 * @author Elias
 */
public class Lobby {
    private int lobbyID;
    private TreeMap<Integer, Player> players;
    private boolean isStarted;

    public Lobby(int lobbyID) {
        this.lobbyID = lobbyID;
        this.players = new TreeMap<>();
        this.isStarted = false;
    }

    public ArrayList<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public void addPlayer(Player p) {
        this.players.put(p.getUid(), p);
    }

    public boolean removePlayer(int uid) {
        return this.players.remove(uid) == null;
    }

    public int getLobbyID() {
        return lobbyID;
    }

    public boolean canStartGame() {
        if (players.size() < 2) {
            return false;
        }
        for (Player p: players.values()) {
            if (!p.isReady()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Give all players a unique color
     */
    public void setupForGameStart() {
        int curColorID = 0;
        for (Player p: players.values()) {
            p.setColorID(curColorID++);
            p.setArmyReserveCount(ArmyCountHelper.getStartCount(players.size()));
        }
    }

    public void setPlayerReady(int playerID, boolean ready) {
        Player p = players.get(playerID);
        if (p == null) {
            throw new IllegalArgumentException("player with id " + playerID + " doesn't exist");
        }
        p.setReady(ready);
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
