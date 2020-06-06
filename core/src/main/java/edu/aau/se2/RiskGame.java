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
import com.badlogic.gdx.utils.Timer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnConnectionChangedListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.utils.LoggerConfigurator;
import edu.aau.se2.view.PopupMessageDisplay;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.game.GameScreen;
import edu.aau.se2.view.game.Territory;
import edu.aau.se2.view.loading.LoadingScreen;
import edu.aau.se2.view.lobby.LobbyScreen;
import edu.aau.se2.view.lobbylist.LobbyListScreen;
import edu.aau.se2.view.mainmenu.MainMenu;

public class RiskGame extends Game {
    private static final String TAG = "RiskGame";

	private AssetManager assetManager;
	private boolean isDoneLoadingAssets = false;
	private PopupMessageDisplay popupMessageDisplay;

	private GameScreen gameScreen;
	private LobbyScreen lobbyScreen;
	private LobbyListScreen lobbyListScreen;
	private MainMenu mainMenuScreen;
	private LoadingScreen loadingScreen;

	public RiskGame(PopupMessageDisplay popupMessageDisplay) {
		if (popupMessageDisplay == null) {
			throw new NullPointerException("popupMessageDisplay must not be null");
		}

		this.popupMessageDisplay = popupMessageDisplay;
	}

	@Override
	public void create () {
		this.assetManager = new AssetManager();
		setupAssetManagerLoadingScreen();
		assetManager.finishLoading();

		setupAssetManagerAllAssets();
		loadingScreen = new LoadingScreen(this, assetManager.getQueuedAssets());
		setScreen(loadingScreen);

        Database db = Database.getInstance();

		db.getListeners().setGameStartListener((players, initialArmyCount) -> Gdx.app.postRunnable(() -> {
            gameScreen = new GameScreen(this);
            setScreen(gameScreen);
        }));
		db.getListeners().setLeftLobbyListener(wasClosed -> Gdx.app.postRunnable(() -> {
			if (wasClosed) {
				popupMessageDisplay.showMessage("Spiel geschlossen");
			}
			mainMenuScreen = new MainMenu(this);
			setScreen(mainMenuScreen);
		}));
		db.getListeners().setJoinedLobbyListener((lobbyID, host, players) -> Gdx.app.postRunnable(() -> {
			lobbyScreen = new LobbyScreen(this);
			setScreen(lobbyScreen);
		}));
		db.getListeners().setConnectionChangedListener(new OnConnectionChangedListener() {
			@Override
			public void connected(Player thisPlayer) {
			}

			@Override
			public void disconnected() {
				LoggerConfigurator.getConfiguredLogger(TAG, Level.SEVERE).log(Level.SEVERE, "Connection lost");
				showMenuScreenWithConnectionLostDialog();
			}
		});
		Timer.post(new Timer.Task() {
			@Override
			public void run() {
				try {
					db.connectIfNotConnected();
				} catch (IOException e) {
					LoggerConfigurator.getConfiguredLogger(TAG, Level.SEVERE).log(Level.SEVERE, "Connection Error: ", e);
				}
			}
		});
	}

	private void showMenuScreenWithConnectionLostDialog() {
		Gdx.app.postRunnable(() -> {
			mainMenuScreen = new MainMenu(this, true);
			setScreen(mainMenuScreen);
		});
	}

	private void setupAssetManagerLoadingScreen() {
		assetManager.load(AssetName.TEX_LOGO, Texture.class);
		assetManager.load(AssetName.UI_SKIN_2, Skin.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_SCREEN, Texture.class);
	}

