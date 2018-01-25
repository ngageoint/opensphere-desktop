package io.opensphere.arcgis2.envoy.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.function.Predicate;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.arcgis2.model.ArcGISLayer;
import io.opensphere.arcgis2.model.ArcGISLayer.Builder;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Unit test for {@link ArcGISTileEnvoy}.
 */
public class ArcGISTileEnvoyTest
{
    /**
     * Tests building the url for export type.
     */
    @Test
    public void testBuildImageUrlExport()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController controller = support.createMock(DataGroupController.class);
        Toolbox toolbox = createToolbox(support, controller);
        ArcGISDataGroupInfo exportGroup = createExport(toolbox);
        addControllerMock(controller, exportGroup);

        support.replayAll();

        ArcGISTileEnvoy envoy = new ArcGISTileEnvoy(toolbox);
        DataModelCategory yes = new DataModelCategory("arcgis", XYZTileUtils.TILES_FAMILY, exportGroup.getURL());
        ZYXImageKey key = new ZYXImageKey(0, 1, 2,
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0), LatLonAlt.createFromDegrees(1, 1)));
        String urlString = envoy.buildImageUrlString(yes, key);

        assertTrue(urlString.contains("/export?"));

        support.verifyAll();
    }

    /**
     * Tests building the url for xyz type.
     */
    @Test
    public void testBuildImageUrlXYZ()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController controller = support.createMock(DataGroupController.class);
        Toolbox toolbox = createToolbox(support, controller);
        ArcGISDataGroupInfo xyzGroup = createXYZ(toolbox);
        addControllerMock(controller, xyzGroup);

        support.replayAll();

        ArcGISTileEnvoy envoy = new ArcGISTileEnvoy(toolbox);
        DataModelCategory yes = new DataModelCategory("arcgis", XYZTileUtils.TILES_FAMILY, xyzGroup.getURL());
        ZYXImageKey key = new ZYXImageKey(0, 1, 2, null);
        String urlString = envoy.buildImageUrlString(yes, key);

        assertTrue(urlString.endsWith("/tile/0/1/2"));

        support.verifyAll();
    }

    /**
     * Tests getting the expiration time for export layers.
     */
    @Test
    public void testGetExpirationTimeExport()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController controller = support.createMock(DataGroupController.class);
        Toolbox toolbox = createToolbox(support, controller);
        ArcGISDataGroupInfo exportGroup = createExport(toolbox);
        addControllerMock(controller, exportGroup);

        support.replayAll();

        ArcGISTileEnvoy envoy = new ArcGISTileEnvoy(toolbox);
        DataModelCategory yes = new DataModelCategory("arcgis", XYZTileUtils.TILES_FAMILY, "export");

        Date expirationTime = envoy.getExpirationTime(yes);

        assertEquals(CacheDeposit.SESSION_END, expirationTime);

        support.verifyAll();
    }

    /**
     * Tests getting the expiration time for xyz layers.
     */
    @Test
    public void testGetExpirationTimeXYZ()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupController controller = support.createMock(DataGroupController.class);
        Toolbox toolbox = createToolbox(support, controller);
        ArcGISDataGroupInfo xyzGroup = createXYZ(toolbox);
        addControllerMock(controller, xyzGroup);

        support.replayAll();

        ArcGISTileEnvoy envoy = new ArcGISTileEnvoy(toolbox);
        DataModelCategory yes = new DataModelCategory("arcgis", XYZTileUtils.TILES_FAMILY, "xyz");

        Date expirationTime = envoy.getExpirationTime(yes);
        assertTrue(expirationTime.getTime() > new Date().getTime());
        assertTrue(expirationTime != CacheDeposit.SESSION_END);

        support.verifyAll();
    }

    /**
     * Tests the provide data for.
     */
    @Test
    public void testProvidesDataFor()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, null);

        support.replayAll();

        ArcGISTileEnvoy envoy = new ArcGISTileEnvoy(toolbox);

        DataModelCategory yes = new DataModelCategory("/rest/services", XYZTileUtils.TILES_FAMILY, "xyz");
        DataModelCategory no = new DataModelCategory("something", XYZTileUtils.TILES_FAMILY, "xyz");
        DataModelCategory no1 = new DataModelCategory(null, XYZTileUtils.TILES_FAMILY, "xyz");

        assertTrue(envoy.providesDataFor(yes));
        assertFalse(envoy.providesDataFor(no));
        assertFalse(envoy.providesDataFor(no1));

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataGroupController}.
     *
     * @param controller The mocked class to add mocked calls to.
     * @param dataGroupInfo The test active data group.
     */
    @SuppressWarnings("unchecked")
    private void addControllerMock(DataGroupController controller, DataGroupInfo dataGroupInfo)
    {
        EasyMock.expect(controller.findActiveDataGroupInfo(EasyMock.isA(Predicate.class), EasyMock.eq(true)))
                .andReturn(New.set(dataGroupInfo));
    }

    /**
     * Creates a layer who needs an export call to retrieve imagery.
     *
     * @param toolbox The system toolbox.
     * @return The layer.
     */
    private ArcGISDataGroupInfo createExport(Toolbox toolbox)
    {
        Builder builder = new Builder();
        builder.setSingleFusedMapCache(false);
        builder.setLayerName("Export");

        ArcGISLayer layer = new ArcGISLayer(builder);

        return new ArcGISDataGroupInfo(toolbox, layer, "http://somehost/export/MapServer", 0);
    }

    /**
     * Creates the easy mocked toolbox.
     *
     * @param support Used to create the toolbox.
     * @param controller A mocked {@link DataGroupController}.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, DataGroupController controller)
    {
        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataGroupController()).andReturn(controller);

        PluginToolboxRegistry pluginTools = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginTools.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantle);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginTools);

        return toolbox;
    }

    /**
     * Creates a layer who needs an xyz call to retrieve imagery.
     *
     * @param toolbox The system toolbox.
     * @return The layer.
     */
    private ArcGISDataGroupInfo createXYZ(Toolbox toolbox)
    {
        Builder builder = new Builder();
        builder.setSingleFusedMapCache(true);
        builder.setLayerName("XYZ");

        ArcGISLayer layer = new ArcGISLayer(builder);

        return new ArcGISDataGroupInfo(toolbox, layer, "http://somehost/xyz/MapServer", 0);
    }
}
