package edu.aau.se2;

import com.badlogic.gdx.Game;
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
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import edu.aau.se2.server.User;
import edu.aau.se2.view.lobbylist.LobbyListScreen;

public class LobbyScreen extends ScreenAdapter {

    private static final String TAG = "LobbyScreen";
    private Texture background;
    private Texture lobbyText;
    private Texture lobbyOverlay;
    private Texture line;
    private SpriteBatch batch;
    private Stage stage;
    private Table outerTable;
    private Table lobbyListTable;
    private Skin skin;

    private Lobby lobby;
    private Game game;

    public LobbyScreen(Game riskGame) {
        lobby = new Lobby();
        lobby.createLobby();
        game = riskGame;
    }

    public LobbyScreen(Game riskGame, Lobby lobby) {
        game = riskGame;
        this.lobby = lobby;
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
        //resize
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG, "dispose");
        background.dispose();
        lobbyOverlay.dispose();
        line.dispose();
        lobbyText.dispose();
    }

    private void updateUsersList() {
        for(User u : lobby.getUsers()) {
            Label text = new Label(u.getName(), skin);
            Label text2 = new Label(u.isReady() + "", skin);
            //text.setAlignment(Align.center);
            //text.setWrap(true);
            lobbyListTable.add(text).minHeight(80f).minWidth(180f);
            lobbyListTable.add(text2).minHeight(30f).minWidth(180f);
            lobbyListTable.row();
        }
    }

    @Override
    public void show() {
        Gdx.app.log(TAG, "Loading assets");
        background = new Texture(Gdx.files.internal("lobby/lobbyScreen.png"));
        lobbyText = new Texture(Gdx.files.internal("lobby/lobby2.png"));
        lobbyOverlay = new Texture(Gdx.files.internal("lobby/lobbyMenuOverlay.png"));
        line = new Texture(Gdx.files.internal("lobby/line.png"));
        batch = new SpriteBatch();

        this.stage = new Stage(new StretchViewport(1920,1080));
        stage.stageToScreenCoordinates(new Vector2(0,0));
        Gdx.input.setInputProcessor(this.stage);
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.getFont("default-font").getData().setScale(0.5f);

        lobbyListTable = new Table();
        lobbyListTable.setBounds(0,0,1600, 1600);
        //lobbyListTable.setDebug(true);

        updateUsersList();

        final ScrollPane scroller = new ScrollPane(lobbyListTable);

        outerTable = new Table();
        outerTable.setFillParent(true);
        //outerTable.setDebug(true);
        outerTable.pad(120f);

        outerTable.add(new Image(lobbyText)).minHeight(lobbyText.getHeight());
        TextButton ready = new TextButton("Bereit", skin);
        ready.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("event = " + event + ", x = " + x + ", y = " + y + ", pointer = " + pointer + ", button = " + button);
                Gdx.app.log(TAG, "Bereit");
                // TODO notify server
                return true;
            }
        });
        TextButton exit = new TextButton("Verlassen", skin);
        exit.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("event = " + event + ", x = " + x + ", y = " + y + ", pointer = " + pointer + ", button = " + button);
                Gdx.app.log(TAG, "Verlassen");
                // TODO notify server
                game.setScreen(new LobbyListScreen(game));
                return true;
            }
        });
        VerticalGroup buttonGroup = new VerticalGroup();
        buttonGroup.addActor(ready);
        buttonGroup.addActor(exit);
        outerTable.add(buttonGroup);
        // add exit lobby button
        outerTable.row();
        outerTable.add(new Image(line)).colspan(2);
        outerTable.row();
        outerTable.add(scroller).colspan(2).fill();

        this.stage.addActor(outerTable);
    }
}
