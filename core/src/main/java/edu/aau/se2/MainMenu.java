package edu.aau.se2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.model.Database;

public class MainMenu implements Screen {
    private RiskGame riskGame;
    private Skin mySkin;
    private Stage stage;
    private Viewport gamePort;
    private Button create;
    private Button join;
    private Button exit;
    private Texture backgroundTxt;

    public MainMenu(RiskGame riskGame){
        this.riskGame = riskGame;
        mySkin = new Skin(Gdx.files.internal("skinMainMenu/glassy-ui.json"));
        gamePort = new ScreenViewport();
        stage = new Stage(gamePort);

        create = new TextButton("Create Game", mySkin);
        create.setTransform(true);
        create.setScale(1f);
        create.setPosition(GameConstants.cX - create.getWidth()/2, GameConstants.cY + GameConstants.row_height);

        join = new TextButton("Join Game", mySkin);
        join.setTransform(true);
        join.setScale(1f);
        join.setPosition(GameConstants.cX - join.getWidth()/2, create.getY() - GameConstants.row_height);

        exit = new TextButton("Exit Game", mySkin);
        exit.setTransform(true);
        exit.setScale(1f);
        exit.setPosition(GameConstants.cX - exit.getWidth()/2, join.getY() - GameConstants.row_height);

        backgroundTxt = new Texture(Gdx.files.internal("lobby/lobbyScreen.png"));

        stage.addActor(create);
        stage.addActor(join);
        stage.addActor(exit);

        onClickButtons();
        Gdx.input.setInputProcessor(stage);
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

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.getBatch().dispose();
        backgroundTxt.dispose();
        mySkin.dispose();
        stage.dispose();
    }
}
