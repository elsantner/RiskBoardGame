package edu.aau.se2.view.dices;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import edu.aau.se2.model.Database;
import edu.aau.se2.server.data.Territory;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;

/**
 * Example use:
 *
 * List<Integer> results = DiceStage.rollDice(true);
 * DiceStage diceStage = new DiceStage(new FitViewport(width, height), this, results, true);
 * db.sendAttackerResults(results, false);
 *
 * Render:
 * diceStage.act(delta);
 * diceStage.draw();
 */
public class DiceStage extends AbstractStage {

    private static final int ATTACKER_DICE_COUNT = 3;
    private static final int DEFENDER_DICE_COUNT = 2;

    private static final int DICE_TEXTURE_WIDTH = 128;
    private static final int DICE_TEXTURE_HEIGHT = 128;

    private static final int DICE_ANIMATION_SECONDS = 8;

    private TextureRegion[] diceTextures = new TextureRegion[6];
    private Skin btnSkin;

    private List<Integer> results;

    private boolean attacker;

    private Database db;

    public DiceStage(AbstractScreen screen, List<Integer> results, boolean attacker) {
        super(screen);
        this.attacker = attacker;
        this.results = results;
        this.db = Database.getInstance();
        loadAssets();
        //setup();
    }

    public DiceStage(Viewport viewport, AbstractScreen screen, List<Integer> results, boolean attacker) {
        super(viewport, screen);
        this.attacker = attacker;
        this.results = results;
        this.db = Database.getInstance();
        loadAssets();
        //setup();
    }

    public DiceStage(Viewport viewport, Batch batch, AbstractScreen screen, List<Integer> results, boolean attacker) {
        super(viewport, batch, screen);
        this.attacker = attacker;
        this.results = results;
        this.db = Database.getInstance();
        loadAssets();
        //setup();
    }

    public static List<Integer> rollDice(boolean attacker) {
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < (attacker ? ATTACKER_DICE_COUNT : DEFENDER_DICE_COUNT); i++) {
            results.add(ThreadLocalRandom.current().nextInt(1, 6 + 1));
        }
        return results;
    }

    public void setPhase(Database.Phase phase) {

        if (phase == Database.Phase.ATTACKING && db.isThisPlayersTurn()) {
            // showing attacker view
            if (db.getAttack() != null && db.getAttack().getAttackerDiceCount() == -1) {
                clear(); // remove all actors
                Territory fromTerritory = db.getTerritoryByID(db.getAttack().getFromTerritoryID());
                int maxDiceCount = fromTerritory.getArmyCount() - 1;

                Table outerTable = new Table();
                outerTable.setWidth(getWidth());
                outerTable.setHeight(getHeight());
                outerTable.pad(120f);

                for (int i = 0; i < maxDiceCount; i++) {
                    TextButton button = new TextButton((i+1) + "", btnSkin);
                    button.addListener(new ClickListener() {
                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            results = DiceStage.rollDice(true);
                            clear();
                            setup();
                            db.sendAttackerResults(results, false);
                            return true;
                        }
                    });
                    outerTable.add(button).pad(20);
                }

                addActor(outerTable);
            }
        } else if (phase == Database.Phase.ATTACKING && db.isThisPlayerDefender()) {
            // show defender view
            // TODO ask for dice count
            // TODO roll dice
            // TODO send to server
        } else if (phase == Database.Phase.ATTACKING) {
            // show results for other players
            // TODO animate dice until we receive result messages
        }
    }

    private void setup() {

        Table outerTable = new Table();
        outerTable.setWidth(getWidth());
        outerTable.setHeight(getHeight());
        outerTable.pad(120f);

        for(Integer r : results) {
            Image dice = new Image(diceTextures[r-1]);
            dice.addAction(new DiceAnimationAction(DICE_ANIMATION_SECONDS, diceTextures, r));
            outerTable.add(dice).pad(20);
        }

        addActor(outerTable);
    }

    private void loadAssets() {
        AssetManager am = getScreen().getGame().getAssetManager();

        Texture dicesTexture = am.get(attacker ? AssetName.TEX_DICE_ATTACKER : AssetName.TEX_DICE_DEFENDER);
        for (int i = 0; i < 6; i++) {
            diceTextures[i] = new TextureRegion(dicesTexture, i*DICE_TEXTURE_WIDTH, 0, DICE_TEXTURE_WIDTH, DICE_TEXTURE_HEIGHT);
        }

        btnSkin = am.get(AssetName.UI_SKIN_1);
    }
}
