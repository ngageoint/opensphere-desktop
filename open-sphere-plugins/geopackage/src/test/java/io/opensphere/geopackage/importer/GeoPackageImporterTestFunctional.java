package io.opensphere.geopackage.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.image.Image;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackageTile;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.geopackage.model.TileMatrix;
import mil.nga.geopackage.GeoPackageConstants;

/**
 * Tests the {@link GeoPackageImporter} class.
 */
@SuppressWarnings("boxing")
public class GeoPackageImporterTestFunctional
{
    /**
     * Tests to see if valid geopackage files can be imported, and non
     * geopackage files can't.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testCanImportFileDropLocation() throws IOException
    {
        File geopackage = File.createTempFile("test", "." + GeoPackageConstants.GEOPACKAGE_EXTENSION);
        geopackage.deleteOnExit();

        File geopackageExt = File.createTempFile("test", "." + GeoPackageConstants.GEOPACKAGE_EXTENDED_EXTENSION);
        geopackageExt.deleteOnExit();

        File nonGeo = File.createTempFile("test", ".txt");
        nonGeo.deleteOnExit();

        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = support.createMock(DataRegistry.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        support.replayAll();

        GeoPackageImporter importer = new GeoPackageImporter(registry, uiRegistry, New.set());

        assertTrue(importer.canImport(geopackage, null));
        assertTrue(importer.canImport(geopackageExt, null));
        assertFalse(importer.canImport(nonGeo, null));
        assertFalse(importer.canImport(new URL("http://somehost/file.gpkg"), null));

        support.verifyAll();
    }

    /**
     * Tests the name of the import file menu.
     */
    @Test
    public void testGetImportMenuItem()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = support.createMock(DataRegistry.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        support.replayAll();

        GeoPackageImporter importer = new GeoPackageImporter(registry, uiRegistry, New.set());

        assertEquals("Import GPKG File", importer.getImportSingleFileMenuItemName());

        support.verifyAll();
    }

    /**
     * Tests the name of the importer.
     */
    @Test
    public void testGetName()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = support.createMock(DataRegistry.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        support.replayAll();

        GeoPackageImporter importer = new GeoPackageImporter(registry, uiRegistry, New.set());

        assertEquals("GPKG File", importer.getName());
        assertNotNull(importer.getDescription());
        assertNull(importer.getFileChooserAccessory());
        assertNull(importer.getImportMultiFileMenuItemName());
        assertEquals(11, importer.getPrecedence());
        assertFalse(importer.importsFileGroups());
        assertTrue(importer.importsFiles());
        assertFalse(importer.importsURLs());

        support.verifyAll();
    }

    /**
     * Tests getting supported file extensions.
     */
    @Test
    public void testGetSupportedFileExtensions()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = support.createMock(DataRegistry.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        support.replayAll();

        GeoPackageImporter importer = new GeoPackageImporter(registry, uiRegistry, New.set());

        List<String> extensions = importer.getSupportedFileExtensions();

        assertEquals(2, extensions.size());
        assertEquals(GeoPackageConstants.GEOPACKAGE_EXTENSION, extensions.get(0));
        assertEquals(GeoPackageConstants.GEOPACKAGE_EXTENDED_EXTENSION, extensions.get(1));

        support.verifyAll();
    }

