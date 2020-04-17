package edu.aau.se2.server.logic;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static edu.aau.se2.server.logic.TerritoryHelper.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerritoryHelperTest {
    @Test
    public void testNeighboringTerritories() {
        List<Integer> neighboursActual = TerritoryHelper.getNeighbouringTerritories(ID.CHINA);
        Integer[] neighboursExpected = {ID.SOUTHEAST_ASIA, ID.INDIA, ID.AFGHANISTAN, ID.URAL, ID.SIBERIA, ID.MONGOLIA};
        assertEquals(neighboursExpected.length, neighboursActual.size());
        assertTrue(Arrays.asList(neighboursExpected).containsAll(neighboursActual));
        assertTrue(neighboursActual.containsAll(Arrays.asList(neighboursExpected)));

        for (Integer tid: neighboursExpected) {
            assertTrue(TerritoryHelper.areNeighbouring(tid, ID.CHINA));
        }
        assertFalse(TerritoryHelper.areNeighbouring(ID.BRAZIL, ID.AFGHANISTAN));
    }

    @Test
    public void testContinentTerritories() {
        List<Integer> territoriesActual = TerritoryHelper.getTerritoriesOfContinent(TerritoryHelper.Continent.AUSTRALIA);
        Integer[] territoriesExpected = {ID.WESTERN_AUSTRALIA, ID.EASTERN_AUSTRALIA, ID.INDONESIA, ID.NEW_GUINEA};
        assertTrue(territoriesActual.containsAll(Arrays.asList(territoriesExpected)));
        assertTrue(Arrays.asList(territoriesExpected).containsAll(territoriesActual));
    }

    @Test
    public void testFullyIncludedContinents() {
        List<Integer> territories = Arrays.asList(ID.WESTERN_AUSTRALIA, ID.EASTERN_AUSTRALIA, ID.INDONESIA, ID.NEW_GUINEA,
                ID.SCANDINAVIA, ID.BRAZIL, ID.ALASKA, ID.CENTRAL_AFRICA, ID.ARGENTINA, ID.MADAGASCAR, ID.PERU, ID.NORTH_AFRICA,
                ID.SOUTH_AFRICA, ID.VENEZUELA);
        List<Integer> continentsActual = TerritoryHelper.getFullyIncludedContinents(territories);
        List<Integer> continentsExpected = Arrays.asList(TerritoryHelper.Continent.SOUTH_AMERICA, TerritoryHelper.Continent.AUSTRALIA);
        assertTrue(continentsActual.containsAll(continentsExpected));
        assertTrue(continentsExpected.containsAll(continentsActual));
    }
}
