package edu.aau.se2.view.dices;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.concurrent.ThreadLocalRandom;

public class DiceAnimationAction extends TemporalAction {

    private TextureRegion[] diceTextures;
    private int endResult;
    private int index;

    public DiceAnimationAction(float duration, TextureRegion[] diceTextures, int endResult) {
        super(duration);
        this.diceTextures = diceTextures;
        this.endResult = endResult;
    }

    @Override
    protected void update(float percent) {
        index = ThreadLocalRandom.current().nextInt(0, 5 + 1);

        if (percent == 1) {
            index = endResult - 1;
        }

        Image dice = (Image)target;

        dice.setDrawable(new TextureRegionDrawable(diceTextures[index]));
    }

    public int getIndex() {
        return index;
    }
}
