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
import edu.aau.se2.view.mainmenu.MainMenu;

public class RiskGame extends Game {
	private GameScreen gameScreen;
	private LobbyScreen lobbyScreen;
	private LobbyListScreen lobbyListScreen;
	private edu.aau.se2.view.mainmenu.MainMenu mainMenuScreen;

	@Override
	public void create () {

        Database db = Database.getInstance();

		gameScreen = new GameScreen();
		setScreen(gameScreen);
		/*db.setGameStartListener((players, initialArmyCount) -> Gdx.app.postRunnable(() -> {
            gameScreen = new GameScreen();
            setScreen(gameScreen);
        }));
		db.setLeftLobbyListener(() -> Gdx.app.postRunnable(() -> {
			mainMenuScreen = new edu.aau.se2.view.mainmenu.MainMenu(this);
			setScreen(mainMenuScreen);
		}));
		db.setJoinedLobbyListener((lobbyID, host, players) -> Gdx.app.postRunnable(() -> {
			lobbyScreen = new LobbyScreen();
			setScreen(lobbyScreen);
		}));
		db.setLobbyListChangedListener(lobbyList -> Gdx.app.postRunnable(() -> {
			lobbyListScreen = new LobbyListScreen(lobbyList);
			setScreen(lobbyListScreen);
		}));

		mainMenuScreen = new MainMenu(this);
		setScreen(mainMenuScreen);

		db.setConnectionChangedListener(new OnConnectionChangedListener() {
			@Override
			public void connected(Player thisPlayer) {
			}

			@Override
			public void disconnected() {
				Logger.getLogger("RiskGame").log(Level.SEVERE, "Connection lost");
				System.exit(-1);
			}
		});
		*/
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
		try {
			gameScreen.dispose();
			lobbyScreen.dispose();
		}
		catch (Exception ex) {
			Logger.getLogger("RiskGame").log(Level.WARNING, "Error: ", ex);
		}
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
