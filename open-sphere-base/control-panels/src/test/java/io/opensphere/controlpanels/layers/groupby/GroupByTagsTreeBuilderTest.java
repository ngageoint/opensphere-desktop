package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link GroupByTagsTreeBuilder}.
 *
 */
public class GroupByTagsTreeBuilderTest
{
    /**
     * Tests the group by name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupByTagsTreeBuilder builder = new GroupByTagsTreeBuilder();
        assertEquals("Tag", builder.getGroupByName());
    }
}
