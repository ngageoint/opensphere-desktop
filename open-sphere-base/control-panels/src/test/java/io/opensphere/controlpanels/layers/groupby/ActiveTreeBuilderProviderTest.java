package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link AvailableTreeBuilderProvider}.
 */
public class ActiveTreeBuilderProviderTest
{
    /**
     * Tests getting the builders.
     */
    @Test
    public void testGetBuilder()
    {
        ActiveTreeBuilderProvider provider = new ActiveTreeBuilderProvider();

        assertTrue(provider.getBuilder("Source") instanceof GroupByDefaultTreeBuilder);
        assertTrue(provider.getBuilder("Source".toUpperCase()) instanceof GroupByDefaultTreeBuilder);
        assertTrue(provider.getBuilder("Tag") instanceof GroupByTagsTreeBuilder);
        assertTrue(provider.getBuilder("Tag".toUpperCase()) instanceof GroupByTagsTreeBuilder);
        assertTrue(provider.getBuilder("Type") instanceof GroupByTypeTreeBuilder);
        assertTrue(provider.getBuilder("Type".toUpperCase()) instanceof GroupByTypeTreeBuilder);
        assertTrue(provider.getBuilder("Z-Order") instanceof GroupByActiveZOrderTreeBuilder);
        assertTrue(provider.getBuilder("Z-Order".toUpperCase()) instanceof GroupByActiveZOrderTreeBuilder);
        assertTrue(provider.getBuilder("Z_ORDER") instanceof GroupByActiveZOrderTreeBuilder);
        assertNotNull(provider.getBuilder("blah"));
    }

    /**
     * Tests getting the group by types.
     */
    @Test
    public void testGetGroupByTypes()
    {
        ActiveTreeBuilderProvider provider = new ActiveTreeBuilderProvider();
        List<String> groupBys = provider.getGroupByTypes();

        assertEquals(4, groupBys.size());
        assertEquals("Source", groupBys.get(0));
        assertEquals("Tag", groupBys.get(1));
        assertEquals("Type", groupBys.get(2));
        assertEquals("Z-Order", groupBys.get(3));
    }
}
