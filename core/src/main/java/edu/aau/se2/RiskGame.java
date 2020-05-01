package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.game.GameScreen;
import edu.aau.se2.view.lobby.LobbyScreen;
import edu.aau.se2.view.lobbylist.LobbyListScreen;
import edu.aau.se2.view.mainmenu.MainMenu;

public class RiskGame extends Game {
	private AssetManager assetManager;

	private GameScreen gameScreen;
	private LobbyScreen lobbyScreen;
	private LobbyListScreen lobbyListScreen;
	private MainMenu mainMenuScreen;

	@Override
	public void create () {
		setupAssetManager();
        Database db = Database.getInstance();

		db.setGameStartListener((players, initialArmyCount) -> Gdx.app.postRunnable(() -> {
            gameScreen = new GameScreen(assetManager);
            setScreen(gameScreen);
        }));
		db.setLeftLobbyListener(() -> Gdx.app.postRunnable(() -> {
			mainMenuScreen = new MainMenu(this);
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
		try {
			db.connectIfNotConnected();
		} catch (IOException e) {
			Logger.getLogger("RiskGame").log(Level.SEVERE, "Connection Error: ", e);
			System.exit(-1);
		}
	}

	private void setupAssetManager() {
		assetManager = new AssetManager();

		FileHandleResolver resolver = new InternalFileHandleResolver();
		assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		FreetypeFontLoader.FreeTypeFontLoaderParameter parameter = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		parameter.fontFileName = AssetName.FONT_1;
		parameter.fontParameters.size = 70;
		parameter.fontParameters.borderColor = Color.BLACK;
        parameter.fontParameters.borderWidth = 2;
		assetManager.load(AssetName.FONT_1, BitmapFont.class, parameter);

		assetManager.load(AssetName.PHASE_DISPLAY_BG, Texture.class);
		assetManager.load(AssetName.UI_SKIN, Skin.class);
	}

	@Override
	public void render() {
		super.render();
		// load assets
		assetManager.update();
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
