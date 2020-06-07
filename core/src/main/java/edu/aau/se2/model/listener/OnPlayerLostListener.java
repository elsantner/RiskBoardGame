package edu.aau.se2.model.listener;

public interface OnPlayerLostListener {
    void informPlayersThatPlayerLost(String playerName, boolean thisPlayerLost);
}
