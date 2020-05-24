package edu.aau.se2.server.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import edu.aau.se2.server.logic.ArmyCountHelper;

public class Lobby {
    private static final Integer[] COLOR_IDS = {0, 1, 2, 3, 4, 5};

    private int lobbyID;
    private TreeMap<Integer, Player> players;
    private Player host;
    private List<Integer> turnOrder;
    private int currentTurnIndex;
    private Territory[] territories;
    private boolean isStarted;
    private boolean areInitialArmiesPlaced;
    private boolean hasCurrentPlayerToActReceivedNewArmies;
    private Attack currentAttack;
    private CardDeck cardDeck;

    public Lobby(int lobbyID) {
        this.lobbyID = lobbyID;
        this.players = new TreeMap<>();
        this.cardDeck = new CardDeck(lobbyID);
        this.isStarted = false;
        this.areInitialArmiesPlaced = false;
        this.currentAttack = null;
        initTerritories();
    }

    public Player getPlayerByID(int id) {
        return players.get(id);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public void addPlayer(Player p) {
        this.players.put(p.getUid(), p);
    }

    public boolean removePlayer(int uid) {
        return this.players.remove(uid) != null;
    }

    public int getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(int lobbyID) {
        this.lobbyID = lobbyID;
    }

    public Player getHost() {
        return host;
    }

    public void setHost(Player host) {
        this.host = host;
        players.put(host.getUid(), host);
    }

    public boolean canStartGame() {
        if (players.size() < 2) {
            return false;
        }
        for (Player p : players.values()) {
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
        int curColorIDIndex = 0;
        List<Integer> randomColorPermutation = Arrays.asList(COLOR_IDS);
        Collections.shuffle(randomColorPermutation);
        for (Player p : players.values()) {
            p.setColorID(randomColorPermutation.get(curColorIDIndex++));
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
        // check if all players have 0 armies remaining and initial armies have not yet been placed
        if (!areInitialArmiesPlaced) {
            int sumRemainingArmies = 0;
            for (Player p : players.values()) {
                sumRemainingArmies += p.getArmyReserveCount();
            }
            if (sumRemainingArmies == 0) {
                areInitialArmiesPlaced = true;
            }
        }
        return areInitialArmiesPlaced;
    }

    public void setInitialArmiesPlaced() {
        this.areInitialArmiesPlaced = true;
    }

    public List<Integer> getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(List<Integer> turnOrder) {
        this.turnOrder = turnOrder;
        this.currentTurnIndex = 0;
    }

    public Player getPlayerToAct() {
        return players.get(turnOrder.get(currentTurnIndex));
    }

    public Player getDefender() {
        if (!attackRunning()) return null;

        return players.get(getTerritoryByID(currentAttack.getToTerritoryID()).getOccupierPlayerID());
    }

    public void nextPlayersTurn() {
        this.currentTurnIndex++;
        this.currentTurnIndex %= turnOrder.size();
        hasCurrentPlayerToActReceivedNewArmies = false;
    }

    private void initTerritories() {
        this.territories = new Territory[42];
        for (int i = 0; i < 42; i++) {
            this.territories[i] = new Territory(i);
        }
    }

    public synchronized Territory getTerritoryByID(int territoryID) {
        try {
            return territories[territoryID];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("no territory with id " + territoryID + " exists");
        }
    }

    public boolean allTerritoriesOccupied() {
        for (Territory t : territories) {
            if (t.isNotOccupied()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate and set the number of new armies based on occupied territories
     *
     * @param playerID Player to give armies to.
     */
    public void giveNewArmiesToPlayer(int playerID) {
        Player p = players.get(playerID);
        p.setArmyReserveCount(ArmyCountHelper.getNewArmyCount(territories, playerID));
        hasCurrentPlayerToActReceivedNewArmies = true;
    }

    public void setCurrentPlayerToActReceivedNewArmies(boolean armiesReceived) {
        this.hasCurrentPlayerToActReceivedNewArmies = armiesReceived;
    }

    public boolean hasCurrentPlayerToActReceivedNewArmies() {
        return hasCurrentPlayerToActReceivedNewArmies;
    }

    public boolean hasCurrentPlayerToActPlacedNewArmies() {
        return hasCurrentPlayerToActReceivedNewArmies &&
                this.getPlayerToAct().getArmyReserveCount() == 0;
    }

    public boolean isJoinable() {
        return !isStarted && players.size() < 6;
    }

    public void join(Player p) {
        if (p == null) {
            throw new NullPointerException("player must not be null");
        }
        if (players.containsKey(p.getUid())) {
            throw new IllegalStateException("player already joined");
        }
        if (!isJoinable()) {
            throw new IllegalStateException("lobby not joinable");
        }
        players.put(p.getUid(), p);
    }

    public void leave(Player p) {
        Territory[] territories = getTerritoriesOccupiedByPlayer(p.getUid());
        for (int i = 0; i < territories.length; i++) {
            territories[i] = new Territory(territories[i].getId());
        }
        players.remove(p.getUid());
    }

    public boolean isPlayerJoined(int playerID) {
        return players.containsKey(playerID);
    }

    public void resetPlayers() {
        for (Player p : players.values()) {
            p.reset();
        }
    }

    public void clearPlayers() {
        players.clear();
    }

    public boolean attackRunning() {
        return currentAttack != null;
    }

    public Attack getCurrentAttack() {
        return currentAttack;
    }

    public void setCurrentAttack(Attack currentAttack) {
        this.currentAttack = currentAttack;
    }

    public Territory[] getTerritoriesOccupiedByPlayer(int playerID) {
        ArrayList<Territory> terr = new ArrayList<>();

        for (Territory t : this.territories) {
            if (t.getOccupierPlayerID() == playerID) terr.add(t);
        }
        return terr.toArray(new Territory[0]);
    }

    public CardDeck getCardDeck() {
        return cardDeck;
    }

    public boolean isPlayersTurn(int playerID) {
        return getPlayerToAct() != null && getPlayerToAct().getUid() == playerID;
    }

    public boolean isPlayersTerritory(int playerID, int territoryID) {
        return territories[territoryID].getOccupierPlayerID() == playerID;
    }

    public Player getPlayerByTerritoryID(int territoryID) {
        return players.get(territories[territoryID].getOccupierPlayerID());
    }

    public Territory[] getUnoccupiedTerritories() {
        List<Territory> unoccupiedTerritories = new ArrayList<>();
        for (Territory territoryDatum : this.territories) {
            if (territoryDatum.getOccupierPlayerID() == -1) {
                unoccupiedTerritories.add(territoryDatum);
            }
        }
        return unoccupiedTerritories.toArray(new Territory[0]);
    }

    public void updatePlayer(Player p) {
        if (!players.containsKey(p.getUid())) {
            throw new IllegalArgumentException("player not found");
        }
        players.put(p.getUid(), p);
    }

    public int getNumberOfTerritories() {
        return this.territories.length;
    }

    public List<Integer> clearTerritoriesOfPlayer(int playerID) {
        ArrayList<Integer> territoryIDs = new ArrayList<>();
        for (Territory t : getTerritoriesOccupiedByPlayer(playerID)) {
            t.setOccupierPlayerID(-1);
            t.setArmyCount(0);
            territoryIDs.add(t.getId());
        }
        return territoryIDs;
    }
}
