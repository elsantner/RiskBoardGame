package edu.aau.se2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
import edu.aau.se2.view.loading.LoadingScreen;
import edu.aau.se2.view.lobby.LobbyScreen;
import edu.aau.se2.view.lobbylist.LobbyListScreen;
import edu.aau.se2.view.mainmenu.MainMenu;

public class RiskGame extends Game {
	private AssetManager assetManager;
	private boolean isDoneLoadingAssets = false;

	private GameScreen gameScreen;
	private LobbyScreen lobbyScreen;
	private LobbyListScreen lobbyListScreen;
	private MainMenu mainMenuScreen;
	private LoadingScreen loadingScreen;

	@Override
	public void create () {
		this.assetManager = new AssetManager();
		setupAssetManagerLoadingScreen();
		assetManager.finishLoading();

		setupAssetManagerAllAssets();
		loadingScreen = new LoadingScreen(this, assetManager.getQueuedAssets());
		setScreen(loadingScreen);

        Database db = Database.getInstance();

		db.setGameStartListener((players, initialArmyCount) -> Gdx.app.postRunnable(() -> {
            gameScreen = new GameScreen(this);
            setScreen(gameScreen);
        }));
		db.setLeftLobbyListener(() -> Gdx.app.postRunnable(() -> {
			mainMenuScreen = new MainMenu(this);
			setScreen(mainMenuScreen);
		}));
		db.setJoinedLobbyListener((lobbyID, host, players) -> Gdx.app.postRunnable(() -> {
			lobbyScreen = new LobbyScreen(this);
			setScreen(lobbyScreen);
		}));
		db.setLobbyListChangedListener(lobbyList -> Gdx.app.postRunnable(() -> {
			lobbyListScreen = new LobbyListScreen(this, lobbyList);
			setScreen(lobbyListScreen);
		}));

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

	private void setupAssetManagerLoadingScreen() {
		assetManager.load(AssetName.TEX_LOGO, Texture.class);
		assetManager.load(AssetName.UI_SKIN_4, Skin.class);
	}

	private void setupAssetManagerAllAssets() {
		FileHandleResolver resolver = new InternalFileHandleResolver();
		assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		FreetypeFontLoader.FreeTypeFontLoaderParameter parameterFont1 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		parameterFont1.fontFileName = "font/CenturyGothic.ttf";
		parameterFont1.fontParameters.size = 70;
		parameterFont1.fontParameters.borderColor = Color.BLACK;
        parameterFont1.fontParameters.borderWidth = 2;
		assetManager.load(AssetName.FONT_1, BitmapFont.class, parameterFont1);

		FreetypeFontLoader.FreeTypeFontLoaderParameter parameterFont2 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		parameterFont2.fontFileName = "font/CenturyGothic.ttf";
		parameterFont2.fontParameters.size = (Gdx.graphics.getHeight() * 150) / 1080;
		parameterFont2.fontParameters.borderColor = Color.BLACK;
		parameterFont2.fontParameters.borderWidth = (Gdx.graphics.getHeight() * 4) / 1080f;
		assetManager.load(AssetName.FONT_2, BitmapFont.class, parameterFont2);

		assetManager.load(AssetName.PHASE_DISPLAY_BG, Texture.class);
		assetManager.load(AssetName.UI_SKIN_1, Skin.class);
		assetManager.load(AssetName.TEX_LOBBY_SCREEN, Pixmap.class);
		assetManager.load(AssetName.TEX_LOBBY_2, Pixmap.class);
		assetManager.load(AssetName.TEX_LOBBY_LINE, Pixmap.class);
		assetManager.load(AssetName.TEX_LOBBY_OVERLAY, Pixmap.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_SCREEN, Texture.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_2, Texture.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_OVERLAY, Texture.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_LINE, Texture.class);
		assetManager.load(AssetName.RISK_BOARD, Texture.class);
	}

	@Override
	public void render() {
		super.render();
		// load assets
		if(assetManager.update() && !isDoneLoadingAssets) {
			isDoneLoadingAssets = true;
			mainMenuScreen = new MainMenu(this);
			setScreen(mainMenuScreen);
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

	public AssetManager getAssetManager() {
		return assetManager;
	}
}
