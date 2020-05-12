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

    private static final int DICE_ANIMATION_SECONDS = 4;

    private TextureRegion[] attackerDiceTextures = new TextureRegion[6];
    private TextureRegion[] defenderDiceTextures = new TextureRegion[6];

    private Table outerTable;

    private BitmapFont font;

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

    private BitmapFont generateFont(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/CenturyGothic.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        BitmapFont font = generator.generateFont(parameter);
        font.getData().setScale((getViewport().getWorldWidth()*1.5f) / Territory.REFERENCE_WIDTH);
        generator.dispose();
        return font;
    }

    private void loadAssets() {
        AssetManager am = getScreen().getGame().getAssetManager();

        font = generateFont();

        Texture dicesTexture = am.get(AssetName.TEX_DICE_ATTACKER);
        for (int i = 0; i < 6; i++) {
            attackerDiceTextures[i] = new TextureRegion(dicesTexture, i*DICE_TEXTURE_WIDTH, 0, DICE_TEXTURE_WIDTH, DICE_TEXTURE_HEIGHT);
        }

        Texture dDicesTexture = am.get(AssetName.TEX_DICE_DEFENDER);
        for (int i = 0; i < 6; i++) {
            defenderDiceTextures[i] = new TextureRegion(dDicesTexture, i*DICE_TEXTURE_WIDTH, 0, DICE_TEXTURE_WIDTH, DICE_TEXTURE_HEIGHT);
        }
    }

    public void showFinalResults(int armiesLostAttacker, int armiesLostDefender, boolean occupyRequired, boolean cheated) {
        outerTable.clearChildren();

        Label attackerResult = new Label("Angreifer hat " + armiesLostAttacker + " Einheiten verloren", new Label.LabelStyle(font, Color.CYAN));
        Label defenderResult = new Label("Verteidiger hat " + armiesLostDefender + " Einheiten verloren", new Label.LabelStyle(font, Color.CYAN));
        Label cheatedResult = new Label("Ergebnis war " + (cheated ? "" : "nicht") + " gecheated", new Label.LabelStyle(font, Color.CYAN));
        Label occupyResult = new Label("Territorium wurde " + (occupyRequired ? "" : "nicht") + " erobert", new Label.LabelStyle(font, Color.CYAN));

        outerTable.add(attackerResult).pad(20f);
        outerTable.row();
        outerTable.add(defenderResult).pad(20f);
        outerTable.row();
        outerTable.add(cheatedResult).pad(20f);
        outerTable.row();
        outerTable.add(occupyResult).pad(20f);
        outerTable.row();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                outerTable.clearChildren();
            }
        }, 3);
    }

    public void hide() {
        outerTable.clearChildren();
    }
}
