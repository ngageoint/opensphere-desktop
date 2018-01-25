package io.opensphere.xyztile.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for {@link XYZDataTypeInfo}.
 */
public class XYZDataTypeInfoTest
{
    /**
     * Test.
     */
    @Test
    public void test()
    {
        XYZTileLayerInfo layerInfo = new XYZTileLayerInfo("name", "displayName", Projection.EPSG_4326, 1, false, 0,
                new XYZServerInfo("serverName", "http://somehost"));
        XYZDataTypeInfo dataType = new XYZDataTypeInfo(null, layerInfo);

        assertEquals("name", dataType.getTypeName());
        assertEquals("displayName", dataType.getDisplayName());
        assertEquals("http://somehost", dataType.getUrl());
        assertEquals("http://somehostname", dataType.getTypeKey());

        assertEquals(layerInfo, dataType.getLayerInfo());
        assertTrue(dataType.isVisible());
    }

    /**
     * Test.
     */
    @Test
    public void testInvisible()
    {
        XYZTileLayerInfo layerInfo = new XYZTileLayerInfo("name", "displayName", Projection.EPSG_4326, 1, false, 0,
                new XYZServerInfo("serverName", "http://somehost"));
        layerInfo.setVisible(false);
        XYZDataTypeInfo dataType = new XYZDataTypeInfo(null, layerInfo);

        assertEquals("name", dataType.getTypeName());
        assertEquals("displayName", dataType.getDisplayName());
        assertEquals("http://somehost", dataType.getUrl());
        assertEquals("http://somehostname", dataType.getTypeKey());

        assertEquals(layerInfo, dataType.getLayerInfo());
        assertFalse(dataType.isVisible());
    }
}
