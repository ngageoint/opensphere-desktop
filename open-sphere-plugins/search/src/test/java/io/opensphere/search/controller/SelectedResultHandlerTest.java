package io.opensphere.search.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import io.opensphere.core.MapManager;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.search.model.SearchModel;

/**
 * Unit test for {@link SelectedResultHandler}.
 */
public class SelectedResultHandlerTest
{
    /**
     * The registered mouse listener.
     */
    private DiscreteEventListener myMouseListener;

    /**
     * The registered pick listener.
     */
    private PickListener myPickListener;

    /**
     * Tests when the user hovers over a search result that is on the globe.
     */
    @Test
    public void testGlobeHover()
    {
        EasyMockSupport support = new EasyMockSupport();

        BiMap<SearchResult, Geometry> map = createBiMap();
        BiMap<SearchResult, Geometry> labelMap = createBiMap();
        ControlRegistry controlRegistry = createControlRegistry(support);
        ViewerAnimatorCreator creator = support.createMock(ViewerAnimatorCreator.class);
        MapManager mapManager = support.createMock(MapManager.class);

        SearchModel model = new SearchModel();
        model.getAllResults().add(map.keySet().iterator().next());
        model.getShownResults().add(map.keySet().iterator().next());

        support.replayAll();

        SelectedResultHandler handler = new SelectedResultHandler(model, controlRegistry, mapManager, map, labelMap, creator);

        PickEvent event = new PickEvent(map.entrySet().iterator().next().getValue(), null);
        myPickListener.handlePickEvent(event);

        assertEquals(model.getShownResults().get(0), model.getHoveredResult().get());

        event = new PickEvent(null, null);
        myPickListener.handlePickEvent(event);

        assertNull(model.getHoveredResult().get());

        handler.close();

        assertNull(myPickListener);
        assertNull(myMouseListener);

        support.verifyAll();
    }

    /**
     * Tests when the user selects a search result that is on the globe.
     */
    @Test
    public void testGlobeSelect()
    {
        EasyMockSupport support = new EasyMockSupport();

        BiMap<SearchResult, Geometry> map = createBiMap();
        BiMap<SearchResult, Geometry> labelMap = createBiMap();
        ControlRegistry controlRegistry = createControlRegistry(support);
        ViewerAnimatorCreator creator = support.createMock(ViewerAnimatorCreator.class);
        MapManager mapManager = support.createMock(MapManager.class);

        SearchModel model = new SearchModel();
        model.getAllResults().add(map.keySet().iterator().next());
        model.getShownResults().add(map.keySet().iterator().next());

        Component component = support.createMock(Component.class);
        EasyMock.expect(component.getLocationOnScreen()).andReturn(new Point(0, 0));

        support.replayAll();

        SelectedResultHandler handler = new SelectedResultHandler(model, controlRegistry, mapManager, map, labelMap, creator);

        PickEvent event = new PickEvent(map.entrySet().iterator().next().getValue(), null);
        myPickListener.handlePickEvent(event);
        MouseEvent mouseEvent = new MouseEvent(component, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON1);
        myMouseListener.eventOccurred(mouseEvent);

        assertEquals(model.getAllResults().get(0), model.getSelectedResult().get());

        myPickListener.handlePickEvent(event);
        myMouseListener.eventOccurred(mouseEvent);

        assertNull(model.getSelectedResult().get());

        handler.close();

        assertNull(myPickListener);
        assertNull(myMouseListener);

        support.verifyAll();
    }

