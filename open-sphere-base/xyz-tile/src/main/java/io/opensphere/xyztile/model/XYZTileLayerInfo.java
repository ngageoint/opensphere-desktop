package io.opensphere.xyztile.model;

import java.util.Observable;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Contains certain information about an XYZ tile layer.
 */
public class XYZTileLayerInfo extends Observable
{
    /**
     * The maximum number of zoom levels property.
     */
    public static final String MAX_LEVELS_PROP = "maxLevels";

    /**
     * The description of the layer, or null if no description.
     */
    private String myDescription;

    /**
     * The name of the layer to display to the user.
     */
    private String myDisplayName;

    /**
     * The geographic extents of the layer, or null if it spans the whole world.
     */
    private GeographicBoundingBox myFootprint;

    /**
     * Indicates if this xyz layer is tms, meaning y starts counting from the
     * south up instead of north down.
     */
    private final boolean myIsTms;

    /**
     * The maximum number of zoom levels.
     */
    private int myMaxLevels = 18;

    /**
     * The minimum zoom level.
     */
    private final int myMinZoomLevel;

    /**
     * The name of the tile layer.
     */
    private final String myName;

    /**
     * The number of top level tiles to start with, typically one or two.
     */
    private final int myNumberOfTopLevels;

    /**
     * The id of this layer's parent or null if there isn't one.
     */
    private String myParentId;

    /**
     * The projection of the tile layer.
     */
    private final Projection myProjection;

    /**
     * Information about the server.
     */
    private final XYZServerInfo myServerInfo;

    /**
     * The valid time span of the tile layer, or null if not applicable.
     */
    private TimeSpan myTimeSpan;

    /**
     * The maximum number of zoom levels set by the user.
     */
    private int myUserSetMaxLevels = -1;

    /**
     * Indicates if the visibility has been specified.
     */
    private boolean myVisibilitySpecified;

    /**
     * Indicates if the tile layer should be visibile or invisible.
     */
    private boolean myVisible = true;

    /**
     * Constructs a new XYZ tile layer.
     *
     * @param name The name of the tile layer.
     * @param displayName The name of the layer to display to the user.
     * @param projection The projection of the tile layer.
     * @param numberOfTopLevelTiles The number of top level tiles to start with,
     *            typically one or two.
     * @param isTms Indicates if this xyz layer is tms, meaning y starts
     *            counting from the south up instead of north down.
     * @param minZoomLevel The minimum zoom level, typically 0.
     * @param serverInfo Information about the server.
     */
    public XYZTileLayerInfo(String name, String displayName, Projection projection, int numberOfTopLevelTiles, boolean isTms,
            int minZoomLevel, XYZServerInfo serverInfo)
    {
        myName = name;
        myDisplayName = displayName;
        myProjection = projection;
        myNumberOfTopLevels = numberOfTopLevelTiles;
        myIsTms = isTms;
        myMinZoomLevel = minZoomLevel;
        myServerInfo = serverInfo;
    }

    /**
     * Gets the description of the layer, if there is one.
     *
     * @return The layer description or null if no description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the name of the layer to display to the user.
     *
     * @return The name of the layer to display to the user.
     */
    public String getDisplayName()
    {
        return myDisplayName;
    }

    /**
     * Gets the geographic extents of this layer.
     *
     * @return The geographic extents of this layer or null if it extends the
     *         whole world..
     */
    public GeographicBoundingBox getFootprint()
    {
        return myFootprint;
    }

    /**
     * Gets the maximum number of zoom levels.
     *
     * @return The maximum number of zoom levels.
     */
    public int getMaxLevels()
    {
        int maxLevels = myMaxLevels;

        if (myUserSetMaxLevels > 0 && myUserSetMaxLevels < myMaxLevels)
        {
            maxLevels = myUserSetMaxLevels;
        }

        return maxLevels;
    }

    /**
     * Gets the default maximum number of zoom levels for this layer.
     *
     * @return The default maximum number of zoom levels.
     */
    public int getMaxLevelsDefault()
    {
        return myMaxLevels;
    }

