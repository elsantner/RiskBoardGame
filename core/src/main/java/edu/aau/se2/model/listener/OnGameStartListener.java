package edu.aau.se2.model.listener;

import java.util.ArrayList;
import edu.aau.se2.server.data.Player;

public interface OnGameStartListener {
    void onGameStarted(ArrayList<Player> players, int initialArmyCount);
}
