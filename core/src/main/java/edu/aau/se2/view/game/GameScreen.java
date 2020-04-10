package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * @author Elias
 */
public class GameScreen implements Screen {
    private BoardStage boardStage;
    private InputMultiplexer inputMultiplexer;

    public GameScreen() {
        boardStage = new BoardStage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }

    @Override
    public void show() {
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(boardStage));
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boardStage.draw();
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
        boardStage.dispose();
    }
}
