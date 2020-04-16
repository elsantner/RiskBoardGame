package edu.aau.se2.server.data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import edu.aau.se2.server.logic.ArmyCountHelper;

/**
 * @author Elias
 */
public class Lobby {
    private int lobbyID;
    private TreeMap<Integer, Player> players;
    private List<Integer> turnOrder;
    private int currentTurnIndex;
    private Territory[] territories;
    private boolean isStarted;
    private boolean areInitialArmiesPlaced;

    public Lobby(int lobbyID) {
        this.lobbyID = lobbyID;
        this.players = new TreeMap<>();
        this.isStarted = false;
        this.areInitialArmiesPlaced = false;
        initTerritories();
    }

    public List<Player> getPlayers() {
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

    public boolean areInitialArmiesPlaced() {
        return areInitialArmiesPlaced;
    }

    public void setInitialArmiesPlaced(boolean areInitialArmiesPlaced) {
        this.areInitialArmiesPlaced = areInitialArmiesPlaced;
    }

    public List<Integer> getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(List<Integer> turnOrder) {
        this.turnOrder = turnOrder;
        this.currentTurnIndex = 0;
    }

    public Player getCurrentPlayer() {
        return players.get(turnOrder.get(currentTurnIndex));
    }

    public void nextPlayerTurn() {
        this.currentTurnIndex++;
        this.currentTurnIndex %= getPlayers().size();
    }

    private void initTerritories() {
        this.territories = new Territory[42];
        for (int i=0; i<42; i++) {
            this.territories[i] = new Territory(i+1);
        }
    }

    public synchronized Territory getTerritoryByID(int territoryID) {
        try {
            return territories[territoryID - 1];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("no territory with id " + territoryID + " exists");
        }
    }

    public boolean allTerritoriesOccupied() {
        for (Territory t: territories) {
            if (t.isNotOccupied()) {
                return false;
            }
        }
        return true;
    }
}
