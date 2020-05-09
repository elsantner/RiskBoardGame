package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.model.Database;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;

public class TempHUDStage extends AbstractStage {
    private PhaseDisplay phaseDisplay;
    private OnHUDInteractionListener hudInteractionListener;
    private Database db;

    public TempHUDStage(AbstractScreen screen, Viewport vp, OnHUDInteractionListener l) {
        super(vp, screen);
        this.db = Database.getInstance();
        this.hudInteractionListener = l;
        setupPhaseDisplay();
        showAttackDisplay();
    }

    private void showAttackDisplay() {
        AttackDisplay attackDisplay = new AttackDisplay(getScreen().getGame().getAssetManager(),
                "Player1", "Player2", "XXX", "YYY", 1);
        this.addActor(attackDisplay);
        attackDisplay.setWidth(Gdx.graphics.getWidth());
        attackDisplay.setHeight(Gdx.graphics.getHeight()/4f);
        attackDisplay.setY(Gdx.graphics.getHeight() * 0.75f);
        attackDisplay.setOrigin(Align.center);
    }

    private void setupPhaseDisplay() {
        this.phaseDisplay = new PhaseDisplay(getScreen().getGame().getAssetManager());
        this.addActor(phaseDisplay);
        phaseDisplay.setWidth(Gdx.graphics.getWidth());
        phaseDisplay.setHeight(Gdx.graphics.getHeight()/7f);
        phaseDisplay.setOrigin(Align.center);
    }

    public void setPhase(Database.Phase phase) {
        phaseDisplay.setPhase(phase);
        if ((phase == Database.Phase.ATTACKING || phase == Database.Phase.MOVING) &&
                db.isThisPlayersTurn()) {

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
