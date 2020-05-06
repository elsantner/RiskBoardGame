package edu.aau.se2.view;

import com.badlogic.gdx.Screen;

import edu.aau.se2.RiskGame;

public abstract class AbstractScreen implements Screen {
    private RiskGame game;

    public AbstractScreen(RiskGame game) {
        this.game = game;
    }

    public RiskGame getGame() {
        return game;
    }
}