    /**
     * Tests when the user hovers over a search result on the search results UI.
     */
    @Test
    public void testSearchHover()
    {
        EasyMockSupport support = new EasyMockSupport();

        BiMap<SearchResult, Geometry> map = createBiMap();
        BiMap<SearchResult, Geometry> labelMap = createBiMap();
        ControlRegistry controlRegistry = createControlRegistry(support);
        ViewerAnimatorCreator creator = support.createMock(ViewerAnimatorCreator.class);
        MapManager mapManager = support.createMock(MapManager.class);

        SearchModel model = new SearchModel();
        model.getAllResults().add(map.keySet().iterator().next());
        model.getShownResults().add(map.keySet().iterator().next());

        support.replayAll();

        SelectedResultHandler handler = new SelectedResultHandler(model, controlRegistry, mapManager, map, labelMap, creator);

        model.getHoveredResult().set(model.getAllResults().get(0));
        assertEquals(Color.RED, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        model.getHoveredResult().set(null);
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        handler.close();

        model.getHoveredResult().set(model.getAllResults().get(0));
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        support.verifyAll();
    }

    /**
     * Tests when the user hovers over a search result on the search results UI.
     */
    @Test
    public void testSearchHoverAndSelect()
    {
        EasyMockSupport support = new EasyMockSupport();

        BiMap<SearchResult, Geometry> map = createBiMap();
        BiMap<SearchResult, Geometry> labelMap = createBiMap();
        ControlRegistry controlRegistry = createControlRegistry(support);
        DynamicViewer viewer = support.createMock(DynamicViewer.class);
        ViewerAnimatorCreator creator = createCreator(support, viewer, 2);
        MapManager mapManager = createMapManager(support, viewer);
        EasyMock.expectLastCall().times(2);

        SearchModel model = new SearchModel();
        model.getAllResults().add(map.keySet().iterator().next());
        model.getShownResults().add(map.keySet().iterator().next());

        support.replayAll();

        SelectedResultHandler handler = new SelectedResultHandler(model, controlRegistry, mapManager, map, labelMap, creator);

        model.getHoveredResult().set(model.getAllResults().get(0));
        assertEquals(Color.RED, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());
        model.getFocusedResult().set(model.getAllResults().get(0));
        assertEquals(Color.RED, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        model.getHoveredResult().set(null);
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        model.getFocusedResult().set(null);
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        model.getFocusedResult().set(model.getAllResults().get(0));
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());
        model.getHoveredResult().set(model.getAllResults().get(0));
        assertEquals(Color.RED, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        model.getSelectedResult().set(null);
        assertEquals(Color.RED, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        model.getHoveredResult().set(null);
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        handler.close();

        model.getHoveredResult().set(model.getAllResults().get(0));
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());
        model.getSelectedResult().set(model.getAllResults().get(0));
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        support.verifyAll();
    }

    /**
     * Tests when the user selects a search result on the search results UI.
     */
    @Test
    public void testSearchSelect()
    {
        EasyMockSupport support = new EasyMockSupport();

        BiMap<SearchResult, Geometry> map = createBiMap();
        BiMap<SearchResult, Geometry> labelMap = createBiMap();
        ControlRegistry controlRegistry = createControlRegistry(support);
        DynamicViewer viewer = support.createMock(DynamicViewer.class);
        ViewerAnimatorCreator creator = createCreator(support, viewer, 1);
        MapManager mapManager = createMapManager(support, viewer);

        SearchModel model = new SearchModel();
        model.getAllResults().add(map.keySet().iterator().next());
        model.getShownResults().add(map.keySet().iterator().next());

        support.replayAll();

        SelectedResultHandler handler = new SelectedResultHandler(model, controlRegistry, mapManager, map, labelMap, creator);

        model.getFocusedResult().set(model.getAllResults().get(0));
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        model.getFocusedResult().set(null);
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        handler.close();

        model.getSelectedResult().set(model.getAllResults().get(0));
        assertEquals(Color.CYAN, ((ColorRenderProperties)map.values().iterator().next().getRenderProperties()).getColor());

        support.verifyAll();
    }

    /**
     * Creates the BiMap and some test data.
     *
     * @return The {@link BiMap} with test data.
     */
    private BiMap<SearchResult, Geometry> createBiMap()
    {
        BiMap<SearchResult, Geometry> map = Maps.synchronizedBiMap(HashBiMap.create());

        SearchResult result = new SearchResult();
        result.setConfidence(1f);
        result.setDescription("a description");
        result.setSearchType("Place Name");
        result.setText("A result");
        result.getLocations().add(LatLonAlt.createFromDegrees(10, 11));

        PointGeometry.Builder<GeographicPosition> builder = new PointGeometry.Builder<>();
        builder.setPosition(new GeographicPosition(result.getLocations().get(0)));

        PointGeometry point = new PointGeometry(builder, new DefaultPointRenderProperties(10, true, true, false), null);

        map.put(result, point);

        return map;
    }

    /**
     * Creates a {@link ControlRegistry} mock.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link ControlRegistry}.
     */
    private ControlRegistry createControlRegistry(EasyMockSupport support)
    {
        ControlContext context = support.createMock(ControlContext.class);
        context.addPickListener(EasyMock.isA(PickListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myPickListener = (PickListener)EasyMock.getCurrentArguments()[0];
            return null;
        });
        context.addListener(EasyMock.isA(DiscreteEventListener.class),
                EasyMock.eq(new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED)));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myMouseListener = (DiscreteEventListener)EasyMock.getCurrentArguments()[0];
            return null;
        });

        context.removePickListener(EasyMock.isA(PickListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myPickListener.equals(EasyMock.getCurrentArguments()[0]))
            {
                myPickListener = null;
            }
            return null;
        });
        context.removeListener(EasyMock.isA(DiscreteEventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myMouseListener.equals(EasyMock.getCurrentArguments()[0]))
            {
                myMouseListener = null;
            }
            return null;
        });

        ControlRegistry controlRegistry = support.createMock(ControlRegistry.class);

        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT)).andReturn(context).times(2);
        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)).andReturn(context).times(2);

        return controlRegistry;
    }

    /**
     * Creates an easy mock {@link ViewerAnimatorCreator}.
     *
     * @param support Used to create the mock.
     * @param viewer The expected viewer.
     * @param times The number of times to expect the functions to be called.
     * @return The mocked {@link ViewerAnimatorCreator}.
     */
    private ViewerAnimatorCreator createCreator(EasyMockSupport support, DynamicViewer viewer, int times)
    {
        ViewerAnimator animator = support.createMock(ViewerAnimator.class);
        animator.start();
        EasyMock.expectLastCall().times(times);

        ViewerAnimatorCreator creator = support.createMock(ViewerAnimatorCreator.class);
        EasyMock.expect(creator.createAnimator(EasyMock.eq(viewer), EasyMock.isA(GeographicPosition.class))).andAnswer(() ->
        {
            assertEquals(LatLonAlt.createFromDegrees(10, 11),
                    ((GeographicPosition)EasyMock.getCurrentArguments()[1]).getLatLonAlt());
            return animator;
        }).times(times);

        return creator;
    }

    /**
     * Creates an easy mocked map manager.
     *
     * @param support Used to create the mock.
     * @param viewer The {@link DynamicViewer} to return.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support, DynamicViewer viewer)
    {
        MapManager mapManager = support.createMock(MapManager.class);

        EasyMock.expect(mapManager.getStandardViewer()).andReturn(viewer);

        return mapManager;
    }
}
