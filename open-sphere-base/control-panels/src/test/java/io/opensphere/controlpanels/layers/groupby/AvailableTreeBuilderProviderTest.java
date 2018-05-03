package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link AvailableTreeBuilderProvider}.
 */
public class AvailableTreeBuilderProviderTest
{
    /**
     * Tests getting the builders.
     */
    @Test
    public void testGetBuilder()
    {
        AvailableTreeBuilderProvider provider = new AvailableTreeBuilderProvider();

        assertTrue(provider.getBuilder("Active") instanceof GroupByAvailableActiveLayersTreeBuilder);
        assertTrue(provider.getBuilder("ACTIVE_LAYERS") instanceof GroupByAvailableActiveLayersTreeBuilder);
        assertTrue(provider.getBuilder("Recent Updates") instanceof GroupByAvailableRecentUpdatesTreeBuilder);
        assertTrue(provider.getBuilder("RECENT_UPDATES") instanceof GroupByAvailableRecentUpdatesTreeBuilder);
        assertTrue(provider.getBuilder("Source") instanceof GroupBySourceTreeBuilder);
        assertTrue(provider.getBuilder("Tag") instanceof GroupByTagsTreeBuilder);
        assertTrue(provider.getBuilder("Type") instanceof GroupByTypeTreeBuilder);

        assertTrue(provider.getBuilder("Active".toUpperCase()) instanceof GroupByAvailableActiveLayersTreeBuilder);
        assertTrue(provider.getBuilder("Recent Updates".toUpperCase()) instanceof GroupByAvailableRecentUpdatesTreeBuilder);
        assertTrue(provider.getBuilder("Source".toUpperCase()) instanceof GroupBySourceTreeBuilder);
        assertTrue(provider.getBuilder("Tag".toUpperCase()) instanceof GroupByTagsTreeBuilder);
        assertTrue(provider.getBuilder("Type".toUpperCase()) instanceof GroupByTypeTreeBuilder);
        assertNotNull(provider.getBuilder("blah"));
    }

    /**
     * Tests getting the group by types.
     */
    @Test
    public void testGetGroupByTypes()
    {
        AvailableTreeBuilderProvider provider = new AvailableTreeBuilderProvider();
        List<String> groupBys = provider.getGroupByTypes();

        assertEquals(5, groupBys.size());
        assertEquals("Active", groupBys.get(0));
        assertEquals("Recent Updates", groupBys.get(1));
        assertEquals("Source", groupBys.get(2));
        assertEquals("Tag", groupBys.get(3));
        assertEquals("Type", groupBys.get(4));
    }
}