    /**
     * Tests importing the ERDC_Whitehorse_GeoPackage.gpkg file.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws IOException Bad IO.
     */
    @Test
    public void testImportFile1() throws InterruptedException, IOException
    {
        String filePath = System.getProperty("geopackageFile1", "X:\\geopackageFiles\\ERDC_Whitehorse_GeoPackage.gpkg");
        List<GeoPackageTile> tiles = New.list();
        List<InputStream> tileImages = New.list();
        List<GeoPackageLayer> layers = runTest(filePath, New.set(), New.set("WhiteHorse"), tiles, tileImages);

        assertEquals(1, layers.size());
        assertTrue(layers.get(0) instanceof GeoPackageTileLayer);
        GeoPackageTileLayer tileLayer = (GeoPackageTileLayer)layers.get(0);

        assertEquals(7730, tileLayer.getRecordCount());
        assertEquals(11, tileLayer.getMinZoomLevel());
        assertEquals(18, tileLayer.getMaxZoomLevel());

        int startWidthAndHeight = 2;
        for (long i = 11; i < 19; i++)
        {
            TileMatrix matrix = tileLayer.getZoomLevelToMatrix().get(i);

            assertEquals(startWidthAndHeight, matrix.getMatrixHeight());
            assertEquals(startWidthAndHeight, matrix.getMatrixWidth());

            startWidthAndHeight *= 2;
        }

        assertEquals(7730, tiles.size());
        assertEquals(7730, tileImages.size());

        GeographicBoundingBox wholeBox = new GeographicBoundingBox(new GeographicPosition(LatLonAlt.createFromDegrees(59, -134)),
                new GeographicPosition(LatLonAlt.createFromDegrees(61, -136)));

        assertTrue(wholeBox.contains(tileLayer.getBoundingBox()));
        for (GeoPackageTile tile : tiles)
        {
            assertTrue(wholeBox.contains(tile.getBoundingBox()));
        }

        for (InputStream image : tileImages)
        {
            assertTrue(image.available() > 0);
        }
    }

    /**
     * Tests importing the gdal_sample.gpkg.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws IOException Bad IO.
     */
    @Test
    public void testImportFile2() throws InterruptedException, IOException
    {
        String filePath = System.getProperty("geopackageFile2", "X:\\geopackageFiles\\gdal_sample.gpkg");
        List<GeoPackageLayer> layers = runTest(filePath,
                New.set("geomcollection2d", "geomcollection3d", "geometry2d", "geometry3d", "linestring2d", "linestring3d",
                        "multilinestring2d", "multilinestring3d", "multipoint2d", "multipoint3d", "multipolygon2d",
                        "multipolygon3d", "point2d", "point3d", "polygon2d", "polygon3d"),
                New.set(), New.list(), New.list());

        assertCounts(layers, new int[] { 2, 2, 2, 2, 2, 2, 5, 8, 2, 2, 2, 2, 2, 2, 5, 8, }, false);
    }

    /**
     * Tests importing the geonames_belgium.gpkg.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws IOException Bad IO.
     */
    @Test
    public void testImportFile3() throws InterruptedException, IOException
    {
        String filePath = System.getProperty("geopackageFile3", "X:\\geopackageFiles\\geonames_belgium.gpkg");
        List<GeoPackageLayer> layers = runTest(filePath, New.set("administrative", "hydrography", "leisure", "places", "roads",
                "spots", "terrain", "undersea", "vegetation"), New.set(), New.list(), New.list());

        assertCounts(layers, new int[] { 11438, 4652, 1356, 36623, 275, 17112, 3202, 7, 2701, }, true);
    }

    /**
     * Tests importing the haiti-vectors-split.gpkg.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws IOException Bad IO.
     */
    @Test
    public void testImportFile4() throws InterruptedException, IOException
    {
        String filePath = System.getProperty("geopackageFile4", "X:\\geopackageFiles\\haiti-vectors-split.gpkg");
        List<GeoPackageLayer> layers = runTest(filePath, New.set("linear_features", "point_features", "polygon_features"),
                New.set(), New.list(), New.list());

        assertCounts(layers, new int[] { 8020, 14726, 4392, }, true);
    }

    /**
     * Tests importing the simple_sewer_features.gpkg.
     *
     * @throws InterruptedException Don't interrupt.
     * @throws IOException Bad IO.
     */
    @Test
    public void testImportFile5() throws InterruptedException, IOException
    {
        String filePath = System.getProperty("geopackageFile5", "X:\\geopackageFiles\\simple_sewer_features.gpkg");
        List<GeoPackageLayer> layers = runTest(filePath, New.set("foul_sewer", "s_manhole", "surface_water_sewer"), New.set(),
                New.list(), New.list());

        assertCounts(layers, new int[] { 69, 82, 21 }, true);
    }

