package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link GroupByAvailableRecentUpdatesTreeBuilder}.
 */
public class GroupByAvailableRecentUpdatesTreeBuilderTest
{
    /**
     * Verifies the group by name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupByAvailableRecentUpdatesTreeBuilder builder = new GroupByAvailableRecentUpdatesTreeBuilder();
        assertEquals("Recent Updates", builder.getGroupByName());
    }
}
