package edu.aau.se2.view.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.server.logic.TerritoryHelper;
import edu.aau.se2.view.GdxTestRunner;
import edu.aau.se2.view.asset.AssetName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(GdxTestRunner.class)
public class TerritoryTest {

	@Before
	public void initTerritories() {
		AssetManager assetManager = new AssetManager();
		assetManager.load(AssetName.ARMY_DISPLAY_CIRCLE, Texture.class);
		assetManager.finishLoading();
		Territory.init(2392, 1440, assetManager);
	}

	@Test
	public void testTerritoryInit() {
		Territory[] territories = Territory.getAll();
		assertEquals(41, territories[territories.length-1].getID());
	}

	@Test
	public void testGetTerritory() {
		assertEquals(12, Territory.getByID(12).getID());
		assertNull(Territory.getByPosition(0, 0));
		assertEquals(TerritoryHelper.ID.EAST_AFRICA, Territory.getByPosition(1385,615).getID());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTerritoryException() {
		Territory.getByID(-1);
	}
}
