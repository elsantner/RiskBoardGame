package edu.aau.se2.view.lobbylist;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

public class LobbyListScreen extends ScreenAdapter {

    private static final String TAG = "LobbyScreen";
    private Texture background;
    private Texture lobbyText;
    private Texture lobbyOverlay;
    private Texture line;
    private SpriteBatch batch;
    private BitmapFont font;
    private List<String> lobbies;
    private Stage stage;
    private Table outerTable;
    private Table lobbyListTable;
    private JoinLobbyDialog joinDialog;

    private RiskGame game;

    public LobbyListScreen(RiskGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();
        batch.draw(background, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.draw(lobbyOverlay, 0, 0,  stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.end();

        //renderLobbies();
        stage.act();
        stage.draw();
    }

    private void renderLobbies() {



        int xCord = 85;
        int yCord = 775;

        batch.begin();
        for (String lobby : lobbies) {
            //font.setColor(new Color(0.6f, 0, 0, 1));
            font.draw(batch, lobby, xCord, yCord);
            yCord -= 150;
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.app.log(TAG, "Loading assets");
        font = new BitmapFont(Gdx.files.internal("font/lobbyFontv2.fnt"));
        font.getData().scale(0.5f);
        font.setColor(new Color(0.6f, 0, 0, 1));
        background = new Texture(Gdx.files.internal("lobby/lobbyScreen.png"));
        lobbyText = new Texture(Gdx.files.internal("lobby/lobby2.png"));
        lobbyOverlay = new Texture(Gdx.files.internal("lobby/lobbyMenuOverlay.png"));
        line = new Texture(Gdx.files.internal("lobby/line.png"));
        batch = new SpriteBatch();

        this.stage = new Stage(new StretchViewport(1920,1080));
        stage.stageToScreenCoordinates(new Vector2(0,0));
        Gdx.input.setInputProcessor(this.stage);
        final Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.getFont("default-font").getData().setScale(0.5f);

        joinDialog = new JoinLobbyDialog("Beitreten", skin);

        lobbyListTable = new Table();
        lobbyListTable.setBounds(0,0,1600, 1600);
        lobbyListTable.setDebug(true);
        for(int i = 0; i < 20; i++) {
            Label text = new Label("Lobby #" + (i+1), skin);
            Label text2 = new Label("Lobby #" + (i+1), skin);
            TextButton text3 = new TextButton("Lobby #" + (i+1), skin);
            text3.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    System.out.println("event = " + event + ", x = " + x + ", y = " + y + ", pointer = " + pointer + ", button = " + button);
                    joinDialog.show(stage);
                    return true;
                }
            });
            //text.setAlignment(Align.center);
            //text.setWrap(true);
            lobbyListTable.add(text).minHeight(80f).minWidth(180f);
            lobbyListTable.add(text2).minHeight(30f).minWidth(180f);
            lobbyListTable.add(text3).minHeight(30f).minWidth(180f);
            lobbyListTable.row();
        }

        final ScrollPane scroller = new ScrollPane(lobbyListTable);




        outerTable = new Table();
        outerTable.setFillParent(true);
        outerTable.setDebug(true);
        outerTable.pad(120f);

        outerTable.add(new Image(lobbyText)).minHeight(lobbyText.getHeight());
        // add exit lobby button
        outerTable.row();
        outerTable.add(new Image(line)) /* .colspan(2) */;
        outerTable.row();
        outerTable.add(scroller).fill();

        this.stage.addActor(outerTable);


    }

    @Override
    public void dispose() {
        background.dispose();
        lobbyOverlay.dispose();
        line.dispose();
        font.dispose();
        lobbyText.dispose();
    }
}
