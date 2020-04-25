package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.asset.AssetName;

public class PhaseDisplay extends Group {

    private Database.Phase phase;
    private AssetManager assetManager;
    private BitmapFont font;
    private Table table;
    private Label label;
    private TextButton buttonSkipStage;

    public PhaseDisplay(AssetManager assetManager) {
        this.assetManager = assetManager;
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
        buttonSkipStage = new TextButton("Beenden", (Skin) assetManager.get(AssetName.UI_SKIN));
        buttonSkipStage.setTransform(true);
        buttonSkipStage.scaleBy(2f);
        buttonSkipStage.setOrigin(Align.center);

        table = new Table();
        table.setFillParent(true);
        table.add(label).minHeight(font.getLineHeight());
        table.row();
        table.add(buttonSkipStage).height(buttonSkipStage.getHeight()).pad(50f);
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
    public void setOrigin (int alignment) {
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
