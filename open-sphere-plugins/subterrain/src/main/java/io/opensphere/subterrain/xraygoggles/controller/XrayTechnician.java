package io.opensphere.subterrain.xraygoggles.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Listens for changes to the xray's geographic coordinates and makes any tiles
 * within the xray window transparent.
 */
public class XrayTechnician implements Observer, GenericSubscriber<Geometry>
{
    /**
     * The opacity to set for the tile.
     */
    private static final float OPACITY = .1f;

    /**
     * The listener curious when a tile's children is created.
     */
    private final Runnable myChildrenListener = this::applyXray;

    /**
     * Contains all of the geometries displayed on the globe.
     */
    private final GeometryRegistry myGeometryRegistry;

    /**
     * Contains the geographic coordinates of the xray window.
     */
    private final XrayGogglesModel myModel;

    /**
     * The previous tile geometries that were made transparent.
     */
    private final List<TileGeometry> myPreviousTrans = Collections.synchronizedList(New.list());

    /**
     * Used to get any {@link TileGeometry} that are displayed within the xray
     * window.
     */
    private final TileWalker myWalker;

    /**
     * Constructs a new xray technician.
     *
     * @param mapManager Used to convert the tile's coordinates into screen
     *            coordinates.
     * @param geometryRegistry Contains all of the geometries displayed on the
     *            globe.
     * @param model Contains the geographic coordinates of the xray window.
     */
    public XrayTechnician(MapManager mapManager, GeometryRegistry geometryRegistry, XrayGogglesModel model)
    {
        myWalker = new TileWalker(mapManager, geometryRegistry, model);
        myModel = model;
        myGeometryRegistry = geometryRegistry;
        myModel.addObserver(this);
        myGeometryRegistry.addSubscriber(this);
    }

    /**
     * Stops listening for geographic position updates.
     */
    public void close()
    {
        myModel.deleteObserver(this);
        myGeometryRegistry.removeSubscriber(this);
        clearPrevious(New.list());
    }

    @Override
    public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        if (!adds.isEmpty())
        {
            boolean hasNewTile = false;
            for (Geometry geom : adds)
            {
                if (geom instanceof TileGeometry && ((TileGeometry)geom).getBounds() instanceof GeographicBoundingBox)
                {
                    hasNewTile = true;
                    break;
                }
            }

            if (hasNewTile)
            {
                applyXray();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (XrayGogglesModel.GEO_POSITION.equals(arg))
        {
            applyXray();
        }
    }

    /**
     * Collects all of the {@link TileGeometry} that are within the xray window
     * and makes them transparent. Also reshows any previous tile that were once
     * transparent.
     */
    private void applyXray()
    {
        List<TileGeometry> geoms = myWalker.collectTiles();

        clearPrevious(geoms);

        for (TileGeometry geom : geoms)
        {
            if (!myPreviousTrans.contains(geom))
            {
                geom.getRenderPropertiesIndividual().setOpacity(OPACITY);
                geom.getRenderPropertiesIndividual().setObscurant(false);
                geom.addChildrenListener(myChildrenListener);
                myPreviousTrans.add(geom);
            }
        }
    }

    /**
     * Resets the tiles that were previously made transparent back to their
     * original state.
     *
     * @param keeps The list of tiles that are staying transparent.
     */
    private void clearPrevious(List<TileGeometry> keeps)
    {
        List<TileGeometry> removes = New.list();
        for (TileGeometry previous : myPreviousTrans.toArray(new TileGeometry[myPreviousTrans.size()]))
        {
            if (previous != null && !keeps.contains(previous))
            {
                previous.clearIndividualRenderProperties();
                previous.removeChildrenListener(myChildrenListener);
                removes.add(previous);
            }
        }

        myPreviousTrans.removeAll(removes);
    }
}
