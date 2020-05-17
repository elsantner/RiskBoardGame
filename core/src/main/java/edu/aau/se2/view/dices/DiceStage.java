package edu.aau.se2.view.dices;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;
import edu.aau.se2.view.game.Territory;

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

    public void showResults(List<Integer> results, boolean fromAttacker) {
        if (fromAttacker) {
            outerTable.clearChildren();
        }

        TextureRegion[] textures = fromAttacker ? attackerDiceTextures : defenderDiceTextures;

        for(Integer r : results) {
            Image dice = new Image(textures[r-1]);
            dice.addAction(new DiceAnimationAction(DICE_ANIMATION_SECONDS, textures, r));
            outerTable.add(dice).pad(20);
        }
        outerTable.row();
    }

    private void loadAssets() {
        AssetManager am = getScreen().getGame().getAssetManager();

        Texture dicesTexture = am.get(AssetName.TEX_DICE_ATTACKER);
        for (int i = 0; i < 6; i++) {
            attackerDiceTextures[i] = new TextureRegion(dicesTexture, i*DICE_TEXTURE_WIDTH, 0, DICE_TEXTURE_WIDTH, DICE_TEXTURE_HEIGHT);
        }

        Texture dDicesTexture = am.get(AssetName.TEX_DICE_DEFENDER);
        for (int i = 0; i < 6; i++) {
            defenderDiceTextures[i] = new TextureRegion(dDicesTexture, i*DICE_TEXTURE_WIDTH, 0, DICE_TEXTURE_WIDTH, DICE_TEXTURE_HEIGHT);
        }
    }

    public void hide() {
        outerTable.clearChildren();
    }
}
