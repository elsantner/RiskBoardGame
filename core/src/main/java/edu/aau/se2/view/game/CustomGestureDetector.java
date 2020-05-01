package edu.aau.se2.view.game;

import com.badlogic.gdx.input.GestureDetector;

public class CustomGestureDetector extends GestureDetector {
    public CustomGestureDetector(GestureListener listener) {
        super(listener);
        // must be set for double tap to work reliably
        this.setTapCountInterval(1000);
        this.setTapSquareSize(50);
    }
}
