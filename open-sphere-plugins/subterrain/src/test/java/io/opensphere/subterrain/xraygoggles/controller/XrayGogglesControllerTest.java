package io.opensphere.subterrain.xraygoggles.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for the {@link XrayGogglesController} class.
 */
public class XrayGogglesControllerTest
{
    /**
     * Verifies the xray goggles controller.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        ScreenViewer viewer = createViewer(support);
        MapManager mapManager = createMapManager(support, viewer);
        GeometryRegistry geometryRegistry = createGeometryRegistry(support);
        XrayGogglesModel model = new XrayGogglesModel();
        ControlRegistry controlRegistry = createControlRegistry(support);

        support.replayAll();

        XrayGogglesController controller = new XrayGogglesController(mapManager, geometryRegistry, controlRegistry, model);

        assertNotNull(model.getUpperLeftGeo());

        controller.close();

        assertNull(model.getUpperLeft());

        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.VIEW_CHANGE);

        assertNull(model.getUpperRight());

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
     * Creates an easy mocked {@link GeometryRegistry}.
     *
     * @param support Used to create the mock.
     * @return The mocked registry.
     */
    @SuppressWarnings("unchecked")
    private GeometryRegistry createGeometryRegistry(EasyMockSupport support)
    {
        GeometryRegistry geometryRegistry = support.createMock(GeometryRegistry.class);

        EasyMock.expect(geometryRegistry.getGeometries()).andReturn(New.list()).atLeastOnce();
        geometryRegistry.addSubscriber(EasyMock.isA(GenericSubscriber.class));
        geometryRegistry.removeSubscriber(EasyMock.isA(GenericSubscriber.class));

        return geometryRegistry;
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

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.getViewChangeSupport()).andReturn(changeSupport).atLeastOnce();
        EasyMock.expect(mapManager.getScreenViewer()).andReturn(viewer);
        EasyMock.expect(mapManager.convertToPosition(EasyMock.isA(Vector2i.class), EasyMock.eq(ReferenceLevel.TERRAIN)))
                .andAnswer(() ->
                {
                    Vector2i vector = (Vector2i)EasyMock.getCurrentArguments()[0];
                    return new GeographicPosition(LatLonAlt.createFromDegrees(vector.getX(), vector.getY()));
                }).atLeastOnce();
        EasyMock.expect(mapManager.getStandardViewer()).andReturn(standardViewer).atLeastOnce();

        return mapManager;
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
