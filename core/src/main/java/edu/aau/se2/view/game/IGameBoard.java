package edu.aau.se2.view.game;

public interface IGameBoard {
    void setInteractable(boolean interactable);
    boolean isInteractable();
    void setArmyCount(int territoryID, int count);
    void setArmyColor(int territoryID, int colorID);
}
