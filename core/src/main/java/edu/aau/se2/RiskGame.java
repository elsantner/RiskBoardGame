package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import java.io.IOException;
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
		db.setLeftLobbyListener(() -> {
			// TODO: Go to Main Menu when implemented
			System.exit(1);
		});
		db.setJoinedLobbyListener((lobbyID, host, players) -> Gdx.app.postRunnable(() -> {
			lobbyScreen = new LobbyScreen();
			setScreen(lobbyScreen);
		}));
		db.setLobbyListChangedListener(lobbyList -> Gdx.app.postRunnable(() -> {
			lobbyListScreen = new LobbyListScreen(lobbyList);
			setScreen(lobbyListScreen);
		}));

		db.setConnectionChangedListener(new OnConnectionChangedListener() {
			@Override
			public void connected(Player thisPlayer) {
				// TODO: Change whether you want the host or joiner varient (until main menu is here...)
				db.hostLobby();
				//db.triggerLobbyListUpdate();
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