    /**
     * The answer to addModels.
     *
     * @param layers The list to add the deposit to.
     * @param tiles The list to add tile deposits to.
     * @param tileImages The list to add tile image deposits to.
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] addModelsAnswer(List<GeoPackageLayer> layers, List<GeoPackageTile> tiles, List<InputStream> tileImages)
    {
        DefaultCacheDeposit<?> cacheDeposit = (DefaultCacheDeposit<?>)EasyMock.getCurrentArguments()[0];

        DataModelCategory category = cacheDeposit.getCategory();

        long[] ids = new long[0];
        if (GeoPackageLayer.class.getName().equals(category.getCategory()))
        {
            DefaultCacheDeposit<GeoPackageLayer> deposit = (DefaultCacheDeposit<GeoPackageLayer>)EasyMock
                    .getCurrentArguments()[0];
            for (GeoPackageLayer layer : deposit.getInput())
            {
                layers.add(layer);
            }
            ids = new long[deposit.getInput().size()];
            for (int i = 0; i < ids.length; i++)
            {
                ids[i] = i;
            }
        }
        else if (Image.class.getName().equals(category.getCategory()))
        {
            DefaultCacheDeposit<InputStream> deposit = (DefaultCacheDeposit<InputStream>)EasyMock.getCurrentArguments()[0];
            tileImages.addAll(deposit.getInput());
        }
        else if (GeoPackageTile.class.getName().equals(category.getCategory()))
        {
            DefaultCacheDeposit<GeoPackageTile> deposit = (DefaultCacheDeposit<GeoPackageTile>)EasyMock.getCurrentArguments()[0];
            tiles.addAll(deposit.getInput());
        }

        return ids;
    }

    /**
     * The answer for the addTaskActivity call.
     *
     * @param taskActivities The list to add the task activity to.
     * @return Null.
     */
    private Void addTaskActivityAnswer(List<CancellableTaskActivity> taskActivities)
    {
        CancellableTaskActivity ta = (CancellableTaskActivity)EasyMock.getCurrentArguments()[0];
        taskActivities.add(ta);

        return null;
    }

    /**
     * Asserts the record count of each layer.
     *
     * @param layers The layer.
     * @param expectedCounts The expected counts.
     * @param expectGeometry True if we should expect a geometry column, false
     *            if the data doesn't have geometries.
     */
    private void assertCounts(List<GeoPackageLayer> layers, int[] expectedCounts, boolean expectGeometry)
    {
        for (int i = 0; i < layers.size(); i++)
        {
            GeoPackageFeatureLayer layer = (GeoPackageFeatureLayer)layers.get(i);
            assertEquals(expectedCounts[i], layer.getRecordCount());

            List<Map<String, Serializable>> data = layer.getData();

            assertEquals(data.size(), layer.getRecordCount());

            for (Map<String, Serializable> row : data)
            {
                if (expectGeometry)
                {
                    Geometry geometry = (Geometry)row.get(GeoPackageColumns.GEOMETRY_COLUMN);
                    assertNotNull(geometry);
                    assertTrue(row.size() > 1);

                    assertTrue(geometry.getCoordinates().length > 0);
                    for (Coordinate coord : geometry.getCoordinates())
                    {
                        assertTrue(coord.x >= -180 && coord.x <= 180);
                        assertTrue(coord.y >= -90 && coord.y <= 90);
                    }
                }
                else
                {
                    assertFalse(row.isEmpty());
                }
            }
        }
    }

    /**
     * Creates an easy mocked callback.
     *
     * @param support Used to create the callback.
     * @param geoFile The expected geo file object.
     * @param latch That latch to count down when called.
     * @return The mocked {@link ImportCallback}.
     */
    private ImportCallback createCallback(EasyMockSupport support, File geoFile, CountDownLatch latch)
    {
        ImportCallback callback = support.createMock(ImportCallback.class);

        callback.fileImportComplete(EasyMock.eq(true), EasyMock.eq(geoFile), EasyMock.isNull());
        EasyMock.expectLastCall().andAnswer(() ->
        {
            latch.countDown();
            return null;
        });

        return callback;
    }