	private void setupAssetManagerAllAssets() {
		// if device is not yet rotated correctly (may happen during startup)
		int screenHeight = Gdx.graphics.getHeight();
		int screenWidth = Gdx.graphics.getHeight();
		if (Gdx.graphics.getWidth() < Gdx.graphics.getHeight()) {
			screenHeight = Gdx.graphics.getWidth();
			screenWidth = Gdx.graphics.getHeight();
		}

        FreeTypeFontGenerator.setMaxTextureSize(2048);
		FileHandleResolver resolver = new InternalFileHandleResolver();
		assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		FreetypeFontLoader.FreeTypeFontLoaderParameter parameterFont1 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		parameterFont1.fontFileName = AssetName.TTF_CENTURY_GOTHIC_LOCATION;
		parameterFont1.fontParameters.size = (screenWidth * 85) / Territory.REFERENCE_WIDTH;
		parameterFont1.fontParameters.borderColor = Color.BLACK;
		parameterFont1.fontParameters.borderWidth = 2;
		assetManager.load(AssetName.FONT_1, BitmapFont.class, parameterFont1);

		FreetypeFontLoader.FreeTypeFontLoaderParameter parameterFont2 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		parameterFont2.fontFileName = AssetName.TTF_CENTURY_GOTHIC_LOCATION;
		parameterFont2.fontParameters.size = (screenHeight * 150) / 1080;
		parameterFont2.fontParameters.borderColor = Color.BLACK;
		parameterFont2.fontParameters.borderWidth = (screenHeight * 4) / 1080f;
		assetManager.load(AssetName.FONT_2, BitmapFont.class, parameterFont2);

		FreetypeFontLoader.FreeTypeFontLoaderParameter parameterFont3 = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		parameterFont3.fontFileName = AssetName.TTF_CENTURY_GOTHIC_LOCATION;
		parameterFont3.fontParameters.size = 20;
		parameterFont3.fontParameters.borderColor = Color.BLACK;
		parameterFont3.fontParameters.borderWidth = 1;
		assetManager.load(AssetName.FONT_3, BitmapFont.class, parameterFont3);

		assetManager.load(AssetName.PHASE_DISPLAY_BG, Texture.class);
        assetManager.load(AssetName.UI_SKIN_1, Skin.class);
		assetManager.load(AssetName.TEX_LOBBY_SCREEN, Texture.class);
		assetManager.load(AssetName.TEX_LOBBY_2, Texture.class);
		assetManager.load(AssetName.TEX_LOBBY_LINE, Texture.class);
		assetManager.load(AssetName.TEX_LOBBY_OVERLAY, Texture.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_2, Texture.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_OVERLAY, Texture.class);
		assetManager.load(AssetName.TEX_LOBBYLIST_LINE, Texture.class);
		assetManager.load(AssetName.RISK_BOARD, Texture.class);
		assetManager.load(AssetName.ARMY_DISPLAY_CIRCLE, Texture.class);
		assetManager.load(AssetName.TEX_DICE_ATTACKER, Texture.class);
		assetManager.load(AssetName.TEX_DICE_DEFENDER, Texture.class);
		assetManager.load(AssetName.ATTACK_ARROW, Texture.class);
		assetManager.load(AssetName.BG_ATTACK_DISPLAY, Texture.class);
		assetManager.load(AssetName.ICON_INFANTRY, Texture.class);
		assetManager.load(AssetName.CARDS_BUTTON, Texture.class);
		assetManager.load(AssetName.END_TURN, Texture.class);
		assetManager.load(AssetName.END_GAME, Texture.class);
	}

	@Override
	public void render() {
		super.render();
		// load assets
		if(assetManager.update() && !isDoneLoadingAssets) {
			isDoneLoadingAssets = true;
			mainMenuScreen = new MainMenu(this, !Database.getInstance().isConnected());
			setScreen(mainMenuScreen);
		}
	}

	public void openLobbyListScreen() {
		Gdx.app.postRunnable(() -> {
			lobbyListScreen = new LobbyListScreen(this);
			setScreen(lobbyListScreen);
		});
	}

	public void showMessage(String msg) {
		popupMessageDisplay.showMessage(msg);
	}

	@Override
	public void dispose () {
		super.dispose();
		try {
			isDoneLoadingAssets = false;
			assetManager.dispose();
			Territory.dispose();
			Database.getInstance().closeConnection();
			mainMenuScreen.dispose();
			loadingScreen.dispose();
			gameScreen.dispose();
			lobbyScreen.dispose();
			lobbyListScreen.dispose();
		}
		catch (Exception ex) {
			Logger.getLogger(TAG).log(Level.WARNING, "Error: ", ex);
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
