package edu.aau.se2.view.game;

import com.badlogic.gdx.Gdx;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class TerritoryTest {
	@Test(expected = IllegalStateException.class)
	public void testTerritoryInitException() {
		assertTrue(Territory.isNotInitialized());
		Territory.getAll();
	}

	@Test
	public void testTerritoryInit() {
		assertTrue(Territory.isNotInitialized());
		Territory.init(1920, 1080);
		Territory[] territories = Territory.getAll();
		assertEquals(42, territories[territories.length-1].getID());
	}

	@Test
	public void testGetTerritory() {
		assertFalse(Territory.isNotInitialized());
		assertEquals(12, Territory.getByID(12).getID());
		assertNull(Territory.getByPosition(0, 0));
	}
}
