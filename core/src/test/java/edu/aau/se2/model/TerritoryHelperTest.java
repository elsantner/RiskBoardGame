package edu.aau.se2.model;

import org.junit.Test;
import java.util.Arrays;
import static edu.aau.se2.model.TerritoryHelper.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerritoryHelperTest {
    @Test
    public void testNeighboringTerritories() {
        Integer[] neighboursActual = TerritoryHelper.getNeighbouringTerritories(ID.China);
        Integer[] neighboursExpected = {ID.SoutheastAsia, ID.India, ID.Afghanistan, ID.Ural, ID.Siberia, ID.Mongolia};
        assertEquals(neighboursExpected.length, neighboursActual.length);
        assertTrue(Arrays.asList(neighboursExpected).containsAll(Arrays.asList(neighboursActual)));
        assertTrue(Arrays.asList(neighboursActual).containsAll(Arrays.asList(neighboursExpected)));

        for (Integer tid: neighboursExpected) {
            assertTrue(TerritoryHelper.areNeighbouring(tid, ID.China));
        }
        assertFalse(TerritoryHelper.areNeighbouring(ID.Brazil, ID.Afghanistan));
    }
}
