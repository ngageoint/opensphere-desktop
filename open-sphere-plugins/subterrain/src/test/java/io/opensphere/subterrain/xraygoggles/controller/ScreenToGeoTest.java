package io.opensphere.subterrain.xraygoggles.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for {@link ScreenToGeo} class.
 */
public class ScreenToGeoTest implements Observer
{
    /**
     * Used to synchronize the tests.
     */
    private CountDownLatch myLatch;

    /**
     * Tests initial calculations.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testInitial() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        MapManager mapManager = createMapManager(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(0, 0), new ScreenPosition(2, 0), new ScreenPosition(0, 2),
                new ScreenPosition(2, 2));
        myLatch = new CountDownLatch(1);
        model.addObserver(this);
        ScreenToGeo screenToGeo = new ScreenToGeo(mapManager, model);

        assertTrue(myLatch.await(1, TimeUnit.SECONDS));

        assertEquals(new Vector2d(10, 10), model.getUpperLeftGeo().asVector2d());
        assertEquals(new Vector2d(12, 10), model.getUpperRightGeo().asVector2d());
        assertEquals(new Vector2d(10, 12), model.getLowerLeftGeo().asVector2d());
        assertEquals(new Vector2d(12, 12), model.getLowerRightGeo().asVector2d());
        assertEquals(new Vector2d(11, 11), model.getCenterGeo().asVector2d());

        screenToGeo.close();

        model.setScreenPosition(new ScreenPosition(20, 20), new ScreenPosition(21, 20), new ScreenPosition(20, 21),
                new ScreenPosition(21, 21));

        Thread.sleep(500);

        support.verifyAll();
    }

    /**
     * Tests updating when the screen coordinates change.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testUpdate() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        MapManager mapManager = createMapManager(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        ScreenToGeo screenToGeo = new ScreenToGeo(mapManager, model);
        myLatch = new CountDownLatch(1);
        model.addObserver(this);
        model.setScreenPosition(new ScreenPosition(0, 0), new ScreenPosition(2, 0), new ScreenPosition(0, 2),
                new ScreenPosition(2, 2));

        assertTrue(myLatch.await(1, TimeUnit.SECONDS));
        assertEquals(new Vector2d(10, 10), model.getUpperLeftGeo().asVector2d());
        assertEquals(new Vector2d(12, 10), model.getUpperRightGeo().asVector2d());
        assertEquals(new Vector2d(10, 12), model.getLowerLeftGeo().asVector2d());
        assertEquals(new Vector2d(12, 12), model.getLowerRightGeo().asVector2d());
        assertEquals(new Vector2d(11, 11), model.getCenterGeo().asVector2d());

        screenToGeo.close();

        model.setScreenPosition(new ScreenPosition(20, 20), new ScreenPosition(21, 20), new ScreenPosition(20, 21),
                new ScreenPosition(21, 21));

        Thread.sleep(500);

        support.verifyAll();
    }

    /**
     * Tests when the viewer has changed.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testViewChanged() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        MapManager mapManager = createMapManager(support);
        EasyMock.expect(mapManager.convertToPosition(EasyMock.isA(Vector2i.class), EasyMock.eq(ReferenceLevel.TERRAIN)))
                .andAnswer(() ->
                {
                    Vector2i screen = (Vector2i)EasyMock.getCurrentArguments()[0];
                    LatLonAlt latLon = LatLonAlt.createFromDegrees(20 + screen.getY(), 20 + screen.getX());
                    return new GeographicPosition(latLon);
                }).times(5);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(0, 0), new ScreenPosition(1, 0), new ScreenPosition(0, 1),
                new ScreenPosition(1, 1));
        myLatch = new CountDownLatch(1);
        model.addObserver(this);
        ScreenToGeo screenToGeo = new ScreenToGeo(mapManager, model);

        assertTrue(myLatch.await(1, TimeUnit.SECONDS));
        assertEquals(new Vector2d(10, 10), model.getUpperLeftGeo().asVector2d());
        assertEquals(new Vector2d(11, 10), model.getUpperRightGeo().asVector2d());
        assertEquals(new Vector2d(10, 11), model.getLowerLeftGeo().asVector2d());
        assertEquals(new Vector2d(11, 11), model.getLowerRightGeo().asVector2d());

        myLatch = new CountDownLatch(1);
        mapManager.getViewChangeSupport().notifyViewChangeListeners(null, (r) ->
        {
            r.run();
        }, ViewChangeType.VIEW_CHANGE);

        assertTrue(myLatch.await(1, TimeUnit.SECONDS));
        assertEquals(new Vector2d(20, 20), model.getUpperLeftGeo().asVector2d());
        assertEquals(new Vector2d(21, 20), model.getUpperRightGeo().asVector2d());
        assertEquals(new Vector2d(20, 21), model.getLowerLeftGeo().asVector2d());
        assertEquals(new Vector2d(21, 21), model.getLowerRightGeo().asVector2d());

        screenToGeo.close();

        mapManager.getViewChangeSupport().notifyViewChangeListeners(null, (r) ->
        {
            r.run();
        }, ViewChangeType.VIEW_CHANGE);

        Thread.sleep(500);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support)
    {
        ViewChangeSupport viewChangeSupport = new ViewChangeSupport();

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.getViewChangeSupport()).andReturn(viewChangeSupport).atLeastOnce();

        EasyMock.expect(mapManager.convertToPosition(EasyMock.isA(Vector2i.class), EasyMock.eq(ReferenceLevel.TERRAIN)))
                .andAnswer(() ->
                {
                    Vector2i screen = (Vector2i)EasyMock.getCurrentArguments()[0];
                    LatLonAlt latLon = LatLonAlt.createFromDegrees(10 + screen.getY(), 10 + screen.getX());
                    return new GeographicPosition(latLon);
                }).times(5);

        return mapManager;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (XrayGogglesModel.GEO_POSITION.equals(arg))
        {
            myLatch.countDown();
        }
    }
}
