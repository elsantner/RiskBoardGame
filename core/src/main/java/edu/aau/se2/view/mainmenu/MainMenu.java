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
    private Skin mySkin;
    private Stage stage;
    private Viewport gamePort;
    private Label gameTitle;
    private Button create;
    private Button join;
    private Button settings;
    private Button exit;


    public MainMenu(RiskGame riskGame){
        mySkin = new Skin(Gdx.files.internal("skinMainMenu/star-soldier-ui.json"));
        gamePort = new ScreenViewport();
        stage = new Stage(gamePort);
        gameTitle = new Label("GAME MENU", mySkin);
        gameTitle.setSize(GameConstants.COL_WIDTH *2, GameConstants.ROW_HEIGHT *2);
        gameTitle.setPosition(GameConstants.C_X - gameTitle.getWidth()/2, GameConstants.C_Y + GameConstants.ROW_HEIGHT);
        gameTitle.setAlignment(Align.center);

        create = new TextButton("Create Game", mySkin);
        create.setSize(GameConstants.COL_WIDTH *2, GameConstants.ROW_HEIGHT);
        create.setPosition(GameConstants.C_X - create.getWidth()/2, GameConstants.C_Y + GameConstants.ROW_HEIGHT);

        join = new TextButton("Join Game", mySkin);
        join.setSize(GameConstants.COL_WIDTH *2, GameConstants.ROW_HEIGHT);
        join.setPosition(GameConstants.C_X - join.getWidth()/2, create.getY() - GameConstants.ROW_HEIGHT);

        settings = new TextButton("Settings", mySkin);
        settings.setSize(GameConstants.COL_WIDTH *2, GameConstants.ROW_HEIGHT);
        settings.setPosition(GameConstants.C_X - settings.getWidth()/2, join.getY() - GameConstants.ROW_HEIGHT);

        exit = new TextButton("Exit Game", mySkin);
        exit.setSize(GameConstants.COL_WIDTH *2, GameConstants.ROW_HEIGHT);
        exit.setPosition(GameConstants.C_X - exit.getWidth()/2, settings.getY() - GameConstants.ROW_HEIGHT);


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
        // TODO: Done display in show rather than constructor
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
    public void dispose() {
        mySkin.dispose();
        stage.dispose();
    }
}