    /**
     * Gets the minimum zoom level, typically 0.
     *
     * @return The minimum zoom level.
     */
    public int getMinZoomLevel()
    {
        return myMinZoomLevel;
    }

    /**
     * Gets the name of the tile layer.
     *
     * @return The name of the tile layer.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the number of top level tiles to start with, typically one or two.
     *
     * @return The number of top level tiles to start with, typically one or
     *         two.
     */
    public int getNumberOfTopLevels()
    {
        return myNumberOfTopLevels;
    }

    /**
     * Gets the id of this layer's parent or null if there isn't one.
     *
     * @return The id of this layer's parent or null if there isn't one.
     */
    public String getParentId()
    {
        return myParentId;
    }

    /**
     * Gets the projection of the tile layer.
     *
     * @return The projection of the tile layer.
     */
    public Projection getProjection()
    {
        return myProjection;
    }

    /**
     * Gets the server info.
     *
     * @return The info on the server.
     */
    public XYZServerInfo getServerInfo()
    {
        return myServerInfo;
    }

    /**
     * Gets the base server url.
     *
     * @return The base server url.
     */
    public String getServerUrl()
    {
        return myServerInfo.getServerUrl();
    }

    /**
     * Gets the valid time span of the tile layer, or null if not applicable.
     *
     * @return The valid time span of the tile layer, or null if not applicable.
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    /**
     * Indicates if this xyz layer is tms, meaning y starts counting from the
     * south up instead of north down.
     *
     * @return True if y starts in the south, false if y starts in the north.
     */
    public boolean isTms()
    {
        return myIsTms;
    }

    /**
     * Indicates if isVisible has been set.
     *
     * @return True if it has been set, false if we should just use what is in
     *         the config files.
     */
    public boolean isVisibilitySpecified()
    {
        return myVisibilitySpecified;
    }

    /**
     * Indicates if the tile layer should be visibile or invisible.
     *
     * @return True if the layer should be visible, false otherwise.
     */
    public boolean isVisible()
    {
        return myVisible;
    }

    /**
     * Sets the description of the layer.
     *
     * @param description The description of the layer or null if there isn't
     *            one.
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets the name of the layer to display to the user.
     *
     * @param displayName The name of the layer to display to the user.
     */
    public void setDisplayName(String displayName)
    {
        myDisplayName = displayName;
    }

    /**
     * Sets the geographic extents of this layer.
     *
     * @param footprint The geographic extents of this layer or null if it
     *            extends the whole world.
     */
    public void setFootprint(GeographicBoundingBox footprint)
    {
        myFootprint = footprint;
    }

    /**
     * Sets the maximum number of zoom levels.
     *
     * @param maxLevels The maximum number of zoom levels.
     */
    public void setMaxLevels(int maxLevels)
    {
        myMaxLevels = maxLevels;
    }

    /**
     * Sets the maximum number of zoom levels and overides the default zoom
     * levels.
     *
     * @param userSetMaxLevels The maximum amount of zoom set by the user.
     */
    public void setMaxLevelsUser(int userSetMaxLevels)
    {
        myUserSetMaxLevels = userSetMaxLevels;
        super.setChanged();
        super.notifyObservers(MAX_LEVELS_PROP);
    }

    /**
     * Sets the id of this layer's parent or null if there isn't one.
     *
     * @param parentId The id of this layer's parent or null if there isn't one.
     */
    public void setParentId(String parentId)
    {
        myParentId = parentId;
    }

    /**
     * Sets the valid time span of the tile layer, or null if not applicable.
     *
     * @param timeSpan The valid time span of the tile layer, or null if not
     *            applicable.
     */
    public void setTimeSpan(TimeSpan timeSpan)
    {
        myTimeSpan = timeSpan;
    }

    /**
     * Sets if the tile layer should be visible or invisible.
     *
     * @param visible True if the layer should be visible, false otherwise.
     */
    public void setVisible(boolean visible)
    {
        myVisibilitySpecified = true;
        myVisible = visible;
    }
}
