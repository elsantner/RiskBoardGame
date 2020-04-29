package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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

import java.util.List;

import edu.aau.se2.RiskGame;
import edu.aau.se2.server.networking.dto.prelobby.LobbyListMessage;
import edu.aau.se2.view.AbstractScreen;

public class LobbyListScreen extends AbstractScreen {

    private static final String TAG = "LobbyScreen";
    private Texture background;
    private Texture lobbyText;
    private Texture lobbyOverlay;
    private Texture line;
    private SpriteBatch batch;

    private Stage stage;

    private List<LobbyListMessage.LobbyData> lobbyData;

    public LobbyListScreen(RiskGame game, List<LobbyListMessage.LobbyData> lobbyData) {
        super(game);
        this.lobbyData = lobbyData;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();
        batch.draw(background, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.draw(lobbyOverlay, 0, 0,  stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.end();

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
        background = new Texture(Gdx.files.internal("lobbylist/lobbyScreen.png"));
        lobbyText = new Texture(Gdx.files.internal("lobbylist/lobby2.png"));
        lobbyOverlay = new Texture(Gdx.files.internal("lobbylist/lobbyMenuOverlay.png"));
        line = new Texture(Gdx.files.internal("lobbylist/line.png"));
        batch = new SpriteBatch();

        this.stage = new Stage(new StretchViewport(1920,1080));
        stage.stageToScreenCoordinates(new Vector2(0,0));
        Gdx.input.setInputProcessor(this.stage);
        final Skin skin = new Skin(Gdx.files.internal("lobbylistskin/uiskin.json"));
        skin.getFont("default-font").getData().setScale(0.5f);

        Table lobbyListTable = new Table();
        lobbyListTable.setBounds(0,0,1600, 1600);

        for (LobbyListMessage.LobbyData l: lobbyData) {
            Label text = new Label(l.getHost().getNickname(), skin);
            Label text2 = new Label("Players: " + l.getPlayerCount(), skin);
            TextButton text3 = new TextButton("Beitreten", skin);
            text3.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    new JoinLobbyDialog("Beitreten", skin, l.getLobbyID()).show(stage);
                    return true;
                }
            });

            lobbyListTable.add(text).minHeight(80f).minWidth(250f).pad(50f);
            lobbyListTable.add(text2).minHeight(30f).minWidth(250f).pad(50f);
            lobbyListTable.add(text3).minHeight(30f).minWidth(250f).pad(50f);
            lobbyListTable.row();
        }

        final ScrollPane scroller = new ScrollPane(lobbyListTable);

        Table outerTable = new Table();
        outerTable.setFillParent(true);
        outerTable.pad(120f);

        outerTable.add(new Image(lobbyText)).minHeight(lobbyText.getHeight());
        outerTable.row();
        outerTable.add(new Image(line));
        outerTable.row();
        outerTable.add(scroller).fill();

        this.stage.addActor(outerTable);
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG, "dispose");
        background.dispose();
        lobbyOverlay.dispose();
        line.dispose();
        lobbyText.dispose();
    }
}
