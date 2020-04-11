package edu.aau.se2.view.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.view.GdxTestRunner;
import edu.aau.se2.view.game.Territory;

import static org.junit.Assert.assertTrue;

@RunWith(GdxTestRunner.class)
public class AssetLoadTest {

    @Test
    public void testLoadAssets() {
        assertTrue(Gdx.files.internal(AssetName.RISK_BOARD).exists());
        assertTrue(Gdx.files.internal(AssetName.ARMY_DISPLAY_CIRCLE).exists());
        Territory.init(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
}