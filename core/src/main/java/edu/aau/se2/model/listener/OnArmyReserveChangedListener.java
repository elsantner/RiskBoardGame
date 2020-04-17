package edu.aau.se2.model.listener;

public interface OnArmyReserveChangedListener {
    /**
     * Called when this players army reserve count changes
     * @param armyCount New army reserve count
     * @param isInitialCount True if the method is called as a consequence of receiving new
     *                       armies at the start of a turn.
     */
    void newArmyCount(int armyCount, boolean isInitialCount);
}
