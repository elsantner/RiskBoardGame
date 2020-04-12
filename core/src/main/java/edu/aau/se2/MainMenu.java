package edu.aau.se2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenu implements Screen {
    private RiskGame parent;
    private Stage stage;
    private Table table;
    private Skin skin;
    private TextButton createGame;
    private TextButton joinGame;
    private TextButton settings;
    private TextButton exit;

    public MainMenu(RiskGame riskGame){
        parent = riskGame; //allows the screen to tell the orchestrator when done
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();

        //create the the table that contains the buttons
        table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        table.addActor(table);

        skin = new Skin(Gdx.files.internal("skin/star-soldier-ui.json"));
        createGame = new TextButton("Create Game", skin);
        joinGame = new TextButton("Join Game", skin);
        settings = new TextButton("Settings", skin);
        exit = new TextButton("Exit", skin);

        //add the buttons to the table
        table.add(createGame).fillX().uniformX();
        table.row().pad(10,0,10,0);
        table.add(joinGame).fillX().uniformX();
        table.row();
        table.add(settings).fillX().uniformX();
        table.row();
        table.add(exit).fillX().uniformX();

    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //clear the screen before drawing the next screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        //automatically recenter when changing elements on the stage
        stage.getViewport().update(width, height, true);
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

    }
}
