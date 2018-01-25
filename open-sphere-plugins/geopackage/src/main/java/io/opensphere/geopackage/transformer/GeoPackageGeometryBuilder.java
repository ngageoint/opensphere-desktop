package io.opensphere.geopackage.transformer;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.cache.matcher.NumberPropertyMatcher;
import io.opensphere.core.cache.matcher.NumberPropertyMatcher.OperatorType;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.Divider;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.terrain.util.ElevationImageReader;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTile;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.util.Constants;

/**
 * Given a zoom level and a {@link GeoPackageLayer} this class knows how to
 * build TileGeometries of the geopackage tiles at that zoom level.
 */
public class GeoPackageGeometryBuilder
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(GeoPackageGeometryBuilder.class);

    /**
     * Used to get the {@link GeoPackageTile} information for a given zoom
     * level.
     */
    private final DataRegistry myRegistry;

    /**
     * The ui registry used to notify the user of geopackage queries.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructs a new geometry builder.
     *
     * @param registry Used to get the {@link GeoPackageTile} information for a
     *            given zoom level.
     * @param uiRegistry The ui registry used to notify the user of geopackage
     *            queries.
     */
    public GeoPackageGeometryBuilder(DataRegistry registry, UIRegistry uiRegistry)
    {
        myRegistry = registry;
        myUIRegistry = uiRegistry;
    }

    /**
     * Builds the geometries for the given zoom level.
     *
     * @param layer The layer to build the geometries for.
     * @param zoomLevel The zoom level to build the geometries for.
     * @param props The rendering properties.
     * @param divider The divider to use to split the geometries.
     * @return The list of geometries or empty if none were found.
     */
    public List<AbstractTileGeometry<?>> buildGeometries(GeoPackageTileLayer layer, long zoomLevel, TileRenderProperties props,
            Divider<GeographicPosition> divider)
    {
        List<AbstractTileGeometry<?>> geometries = Collections.synchronizedList(New.list());

        String terrainExtension = layer.getExtensions().get(Constants.TERRAIN_EXTENSION);

        GeoPackageQueryTracker tracker = new GeoPackageQueryTracker(myUIRegistry, layer.getName());
        List<GeoPackageTile> tiles = getTiles(layer, zoomLevel);
        for (GeoPackageTile tile : tiles)
        {
            if (StringUtils.isEmpty(terrainExtension))
            {
                TileGeometry.Builder<GeographicPosition> tileBuilder = new TileGeometry.Builder<GeographicPosition>();
                tileBuilder.setMinimumDisplaySize(384);
                tileBuilder.setMaximumDisplaySize(1280);
                tileBuilder.setRapidUpdate(true);
                setCommon(layer, tile, tileBuilder, divider, tracker);

                geometries.add(new TileGeometry(tileBuilder, props, null, layer.getId()));
            }
            else
            {
                TerrainTileGeometry.Builder<GeographicPosition> tileBuilder = new TerrainTileGeometry.Builder<GeographicPosition>();
                tileBuilder.setMinimumDisplaySize(50);
                tileBuilder.setMaximumDisplaySize(250);
                setCommon(layer, tile, tileBuilder, divider, tracker);

                ElevationImageReader elevationReader = null;
                ServiceLoader<ElevationImageReader> loader = ServiceLoader.load(ElevationImageReader.class);
                for (ElevationImageReader reader : loader)
                {
                    if (terrainExtension.equals(reader.getImageFormat()))
                    {
                        elevationReader = reader;
                        elevationReader.init(tile.getBoundingBox(), -Short.MIN_VALUE, "EPSG:4326", layer.getId());
                        break;
                    }
                }

                if (elevationReader != null)
                {
                    tileBuilder.setElevationReader(elevationReader);
                    geometries.add(new TerrainTileGeometry(tileBuilder, props, layer.getId()));
                }
                else
                {
                    LOGGER.warn("Could not load terrain for " + layer.getName() + " could not find elevation reader for format "
                            + terrainExtension);
                }
            }
        }

        return geometries;
    }

    /**
     * Gets the {@link GeoPackageTile} from the registry at the specified zoom
     * level.
     *
     * @param layer The layer to get tiles for.
     * @param zoomLevel The zoom level to get them for.
     * @return The list of tiles at the zoom level, or empty if none found.
     */
    private List<GeoPackageTile> getTiles(GeoPackageLayer layer, long zoomLevel)
    {
        List<GeoPackageTile> tiles = New.list();

        DataModelCategory category = new DataModelCategory(layer.getPackageFile(), layer.getName(),
                GeoPackageTile.class.getName());
        NumberPropertyMatcher<Long> matcher = new NumberPropertyMatcher<Long>(
                GeoPackagePropertyDescriptors.ZOOM_LEVEL_PROPERTY_DESCRIPTOR, OperatorType.EQ, Long.valueOf(zoomLevel));
        SimpleQuery<GeoPackageTile> query = new SimpleQuery<>(category,
                GeoPackagePropertyDescriptors.GEOPACKAGE_TILE_PROPERTY_DESCRIPTOR, New.list(matcher));
        myRegistry.performQuery(query);
        if (query.getResults() != null && !query.getResults().isEmpty())
        {
            tiles.addAll(query.getResults());
        }

        return tiles;
    }

    /**
     * Sets some common parameters between terrain tiles and raster tiles in the
     * builder.
     *
     * @param layer The layer to build the geometries for.
     * @param tile The tile we are building the geometry for.
     * @param builder The builder used to build the tile geometry.
     * @param divider The divider to use to split the geometries.
     * @param queryTracker Used to notify the user of the on going geopackage
     *            queries.
     */
    private void setCommon(GeoPackageTileLayer layer, GeoPackageTile tile,
            AbstractTileGeometry.Builder<GeographicPosition> builder, Divider<GeographicPosition> divider,
            GeoPackageQueryTracker queryTracker)
    {
        builder.setBounds(tile.getBoundingBox());
        ImageManager imageManager = new ImageManager(
                new ZYXImageKey((int)tile.getZoomLevel(), tile.getY(), tile.getX(), tile.getBoundingBox()),
                new GeoPackageImageProvider(myRegistry, layer, queryTracker));
        builder.setImageManager(imageManager);
        if (layer.getMaxZoomLevel() > tile.getZoomLevel())
        {
            builder.setDivider(divider);
        }
    }
}
