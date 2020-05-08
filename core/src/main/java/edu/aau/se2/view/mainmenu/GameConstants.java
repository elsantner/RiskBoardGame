package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;

public abstract class GameConstants {
    public static final int SCREEN_WIDTH = Gdx.graphics.getWidth();
    public static final int SCREEN_HEIGHT = Gdx.graphics.getHeight();
    public static final int C_X = SCREEN_WIDTH /2;
    public static final int C_Y = SCREEN_HEIGHT /2;
    public static final int COL_WIDTH = SCREEN_WIDTH /8;
    public static final int ROW_HEIGHT = SCREEN_HEIGHT /8;

    private GameConstants() {
        // defeat instantiation
    }
}
