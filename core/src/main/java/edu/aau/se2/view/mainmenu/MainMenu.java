package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Collections;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.model.listener.OnNicknameChangeListener;
import edu.aau.se2.server.networking.dto.prelobby.ChangeNicknameMessage;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.DefaultNameProvider;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.game.HudStage;

public class MainMenu extends AbstractScreen implements OnNicknameChangeListener {
    private Skin mySkin;
    private Stage stage;
    private Viewport gamePort;
    private Button create;
    private Button join;
    private Button exit;
    private Texture backgroundTxt;
    private Table table;
    private Button settings;
    private DefaultNameProvider defaultNameProvider;
    private String nickname;
    private boolean showChangeNameUI;
    private ChangeNameStage changeNameStage;


    public MainMenu(RiskGame riskGame, DefaultNameProvider defaultNameProvider){
        super(riskGame);
        mySkin = getGame().getAssetManager().get(AssetName.UI_SKIN_2);
        gamePort = new ScreenViewport();
        stage = new Stage(gamePort);
        this.defaultNameProvider = defaultNameProvider;

        backgroundTxt = getGame().getAssetManager().get(AssetName.TEX_LOBBY_SCREEN);

        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        int screenHeight = Gdx.graphics.getHeight();
        int screenWidth = Gdx.graphics.getWidth();
        showChangeNameUI = false;

        setupLogo();
        setupButtons();
        onClickButtons();
        Gdx.input.setInputProcessor(stage);

        changeNameStage = new ChangeNameStage(this, new FitViewport(screenWidth, screenHeight));
    }

    public void setupButtons(){

        create = new TextButton("Spiel erstellen", mySkin);
        join = new TextButton("Spiel beitreten", mySkin);
        exit = new TextButton("Spiel verlassen", mySkin);
        settings = new TextButton("Einstellungen", mySkin);
        settings.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showChangeNameUI = !showChangeNameUI;
                System.out.println(showChangeNameUI);
            }
        });


        table.add(create).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f).padBottom(gamePort.getWorldHeight() * 0.03f);
        table.row();
        table.add(join).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f).padBottom(gamePort.getWorldHeight() * 0.03f);
        table.row();
        table.add(exit).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f).padBottom(gamePort.getWorldHeight() * 0.03f);
        table.row();
        table.add(settings).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.1f);

    }

    public void setupLogo(){
        Texture logoTxt = getGame().getAssetManager().get(AssetName.TEX_LOGO);
        Image logoImage = new Image(logoTxt);
        logoImage.setScale(gamePort.getWorldWidth() / (logoImage.getWidth() * 2));
        logoImage.setOrigin(Align.center);

        table.add(logoImage).padBottom(gamePort.getWorldHeight() * 0.1f).row();
    }

    public void onClickButtons(){
        create.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Database.getInstance().hostLobby();
            }
        });


        join.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Database.getInstance().triggerLobbyListUpdate();
            }
        });


        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        /* get device name on click
        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Database.getInstance().setPlayerNickname(defaultNameProvider.getDeviceName());
            }
        });*/
    }

    @Override
    public void show() {
        //InputMultiplexer inputMultiplexer = new InputMultiplexer();
        //inputMultiplexer.addProcessor(changeNameStage);
        //Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        stage.act(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClearColor(1, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        stage.getBatch().draw(backgroundTxt, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        stage.getBatch().end();

        stage.act();
        stage.draw();

        //changeNameStage.getViewport().apply();
        //changeNameStage.draw();

        if (getShowChangeNameUI()) {
            changeNameStage.getViewport().apply();
            changeNameStage.act();
            changeNameStage.draw();
            System.out.println("this.getShowChangeNameUI(): ### " + getShowChangeNameUI());
        }
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height, true);
        stage.getViewport().update(width, height);
        changeNameStage.getViewport().update(width,height);
    }

    @Override
    public void pause() {
        //currently unused
    }

    @Override
    public void resume() {
        //currently unused
    }

    @Override
    public void hide() {
        //currently unused
    }

    @Override
    public void dispose() {
        stage.dispose();
        changeNameStage.dispose();
    }

    @Override
    public void nicknameChanged(String nickname) {
        this.nickname = nickname;
    }

    public boolean getShowChangeNameUI() {
        return this.showChangeNameUI;
    }

    public void setShowChangeNameUI(boolean showChangeNameUI) {
        this.showChangeNameUI = showChangeNameUI;
    }

}