    /**
     * Creates an easy mocked {@link DataRegistry}.
     *
     * @param support used to create the mock.
     * @param layers The list to add the layers to.
     * @param tiles The list to add tile deposits to.
     * @param tileImages The list to add tile image deposits to.
     * @return The mocked {@link DataRegistry}.
     */
    @SuppressWarnings("unchecked")
    private DataRegistry createDataRegistry(EasyMockSupport support, List<GeoPackageLayer> layers, List<GeoPackageTile> tiles,
            List<InputStream> tileImages)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.addModels(EasyMock.isA(DefaultCacheDeposit.class)))
                .andAnswer(() -> addModelsAnswer(layers, tiles, tileImages)).atLeastOnce();
        EasyMock.expect(registry.performLocalQuery(EasyMock.isA(DefaultQuery.class))).andReturn(new long[] { 0 });

        return registry;
    }

    /**
     * Creates an easy mocked UI registry.
     *
     * @param support Used to create the mock.
     * @param taskActivities the list of task activities to add to.
     * @return The mocked {@link UIRegistry}.
     */
    private UIRegistry createUIRegistry(EasyMockSupport support, List<CancellableTaskActivity> taskActivities)
    {
        MenuBarRegistry menuBar = support.createMock(MenuBarRegistry.class);
        menuBar.addTaskActivity(EasyMock.isA(CancellableTaskActivity.class));
        EasyMock.expectLastCall().andAnswer(() -> addTaskActivityAnswer(taskActivities));

        UIRegistry registry = support.createMock(UIRegistry.class);
        EasyMock.expect(registry.getMenuBarRegistry()).andReturn(menuBar);

        return registry;
    }

    /**
     * Gets the file object, given a path.
     *
     * @param filePath The file path.
     * @return The geo file.
     */
    private File getFile(String filePath)
    {
        File file = new File(filePath);
        if (!file.exists())
        {
            String linuxPath = filePath.replace("\\", "/");
            linuxPath = linuxPath.replace("X:", "/data");
            file = new File(linuxPath);
        }

        return file;
    }

    /**
     * Runs the import geopackage file test on the specified file.
     *
     * @param filePath The path to the geo package file.
     * @param expectedFeatureLayers The expected feature layers to be imported.
     * @param expectedTileLayers The expected tile layers.
     * @param tiles The list to add tile deposits to.
     * @param tileImages The list to add tile image deposits to.
     * @return the layers deposited.
     * @throws InterruptedException Don't interrupt.
     * @throws IOException Bad IO.
     */
    private List<GeoPackageLayer> runTest(String filePath, Set<String> expectedFeatureLayers, Set<String> expectedTileLayers,
            List<GeoPackageTile> tiles, List<InputStream> tileImages)
        throws InterruptedException, IOException
    {
        File geoFile = getFile(filePath);
        File existingFile = File.createTempFile("existing", "." + GeoPackageConstants.GEOPACKAGE_EXTENSION);
        existingFile.deleteOnExit();

        List<CancellableTaskActivity> tas = New.list();
        List<GeoPackageLayer> layers = New.list();
        CountDownLatch latch = new CountDownLatch(1);

        EasyMockSupport support = new EasyMockSupport();

        UIRegistry uiRegistry = createUIRegistry(support, tas);
        DataRegistry registry = createDataRegistry(support, layers, tiles, tileImages);
        ImportCallback callback = createCallback(support, geoFile, latch);

        support.replayAll();

        GeoPackageImporter importer = new GeoPackageImporter(registry, uiRegistry, New.set(existingFile.toString()));

        assertTrue(importer.canImport(geoFile, null));

        importer.importFile(geoFile, callback);

        assertTrue(latch.await(1, TimeUnit.MINUTES));

        assertEquals(1, tas.size());
        assertTrue(tas.get(0).isActive());
        assertTrue(tas.get(0).isComplete());
        assertTrue(tas.get(0).getLabelValue(), tas.get(0).getLabelValue().contains(geoFile.getPath()));

        assertEquals(expectedFeatureLayers.size() + expectedTileLayers.size(), layers.size());

        for (GeoPackageLayer layer : layers)
        {
            if (layer.getLayerType() == LayerType.FEATURE)
            {
                assertTrue(expectedFeatureLayers.contains(layer.getName()));
            }
            else
            {
                assertTrue(expectedTileLayers.contains(layer.getName()));
            }
        }

        support.verifyAll();

        return layers;
    }
}
