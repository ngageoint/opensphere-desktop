package io.opensphere.xyztile.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Tests the {@link XYZTileLayerInfo} class.
 */
public class XYZTileLayerInfoTest
{
    /**
     * Tests the class.
     */
    @Test
    public void test()
    {
        XYZTileLayerInfo layerInfo = new XYZTileLayerInfo("aName", "A Name", Projection.EPSG_4326, 2, true, 4,
                new XYZServerInfo("serverName", "http://somehost"));
        layerInfo.setDescription("I am description");
        layerInfo.setParentId("parent");

        assertEquals("aName", layerInfo.getName());
        assertEquals("A Name", layerInfo.getDisplayName());
        assertEquals(Projection.EPSG_4326, Projection.EPSG_4326);
        assertEquals(2, layerInfo.getNumberOfTopLevels());
        assertTrue(layerInfo.isTms());
        assertEquals("http://somehost", layerInfo.getServerUrl());
        assertEquals(4, layerInfo.getMinZoomLevel());
        assertNull(layerInfo.getFootprint());

        GeographicBoundingBox footprint = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10, -10),
                LatLonAlt.createFromDegrees(10, 10));
        layerInfo.setFootprint(footprint);

        assertEquals(footprint, layerInfo.getFootprint());

        assertEquals("I am description", layerInfo.getDescription());
        assertEquals("parent", layerInfo.getParentId());
        assertTrue(layerInfo.isVisible());

        layerInfo.setVisible(false);
        assertFalse(layerInfo.isVisible());

        assertNull(layerInfo.getTimeSpan());

        TimeSpan span = TimeSpan.get(System.currentTimeMillis() - 1000, System.currentTimeMillis());
        layerInfo.setTimeSpan(span);
        assertEquals(span, layerInfo.getTimeSpan());
    }

    /**
     * Tests when the user sets the maximum zoom level.
     */
    @Test
    public void testMaxLevelsUser()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.isA(XYZTileLayerInfo.class), EasyMock.cmpEq(XYZTileLayerInfo.MAX_LEVELS_PROP));

        support.replayAll();

        XYZTileLayerInfo layerInfo = new XYZTileLayerInfo("aName", "A Name", Projection.EPSG_4326, 2, true, 4,
                new XYZServerInfo("serverName", "http://somehost"));
        layerInfo.addObserver(observer);

        assertEquals(18, layerInfo.getMaxLevels());

        layerInfo.setMaxLevels(16);
        assertEquals(16, layerInfo.getMaxLevels());

        layerInfo.setMaxLevelsUser(12);
        assertEquals(12, layerInfo.getMaxLevels());
        assertEquals(16, layerInfo.getMaxLevelsDefault());

        support.verifyAll();
    }
}
