package io.opensphere.analysis.binning.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link CriteriaTypeFactory}.
 */
public class CriteriaTypeFactoryTest
{
    /**
     * Tests getting the available criteria types.
     */
    @Test
    public void testGetTypes()
    {
        List<String> available = CriteriaTypeFactory.getInstance().getAvailableTypes();

        assertEquals(2, available.size());

        assertEquals(UniqueCriteria.CRITERIA_TYPE, available.get(0));
        assertEquals(RangeCriteria.CRITERIA_TYPE, available.get(1));
    }

    /**
     * Tests creating criteria types, also tests returning null for unknown
     * type.
     */
    @Test
    public void testNewCriteriaType()
    {
        CriteriaType criteriaType = CriteriaTypeFactory.getInstance().newCriteriaType(UniqueCriteria.CRITERIA_TYPE);
        assertTrue(criteriaType instanceof UniqueCriteria);

        criteriaType = CriteriaTypeFactory.getInstance().newCriteriaType(RangeCriteria.CRITERIA_TYPE);
        assertTrue(criteriaType instanceof RangeCriteria);

        criteriaType = CriteriaTypeFactory.getInstance().newCriteriaType("something");
        assertNull(criteriaType);
    }
}
