package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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


    public MainMenu(RiskGame riskGame){
        super(riskGame);
        mySkin = getGame().getAssetManager().get(AssetName.UI_SKIN_2);
        gamePort = new ScreenViewport();
        stage = new Stage(gamePort);

        backgroundTxt = getGame().getAssetManager().get(AssetName.TEX_LOBBY_SCREEN);

        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        setupLogo();
        setupButtons();
        onClickButtons();
        Gdx.input.setInputProcessor(stage);
    }

    public void setupButtons(){

        create = new TextButton("Spiel erstellen", mySkin);
        create.setTransform(true);
        create.setScale(1f);
        create.setPosition(GameConstants.C_X - create.getWidth()/2, GameConstants.C_Y + GameConstants.ROW_HEIGHT);

        join = new TextButton("Spiel beitreten", mySkin);
        join.setTransform(true);
        join.setScale(1f);
        join.setPosition(GameConstants.C_X - join.getWidth()/2, create.getY() - GameConstants.ROW_HEIGHT);

        exit = new TextButton("Spiel verlassen", mySkin);
        exit.setTransform(true);
        exit.setScale(1f);
        exit.setPosition(GameConstants.C_X - exit.getWidth()/2, join.getY() - GameConstants.ROW_HEIGHT);

        table.add(create).width(450).pad(10);
        table.row();
        table.add(join).width(450).pad(10);
        table.row();
        table.add(exit).width(450);

    }

    public void setupLogo(){
        Texture logoTxt = getGame().getAssetManager().get(AssetName.TEX_LOGO);
        Image logoImage = new Image(logoTxt);
        logoImage.setScale(gamePort.getWorldWidth() / (logoImage.getWidth() * 2));
        logoImage.setOrigin(Align.center);

        table.add(logoImage).row();
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


    }

    @Override
    public void show() {
        //currently unused
    }

    @Override
    public void render(float delta) {
        stage.act(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClearColor(1,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        stage.getBatch().draw(backgroundTxt,0,0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        stage.getBatch().end();

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
        stage.dispose();
    }
}
