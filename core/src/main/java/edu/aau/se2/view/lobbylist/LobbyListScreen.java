package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;
import java.util.List;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnErrorListener;
import edu.aau.se2.model.listener.OnLobbyListChangedListener;
import edu.aau.se2.server.networking.dto.lobby.ErrorMessage;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.asset.AssetName;

public class LobbyListScreen extends AbstractScreen implements OnLobbyListChangedListener, OnErrorListener {

    private static final String TAG = "LobbyScreen";
    private Texture background;
    private Texture lobbyText;
    private Texture lobbyOverlay;
    private Texture line;
    private SpriteBatch batch;
    private Table lobbyListTable;

    private Stage stage;

    private List<LobbyListMessage.LobbyData> lobbyData;
    private boolean dataChanged = false;

    public LobbyListScreen(RiskGame game) {
        super(game);

        lobbyData = new ArrayList<>();
        Database.getInstance().getListeners().setLobbyListChangedListener(this);
        Database.getInstance().getListeners().setErrorListener(this);
        Database.getInstance().triggerLobbyListUpdate();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();
        batch.draw(background, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.draw(lobbyOverlay, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.end();

        if (dataChanged){
            dataChanged = false;
            AssetManager assetManager = getGame().getAssetManager();
            final Skin skin = assetManager.get(AssetName.UI_SKIN_2);
            fillLobbyList(assetManager, skin);
        }

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // currently unused
    }

    @Override
    public void resume() {
        // currently unused
    }

    @Override
    public void hide() {
        // currently unused
    }

    @Override
    public void show() {
        Gdx.app.log(TAG, "Loading assets");
        AssetManager assetManager = getGame().getAssetManager();
        background = assetManager.get(AssetName.TEX_LOBBYLIST_SCREEN);
        lobbyText = assetManager.get(AssetName.TEX_LOBBYLIST_2);
        lobbyOverlay = assetManager.get(AssetName.TEX_LOBBYLIST_OVERLAY);
        line = assetManager.get(AssetName.TEX_LOBBYLIST_LINE);
        batch = new SpriteBatch();

        stage = new Stage(new StretchViewport(1920, 1080));
        stage.stageToScreenCoordinates(new Vector2(0, 0));

        addInputProcessor(stage);

        final Skin skin = assetManager.get(AssetName.UI_SKIN_2);

        fillLobbyList(assetManager, skin);

        final ScrollPane scroller = new ScrollPane(lobbyListTable);

        Table outerTable = new Table();
        outerTable.setFillParent(true);
        outerTable.pad(120f);

        TextButton updateButton = new TextButton("Aktualisieren", skin);
        updateButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Database.getInstance().triggerLobbyListUpdate();
                return true;
            }
        });

        TextButton exitButton = new TextButton("Verlassen", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Database.getInstance().returnToMainMenu();
                return true;
            }
        });

        Table buttonTable = new Table();

        outerTable.add(new Image(lobbyText)).minHeight(lobbyText.getHeight());
        buttonTable.add(updateButton).padBottom(stage.getViewport().getWorldHeight() * 0.01f);
        buttonTable.row();
        buttonTable.add(exitButton);
        outerTable.add(buttonTable).fill();
        outerTable.row();
        outerTable.add(new Image(line));
        outerTable.row();
        outerTable.add(scroller).fill();

        this.stage.addActor(outerTable);
    }

    private void fillLobbyList(AssetManager assetManager, Skin skin) {
        if (lobbyListTable == null) {
            lobbyListTable = new Table();
            lobbyListTable.setBounds(0, 0, 1600, 1600);
        } else {
            lobbyListTable.clearChildren();
        }

        for (LobbyListMessage.LobbyData l : lobbyData) {
            Label text = new Label(l.getHost().getNickname(), skin, "font-big", Color.BLACK);
            Label text2 = new Label("Players: " + l.getPlayerCount(), skin, "font-big", Color.BLACK);
            TextButton text3 = new TextButton("Beitreten", skin);
            text3.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    showDialog(new JoinLobbyDialog("Beitreten", assetManager.get(AssetName.UI_SKIN_1, Skin.class), l.getLobbyID()), stage, 3);
                    return true;
                }
            });

            lobbyListTable.add(text).minHeight(80f).minWidth(250f).pad(50f);
            lobbyListTable.add(text2).minHeight(30f).minWidth(250f).pad(50f);
            lobbyListTable.add(text3).minHeight(30f).minWidth(250f).pad(50f);
            lobbyListTable.row();
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG, "dispose");
        background = null;
        lobbyOverlay = null;
        line = null;
        lobbyText = null;
    }

    @Override
    public void lobbyListChanged(List<LobbyListMessage.LobbyData> lobbyList) {
        lobbyData = lobbyList;
        dataChanged = true;
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage;
        if (errorCode == ErrorMessage.JOIN_LOBBY_ALREADY_JOINED)
            errorMessage = "Already joined";
        else if (errorCode == ErrorMessage.JOIN_LOBBY_CLOSED)
            errorMessage = "Lobby closed";
        else if (errorCode == ErrorMessage.JOIN_LOBBY_FULL)
            errorMessage = "Lobby full";
        else
            errorMessage = "Unknown error while joining";

        getGame().showMessage(errorMessage);
        Database.getInstance().triggerLobbyListUpdate();
    }
}
