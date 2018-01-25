package io.opensphere.subterrain.xraygoggles.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for {@link ScreenPositionManager}.
 */
public class ScreenPositionManagerTest
{
    /**
     * The {@link DiscreteEventListener} listening for mouse input.
     */
    private DiscreteEventListener myDiscreteListener;

    /**
     * The count down latch to synchronize test.
     */
    private CountDownLatch myLatch;

    /**
     * Tests the screen position manager.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void test() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        ScreenViewer viewer = createViewer(support);
        DynamicViewer standardViewer = support.createMock(DynamicViewer.class);
        EasyMock.expect(Double.valueOf(standardViewer.getPitch())).andReturn(Double.valueOf(Math.toRadians(45))).atLeastOnce();
        MapManager mapManager = createMapManager(support, viewer, standardViewer);
        ControlRegistry controlRegistry = createControlRegistry(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        ScreenPositionManager manager = new ScreenPositionManager(mapManager, controlRegistry, model);

        assertEquals(480, model.getUpperLeft().getX(), 0d);
        assertEquals(720, model.getUpperLeft().getY(), 0d);
        assertEquals(1440, model.getUpperRight().getX(), 0d);
        assertEquals(720, model.getUpperRight().getY(), 0d);
        assertEquals(640, model.getLowerLeft().getX(), 0d);
        assertEquals(1080, model.getLowerLeft().getY(), 0d);
        assertEquals(1280, model.getLowerRight().getX(), 0d);
        assertEquals(1080, model.getLowerRight().getY(), 0d);

        model.addObserver((o, arg) ->
        {
            myLatch.countDown();
        });
        myLatch = new CountDownLatch(1);
        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.WINDOW_RESIZE);

        assertTrue(myLatch.await(1, TimeUnit.SECONDS));
        assertEquals(240, model.getUpperLeft().getX(), 0d);
        assertEquals(360, model.getUpperLeft().getY(), 0d);
        assertEquals(720, model.getUpperRight().getX(), 0d);
        assertEquals(360, model.getUpperRight().getY(), 0d);
        assertEquals(320, model.getLowerLeft().getX(), 0d);
        assertEquals(540, model.getLowerLeft().getY(), 0d);
        assertEquals(640, model.getLowerRight().getX(), 0d);
        assertEquals(540, model.getLowerRight().getY(), 0d);

        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.NEW_VIEWER);
        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.VIEW_CHANGE);

        manager.close();

        assertNull(model.getUpperLeft());
        assertNull(model.getUpperRight());
        assertNull(model.getLowerLeft());
        assertNull(model.getLowerRight());
        assertNull(model.getCenterGeo());
        assertNull(model.getUpperLeftGeo());
        assertNull(model.getUpperRightGeo());
        assertNull(model.getLowerLeftGeo());
        assertNull(model.getLowerRightGeo());

        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.WINDOW_RESIZE);

        Thread.sleep(100);

        support.verifyAll();
    }

    /**
     * Tests the screen position manager.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testCameraPitch() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        ScreenViewer viewer = createViewer(support);
        DynamicViewer standardViewer = support.createMock(DynamicViewer.class);
        EasyMock.expect(Double.valueOf(standardViewer.getPitch())).andReturn(Double.valueOf(Math.toRadians(45)));
        EasyMock.expect(Double.valueOf(standardViewer.getPitch())).andReturn(Double.valueOf(Math.toRadians(60)));
        EasyMock.expect(Double.valueOf(standardViewer.getPitch())).andReturn(Double.valueOf(Math.toRadians(65)));
        EasyMock.expect(Double.valueOf(standardViewer.getPitch())).andReturn(Double.valueOf(Math.toRadians(70)));

        XrayGogglesModel model = new XrayGogglesModel();

        GeographicPosition upperLeftGeo = new GeographicPosition(LatLonAlt.createFromDegrees(1, 0));
        GeographicPosition upperRightGeo = new GeographicPosition(LatLonAlt.createFromDegrees(1, 1));

        MapManager mapManager = createMapManager(support, viewer, standardViewer);
        EasyMock.expect(mapManager.convertToPoint(EasyMock.eq(upperLeftGeo))).andReturn(new Vector2i(400, 500));
        EasyMock.expect(mapManager.convertToPoint(EasyMock.eq(upperRightGeo))).andReturn(new Vector2i(1000, 500));
        ControlRegistry controlRegistry = createControlRegistry(support);

        support.replayAll();

        ScreenPositionManager manager = new ScreenPositionManager(mapManager, controlRegistry, model);

        assertNotNull(myDiscreteListener);
        assertEquals(480, model.getUpperLeft().getX(), 0d);
        assertEquals(720, model.getUpperLeft().getY(), 0d);
        assertEquals(1440, model.getUpperRight().getX(), 0d);
        assertEquals(720, model.getUpperRight().getY(), 0d);
        assertEquals(640, model.getLowerLeft().getX(), 0d);
        assertEquals(1080, model.getLowerLeft().getY(), 0d);
        assertEquals(1280, model.getLowerRight().getX(), 0d);
        assertEquals(1080, model.getLowerRight().getY(), 0d);
        model.setGeoPosition(upperLeftGeo, upperRightGeo, new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 1)),
                new GeographicPosition(LatLonAlt.createFromDegrees(.5, .5)));

        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.VIEW_CHANGE);

        Thread.sleep(500);

        assertEquals(480, model.getUpperLeft().getX(), 0d);
        assertEquals(720, model.getUpperLeft().getY(), 0d);
        assertEquals(1440, model.getUpperRight().getX(), 0d);
        assertEquals(720, model.getUpperRight().getY(), 0d);
        assertEquals(640, model.getLowerLeft().getX(), 0d);
        assertEquals(1080, model.getLowerLeft().getY(), 0d);
        assertEquals(1280, model.getLowerRight().getX(), 0d);
        assertEquals(1080, model.getLowerRight().getY(), 0d);

        model.addObserver((o, arg) ->
        {
            myLatch.countDown();
        });
        JPanel panel = new JPanel();
        myDiscreteListener.eventOccurred(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 0, 0, 0,
                0, 1, false, MouseEvent.BUTTON3));
        myLatch = new CountDownLatch(1);
        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.VIEW_CHANGE);

        assertTrue(myLatch.await(1, TimeUnit.SECONDS));
        assertEquals(400, model.getUpperLeft().getX(), 0d);
        assertEquals(500, model.getUpperLeft().getY(), 0d);
        assertEquals(1000, model.getUpperRight().getX(), 0d);
        assertEquals(500, model.getUpperRight().getY(), 0d);
        assertEquals(640, model.getLowerLeft().getX(), 0d);
        assertEquals(1080, model.getLowerLeft().getY(), 0d);
        assertEquals(1280, model.getLowerRight().getX(), 0d);
        assertEquals(1080, model.getLowerRight().getY(), 0d);

        myDiscreteListener.eventOccurred(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 0, 0, 0,
                0, 1, false, MouseEvent.BUTTON3));

        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.VIEW_CHANGE);

        Thread.sleep(500);

        assertEquals(400, model.getUpperLeft().getX(), 0d);
        assertEquals(500, model.getUpperLeft().getY(), 0d);
        assertEquals(1000, model.getUpperRight().getX(), 0d);
        assertEquals(500, model.getUpperRight().getY(), 0d);
        assertEquals(640, model.getLowerLeft().getX(), 0d);
        assertEquals(1080, model.getLowerLeft().getY(), 0d);
        assertEquals(1280, model.getLowerRight().getX(), 0d);
        assertEquals(1080, model.getLowerRight().getY(), 0d);

        manager.close();

        assertNull(myDiscreteListener);
        assertNull(model.getUpperLeft());
        assertNull(model.getUpperRight());
        assertNull(model.getLowerLeft());
        assertNull(model.getLowerRight());
        assertNull(model.getCenterGeo());
        assertNull(model.getUpperLeftGeo());
        assertNull(model.getUpperRightGeo());
        assertNull(model.getLowerLeftGeo());
        assertNull(model.getLowerRightGeo());

        mapManager.getViewChangeSupport().notifyViewChangeListeners(viewer, (r) ->
        {
            r.run();
        }, ViewChangeType.WINDOW_RESIZE);

        Thread.sleep(500);

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link ControlRegistry}.
     *
     * @param support Used to create the mock.
     * @return The {@link ControlRegistry}.
     */
    private ControlRegistry createControlRegistry(EasyMockSupport support)
    {
        ControlContext globe = support.createMock(ControlContext.class);
        globe.addListener(EasyMock.isA(DiscreteEventListener.class),
                EasyMock.eq(new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON3_DOWN_MASK)),
                EasyMock.eq(new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED, InputEvent.BUTTON3_MASK)));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myDiscreteListener = (DiscreteEventListener)EasyMock.getCurrentArguments()[0];
            return null;
        });
        globe.removeListener(EasyMock.isA(DiscreteEventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myDiscreteListener.equals(EasyMock.getCurrentArguments()[0]))
            {
                myDiscreteListener = null;
            }
            return null;
        });

        ControlRegistry controlRegistry = support.createMock(ControlRegistry.class);
        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)).andReturn(globe).atLeastOnce();

        return controlRegistry;
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @param viewer A mocked {@link ScreenViewer} to return.
     * @param standardViewer A mocked {@link DynamicViewer} to return.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support, ScreenViewer viewer, DynamicViewer standardViewer)
    {
        ViewChangeSupport changeSupport = new ViewChangeSupport();

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.getViewChangeSupport()).andReturn(changeSupport).atLeastOnce();
        EasyMock.expect(mapManager.getScreenViewer()).andReturn(viewer);
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
        EasyMock.expect(Integer.valueOf(viewer.getViewportHeight())).andReturn(Integer.valueOf(540));
        EasyMock.expect(Integer.valueOf(viewer.getViewportWidth())).andReturn(Integer.valueOf(960));

        return viewer;
    }
}
