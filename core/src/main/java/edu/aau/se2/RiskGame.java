package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.Arrays;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.game.GameScreen;
import edu.aau.se2.view.lobby.LobbyScreen;

public class RiskGame extends Game {
	private GameScreen gameScreen;
  	private LobbyScreen lobbyScreen;

	@Override
	public void create () {
        Database db = Database.getInstance();
		db.setGameStartListener((players, initialArmyCount) -> Gdx.app.postRunnable(() -> {
            gameScreen = new GameScreen();
            setScreen(gameScreen);
        }));
		// TODO: Replace once Main Menu is done
		lobbyScreen = new LobbyScreen();
		lobbyScreen.users = new ArrayList<>(Arrays.asList(new Player(0, "Player 1"), new Player(1, "Player 2")));
		setScreen(lobbyScreen);

		db.setConnectionChangedListener(new OnConnectionChangedListener() {
			@Override
			public void connected(Player thisPlayer) {
				db.setPlayerReady(true);
			}

			@Override
			public void disconnected() {

			}
		});
	}

	@Override
	public void dispose () {
		super.dispose();
		gameScreen.dispose();
		lobbyScreen.dispose();
	}
}
