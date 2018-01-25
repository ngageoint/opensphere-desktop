package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link GroupByAvailableRecentlyActiveTreeBuilder}.
 */
public class GroupByAvailableRecentlyActiveTreeBuilderTest
{
    /**
     * Verifies the group by name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupByAvailableRecentlyActiveTreeBuilder builder = new GroupByAvailableRecentlyActiveTreeBuilder();
        assertEquals("Recently Used", builder.getGroupByName());
    }
}
