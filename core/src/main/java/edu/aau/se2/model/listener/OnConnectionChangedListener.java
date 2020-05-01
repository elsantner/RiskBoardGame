package edu.aau.se2.model.listener;

import edu.aau.se2.server.data.Player;

public interface OnConnectionChangedListener {
    void connected(Player thisPlayer);
    void disconnected();
}
