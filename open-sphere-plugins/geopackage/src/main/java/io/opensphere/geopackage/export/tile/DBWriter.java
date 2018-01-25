package io.opensphere.geopackage.export.tile;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TIntIntHashMap;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.geopackage.export.model.ExportModel;
import io.opensphere.geopackage.export.tile.walker.TileInfo;
import io.opensphere.geopackage.util.Constants;
import io.opensphere.mantle.data.DataTypeInfo;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/** Writes to a geopackage database. */
public class DBWriter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DBWriter.class);

    /** The export model. */
    private final ExportModel myModel;

    /** The data type. */
    private final DataTypeInfo myDataType;

    /** The top level geometries. */
    private final Collection<AbstractTileGeometry<?>> myTopLevelGeometries;

    /** The executor. */
    private final ExecutorService myExecutor;

    /** The completion latch. */
    private final CountDownLatch myCompletionLatch;

    /** The current count of tiles to be processed. */
    private final AtomicInteger myTileCount = new AtomicInteger();

    /** The zoom level to max matrix column map. */
    private final TIntIntHashMap myZoomToMaxCol = new TIntIntHashMap();

    /** The zoom level to max matrix row map. */
    private final TIntIntHashMap myZoomToMaxRow = new TIntIntHashMap();

    /** The table name. */
    private String myTableName;

    /** The tile matrix set. */
    private TileMatrixSet myTileMatrixSet;

    /** The tile DAO. */
    private TileDao myTileDao;

    /** The tile matrix DAO. */
    private TileMatrixDao myTileMatrixDao;

    /** The most recent image. */
    private GeoPackageImage myMostRecentImage;

    /** Whether we've finished. */
    private boolean myFinished;

    /**
     * Constructor.
     *
     * @param model the export model
     * @param dataType the data type
     * @param topLevelGeometries the top level geometries
     * @param executor the executor
     * @param completionLatch the completion latch
     */
    public DBWriter(ExportModel model, DataTypeInfo dataType, Collection<AbstractTileGeometry<?>> topLevelGeometries,
            ExecutorService executor, CountDownLatch completionLatch)
    {
        myModel = model;
        myDataType = dataType;
        myTopLevelGeometries = topLevelGeometries;
        myExecutor = executor;
        myCompletionLatch = completionLatch;
    }

    /**
     * Gets the tile count to process.
     *
     * @return the tile count
     */
    public AtomicInteger getTileCount()
    {
        return myTileCount;
    }

    /** Init. */
    public void init()
     {
        execute(() ->
        {
            GeoPackage geoPackage = myModel.getGeoPackage();
            myTableName = getTileTableName(geoPackage, myDataType);

            BoundingBox bbox = getTileBoundingBox();
            myTileMatrixSet = geoPackage.createTileTableWithMetadata(myTableName, bbox,
                    ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM, bbox, ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            myTileDao = geoPackage.getTileDao(myTileMatrixSet);
            myTileMatrixDao = geoPackage.getTileMatrixDao();

            addTerrainExtension();
        });
    }

    /**
     * Adds a tile.
     *
     * @param tileInfo the tile info
     * @param image the image tile
     */
    public void addTile(TileInfo tileInfo, GeoPackageImage image)
    {
        execute(() ->
        {
            myMostRecentImage = image;

//            LOGGER.info("Writing " + tileInfo);
            TileRow newRow = myTileDao.newRow();
            newRow.setZoomLevel(tileInfo.getZoomLevel());
            newRow.setTileColumn(tileInfo.getCol());
            newRow.setTileRow(tileInfo.getRow());
            newRow.setTileData(image.getImageBytes().array());
            myTileDao.create(newRow);

            updateMatrixMaps(tileInfo);

            if (myTileCount.decrementAndGet() == 0)
            {
                addTileMatrices(image);

                if (myCompletionLatch != null)
                {
                    myCompletionLatch.countDown();
                }
            }

            myModel.getProgressReporter().getModel().incrementCompletedCount();
        });
    }

    /** Finishes by writing out the tile matrices. */
    public void finish()
    {
        execute(() -> addTileMatrices(myMostRecentImage));
    }

    /**
     * Adds all the tile matrices.
     *
     * @param sampleImage a sample image (for sizing)
     */
    private void addTileMatrices(GeoPackageImage sampleImage)
    {
        if (!myFinished)
        {
            for (int zoom : myZoomToMaxCol.keys())
            {
                addTileMatrix(zoom, myZoomToMaxCol.get(zoom) + 1, myZoomToMaxRow.get(zoom) + 1, sampleImage);
            }
            myFinished = true;
        }
    }

    /**
     * Adds a tile matrix for the given zoom level.
     *
     * @param zoomLevel the zoom level
     * @param matrixWidth the matrix width
     * @param matrixHeight the matrix height
     * @param sampleImage a sample image
     */
    private void addTileMatrix(int zoomLevel, long matrixWidth, long matrixHeight, GeoPackageImage sampleImage)
    {
        // Create the tile matrix for this zoom level
        TileMatrix tileMatrix = new TileMatrix();
        tileMatrix.setContents(myTileMatrixSet.getContents());
        tileMatrix.setZoomLevel(zoomLevel);
        tileMatrix.setMatrixWidth(matrixWidth);
        tileMatrix.setMatrixHeight(matrixHeight);
        if (sampleImage != null)
        {
            tileMatrix.setTileWidth(sampleImage.getWidth());
            tileMatrix.setTileHeight(sampleImage.getHeight());
        }
        else
        {
            tileMatrix.setTileWidth(512);
            tileMatrix.setTileHeight(512);
        }
        try
        {
            myTileMatrixDao.create(tileMatrix);
        }
        catch (SQLException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Adds the terrain extension to the geo package if the tiles are terrain
     * tiles.
     */
    private void addTerrainExtension()
    {
        AbstractTileGeometry<?> sampleGeom = CollectionUtilities.getItemOrNull(myTopLevelGeometries, 0);
        if (sampleGeom instanceof TerrainTileGeometry)
        {
            String imageFormat = ((TerrainTileGeometry)sampleGeom).getReader().getImageFormat();

            Extensions extension = new Extensions();
            extension.setTableName(myTableName);
            extension.setExtensionName(Constants.TERRAIN_EXTENSION);
            extension.setDefinition(imageFormat);
            extension.setScope(ExtensionScopeType.READ_WRITE);

            GeoPackage geoPackage = myModel.getGeoPackage();
            geoPackage.createExtensionsTable();
            try
            {
                geoPackage.getExtensionsDao().create(extension);
            }
            catch (SQLException e)
            {
                LOGGER.error(e, e);
            }
        }
    }

    /**
     * Updates the matrix maps with the tile info.
     *
     * @param tileInfo the tile info
     */
    private void updateMatrixMaps(TileInfo tileInfo)
    {
        int maxWidth = myZoomToMaxCol.get(tileInfo.getZoomLevel());
        if (tileInfo.getCol() > maxWidth)
        {
            myZoomToMaxCol.put(tileInfo.getZoomLevel(), tileInfo.getCol());
        }
        int maxHeight = myZoomToMaxRow.get(tileInfo.getZoomLevel());
        if (tileInfo.getRow() > maxHeight)
        {
            myZoomToMaxRow.put(tileInfo.getZoomLevel(), tileInfo.getRow());
        }
    }

    /**
     * Executes the command.
     *
     * @param command the command
     */
    private void execute(Runnable command)
    {
        if (!myExecutor.isShutdown())
        {
            myExecutor.execute(command);
        }
    }

    /**
     * Gets the bounding box of the tiles.
     *
     * @return the bounding box
     */
    private BoundingBox getTileBoundingBox()
    {
        Set<GeographicPosition> positions = myTopLevelGeometries.stream().flatMap(g -> g.getBounds().getVertices().stream())
                .map(v -> (GeographicPosition)v).collect(Collectors.toSet());
        GeographicBoundingBox tileBbox = GeographicBoundingBox.getMinimumBoundingBox(positions);
        BoundingBox bbox = new BoundingBox(tileBbox.getMinLonD(), tileBbox.getMaxLonD(), tileBbox.getMinLatD(),
                tileBbox.getMaxLatD());
        return bbox;
    }

    /**
     * Gets the tile table name.
     *
     * @param geoPackage the geo package
     * @param dataType the data type
     * @return the tile table name
     */
    private static String getTileTableName(GeoPackage geoPackage, DataTypeInfo dataType)
    {
        String tableName = StringUtilities.replaceSpecialCharacters(dataType.getDisplayName()).replace('-', '_');
        List<String> existingTileTables = geoPackage.getTileTables();
        if (existingTileTables.contains(tableName))
        {
            tableName = StringUtilities.getUniqueName(tableName + "_", existingTileTables);
        }
        return tableName;
    }
}
