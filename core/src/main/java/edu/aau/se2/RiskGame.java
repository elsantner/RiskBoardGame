package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
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
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						System.out.println("Starting game with " + initialArmyCount + " armies and " + players.toString());
						gameScreen = new GameScreen();
						setScreen(gameScreen);
					}
				});
			}
		});
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		db.setPlayerReady(true);
	}

	@Override
	public void dispose () {
		super.dispose();
		gameScreen.dispose();
	}
}
