package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link GroupBySourceTreeBuilder}.
 */
public class GroupBySourceTreeBuilderTest
{
    /**
     * Verifies its group by name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupBySourceTreeBuilder builder = new GroupBySourceTreeBuilder();
        assertEquals("Source", builder.getGroupByName());
    }
}
