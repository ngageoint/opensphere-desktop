package io.opensphere.wfs.placenames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.Viewer;

/** A layer of place names tiles. */
public class PlaceNameLayer
{
    /** Whether or not this layer is active. */
    private boolean myActive = true;

    /** Configuration for this layer. */
    private final PlaceNameLayerConfig myConfiguration;

    /** Lock for synchronization. */
    private final Object myLock = new Object();

    /** The Depth at which tiles will be displayed. */
    private final int mySplitDepth;

    /** The currently published place name tiles. */
    private Map<PlaceNameKey, PlaceNameTile> myTiles = new HashMap<>();

    /** The event manager. */
    private final Toolbox myToolbox;

    /** The top level tiles for this layer. */
    private final List<PlaceNameTile> myTopLevelTiles = new ArrayList<>(2);

    /** The Server name. */
    private final String myServerName;

    /** The Type key. Provide a way to uniquely identify this layer. */
    private String myTypeKey;

    /**
     * Constructor.
     *
     * @param configuration Configuration for the layer.
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     * @param serverName The server name.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public PlaceNameLayer(PlaceNameLayerConfig configuration, Toolbox toolbox, String serverName)
    {
        myConfiguration = configuration;
        myToolbox = toolbox;
        myServerName = serverName;

        // create a list of PlaceNameTiles for the top level. Each tile will be
        // a tree.
        GeographicBoundingBox left = new GeographicBoundingBox(new GeographicPosition(LatLonAlt.createFromDegrees(-90d, -180d)),
                new GeographicPosition(LatLonAlt.createFromDegrees(90d, 0d)));
        myTopLevelTiles.add(new PlaceNameTile(new PlaceNameKey(left), this, null, serverName));

        GeographicBoundingBox right = new GeographicBoundingBox(new GeographicPosition(LatLonAlt.createFromDegrees(-90d, 0d)),
                new GeographicPosition(LatLonAlt.createFromDegrees(90d, 180d)));
        myTopLevelTiles.add(new PlaceNameTile(new PlaceNameKey(right), this, null, serverName));

        double tileSize = 180d;
        int depth = 0;
        while (true)
        {
            if (tileSize < myConfiguration.getTileSize() + MathUtil.DBL_EPSILON)
            {
                break;
            }

            ++depth;
            tileSize /= 2d;
        }
        mySplitDepth = depth;
    }

    /**
     * Perform any necessary cleanup before removal of the layer.
     *
     * @param cleanupListener the cleanup listener
     */
    public void cleanup(boolean cleanupListener)
    {
        synchronized (myLock)
        {
            clearGeometries();
            myTiles.clear();
        }
    }

    /**
     * Get the configuration.
     *
     * @return the configuration
     */
    public PlaceNameLayerConfig getConfiguration()
    {
        return myConfiguration;
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    public String getServerName()
    {
        return myServerName;
    }

    /**
     * Get the splitDepth.
     *
     * @return the splitDepth
     */
    public int getSplitDepth()
    {
        return mySplitDepth;
    }

    /**
     * Get the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Accessor for the tiles.
     *
     * @return The top level tiles.
     */
    public List<PlaceNameTile> getTopLevelTiles()
    {
        return myTopLevelTiles;
    }

    /**
     * Gets the type key.
     *
     * @return the type key
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }

    /** Handle the view being changed. */
    public void handleViewChanged()
    {
        synchronized (myLock)
        {
            // If the viewer is above the altitude (from the surface) for this
            // layer, do not display any tiles.
            Viewer viewer = myToolbox.getMapManager().getStandardViewer();
            double distance = viewer.getPosition().getLocation().distance(viewer.getClosestModelPosition());

            if (distance > myConfiguration.getMaxDisplayDistance() || distance < myConfiguration.getMinDisplayDistance())
            {
                myTiles.clear();
                clearGeometries();
                return;
            }

            List<PlaceNameTile> visibleTiles = new ArrayList<>();

            for (PlaceNameTile tile : myTopLevelTiles)
            {
                visibleTiles.addAll(tile.buildVisible());
            }

            // 2. build a new map containing each key which is in view. Use the
            // tile
            // from the old map if available, or create a new one if not. Remove
            // from the old map as we go, so that we can use it for removes
            // list.
            Map<PlaceNameKey, PlaceNameTile> tempTiles = new HashMap<>();
            List<PlaceNameTile> adds = new ArrayList<>();
            for (PlaceNameTile visTile : visibleTiles)
            {
                if (myTiles.remove(visTile.getKey()) == null)
                {
                    adds.add(visTile);
                }
                tempTiles.put(visTile.getKey(), visTile);
            }

            publishTiles(adds, myTiles.values());

            myTiles = tempTiles;
        }
    }

    /**
     * Get whether the layer is active or not.
     *
     * @return True if active, false otherwise.
     */
    public boolean isActive()
    {
        return myActive;
    }

    /**
     * Sets the active.
     *
     * @param isActive the new active
     */
    public void setActive(boolean isActive)
    {
        myActive = isActive;
    }

    /**
     * Sets the type key.
     *
     * @param typeKey the new type key
     */
    public void setTypeKey(String typeKey)
    {
        myTypeKey = typeKey;
    }

    /** Clear all geometries for this layer. */
    private void clearGeometries()
    {
        for (PlaceNameTile tile : myTopLevelTiles)
        {
            tile.clearGeometries();
        }
    }

    /**
     * Publish or remove place names.
     *
     * @param adds Tiles to publish.
     * @param removes Tiles to remove.
     */
    private void publishTiles(Collection<PlaceNameTile> adds, Collection<PlaceNameTile> removes)
    {
        if (adds != null)
        {
            for (PlaceNameTile tile : adds)
            {
                tile.publishGeometries();
            }
        }

        if (removes != null)
        {
            for (PlaceNameTile tile : removes)
            {
                tile.clearGeometries();
            }
        }
    }
}
