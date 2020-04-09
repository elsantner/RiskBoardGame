package edu.aau.se2;

import com.badlogic.gdx.Game;

import edu.aau.se2.view.game.GameScreen;

public class RiskGame extends Game {
	private GameScreen gameScreen;
	
	@Override
	public void create () {
		gameScreen = new GameScreen();
		setScreen(gameScreen);
	}

	@Override
	public void dispose () {
		super.dispose();
	}
}
