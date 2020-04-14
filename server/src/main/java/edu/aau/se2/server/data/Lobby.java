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

    public Lobby(int lobbyID) {
        this.lobbyID = lobbyID;
        this.players = new TreeMap<>();
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

    // TODO: only return true if all players are ready
    public boolean canStartGame() {
        // Just for Testing
        return players.size() > 1;
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
}
