package edu.aau.se2.view.game;

public interface OnBoardInteractionListener {
    void armyPlaced(int territoryID, int count);
    void armyMoved(int fromTerritoryID, int toTerritoryID, int count);
    void attackStarted(int fromTerritoryID, int onTerritoryID, int count);
}
