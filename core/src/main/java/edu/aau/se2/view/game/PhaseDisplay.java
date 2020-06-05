package edu.aau.se2.view.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.asset.AssetName;

public class PhaseDisplay extends Group {

    private Database.Phase phase;
    private AssetManager assetManager;
    private BitmapFont font;
    private Table table;
    private Viewport viewport;
    private Label label;
    private ImageButton buttonSkipStage;


    public PhaseDisplay(AssetManager assetManager, Viewport viewport) {
        this.assetManager = assetManager;
        this.viewport = viewport;
        phase = Database.Phase.NONE;

        if (!assetManager.isFinished()) {
            assetManager.finishLoading();
        }
        font = assetManager.get(AssetName.FONT_1);
        setupTable();
        setSkipButtonVisible(false);
    }

    private void setupTable() {
        label = new Label(getPhaseName(this.phase), new Label.LabelStyle(font, new Color(0.6f, 0, 0, 1)));
        label.setOrigin(Align.center);
        buttonSkipStage = new ImageButton(new TextureRegionDrawable(new TextureRegion((Texture) assetManager.get(AssetName.END_TURN))));
        buttonSkipStage.getImage().setFillParent(true);
        buttonSkipStage.left().bottom();

        table = new Table();
        table.setFillParent(true);
        // Table: [empty cell expandX] [cell with PhaseName centered] [empty cell expandX] [End-Turn Button]
        table.add().expandX();
        table.add(label).minHeight(font.getLineHeight()).center().padLeft(viewport.getWorldWidth() * 0.077f).bottom().padBottom(viewport.getWorldHeight() * 0.012f);
        table.add().expandX();
        table.add(buttonSkipStage).size(viewport.getWorldHeight() * 0.132f).right()
                .padRight(viewport.getWorldWidth() * 0.01f).bottom().padBottom(viewport.getWorldHeight() * 0.01f);
        table.row();


        this.addActor(table);
    }

    public void setPhase(Database.Phase phase) {
        this.phase = phase;
        this.label.setText(getPhaseName(phase));
    }

    public void setSkipButtonVisible(boolean visible) {
        buttonSkipStage.setVisible(visible);
    }

    public void setSkipButtonListener(ClickListener listener) {
        buttonSkipStage.addListener(listener);
    }

    @Override
    public void setOrigin(int alignment) {
        super.setOrigin(alignment);
        table.setOrigin(alignment);
    }

    private String getPhaseName(Database.Phase phase) {
        switch (phase) {
            case ATTACKING:
                return "Angreifen";
            case MOVING:
                return "Verschieben";
            case PLACING:
                return "Platzieren";
            default:
                return "Spielbeginn";
        }
    }
}
