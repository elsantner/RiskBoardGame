package edu.aau.se2.server.networking.dto.game;

import java.util.List;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class DiceResultMessage extends InLobbyMessage {

    private List<Integer> results;
    private boolean cheated;

    public DiceResultMessage(int lobbyID, int fromPlayerID, List<Integer> results) {
        super(lobbyID, fromPlayerID);
        this.results = results;
    }

    public DiceResultMessage(int lobbyID, int fromPlayerID, List<Integer> results, boolean cheated) {
        super(lobbyID, fromPlayerID);
        this.results = results;
        this.cheated = cheated;
    }

    public DiceResultMessage(DiceResultMessage other) {
        super(other.getLobbyID(), other.getFromPlayerID());
        this.results = other.getResults();
        this.cheated = false;
    }

    public List<Integer> getResults() {
        return results;
    }

    public boolean isCheated() {
        return cheated;
    }
}
