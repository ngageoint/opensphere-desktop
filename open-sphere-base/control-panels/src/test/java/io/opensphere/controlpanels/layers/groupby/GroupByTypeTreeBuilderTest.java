package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link GroupByTypeTreeBuilder}.
 */
public class GroupByTypeTreeBuilderTest
{
    /**
     * Verifies the group name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupByTypeTreeBuilder builder = new GroupByTypeTreeBuilder();
        assertEquals("Type", builder.getGroupByName());
    }
}
