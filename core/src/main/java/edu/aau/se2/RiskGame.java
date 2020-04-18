package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.game.GameScreen;
import edu.aau.se2.view.lobby.LobbyScreen;
import edu.aau.se2.view.lobbylist.LobbyListScreen;

public class RiskGame extends Game {
	private GameScreen gameScreen;
	private LobbyScreen lobbyScreen;
	private LobbyListScreen lobbyListScreen;

	@Override
	public void create () {
        Database db = Database.getInstance();
		db.setGameStartListener((players, initialArmyCount) -> Gdx.app.postRunnable(() -> {
            gameScreen = new GameScreen();
            setScreen(gameScreen);
        }));
		/*// TODO: Replace once Main Menu is done
		lobbyScreen = new LobbyScreen(this);
		lobbyScreen.setPlayers(new ArrayList<>(Arrays.asList(new Player(0, "Player 1"), new Player(1, "Player 2"))));
		setScreen(lobbyScreen);*/
		lobbyListScreen = new LobbyListScreen(this);
		setScreen(lobbyListScreen);

		db.setConnectionChangedListener(new OnConnectionChangedListener() {
			@Override
			public void connected(Player thisPlayer) {
				db.setPlayerReady(true);
			}

			@Override
			public void disconnected() {
				Logger.getLogger("RiskGame").log(Level.SEVERE, "Connection lost");
				System.exit(-1);
			}
		});

		try {
			db.connectIfNotConnected();
		} catch (IOException e) {
			Logger.getLogger("RiskGame").log(Level.SEVERE, "Connection Error: ", e);
			System.exit(-1);
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		gameScreen.dispose();
		lobbyScreen.dispose();
	}
    @Override
    public void setScreen(Screen screen) {
        // making sure no memory leaks are happening
        Screen oldScreen = getScreen();
        if (oldScreen != null)
            oldScreen.dispose();
        super.setScreen(screen);
    }
}
