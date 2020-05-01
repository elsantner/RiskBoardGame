package edu.aau.se2.view.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import edu.aau.se2.RiskGame;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.asset.AssetName;

public class LoadingScreen extends AbstractScreen {
    private LoadingStage stage;
    private int numAssets;
    private SpriteBatch batch;
    private Texture texBG;
    private AssetManager assetManager;

    public LoadingScreen(RiskGame game, int numAssets) {
        super(game);
        this.assetManager= getGame().getAssetManager();
        this.numAssets = numAssets;
        this.stage = new LoadingStage(this, numAssets);
        this.texBG = assetManager.get(AssetName.TEX_LOBBYLIST_SCREEN);
    }

    @Override
    public void show() {
        this.batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        // render "unused" area in red
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(texBG, 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
        batch.end();

        this.stage.setProgress(numAssets - assetManager.getQueuedAssets());
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // currently unused
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
        stage.dispose();
        batch.dispose();
        texBG = null;
    }
}
