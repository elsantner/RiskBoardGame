package edu.aau.se2.view.game;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.aau.se2.model.TerritoryHelper;
import edu.aau.se2.view.GdxTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(GdxTestRunner.class)
public class TerritoryTest {

	@Before
	public void initTerritories() {
		Territory.init(2392, 1440);
	}

	@Test
	public void testTerritoryInit() {
		Territory[] territories = Territory.getAll();
		assertEquals(42, territories[territories.length-1].getID());
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
