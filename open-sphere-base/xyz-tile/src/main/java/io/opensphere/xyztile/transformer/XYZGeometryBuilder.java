package io.opensphere.xyztile.transformer;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Builds the initial geometries for a given XYZ tile layer.
 */
public class XYZGeometryBuilder
{
    /**
     * The maximum latitude of the Mapboxe's tiles.
     */
    private static final double ourWebMaxLatitude = 85.0511;

    /**
     * Used to get tile images.
     */
    private final DataRegistry myRegistry;

    /**
     * Used to notify the user of tile downloads.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructs a new geometry builder.
     *
     * @param registry Used to get tile images.
     * @param uiRegistry Used to inform the user of tile downloads.
     */
    public XYZGeometryBuilder(DataRegistry registry, UIRegistry uiRegistry)
    {
        myRegistry = registry;
        myUIRegistry = uiRegistry;
    }

    /**
     * Builds the geometries for the given zoom level.
     *
     * @param layer The layer to build the geometries for.
     * @param layerId The id of the layer.
     * @param props The rendering properties.
     * @return The top level geometry for the layer.
     */
    public List<TileGeometry> buildTopGeometry(XYZTileLayerInfo layer, String layerId, TileRenderProperties props)
    {
        TileGeometry.Builder<GeographicPosition> tileBuilder = new TileGeometry.Builder<GeographicPosition>();
        tileBuilder.setMinimumDisplaySize(384);
        tileBuilder.setMaximumDisplaySize(1280);

        double minLatitude = -90;
        double maxLatitude = 90;
        double totalLonDegrees = 360d;
        double minLon = -180;

        if (layer.getFootprint() != null)
        {
            minLatitude = layer.getFootprint().getMinLatD();
            maxLatitude = layer.getFootprint().getMaxLatD();
            minLon = layer.getFootprint().getMinLonD();
            totalLonDegrees = layer.getFootprint().getWidth();
        }
        else if (layer.getProjection() == Projection.EPSG_3857)
        {
            maxLatitude = ourWebMaxLatitude;
            minLatitude = -ourWebMaxLatitude;
        }

        List<TileGeometry> noParentGeometries = New.list();

        Constraints constraints = null;
        if (layer.getTimeSpan() != null)
        {
            constraints = Constraints.createTimeOnlyConstraint(layer.getTimeSpan());
        }

        XYZQueryTracker tracker = new XYZQueryTracker(myUIRegistry, layer.getDisplayName());
        double deltaLon = totalLonDegrees / layer.getNumberOfTopLevels();
        for (int j = 0; j < layer.getNumberOfTopLevels(); j++)
        {
            GeographicBoundingBox boundingBox = new GeographicBoundingBox(
                    LatLonAlt.createFromDegrees(minLatitude, minLon + deltaLon * j),
                    LatLonAlt.createFromDegrees(maxLatitude, minLon + deltaLon * (j + 1)));
            tileBuilder.setBounds(boundingBox);
            ImageManager imageManager = new ImageManager(new ZYXImageKey(0, 0, j, boundingBox),
                    new XYZImageProvider(myRegistry, layer));
            tileBuilder.setImageManager(imageManager);

            XYZBaseDivider divider = null;
            if (layer.getProjection() == Projection.EPSG_3857)
            {
                divider = new XYZ3857Divider(layer, tracker);
            }
            else
            {
                divider = new XYZ4326Divider(layer, tracker);
            }

            tileBuilder.setDivider(divider);

            TileGeometry topGeometry = new TileGeometry(tileBuilder, props, constraints, layerId);

            List<TileGeometry> geometries = zoomInIfNeedBe(topGeometry, layer);

            for (TileGeometry geometry : geometries)
            {
                tileBuilder = new TileGeometry.Builder<GeographicPosition>();
                tileBuilder.setMinimumDisplaySize(300);
                tileBuilder.setMaximumDisplaySize(600);
                boundingBox = (GeographicBoundingBox)geometry.getBounds();
                tileBuilder.setBounds(boundingBox);
                imageManager = new ImageManager((ZYXImageKey)geometry.getImageManager().getImageKey(),
                        new XYZImageProvider(myRegistry, layer));
                tileBuilder.setImageManager(imageManager);
                imageManager.addRequestObserver(tracker);
                tileBuilder.setDivider(divider);

                noParentGeometries.add(new TileGeometry(tileBuilder, props, constraints, layerId));
            }
        }

        return noParentGeometries;
    }

    /**
     * Zooms in on the topGeometry until we have met the layer's minimum zoom
     * level.
     *
     * @param topGeometry The top level geometry.
     * @param layer Contains the minimum zoom level information.
     * @return The geometries to use.
     */
    private List<TileGeometry> zoomInIfNeedBe(TileGeometry topGeometry, XYZTileLayerInfo layer)
    {
        List<TileGeometry> geometries = New.list(topGeometry);
        List<TileGeometry> allSubTiles = New.list();
        for (int i = 0; i < layer.getMinZoomLevel(); i++)
        {
            for (TileGeometry geometry : geometries)
            {
                Collection<? extends AbstractTileGeometry<?>> subTiles = geometry.getChildren(true);
                for (AbstractTileGeometry<?> subTile : subTiles)
                {
                    if (subTile instanceof TileGeometry)
                    {
                        allSubTiles.add((TileGeometry)subTile);
                    }
                }
            }

            geometries.clear();
            geometries.addAll(allSubTiles);
            allSubTiles.clear();
        }

        return geometries;
    }
}
