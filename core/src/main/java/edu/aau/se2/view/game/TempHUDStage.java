package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.Viewport;

import edu.aau.se2.model.Database;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;

public class TempHUDStage extends AbstractStage {
    private PhaseDisplay phaseDisplay;
    private OnHUDInteractionListener hudInteractionListener;
    private Database db;
    private AttackDisplay attackDisplay;

    public TempHUDStage(AbstractScreen screen, Viewport vp, OnHUDInteractionListener l) {
        super(vp, screen);
        this.db = Database.getInstance();
        this.hudInteractionListener = l;
        setupPhaseDisplay();
        setupAttackDisplay();
        //updateAttackDisplay("Player1", "Player2", "Ost-Australien", "West-Australien", 3);
    }

    public void setCurrentAttack(Attack attack) {
        if (attack != null) {
            String attackerName = db.getPlayerByTerritoryID(attack.getFromTerritoryID()).getNickname();
            String defenderName = db.getPlayerByTerritoryID(attack.getToTerritoryID()).getNickname();
            String fromTerritoryName = Territory.getByID(attack.getFromTerritoryID()).getTerritoryName();
            String toTerritoryName = Territory.getByID(attack.getToTerritoryID()).getTerritoryName();
            updateAttackDisplay(attackerName, defenderName, fromTerritoryName, toTerritoryName, attack.getAttackerDiceCount());
            attackDisplay.setVisible(true);
        }
        else {
            // hide attack display 3 seconds later
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    // if no new attack has been started during wait
                    if (db.getAttack() == null) {
                        attackDisplay.setVisible(false);
                    }
                }
            }, 3);
        }
    }

    private void setupAttackDisplay() {
        attackDisplay = new AttackDisplay(getScreen().getGame().getAssetManager());
        attackDisplay.setWidth(Gdx.graphics.getWidth());
        attackDisplay.setHeight(Gdx.graphics.getHeight() * 0.25f);
        attackDisplay.setY(Gdx.graphics.getHeight() * 0.75f);
        this.addActor(attackDisplay);
        attackDisplay.setOrigin(Align.center);
        attackDisplay.setVisible(false);
    }

    private void updateAttackDisplay(String attacker, String defender, String fromTerritory, String toTerritory, int armyCount) {
        attackDisplay.updateData(attacker, defender, fromTerritory, toTerritory, armyCount);
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

    public void setPhaseSkipable(boolean b) {
        if (db.isThisPlayersTurn()) {
            phaseDisplay.setSkipButtonVisible(b);
        }
    }
}
