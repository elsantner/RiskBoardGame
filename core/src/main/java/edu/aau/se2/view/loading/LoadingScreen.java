package edu.aau.se2.view.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import edu.aau.se2.RiskGame;
import edu.aau.se2.view.AbstractScreen;

public class LoadingScreen extends AbstractScreen {
    private LoadingStage stage;
    private int numAssets;

    public LoadingScreen(RiskGame game, int numAssets) {
        super(game);
        this.numAssets = numAssets;
        this.stage = new LoadingStage(this, numAssets);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.stage.setProgress(numAssets - getGame().getAssetManager().getQueuedAssets());
        this.stage.draw();
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
        stage.dispose();
    }
}
