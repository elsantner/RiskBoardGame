package edu.aau.se2.view.lobby;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.List;
import java.util.logging.Level;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnPlayersChangedListener;
import edu.aau.se2.server.data.Player;
import edu.aau.se2.utils.LoggerConfigurator;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.lobbylist.ExitButtonListener;
import edu.aau.se2.view.lobbylist.ReadyButtonListener;

public class LobbyScreen extends AbstractScreen implements OnPlayersChangedListener {

    private static final String TAG = "LobbyScreen";
    private Texture background;
    private Texture lobbyText;
    private Texture lobbyOverlay;
    private Texture line;
    private SpriteBatch batch;
    private BitmapFont font;
    private int height;
    private int width;
    private int lineHeight;
    private Stage stage;
    private Skin skin;

    private Database db;
    private List<Player> users;

    public LobbyScreen(RiskGame game) {
        super(game);
        db = Database.getInstance();
        db.getListeners().setPlayersChangedListener(this);

        users = db.getLobby().getPlayers();

        height = Gdx.graphics.getHeight();
        width = Gdx.graphics.getWidth();
        // if for some reason phone screen is flipped
        if (height > width) {
            int tmp = height;
            height = width;
            width = tmp;
        }
        batch = new SpriteBatch();

        assets();
    }

    @Override
    public void show() {
        stage = new Stage(new StretchViewport(1920, 1080));
        stage.stageToScreenCoordinates(new Vector2(0, 0));

        addInputProcessor(stage);

        skin = getGame().getAssetManager().get(AssetName.UI_SKIN_2);

        Table outerTable = new Table();
        outerTable.setX(stage.getViewport().getWorldWidth() * 0.8f);
        outerTable.setY(stage.getViewport().getWorldHeight() * 0.85f);

        TextButton ready = new TextButton("Bereit", skin);
        ready.addListener(new ReadyButtonListener());
        TextButton exit = new TextButton("Verlassen", skin);
        exit.addListener(new ExitButtonListener());

        outerTable.add(ready).width(stage.getViewport().getWorldWidth() * 0.2f).padBottom(stage.getViewport().getWorldHeight() * 0.01f).row();
        outerTable.add(exit).width(stage.getViewport().getWorldWidth() * 0.2f);
        this.stage.addActor(outerTable);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(background, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.draw(lobbyOverlay, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.draw(line, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.draw(lobbyText, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());


        batch.end();
        renderUsers();
        stage.act();
        stage.draw();
    }

    private void renderUsers() {

        int xCord = (width * 55) / 1080;
        int yCord = (height * 760) / 1080;

        batch.begin();
        for (Player us : users
        ) {
            String name = us.getNickname();
            boolean ready = us.isReady();
            font.setColor(new Color(0.6f, 0, 0, 1));
            font.draw(batch, name, xCord, yCord);
            if (ready) {
                font.setColor(new Color(0, 0.8f, 0, 1));
                font.draw(batch, "ready", (xCord + (int) ((width * 1200) / 1920)), yCord);
            } else {
                font.setColor(new Color(0.8f, 0, 0, 1));
                font.draw(batch, "!ready", (xCord + (int) ((width * 1200) / 1920)), yCord);
            }
            yCord -= (height * lineHeight) / 1080;
        }
        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        //resize
    }

    @Override
    public void pause() {
        //pause
    }

    @Override
    public void resume() {
        //resume
    }

    @Override
    public void hide() {
        //hide
    }

    @Override
    public void dispose() {
        try {
            batch.dispose();
            background = null;
            lobbyOverlay = null;
            line = null;
            font = null;
            lobbyText = null;
            skin = null;
        } catch (Exception ex) {
            LoggerConfigurator.getConfiguredLogger(TAG, Level.WARNING).log(Level.WARNING, "Error disposing assets", ex);
        }
    }

    @Override
    public void handleBackButton() {
        db.leaveLobby();
    }

    private void assets() {
        Gdx.app.log(TAG, "Loading assets" + Gdx.graphics.getDensity() + "  " + height);
        AssetManager assetManager = getGame().getAssetManager();

        background = assetManager.get(AssetName.TEX_LOBBY_SCREEN);
        lobbyText = assetManager.get(AssetName.TEX_LOBBY_2);
        line = assetManager.get(AssetName.TEX_LOBBY_LINE);
        lobbyOverlay = assetManager.get(AssetName.TEX_LOBBY_OVERLAY);
        font = getGame().getAssetManager().get(AssetName.FONT_2);
        adjustFontSize();
        font.setColor(new Color(0.6f, 0, 0, 1));
    }

    public List<Player> getUsers() {
        return users;
    }

    public void setUsers(List<Player> users) {
        this.users = users;
    }

    @Override
    public void playersChanged(List<Player> newPlayers) {
        this.users = newPlayers;
        adjustFontSize();
    }

    private void adjustFontSize() {
        if (font == null) {
            font = getGame().getAssetManager().get(AssetName.FONT_2);
        }
        if (this.users.size() < 5) {
            font.getData().setScale(1f);
            lineHeight = 155;
        } else if (this.users.size() == 5) {
            font.getData().setScale(0.85f);
            lineHeight = 135;
        } else if (this.users.size() == 6) {
            font.getData().setScale(0.75f);
            lineHeight = 120;
        }
    }
}
