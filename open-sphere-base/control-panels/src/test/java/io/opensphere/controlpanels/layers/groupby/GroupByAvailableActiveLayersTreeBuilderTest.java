package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link GroupByAvailableActiveLayersTreeBuilder}.
 */
public class GroupByAvailableActiveLayersTreeBuilderTest
{
    /**
     * Verifies the group by name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupByAvailableActiveLayersTreeBuilder builder = new GroupByAvailableActiveLayersTreeBuilder();
        assertEquals("Active", builder.getGroupByName());
    }
}
