package edu.aau.se2.view.game;

import com.badlogic.gdx.graphics.Color;

public interface IGameBoard {
    void setInteractable(boolean interactable);
    boolean isInteractable();
    void setArmiesPlacable(boolean armiesPlacable);
    boolean isArmiesPlacable();
    void setAttackAllowed(boolean attackAllowed);
    boolean isAttackAllowed();
    void setArmyCount(int territoryID, int count);
    void setArmyColor(int territoryID, Color color);
}
