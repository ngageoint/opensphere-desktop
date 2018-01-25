package io.opensphere.geopackage.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * Unit test for {@link GeoPackageQueryTracker}.
 */
public class GeoPackageQueryTrackerTest
{
    /**
     * The added task activity.
     */
    private TaskActivity myTaskActivity;

    /**
     * Tests the query tracker.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        UIRegistry uiRegistry = createUIRegistry(support);

        support.replayAll();

        String layerName = "imagery";

        GeoPackageQueryTracker tracker = new GeoPackageQueryTracker(uiRegistry, layerName);

        tracker.upCounter();
        assertTrue(myTaskActivity.isActive());
        assertEquals(layerName + " Tile Queries 1", myTaskActivity.getLabelValue());

        tracker.upCounter();
        assertEquals(layerName + " Tile Queries 2", myTaskActivity.getLabelValue());

        tracker.subtractCounter();
        assertEquals(layerName + " Tile Queries 1", myTaskActivity.getLabelValue());

        tracker.subtractCounter();
        assertTrue(myTaskActivity.isComplete());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support)
    {
        MenuBarRegistry menuRegistry = support.createMock(MenuBarRegistry.class);

        menuRegistry.addTaskActivity(EasyMock.isA(TaskActivity.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myTaskActivity = (TaskActivity)EasyMock.getCurrentArguments()[0];
            return null;
        });

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(menuRegistry).atLeastOnce();

        return uiRegistry;
    }
}
