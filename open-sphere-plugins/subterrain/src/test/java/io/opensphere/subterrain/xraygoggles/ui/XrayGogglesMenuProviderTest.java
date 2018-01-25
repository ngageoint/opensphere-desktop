package io.opensphere.subterrain.xraygoggles.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for the {@link XrayGogglesMenuProvider}.
 */
public class XrayGogglesMenuProviderTest
{
    /**
     * Tests the menu provider.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        JMenu viewMenu = new JMenu("View");
        UIRegistry uiRegistry = createUIRegistry(support, viewMenu);
        ScreenViewer viewer = createViewer(support);
        MapManager mapManager = createMapManager(support, viewer);
        GeometryRegistry geomRegistry = support.createNiceMock(GeometryRegistry.class);
        Toolbox toolbox = createToolbox(support, uiRegistry, mapManager, geomRegistry);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        XrayGogglesMenuProvider provider = new XrayGogglesMenuProvider(toolbox, model);
        EventQueueUtilities.runOnEDTAndWait(() ->
        {
        });
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)viewMenu.getItem(0);
        assertEquals("Underground", menuItem.getText());

        assertNull(model.getLowerLeft());

        menuItem.doClick();

        assertNotNull(model.getLowerLeft());

        menuItem.doClick();

        assertNull(model.getLowerLeft());

        provider.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ControlRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link ControlRegistry}.
     */
    private ControlRegistry createControlRegistry(EasyMockSupport support)
    {
        ControlContext glui = support.createNiceMock(ControlContext.class);
        ControlContext globe = support.createNiceMock(ControlContext.class);

        ControlRegistry controlRegistry = support.createMock(ControlRegistry.class);
        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT)).andReturn(glui).times(2);
        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)).andReturn(globe).times(4);

        return controlRegistry;
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @param viewer A mocked {@link ScreenViewer} to return.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support, ScreenViewer viewer)
    {
        DynamicViewer standardViewer = support.createNiceMock(DynamicViewer.class);
        ViewChangeSupport changeSupport = new ViewChangeSupport();

        MapManager mapManager = support.createNiceMock(MapManager.class);
        EasyMock.expect(mapManager.getViewChangeSupport()).andReturn(changeSupport).atLeastOnce();
        EasyMock.expect(mapManager.getScreenViewer()).andReturn(viewer);
        EasyMock.expect(mapManager.getStandardViewer()).andReturn(standardViewer);

        return mapManager;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param uiRegistry The mocked {@link UIRegistry} to return.
     * @param mapManager The mocked {@link MapManager} to return.
     * @param geomRegistry The mocked {@link GeometryRegistry} to return.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support, UIRegistry uiRegistry, MapManager mapManager,
            GeometryRegistry geomRegistry)
    {
        ControlRegistry controlRegistry = createControlRegistry(support);

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry);
        EasyMock.expect(toolbox.getMapManager()).andReturn(mapManager);
        EasyMock.expect(toolbox.getGeometryRegistry()).andReturn(geomRegistry);
        EasyMock.expect(toolbox.getControlRegistry()).andReturn(controlRegistry);

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link UIRegistry}.
     *
     * @param support Used to create the mock.
     * @param viewMenu The test view menu.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support, JMenu viewMenu)
    {
        MenuBarRegistry menuBarRegistry = support.createMock(MenuBarRegistry.class);
        EasyMock.expect(menuBarRegistry.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.VIEW_MENU)).andReturn(viewMenu);

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(menuBarRegistry);

        return uiRegistry;
    }

    /**
     * Creates an easy mocked {@link ScreenViewer}.
     *
     * @param support Used to create the mock.
     * @return The mocked screen viewer.
     */
    private ScreenViewer createViewer(EasyMockSupport support)
    {
        ScreenViewer viewer = support.createMock(ScreenViewer.class);

        EasyMock.expect(Integer.valueOf(viewer.getViewportHeight())).andReturn(Integer.valueOf(1080));
        EasyMock.expect(Integer.valueOf(viewer.getViewportWidth())).andReturn(Integer.valueOf(1920));

        return viewer;
    }
}
