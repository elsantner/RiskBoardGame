package edu.aau.se2.view.loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;

public class LoadingStage extends AbstractStage {
    private Texture texLogo;
    private ProgressBar progressBar;
    private int numAssets;
    private AssetManager assetManager;

    public LoadingStage(AbstractScreen screen, int numAssets) {
        super(screen);
        this.numAssets = numAssets;
        this.assetManager = getScreen().getGame().getAssetManager();
        getAssets();
        setupDisplay();
    }

    private void getAssets() {
        this.texLogo = assetManager.get(AssetName.TEX_LOGO);
    }

    private void setupDisplay() {
        Image imgLogo = new Image(texLogo);
        // scale image to fill half the screen width (and keep aspect ration)
        imgLogo.setScale(getViewport().getWorldWidth() / (imgLogo.getWidth() * 2));
        imgLogo.setOrigin(Align.center);
        progressBar = new ProgressBar(0, numAssets, 1, false,
                (Skin)getScreen().getGame().getAssetManager().get(AssetName.UI_SKIN_2));

        Table table = new Table();
        table.setFillParent(true);
        table.add(imgLogo).row();
        table.add(progressBar)
                .width(getViewport().getWorldWidth() / 1.5f)
                .height(getViewport().getWorldHeight() / 6f)
                .padTop(getViewport().getWorldWidth() / 10f)
                .row();
        this.addActor(table);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.texLogo = null;
        this.progressBar = null;
    }

    public void setProgress(int loadedAssets) {
        if (loadedAssets > progressBar.getValue()) {
            this.progressBar.setValue(loadedAssets);
        }
    }
}
