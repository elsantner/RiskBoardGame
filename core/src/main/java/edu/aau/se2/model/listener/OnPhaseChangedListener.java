package edu.aau.se2.model.listener;

import edu.aau.se2.model.Database;

public interface OnPhaseChangedListener {
    void phaseChanged(Database.Phase newPhase);
}
