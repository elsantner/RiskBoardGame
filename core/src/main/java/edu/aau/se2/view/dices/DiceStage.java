package edu.aau.se2.view.dices;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import edu.aau.se2.view.AbstractScreen;
import edu.aau.se2.view.AbstractStage;
import edu.aau.se2.view.asset.AssetName;

public class DiceStage extends AbstractStage {

    private static final int ATTACKER_DICE_COUNT = 3;
    private static final int DEFENDER_DICE_COUNT = 2;

    private static final int DICE_TEXTURE_WIDTH = 128;
    private static final int DICE_TEXTURE_HEIGHT = 128;

    private static final int DICE_ANIMATION_SECONDS = 8;

    private TextureRegion[] diceTextures = new TextureRegion[6];

    private List<Integer> results = new ArrayList<>();

    private boolean attacker;

    public DiceStage(AbstractScreen screen, boolean attacker) {
        super(screen);
        this.attacker = attacker;
        loadAssets();
        rollDice();
        setup();
    }

    public DiceStage(Viewport viewport, AbstractScreen screen, boolean attacker) {
        super(viewport, screen);
        this.attacker = attacker;
        loadAssets();
        rollDice();
        setup();
    }

    public DiceStage(Viewport viewport, Batch batch, AbstractScreen screen, boolean attacker) {
        super(viewport, batch, screen);
        this.attacker = attacker;
        loadAssets();
        rollDice();
        setup();
    }

    private void rollDice() {
        for (int i = 0; i < (attacker ? ATTACKER_DICE_COUNT : DEFENDER_DICE_COUNT); i++) {
            results.add(ThreadLocalRandom.current().nextInt(1, 6 + 1));
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
    }
}
