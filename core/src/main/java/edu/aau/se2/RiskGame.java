package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import edu.aau.se2.view.game.GameScreen;


public class RiskGame extends Game {
	public static final int V_WIDTH = 500;
	public static final int V_HEIGHT = 250;
	//there should be ONLY ONE SpriteBatch, since memory intense
	public SpriteBatch batch;

	@Override
	public void create () {
		batch = new SpriteBatch();
		setScreen(new GameScreen(this));
	}

	@Override
	public void dispose () {
		super.dispose();
	}
}
