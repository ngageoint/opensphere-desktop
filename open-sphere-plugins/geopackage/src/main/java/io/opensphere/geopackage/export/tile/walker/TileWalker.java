package io.opensphere.geopackage.export.tile.walker;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang.ArrayUtils;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.export.model.ExportModel;

/**
 * Walks tiles breadth first (walks all tiles at one zoom level before going to
 * the next zoom level) and sends the tile information to a tile consumer for
 * further processing.
 */
public class TileWalker
{
    /**
     * Used for geometry calculations.
     */
    private final GeometryCalculator myGeomCalc = new GeometryCalculator();

    /**
     * Gets top level {@link TileGeometry} for a given layer.
     */
    private final TopLevelGeometryProvider myGeometryProvider;

    /** The maximum zoom level. */
    private final int myMaxZoomLevel;

    /** The export model. */
    private final ExportModel myModel;

    /**
     * Constructs a new {@link TileWalker}.
     *
     * @param geometryRegistry The geometry registry.
     * @param model The export model
     */
    public TileWalker(GeometryRegistry geometryRegistry, ExportModel model)
    {
        myGeometryProvider = new TopLevelGeometryProvider(geometryRegistry);
        myMaxZoomLevel = model.getMaxZoomLevel();
        myModel = model;
    }

    /**
     * Gets all the geometries for the given layer that are displayed in the
     * given bounding box.
     *
     * @param typeKey The type key.
     * @param boundingBox The bounding box.
     * @param tileConsumer The consumer interested in the tiles as we walk
     *            through them.
     */
    public void getGeometries(String typeKey, GeographicBoundingBox boundingBox, Consumer<TileInfo> tileConsumer)
    {
        List<AbstractTileGeometry<?>> bestTopLevels = getTopLevelGeometries(typeKey, boundingBox);

        AbstractTileGeometry<?>[][] grid = GridUtilities.getGrid(bestTopLevels);
        List<TileInfo> tileInfos = New.list();
        for (int r = 0; r < grid.length; r++)
        {
            AbstractTileGeometry<?>[] row = grid[r];
            for (int c = 0; c < row.length; c++)
            {
                TileInfo tileInfo = new TileInfo(row[c], row[c].getGeneration(), r, c);
                tileInfos.add(tileInfo);
            }
        }

        CancellableTaskActivity taskActivity = myModel.getProgressReporter().getTaskActivity();
        int zoomLevel = tileInfos.get(0).getZoomLevel();
        while (zoomLevel <= myMaxZoomLevel)
        {
            for (TileInfo tileInfo : tileInfos)
            {
                goDeep(tileInfo, tileConsumer, zoomLevel);

                if (taskActivity.isCancelled())
                {
                    return;
                }
            }
            zoomLevel++;
        }
    }

    /**
     * Gets the top level geometries for the given data type and bounding box.
     *
     * @param typeKey The data type key to get geometries for.
     * @param boundingBox The bounding box the geometries need to be contained
     *            in.
     * @return The top level geometries.
     */
    public List<AbstractTileGeometry<?>> getTopLevelGeometries(String typeKey, GeographicBoundingBox boundingBox)
    {
        List<AbstractTileGeometry<?>> tops = myGeometryProvider.getTopLevelGeometries(typeKey);

        tops = myGeomCalc.getContainingGeometries(tops, boundingBox);
        List<AbstractTileGeometry<?>> bestTopLevels = getBestGeometriesInBox(tops, boundingBox, 0);
        for (AbstractTileGeometry<?> bestTopLevel : bestTopLevels)
        {
            bestTopLevel.clearChildren();
        }

        return bestTopLevels;
    }

    /**
     * Traverses the tiles' children.
     *
     * @param info The tile to traverse.
     * @param tileConsumer The consumer interested in the tiles as we walk
     *            through them.
     * @param zoomLevel The zoom level to traverse to.
     */
    private void goDeep(TileInfo info, Consumer<TileInfo> tileConsumer, int zoomLevel)
    {
        if (info.getZoomLevel() == zoomLevel)
        {
            tileConsumer.accept(info);
        }
        else
        {
            List<? extends AbstractTileGeometry<?>> children = New.list(info.getGeometry().getChildren(true));
            if (!children.isEmpty())
            {
                CancellableTaskActivity taskActivity = myModel.getProgressReporter().getTaskActivity();
                int[] newXs = calculateNewXorY(info.getCol());
                int[] newYs = calculateNewXorY(info.getRow());
                int index = 0;
                boolean isTms = children.get(0).getBounds().getCenter().asVector2d().getY() < children.get(children.size() - 1)
                        .getBounds().getCenter().asVector2d().getY();
                if (isTms)
                {
                    ArrayUtils.reverse(newYs);
                }

                for (int newY : newYs)
                {
                    for (int newX : newXs)
                    {
                        TileInfo childInfo = new TileInfo(children.get(index++), info.getZoomLevel() + 1, newY, newX);
                        goDeep(childInfo, tileConsumer, zoomLevel);

                        if (taskActivity.isCancelled())
                        {
                            return;
                        }
                    }
                }

                info.getGeometry().clearChildren();
            }
        }
    }

    /**
     * Calculates the new x or y coordinates for the divided tiles.
     *
     * @param currentXorY The current x or y coordinate of the tile we are
     *            dividing.
     * @return The new x or y coordinates of the sub tiles.
     */
    private int[] calculateNewXorY(int currentXorY)
    {
        int count = currentXorY + 1;
        int newCount = count * 2;

        return new int[] { newCount - 2, newCount - 1 };
    }

    /**
     * Recursively collects all the geometries that are contained within the
     * specified bounding box.
     *
     * @param geometries The geometries that overlap or are contained in the
     *            bounding box at the specified zoom level.
     * @param boundingBox The bounding box.
     * @param currentZoomLevel The current zoom level.
     * @return The geometries in the bounding box.
     */
    private List<AbstractTileGeometry<?>> getBestGeometriesInBox(Collection<? extends AbstractTileGeometry<?>> geometries,
            GeographicBoundingBox boundingBox, int currentZoomLevel)
    {
        List<AbstractTileGeometry<?>> bestTopLevel = New.list();

        Collection<AbstractTileGeometry<?>> allSubTiles = New.collection();

        boolean anyAtFullContainment = myGeomCalc.anyAtFullContainment(geometries, boundingBox);

        boolean isDeepestLevel = false;
        for (AbstractTileGeometry<?> geometry : geometries)
        {
            isDeepestLevel = geometry.getSplitJoinRequestProvider() == null;
            if (!isDeepestLevel)
            {
                Collection<? extends AbstractTileGeometry<?>> subTiles = geometry.getChildren(true);
                if (!anyAtFullContainment)
                {
                    subTiles = myGeomCalc.getContainingGeometries(subTiles, boundingBox);
                }
                allSubTiles.addAll(subTiles);
            }
        }

        if (!anyAtFullContainment && !allSubTiles.isEmpty() && currentZoomLevel < myMaxZoomLevel)
        {
            bestTopLevel.addAll(getBestGeometriesInBox(allSubTiles, boundingBox, currentZoomLevel + 1));
        }
        else if (anyAtFullContainment || isDeepestLevel || currentZoomLevel >= myMaxZoomLevel)
        {
            bestTopLevel.addAll(geometries);
        }

        return bestTopLevel;
    }
}
