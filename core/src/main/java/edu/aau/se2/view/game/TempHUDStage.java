package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.model.Database;

public class TempHUDStage extends Stage {
    private PhaseDisplay phaseDisplay;
    private AssetManager assetManager;
    private OnHUDInteractionListener hudInteractionListener;

    public TempHUDStage(Viewport vp, AssetManager assetManager, OnHUDInteractionListener l) {
        super(vp);
        this.assetManager = assetManager;
        this.hudInteractionListener = l;
        setupPhaseDisplay();
    }

    private void setupPhaseDisplay() {
        this.phaseDisplay = new PhaseDisplay(assetManager);
        this.addActor(phaseDisplay);
        phaseDisplay.setWidth(Gdx.graphics.getWidth());
        phaseDisplay.setHeight(Gdx.graphics.getHeight()/7f);
        phaseDisplay.setOrigin(Align.center);
    }

    public void setPhase(Database.Phase phase) {
        phaseDisplay.setPhase(phase);
        if (phase == Database.Phase.ATTACKING) {
            phaseDisplay.setSkipButtonVisible(true);
            phaseDisplay.setSkipButtonListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    hudInteractionListener.stageSkipButtonClicked();
                }
            });
        }
        else {
            phaseDisplay.setSkipButtonVisible(false);
        }
    }
}
