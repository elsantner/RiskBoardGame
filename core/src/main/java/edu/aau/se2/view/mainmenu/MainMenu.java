package edu.aau.se2.view.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.RiskGame;
import edu.aau.se2.model.Database;

public class MainMenu implements Screen {
    private RiskGame riskGame;
    private Texture texture;
    private Skin mySkin;
    private Stage stage;
    private Viewport gamePort;
    private Label gameTitle;
    private Button create;
    private Button join;
    private Button settings;
    private Button exit;


    public MainMenu(RiskGame riskGame){
        this.riskGame = riskGame;
        mySkin = new Skin(Gdx.files.internal("skinMainMenu/star-soldier-ui.json"));
        gamePort = new ScreenViewport();
        stage = new Stage(gamePort);
        gameTitle = new Label("GAME MENU", mySkin);
        gameTitle.setSize(GameConstants.col_width*2, GameConstants.row_height*2);
        gameTitle.setPosition(GameConstants.cX - gameTitle.getWidth()/2, GameConstants.cY + GameConstants.row_height);
        gameTitle.setAlignment(Align.center);

        create = new TextButton("Create Game", mySkin);
        create.setSize(GameConstants.col_width*2, GameConstants.row_height);
        create.setPosition(GameConstants.cX - create.getWidth()/2, GameConstants.cY + GameConstants.row_height);

        join = new TextButton("Join Game", mySkin);
        join.setSize(GameConstants.col_width*2, GameConstants.row_height);
        join.setPosition(GameConstants.cX - join.getWidth()/2, create.getY() - GameConstants.row_height);

        settings = new TextButton("Settings", mySkin);
        settings.setSize(GameConstants.col_width*2, GameConstants.row_height);
        settings.setPosition(GameConstants.cX - settings.getWidth()/2, join.getY() - GameConstants.row_height);

        exit = new TextButton("Exit Game", mySkin);
        exit.setSize(GameConstants.col_width*2, GameConstants.row_height);
        exit.setPosition(GameConstants.cX - exit.getWidth()/2, settings.getY() - GameConstants.row_height);


        stage.addActor(gameTitle);
        stage.addActor(create);
        stage.addActor(join);
        stage.addActor(settings);
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

        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //ToDo Implement the Settings Screen
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
        Gdx.gl.glClearColor(1,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
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
        mySkin.dispose();
        stage.dispose();
    }
}
