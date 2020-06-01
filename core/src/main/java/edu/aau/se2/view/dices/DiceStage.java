package edu.aau.se2.view.dices;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import edu.aau.se2.model.Database;
import edu.aau.se2.sensor.ShakeDetector;
import edu.aau.se2.server.data.Attack;
import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.dices.DiceAnimationAction;

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

    private static final int DICE_TEXTURE_WIDTH = 128;
    private static final int DICE_TEXTURE_HEIGHT = 128;

    private static final int DICE_ANIMATION_SECONDS = 2;

    private TextureRegion[] attackerDiceTextures = new TextureRegion[6];
    private TextureRegion[] defenderDiceTextures = new TextureRegion[6];

    private Table outerTable;

    private List<Integer> attackerDiceResults;
    private boolean cheated = false;

    public DiceStage(AbstractScreen screen) {
        super(screen);
        loadAssets();
        setup();
    }

    public DiceStage(Viewport viewport, AbstractScreen screen) {
        super(viewport, screen);
        loadAssets();
        setup();
    }

    public DiceStage(Viewport viewport, Batch batch, AbstractScreen screen) {
        super(viewport, batch, screen);
        loadAssets();
        setup();
    }

    public static List<Integer> rollDice(int diceCount) {
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < diceCount; i++) {
            results.add(ThreadLocalRandom.current().nextInt(1, 6 + 1));
        }
        return results;
    }

    private void setup() {
        outerTable = new Table();
        outerTable.setWidth(getWidth());
        outerTable.setHeight(getHeight());
        outerTable.pad(120f);
        addActor(outerTable);
    }

    public void playAttackerDiceAnimation(int diceCount, boolean isThisPlayerAttacker) {
        // generate initial results (can be changed by cheating)
        //attackerDiceResults = attackerDiceResults(diceCount);

        // play dicing animation
        outerTable.clearChildren();
        TextureRegion[] textures = attackerDiceTextures;
        for (int i = 0; i < diceCount; i++) {
            Image dice = new Image(textures[0]);
            dice.addAction(new DiceAnimationAction(DICE_ANIMATION_SECONDS, textures, 1));
            outerTable.add(dice).pad(20);
        }
        outerTable.row();

        // send results to server after animation is done
        if (isThisPlayerAttacker) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    //Database.getInstance().sendAttackerResults(attackerDiceResults, cheated);
                    attackerDiceResults(diceCount);
                }
            }, DICE_ANIMATION_SECONDS);
        }
    }

    public void showAttackerResults(List<Integer> results) {
        // show actual results
        outerTable.clearChildren();
        TextureRegion[] textures = attackerDiceTextures;

        for (Integer r : results) {
            Image dice = new Image(textures[r - 1]);
            outerTable.add(dice).pad(20);
        }
        outerTable.row();
    }

    public void showDefenderResults(List<Integer> results, boolean playAnimation) {
        TextureRegion[] textures = defenderDiceTextures;

        for (Integer r : results) {
            Image dice = new Image(textures[r - 1]);
            if (playAnimation) {
                dice.addAction(new DiceAnimationAction(DICE_ANIMATION_SECONDS, textures, r));
            }
            outerTable.add(dice).pad(20);
        }
        outerTable.row();
    }

    public static List<Integer> shakingresults(int diceCount) {
        Random random = new Random();
        ShakeDetector sk = new ShakeDetector();
        List<Integer> results = new ArrayList<>();
        //if the phone is shaking generate a random number: 0, 1, 2
        if (sk.isShaking()) {
            sk.vibrate();
            for (int i = 0; i < diceCount; i++) {
                results.add(random.nextInt(3) + 1);
            }
        }
        return results;
    }

    public static List<Integer> attackerDiceResults(int diceCount) {
        Attack a = Database.getInstance().getLobby().getCurrentAttack();
        List<Integer> totalResults = new ArrayList<>();
        List<Integer> resultsRollDice = DiceStage.rollDice(a.getAttackerDiceCount());
        List<Integer> resultsShaking = DiceStage.shakingresults(a.getAttackerDiceCount());

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                for (int i = 0; i < diceCount; i++) {
                    totalResults.add(resultsRollDice.get(i) + resultsShaking.get(i));
                }
            }
        }, DICE_ANIMATION_SECONDS);
        Database.getInstance().sendAttackerResults(totalResults, false);
        //showAttackerResults(totalResults);
        return totalResults;
    }

    private void loadAssets() {
        AssetManager am = getScreen().getGame().getAssetManager();

        Texture dicesTexture = am.get(AssetName.TEX_DICE_ATTACKER);
        for (int i = 0; i < 6; i++) {
            attackerDiceTextures[i] = new TextureRegion(dicesTexture, i * DICE_TEXTURE_WIDTH, 0, DICE_TEXTURE_WIDTH, DICE_TEXTURE_HEIGHT);
        }

        Texture dDicesTexture = am.get(AssetName.TEX_DICE_DEFENDER);
        for (int i = 0; i < 6; i++) {
            defenderDiceTextures[i] = new TextureRegion(dDicesTexture, i * DICE_TEXTURE_WIDTH, 0, DICE_TEXTURE_WIDTH, DICE_TEXTURE_HEIGHT);
        }
    }

    public void hide() {
        outerTable.clearChildren();
    }

    public void reset() {
        attackerDiceResults = null;
        cheated = false;
    }
}