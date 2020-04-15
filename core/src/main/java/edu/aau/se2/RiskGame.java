package edu.aau.se2;

import com.badlogic.gdx.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnGameStartListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.game.GameScreen;

public class RiskGame extends Game {
	private GameScreen gameScreen;
	private Database db;
	
	@Override
	public void create () {
		db = Database.getInstance();
		db.setOnGameStartListener(new OnGameStartListener() {
			@Override
			public void onGameStarted(ArrayList<Player> players, int initialArmyCount) {
				System.out.println("Starting game with " + initialArmyCount + " armies and " + players.toString());
				gameScreen = new GameScreen();
				setScreen(gameScreen);
			}
		});
		db.setPlayerReady(true);
	}

	@Override
	public void dispose () {
		super.dispose();
		gameScreen.dispose();
	}
}
