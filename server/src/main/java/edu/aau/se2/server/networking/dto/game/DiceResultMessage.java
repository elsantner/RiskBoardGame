package edu.aau.se2.server.networking.dto.game;

import java.util.List;

import edu.aau.se2.server.networking.dto.InLobbyMessage;

public class DiceResultMessage extends InLobbyMessage {

    private List<Integer> results;
    private boolean cheated;
    private boolean attacker;


    public DiceResultMessage() {
    }

    public DiceResultMessage(int lobbyID, int fromPlayerID, List<Integer> results, boolean attacker) {
        super(lobbyID, fromPlayerID);
        this.results = results;
        this.attacker = attacker;
        this.cheated = false;
    }

    public DiceResultMessage(int lobbyID, int fromPlayerID, List<Integer> results, boolean cheated, boolean attacker) {
        super(lobbyID, fromPlayerID);
        this.results = results;
        this.cheated = cheated;
        this.attacker = attacker;
    }

    public DiceResultMessage(DiceResultMessage other) {
        super(other.getLobbyID(), other.getFromPlayerID());
        this.results = other.getResults();
        this.attacker = other.isFromAttacker();
        this.cheated = false;
    }

    public List<Integer> getResults() {
        return results;
    }

    public boolean isCheated() {
        return cheated;
    }

    public boolean isFromAttacker() {
        return attacker;
    }
}
