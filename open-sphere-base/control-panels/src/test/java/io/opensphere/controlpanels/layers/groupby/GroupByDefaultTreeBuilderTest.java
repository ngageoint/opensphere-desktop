package io.opensphere.controlpanels.layers.groupby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;

/**
 * Unit test for {@link GroupByDefaultTreeBuilder}.
 */
public class GroupByDefaultTreeBuilderTest
{
    /**
     * Tests the group by name.
     */
    @Test
    public void testGetGroupByName()
    {
        GroupByDefaultTreeBuilder builder = new GroupByDefaultTreeBuilder();

        assertEquals("Source", builder.getGroupByName());
    }

    /**
     * Tests initializing for active.
     */
    @Test
    public void testInitializeForActive()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        GroupByDefaultTreeBuilder builder = new GroupByDefaultTreeBuilder();
        builder.initializeForActive(toolbox);

        assertEquals(toolbox, builder.getToolbox());
        assertTrue(builder.isActiveGroupsOnly());
        assertTrue(builder.isSubNodesForMultiMemberGroups());

        support.verifyAll();
    }

    /**
     * Tests initializing for available.
     */
    @Test
    public void testInitializeForAvailable()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        GroupByDefaultTreeBuilder builder = new GroupByDefaultTreeBuilder();
        builder.initializeForAvailable(toolbox);

        assertEquals(toolbox, builder.getToolbox());
        assertFalse(builder.isActiveGroupsOnly());
        assertFalse(builder.isSubNodesForMultiMemberGroups());

        support.verifyAll();
    }
}
