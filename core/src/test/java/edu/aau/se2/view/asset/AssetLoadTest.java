package edu.aau.se2.view.asset;

import com.badlogic.gdx.Gdx;

import org.junit.Test;
import org.junit.runner.RunWith;
import edu.aau.se2.view.GdxTestRunner;
import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class AssetLoadTest {

    @Test
    public void testLoadAssets() {
        assertTrue(Gdx.files.internal(AssetName.RISK_BOARD).exists());
        assertTrue(Gdx.files.internal(AssetName.ARMY_DISPLAY_CIRCLE).exists());
    }
}