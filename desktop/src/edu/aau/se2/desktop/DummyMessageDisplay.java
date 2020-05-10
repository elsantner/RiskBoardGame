package edu.aau.se2.desktop;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.view.PopupMessageDisplay;

public class DummyMessageDisplay implements PopupMessageDisplay {
    @Override
    public void showMessage(String message) {
        Logger.getAnonymousLogger().log(Level.INFO, message);
    }
}
