package edu.aau.se2.view.dices;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.view.GdxTestRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(GdxTestRunner.class)
public class DiceAnimationActionTest {

    private TextureRegion[] diceTextures;

    @Before
    public void setUp() {
        diceTextures = new TextureRegion[6];

        for (int i = 0; i < 6; i++) {
            diceTextures[i] = mock(TextureRegion.class);
        }
    }

    @Test
    public void update() {
        int endResult = 6;

        DiceAnimationAction action = new DiceAnimationAction(2, diceTextures, endResult);
        Image dice = mock(Image.class);
        action.setTarget(dice);

        action.update(0);
        verify(dice).setDrawable(any());

        action.update(1);
        assertEquals(endResult - 1, action.getIndex());
    }
}
