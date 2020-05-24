package edu.aau.se2.server.logic;

import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.server.data.DataStore;
import edu.aau.se2.server.data.Lobby;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.server.networking.dto.InLobbyMessage;
import edu.aau.se2.server.networking.dto.game.OccupyTerritoryMessage;
import edu.aau.se2.server.networking.dto.game.PlayerLostMessage;

public abstract class VictoryHelper {
    private VictoryHelper() {
        // no instance required
    }

    private static DataStore ds = DataStore.getInstance();


    public static InLobbyMessage handleTerritoryOccupation(OccupyTerritoryMessage msg) {
        Lobby l = ds.getLobbyByID(msg.getLobbyID());

        // one player has every occupied territory => Victory
        if (l.getNumberOfTerritories() - l.getTerritoriesOccupiedByPlayer(msg.getFromPlayerID()).length - l.getUnoccupiedTerritories().length == 0) {
            return handlePlayerVictory();
        }

        // check if player has 0 Territories left => Lose
        ArrayList<Player> players = new ArrayList<>(l.getPlayers());
        for (int i = 0; i < players.size(); i++) {
            if (l.getTerritoriesOccupiedByPlayer(players.get(i).getUid()).length == 0)
                return handlePlayerLost(l, players.get(i).getUid());
        }

        return null;
    }

    private static InLobbyMessage handlePlayerVictory() {
        //todo handle player victory
        return null;
    }

    private static InLobbyMessage handlePlayerLost(Lobby l, int uid) {

        //todo make turnOrder removal a global method
        //todo make sure turnOrder cant choose same player 2 times in row..
        List<Integer> turnOrder = l.getTurnOrder();
        for (int i = 0; i < turnOrder.size(); i++) {
            if (turnOrder.get(i) == uid) {
                turnOrder.remove(i);
                break;
            }
        }
        l.setTurnOrder(turnOrder);
        ds.updateLobby(l);
        return new PlayerLostMessage(l.getLobbyID(), uid);
    }
}
