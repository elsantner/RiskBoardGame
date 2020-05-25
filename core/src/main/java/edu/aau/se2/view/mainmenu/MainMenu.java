package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.asset.AssetName;

public class MainMenu extends AbstractScreen {
    private Skin mySkin;
    private Stage stage;
    private Viewport gamePort;
    private Button create;
    private Button join;
    private Button exit;
    private Texture backgroundTxt;
    private Table table;
    private SpriteBatch batch;


    public MainMenu(RiskGame riskGame){
        this(riskGame, false);
    }

    public MainMenu(RiskGame riskGame, boolean showDisconnectedDialog) {
        super(riskGame);

        mySkin = getGame().getAssetManager().get(AssetName.UI_SKIN_2);
        gamePort = new ScreenViewport();
        stage = new Stage(gamePort);
        batch = new SpriteBatch();

        backgroundTxt = getGame().getAssetManager().get(AssetName.TEX_LOBBY_SCREEN);

        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        setupLogo();
        setupButtons();
        onClickButtons();

        addInputProcessor(stage);
        if (showDisconnectedDialog) {
            setButtonsEnabled(false);
            displayDisconnectedDialog();
        }
    }

    private void displayDisconnectedDialog() {
        Skin uiSkin = getGame().getAssetManager().get(AssetName.UI_SKIN_1);
        ReconnectDialog dialog = new ReconnectDialog(uiSkin, "Keine Verbindung",
                "Moechten Sie es erneut versuchen?", success -> {
                    if (!success) {
                        displayDisconnectedDialog();
                    }
                    else {
                        setButtonsEnabled(true);
                    }
                });
        showDialog(dialog, stage, 3);
    }

    private void setButtonsEnabled(boolean enabled) {
        create.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        join.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        exit.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
    }

    public void setupButtons(){

        create = new TextButton("Spiel erstellen", mySkin);
        join = new TextButton("Spiel beitreten", mySkin);
        exit = new TextButton("Spiel verlassen", mySkin);

        table.add(create).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.15f).padBottom(gamePort.getWorldHeight() * 0.05f);
        table.row();
        table.add(join).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.15f).padBottom(gamePort.getWorldHeight() * 0.05f);
        table.row();
        table.add(exit).width(gamePort.getWorldWidth() * 0.35f).height(gamePort.getWorldHeight() * 0.15f);

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
                getGame().openLobbyListScreen();
            }
        });


        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });


    }

    @Override
    public void show() {
        //currently unused
    }

    @Override
    public void handleBackButton() {
        Gdx.app.exit();
    }

    @Override
    public void render(float delta) {
        stage.act(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClearColor(1,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(backgroundTxt,0,0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height, true);
        stage.getViewport().update(width, height);
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
        batch.dispose();
        stage.dispose();
    }
}
