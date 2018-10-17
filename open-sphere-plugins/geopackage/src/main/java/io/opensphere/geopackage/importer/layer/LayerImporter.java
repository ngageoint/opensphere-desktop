package io.opensphere.geopackage.importer.layer;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.importer.feature.FeatureImporter;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.model.ProgressModel;
import io.opensphere.geopackage.progress.ProgressReporter;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * Reads a layer information from a geopackage file and puts the necessary
 * information in the {@link DataRegistry}.
 */
public class LayerImporter
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(LayerImporter.class);

    /**
     * Used to import feature data into the system.
     */
    private final FeatureImporter myFeatureImporter = new FeatureImporter();

    /**
     * Used to save the imported data.
     */
    private final DataRegistry myRegistry;

    /**
     * Constructs a new layer importer.
     *
     * @param registry Used to save the imported data.
     */
    public LayerImporter(DataRegistry registry)
    {
        myRegistry = registry;
    }

    /**
     * Imports the layers contained in the specified geopackage.
     *
     * @param geopackage The geopackage to import.
     * @param layers The list to add the the layers that were deposited to.
     * @param ta Monitored to see if user has cancelled import.
     *
     * @return The created progress reporter used to notify progress of import
     *         to user.
     */
    public ProgressReporter importLayers(GeoPackage geopackage, List<GeoPackageLayer> layers, CancellableTaskActivity ta)
    {
        List<String> featureTables = geopackage.getFeatureTables();
        List<String> tiles = geopackage.getTileTables();
        String path = geopackage.getPath();
        String packageName = geopackage.getName();

        Map<String, GeoPackageFeatureLayer> featureLayers = New.map();
        for (String featureTable : featureTables)
        {
            FeatureDao featureDao = geopackage.getFeatureDao(featureTable);
            GeoPackageFeatureLayer featureLayer = new GeoPackageFeatureLayer(packageName, path, featureTable, featureDao.count());
            layers.add(featureLayer);
            featureLayers.put(featureTable, featureLayer);
        }

        ExtensionsDao extensionsDao = geopackage.getExtensionsDao();
        List<Extensions> extensions = null;
        try
        {
            if (extensionsDao.isTableExists())
            {
                extensions = extensionsDao.queryForAll();
            }
        }
        catch (SQLException e)
        {
            LOGGER.error(e, e);
        }

        for (String tile : tiles)
        {
            TileDao tileDao = geopackage.getTileDao(tile);
            layers.add(buildTileLayer(tileDao, packageName, path, tile, extensions));
        }

        ProgressModel model = new ProgressModel();
        ProgressReporter reporter = new ProgressReporter(model, layers, ta);

        myFeatureImporter.importFeatures(geopackage, featureLayers, ta, model);

        DataModelCategory category = new DataModelCategory(path, packageName, GeoPackageLayer.class.getName());
        SerializableAccessor<GeoPackageLayer, GeoPackageLayer> layerAccessor = SerializableAccessor
                .getHomogeneousAccessor(GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR);
        DefaultCacheDeposit<GeoPackageLayer> deposit = new DefaultCacheDeposit<>(category, New.list(layerAccessor), layers, true,
                new Date(Long.MAX_VALUE), false);

        myRegistry.addModels(deposit);

        return reporter;
    }

    /**
     * Builds a {@link GeoPackageTileLayer} that represents the tile table
     * specified by tile.
     *
     * @param tileDao Contains information about the tile layer.
     * @param packageName The geopackage name.
     * @param path The file path to the geopackage file.
     * @param tile The name of the tile layer.
     * @param extensions The list of extensions contained in the geopackage
     *            file.
     * @return A new {@link GeoPackageTileLayer}.
     */
    private GeoPackageTileLayer buildTileLayer(TileDao tileDao, String packageName, String path, String tile,
            List<Extensions> extensions)
    {
        long minZoom = tileDao.getMinZoom();
        int minZoomCount = tileDao.count(minZoom);
        GeoPackageTileLayer tileLayer = new GeoPackageTileLayer(packageName, path, tile, minZoomCount);
        tileLayer.setMaxZoomLevel(tileDao.getMaxZoom());
        tileLayer.setMinZoomLevel(minZoom);

        BoundingBox box = tileDao.getBoundingBox();
        Projection tileProjection = tileDao.getProjection();
        if (!StringUtils.equals(String.valueOf(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM), tileProjection.getCode()))
        {
            ProjectionTransform layerToGeodetic = tileProjection
                    .getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            box = box.transform(layerToGeodetic);
        }

        GeographicBoundingBox boundingBox = new GeographicBoundingBox(
                LatLonAlt.createFromDegrees(box.getMinLatitude(), box.getMinLongitude()),
                LatLonAlt.createFromDegrees(box.getMaxLatitude(), box.getMaxLongitude()));
        tileLayer.setBoundingBox(boundingBox);
        for (TileMatrix matrix : tileDao.getTileMatrices())
        {
            io.opensphere.geopackage.model.TileMatrix lite = new io.opensphere.geopackage.model.TileMatrix(
                    matrix.getMatrixHeight(), matrix.getMatrixWidth());
            tileLayer.getZoomLevelToMatrix().put(Long.valueOf(matrix.getZoomLevel()), lite);
        }

        if (extensions != null)
        {
            for (Extensions extension : extensions)
            {
                if (tile.equals(extension.getTableName()))
                {
                    tileLayer.getExtensions().put(extension.getExtensionName(), extension.getDefinition());
                }
            }
        }

        return tileLayer;
    }
}
