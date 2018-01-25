package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link GroupByActiveZOrderTreeBuilder}.
 */
public class GroupByActiveZOrderTreeBuilderTest
{
    /**
     * Verifies the group by name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupByActiveZOrderTreeBuilder builder = new GroupByActiveZOrderTreeBuilder();
        assertEquals("Z-Order", builder.getGroupByName());
    }
}
