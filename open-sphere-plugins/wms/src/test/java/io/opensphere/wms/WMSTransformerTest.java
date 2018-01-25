package io.opensphere.wms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.preferences.PreferencesImpl;
import io.opensphere.wms.config.v1.WMSBoundingBoxConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Test for {@link WMSTransformer}.
 */
public class WMSTransformerTest
{
    /**
     * Create a layer and pass it to a transformer and test the geometries that
     * are produced.
     */
    @Test
    public void testTransform()
    {
        final ImageProvider<LevelRowCol> imageProvider = new ImageProvider<LevelRowCol>()
        {
            @Override
            public Image getImage(LevelRowCol key)
            {
                return null;
            }
        };
        final Collection<GeographicBoundingBox> grid = new ArrayList<>();
        grid.add(new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10f, -10f), LatLonAlt.createFromDegrees(0f, 0f)));
        grid.add(new GeographicBoundingBox(LatLonAlt.createFromDegrees(-10f, 0f), LatLonAlt.createFromDegrees(0f, 10f)));
        grid.add(new GeographicBoundingBox(LatLonAlt.createFromDegrees(0f, -10f), LatLonAlt.createFromDegrees(10f, 0f)));
        grid.add(new GeographicBoundingBox(LatLonAlt.createFromDegrees(0f, 0f), LatLonAlt.createFromDegrees(10f, 10f)));
        List<Vector2d> layerCellDimensions = Collections.emptyList();

        WMSLayerConfig conf = new WMSLayerConfig();
        conf.setLayerName("layername");
        conf.setLayerTitle("title");
        conf.setLayerKey("layerkey");
        conf.setCacheImageFormat("notnull");
        WMSBoundingBoxConfig bboxConf = new WMSBoundingBoxConfig();
        bboxConf.setGeographicBoundingBox(GeographicBoundingBox.WHOLE_GLOBE);
        conf.setBoundingBoxConfig(bboxConf);
        WMSDataTypeInfo info = new WMSDataTypeInfo(WMSTestToolbox.getToolbox(true), new PreferencesImpl(""), "WMSTransfomerTest",
                new WMSLayerConfigurationSet(null, conf, null), conf.getLayerKey(), conf.getLayerTitle());
        info.setVisible(true, this);

        FileWMSLayer.Builder builder = new FileWMSLayer.Builder(info);
        builder.setLRCImageProvider(imageProvider);
        builder.setLayerCellDimensions(layerCellDimensions);
        final FileWMSLayer model = new FileWMSLayer(builder)
        {
            @Override
            public Collection<GeographicBoundingBox> generateFixedGrid(int level)
            {
                return grid;
            }

            @Override
            public Collection<GeographicBoundingBox> generateGrid(int level)
            {
                return grid;
            }

            @Override
            public Vector2d getMinimumGridSize()
            {
                return new Vector2d(5f, 5f);
            }
        };

        DataModelCategory category = new DataModelCategory("source", WMSLayer.class.getName(), "category");

        DataRegistry dataRegistry = EasyMock.createMock(DataRegistry.class);
        dataRegistry.addChangeListener(EasyMock.<DataRegistryListener<WMSLayer>>anyObject(),
                EasyMock.<DataModelCategory>anyObject(), EasyMock.same(WMSLayer.PROPERTY_DESCRIPTOR));
        @SuppressWarnings("unchecked")
        final DataRegistryListener<WMSLayer>[] listener = (DataRegistryListener<WMSLayer>[])new DataRegistryListener<?>[1];
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer()
            {
                listener[0] = (DataRegistryListener<WMSLayer>)EasyMock.getCurrentArguments()[0];
                return null;
            }
        });

        EasyMock.replay(dataRegistry);

        Toolbox toolbox = WMSTestToolbox.getToolbox(false);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).anyTimes();
        EasyMock.replay(toolbox);
        final Collection<Geometry> addGeometries = new ArrayList<>();
        final Collection<Geometry> remGeometries = new ArrayList<>();
        WMSTransformer wmsTransformer = new WMSTransformer(toolbox, null);
        wmsTransformer.open();
        GenericSubscriber<Geometry> subscriber = new GenericSubscriber<Geometry>()
        {
            @Override
            public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
            {
                addGeometries.addAll(adds);
                remGeometries.addAll(removes);
            }
        };
        wmsTransformer.addSubscriber(subscriber);

        listener[0].valuesAdded(category, null, Collections.singletonList(model), null);
        assertEquals(4, addGeometries.size());
        assertEquals(0, remGeometries.size());
        listener[0].valuesRemoved(category, null, Collections.singletonList(model), null);
        assertEquals(4, addGeometries.size());
        assertEquals(4, remGeometries.size());

        Collection<BoundingBox<?>> boxesToFind = new ArrayList<>(grid);
        for (Geometry geometry : addGeometries)
        {
            TileGeometry geom = (TileGeometry)geometry;
            GeographicBoundingBox bbox = (GeographicBoundingBox)geom.getBounds();
            assertTrue(boxesToFind.remove(bbox));
            assertNull(geom.getParent());
            assertEquals(0, geom.getGeneration());
            assertEquals(model, geom.getImageManager().getImageProvider());
            assertTrue(geom.isDivisible());
            Collection<TileGeometry> children = geom.getChildren(true);
            assertEquals(4, children.size());
            Collection<LatLonAlt> expectedCorners = new ArrayList<>(4);
            expectedCorners.add(bbox.getLowerLeft().getLatLonAlt());
            expectedCorners.add(bbox.getLeftCenter().getLatLonAlt());
            expectedCorners.add(bbox.getCenter().getLatLonAlt());
            expectedCorners.add(bbox.getLowerCenter().getLatLonAlt());
            for (TileGeometry child : children)
            {
                BoundingBox<?> childBbox = (BoundingBox<?>)child.getBounds();
                assertTrue(expectedCorners.remove(((GeographicBoundingBox)childBbox).getLowerLeft().getLatLonAlt()));
                assertEquals(geom, child.getParent());
                assertEquals(1, child.getGeneration());
                assertEquals(model, geom.getImageManager().getImageProvider());
                assertFalse(child.isDivisible());
            }
        }
        assertTrue(boxesToFind.isEmpty());
    }
}
