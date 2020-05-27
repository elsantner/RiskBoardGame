package edu.aau.se2.server.logic;

import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.InLobbyMessage;
import edu.aau.se2.server.networking.dto.game.OccupyTerritoryMessage;
import edu.aau.se2.server.networking.dto.game.PlayerLostMessage;
import edu.aau.se2.server.networking.dto.game.VictoryMessage;

public abstract class VictoryHelper {
    private VictoryHelper() {
        // no instance required
    }

    private static DataStore ds = DataStore.getInstance();


    public static InLobbyMessage handleTerritoryOccupation(OccupyTerritoryMessage msg) {
        Lobby l = ds.getLobbyByID(msg.getLobbyID());
        if (l == null) return null;

        // one player has every occupied territory => Victory
        if (l.getNumberOfTerritories() - l.getTerritoriesOccupiedByPlayer(msg.getFromPlayerID()).length - l.getUnoccupiedTerritories().length == 0) {
            return handlePlayerVictory(l, msg.getFromPlayerID());
        }

        // check if player has 0 Territories left => Lose
        ArrayList<Player> players = new ArrayList<>(l.getPlayers());
        for (int i = 0; i < players.size(); i++) {
            if (l.getTerritoriesOccupiedByPlayer(l.getTurnOrder().get(i)).length == 0)
                return handlePlayerLost(l, l.getTurnOrder().get(i));
        }

        return null;
    }

    private static InLobbyMessage handlePlayerVictory(Lobby l, int uid) {
        for (int i = 0; i < l.getTurnOrder().size(); i++) {
            if (l.getTurnOrder().get(i) != uid) {
                l.getPlayerByID(l.getTurnOrder().get(i)).setHasLost(true);
            }
        }
        return new VictoryMessage(l.getLobbyID(), uid);
    }

    private static InLobbyMessage handlePlayerLost(Lobby l, int uid) {
        removePlayerFromTurnOrder(l, uid);
        l.getPlayerByID(uid).setHasLost(true);
        ds.updateLobby(l);
        return new PlayerLostMessage(l.getLobbyID(), uid);
    }

    public static void removePlayerFromTurnOrder(Lobby l, int uid) {
        List<Integer> turnOrder = l.getTurnOrder();
        for (int i = 0; i < turnOrder.size(); i++) {
            if (turnOrder.get(i) == uid) {
                turnOrder.remove(i);
                break;
            }
        }
        l.setTurnOrder(turnOrder);
    }
}
