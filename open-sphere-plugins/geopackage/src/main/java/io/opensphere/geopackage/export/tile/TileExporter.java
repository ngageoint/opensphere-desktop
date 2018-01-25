package io.opensphere.geopackage.export.tile;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.core.export.ExportException;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.export.model.ExportModel;
import io.opensphere.geopackage.export.model.GeoPackageSubExporter;
import io.opensphere.geopackage.export.tile.walker.TileWalker;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * Given a list of {@link DataTypeInfo} this class will export all with tile
 * data to geopackage tables.
 */
public class TileExporter implements GeoPackageSubExporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TileExporter.class);

    /** The export model. */
    private final ExportModel myModel;

    /** The data types to export. */
    private final Collection<DataTypeInfo> myDataTypes;

    /** The tile walker. */
    private final TileWalker myTileWalker;

    /** The bounding box. */
    private final GeographicBoundingBox myBbox;

    /**
     * Determines if the data type if supported by this exporter type.
     *
     * @param dataType the data type
     * @return whether the data type is exportable
     */
    public static boolean isExportable(DataTypeInfo dataType)
    {
        boolean exportable = false;
        MapVisualizationInfo mapVisInfo = dataType.getMapVisualizationInfo();
        if (mapVisInfo != null)
        {
            MapVisualizationType visualizationType = mapVisInfo.getVisualizationType();
            exportable = visualizationType == MapVisualizationType.IMAGE_TILE && mapVisInfo.getTileLevelController() != null
                    || visualizationType == MapVisualizationType.TERRAIN_TILE;
        }
        return exportable;
    }

    /**
     * Constructs a new tile exporter.
     *
     * @param model the export model
     * @param geometryRegistry The geometry registry
     * @param bbox The bounding box
     */
    public TileExporter(ExportModel model, GeometryRegistry geometryRegistry, GeographicBoundingBox bbox)
    {
        myModel = model;
        myDataTypes = model.getDataTypesToExport().stream().filter(t -> isExportable(t)).collect(Collectors.toList());
        myTileWalker = new TileWalker(geometryRegistry, model);
        myBbox = bbox;
    }

    @Override
    public Collection<DataTypeInfo> getExportableTypes()
    {
        return myDataTypes;
    }

    @Override
    public int getRecordCount()
    {
        int estimatedRecordCount = 0;
        for (DataTypeInfo dataType : myDataTypes)
        {
            List<AbstractTileGeometry<?>> topLevelGeometries = myTileWalker.getTopLevelGeometries(dataType.getTypeKey(), myBbox);
            if (!topLevelGeometries.isEmpty())
            {
                int minZoomLevel = topLevelGeometries.iterator().next().getGeneration();
                int power = myModel.getMaxZoomLevel() - minZoomLevel;
                final double totalToMaxRatio = 1.33;
                double estLayerRecordCount = (int)(Math.pow(4, power) * topLevelGeometries.size() * totalToMaxRatio);
                estimatedRecordCount += estLayerRecordCount;
            }
        }
        return estimatedRecordCount;
    }

    @Override
    public void export(ExportModel model) throws ExportException
    {
        ExportResources exportResources = new ExportResources(model);

        try
        {
            for (DataTypeInfo dataType : myDataTypes)
            {
                doExport(exportResources, dataType);
            }

            exportResources.getCompletionLatch().await();
        }
        catch (InterruptedException e)
        {
            LOGGER.error(e, e);
        }
        finally
        {
            exportResources.close(false);
        }
    }

    /**
     * Does the export.
     *
     * @param exportResources the export resources
     * @param dataType the data type
     */
    private void doExport(ExportResources exportResources, DataTypeInfo dataType)
    {
        ExportModel model = exportResources.getModel();

        TileImageByteProvider imageByteProvider = new TileImageByteProvider();
        List<AbstractTileGeometry<?>> topLevelGeoms = myTileWalker.getTopLevelGeometries(dataType.getTypeKey(), myBbox);
        DBWriter writer = new DBWriter(model, dataType, topLevelGeoms, exportResources.getDbExecutor(),
                exportResources.getCompletionLatch());
        writer.init();

        myTileWalker.getGeometries(dataType.getTypeKey(), myBbox, tileInfo ->
        {
            writer.getTileCount().incrementAndGet();
//            LOGGER.info("Walked " + tileInfo);

            exportResources.executeServerCommand(() ->
            {
                CancellableTaskActivity taskActivity = model.getProgressReporter().getTaskActivity();
                if (taskActivity.isCancelled())
                {
                    writer.finish();
                    exportResources.close(true);
                    return;
                }

//                LOGGER.info("Downloading " + tileInfo);
                GeoPackageImage image = imageByteProvider.getImageBytes(tileInfo.getGeometry());

                if (taskActivity.isCancelled())
                {
                    writer.finish();
                    exportResources.close(true);
                    return;
                }

                writer.addTile(tileInfo, image);
            });
        });
    }

    /** Export resources. */
    private static class ExportResources
    {
        /** The server thread count. */
        private static final int SERVER_THREAD_COUNT = 16;

        /** The server executor queue. */
        private final BlockingQueue<Runnable> myServerQueue = new LinkedBlockingQueue<>(SERVER_THREAD_COUNT);

        /** The server executor. */
        private final ExecutorService myServerExecutor = new ThreadPoolExecutor(SERVER_THREAD_COUNT, SERVER_THREAD_COUNT, 0L,
                TimeUnit.MILLISECONDS, myServerQueue);

        /** The DB executor. */
        private final ExecutorService myDbExecutor = Executors.newSingleThreadExecutor();

        /** The export model. */
        private final ExportModel myModel;

        /** The completion latch. */
        private final CountDownLatch myCompletionLatch;

        /**
         * Constructor.
         *
         * @param model The export model
         */
        public ExportResources(ExportModel model)
        {
            myModel = model;
            myCompletionLatch = new CountDownLatch(model.getDataTypesToExport().size());
        }

        /**
         * Executes a server command.
         *
         * @param command the server command
         */
        public void executeServerCommand(Runnable command)
        {
            // Sleep to avoid an execution rejected exception
            while (myServerQueue.remainingCapacity() == 0)
            {
                ThreadUtilities.sleep(500);
            }
            myServerExecutor.execute(command);
        }

        /**
         * Gets the DB executor.
         *
         * @return the DB executor
         */
        public ExecutorService getDbExecutor()
        {
            return myDbExecutor;
        }

        /**
         * Gets the export model.
         *
         * @return the export model
         */
        public ExportModel getModel()
        {
            return myModel;
        }

        /**
         * Gets the completion latch.
         *
         * @return the completion latch
         */
        public CountDownLatch getCompletionLatch()
        {
            return myCompletionLatch;
        }

        /**
         * Closes (finishes) everything.
         *
         * @param hardShutdown whether to do a hard shutdown
         */
        @SuppressWarnings("unused")
        public void close(boolean hardShutdown)
        {
            if (!myServerExecutor.isShutdown())
            {
                if (hardShutdown)
                {
                    myServerExecutor.shutdownNow();
                    for (DataTypeInfo dataType : myModel.getDataTypesToExport())
                    {
                        myCompletionLatch.countDown();
                    }
                }
                else
                {
                    myServerExecutor.shutdown();
                }
                myDbExecutor.execute(myModel.getGeoPackage()::close);
                myDbExecutor.shutdown();
                myModel.getProgressReporter().close();

                LOGGER.info("Export finished");
            }
        }
    }
}
