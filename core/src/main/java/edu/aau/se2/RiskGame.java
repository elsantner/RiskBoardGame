package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.game.GameScreen;

public class RiskGame extends Game {
	private GameScreen gameScreen;

    @Override
	public void create () {
        Database db = Database.getInstance();
		db.setOnGameStartListener((players, initialArmyCount) -> Gdx.app.postRunnable(() -> {
            gameScreen = new GameScreen();
            setScreen(gameScreen);
        }));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt();
		}
		db.setPlayerReady(true);
	}

	@Override
	public void dispose () {
		super.dispose();
		gameScreen.dispose();
	}
}
