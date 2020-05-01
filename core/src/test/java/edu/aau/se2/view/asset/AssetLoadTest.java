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
        assertTrue(Gdx.files.internal(AssetName.PHASE_DISPLAY_BG).exists());
        assertTrue(Gdx.files.internal(AssetName.UI_SKIN_1).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBY_2).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBY_LINE).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBY_OVERLAY).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBY_SCREEN).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBYLIST_2).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBYLIST_LINE).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBYLIST_OVERLAY).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOBBYLIST_SCREEN).exists());
        assertTrue(Gdx.files.internal(AssetName.TEX_LOGO).exists());
        assertTrue(Gdx.files.internal(AssetName.UI_SKIN_1).exists());
        assertTrue(Gdx.files.internal(AssetName.UI_SKIN_2).exists());
    }
}