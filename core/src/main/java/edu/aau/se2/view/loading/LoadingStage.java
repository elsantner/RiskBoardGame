package edu.aau.se2.view.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;

public class LoadingStage extends AbstractStage {
    private Texture texLogo;
    private ProgressBar progressBar;
    private int numAssets;

    public LoadingStage(AbstractScreen screen, int numAssets) {
        super(screen);
        this.numAssets = numAssets;
        getAssets();
        setupDisplay();
    }

    private void getAssets() {
        this.texLogo = getScreen().getGame().getAssetManager().get(AssetName.TEX_LOGO);
    }

    private void setupDisplay() {
        Image imgLogo = new Image(texLogo);
        imgLogo.setWidth(Gdx.graphics.getWidth() / 3f);

        progressBar = new ProgressBar(0, numAssets, 1, false,
                (Skin)getScreen().getGame().getAssetManager().get(AssetName.UI_SKIN_4));

        Table table = new Table();
        table.setFillParent(true);
        table.add(imgLogo);
        table.row();
        table.add(progressBar)
                .width(Gdx.graphics.getWidth() / 1.5f)
                .height(Gdx.graphics.getHeight() / 6f)
                .pad(Gdx.graphics.getWidth() / 20f)
                .row();
        this.addActor(table);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.texLogo = null;
    }

    public void setProgress(int loadedAssets) {
        this.progressBar.setValue(loadedAssets);
    }
}
