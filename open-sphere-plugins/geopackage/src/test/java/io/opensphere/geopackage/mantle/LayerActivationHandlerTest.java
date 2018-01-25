package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.image.Image;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.geopackage.envoy.GeoPackageImageEnvoy;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.LayerType;
import mil.nga.geopackage.manager.GeoPackageManager;

/**
 * Unit test for {@link LayerActivationHandler}.
 */
public class LayerActivationHandlerTest
{
    /**
     * Tests when the layer is activated.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testLayerActivated() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        File testFile = File.createTempFile("test", ".gpkg");
        testFile.deleteOnExit();
        testFile = new File(testFile.getParentFile(), "a" + testFile.getName());
        testFile.deleteOnExit();
        GeoPackageManager.create(testFile);
        GeoPackageLayer geoLayer = new GeoPackageLayer("package", testFile.toString(), "tile", LayerType.TILE, 100);

        GenericRegistry<Envoy> envoyRegistry = new GenericRegistry<>();
        Toolbox toolbox = createToolbox(support, envoyRegistry);
        LayerActivationListener tileListener = support.createMock(LayerActivationListener.class);
        GeoPackageDataTypeInfo layer = createFeatureLayer(geoLayer);

        tileListener.layerActivated(EasyMock.eq(layer));
        tileListener.layerDeactivated(EasyMock.eq(layer));

        support.replayAll();

        LayerActivationHandler handler = new LayerActivationHandler(toolbox, tileListener);
        handler.layerActivated(layer);

        Collection<Envoy> envoys = envoyRegistry.getObjectsForSource(geoLayer.getPackageFile());

        GeoPackageImageEnvoy envoy = (GeoPackageImageEnvoy)envoys.iterator().next();
        DataModelCategory category = new DataModelCategory(geoLayer.getPackageFile(), "tile", Image.class.getName());
        assertTrue(envoy.providesDataFor(category));

        handler.layerDeactivated(layer);
        assertTrue(envoyRegistry.getObjectsForSource(geoLayer.getPackageFile()).isEmpty());

        support.verifyAll();
    }

    /**
     * Creates a test geopackage feature data type.
     *
     * @param geoLayer The test layer.
     * @return The test data type.
     */
    private GeoPackageDataTypeInfo createFeatureLayer(GeoPackageLayer geoLayer)
    {
        GeoPackageDataTypeInfo featureLayer = new GeoPackageDataTypeInfo(null, geoLayer, "key3");

        return featureLayer;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param envoyRegistry The envoy registry to return.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support, GenericRegistry<Envoy> envoyRegistry)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getEnvoyRegistry()).andReturn(envoyRegistry);

        return toolbox;
    }
}
