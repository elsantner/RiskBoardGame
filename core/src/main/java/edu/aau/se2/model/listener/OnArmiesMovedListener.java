package edu.aau.se2.model.listener;

public interface OnArmiesMovedListener {
    void armiesMoved(int playerID, int fromTerritoryID, int toTerritoryID, int count);
}
