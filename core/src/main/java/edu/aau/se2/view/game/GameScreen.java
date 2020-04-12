package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.FitViewport;

import edu.aau.se2.RiskGame;
import edu.aau.se2.view.game.HudStage;


public class GameScreen implements Screen {
    private HudStage hudStage;
    private RiskGame game;
    private InputMultiplexer inputMultiplexer;


    public GameScreen(RiskGame game) {
        this.game = game;
        hudStage = new HudStage(game.batch);
    }

    @Override
    public void show() {
        inputMultiplexer = new InputMultiplexer();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(hudStage.stage.getCamera().combined);
        hudStage.stage.draw();
    }



    @Override
    public void resize(int width, int height) {

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
        hudStage.dispose();
    }

}
