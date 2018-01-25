package io.opensphere.osm.envoy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for {@link OSMTileEnvoy}.
 */
public class OSMTileEnvoyTest
{
    /**
     * The test layer name.
     */
    private static final String ourLayer = "OSM";

    /**
     * The test server url.
     */
    private static final String ourServer = "http://osm.geointservices.io/osm_tiles_pc/{z}/{x}/{y}.png";

    /**
     * Tests building the image url.
     */
    @Test
    public void testBuildImageUrlString()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        OSMTileEnvoy envoy = new OSMTileEnvoy(toolbox);
        DataModelCategory category = new DataModelCategory(ourServer, null, null);
        ZYXImageKey key = new ZYXImageKey(1, 2, 3, null);
        String url = envoy.buildImageUrlString(category, key);

        assertEquals("http://osm.geointservices.io/osm_tiles_pc/1/3/2.png", url);

        support.verifyAll();
    }

    /**
     * Tests the thread pool name.
     */
    @Test
    public void testGetThreadPoolName()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        OSMTileEnvoy envoy = new OSMTileEnvoy(toolbox);
        assertTrue(envoy.getThreadPoolName().startsWith("OSM"));

        support.verifyAll();
    }

    /**
     * Tests the provides data for.
     */
    @Test
    public void testProvidesDataFor()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        OSMTileEnvoy envoy = new OSMTileEnvoy(toolbox);
        DataModelCategory provides = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, ourLayer);
        DataModelCategory notProvides = new DataModelCategory(ourServer, Image.class.getName(), ourLayer);
        DataModelCategory notProvides2 = new DataModelCategory(null, XYZTileUtils.TILES_FAMILY, ourLayer);
        DataModelCategory notProvides3 = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, null);
        DataModelCategory notProvides4 = new DataModelCategory(ourServer, null, ourLayer);
        DataModelCategory notProvides5 = new DataModelCategory(ourServer, XYZTileUtils.TILES_FAMILY, "Open Street Map");

        assertTrue(envoy.providesDataFor(provides));
        assertFalse(envoy.providesDataFor(notProvides));
        assertFalse(envoy.providesDataFor(notProvides2));
        assertFalse(envoy.providesDataFor(notProvides3));
        assertFalse(envoy.providesDataFor(notProvides4));
        assertFalse(envoy.providesDataFor(notProvides5));

        support.verifyAll();
    }
}
