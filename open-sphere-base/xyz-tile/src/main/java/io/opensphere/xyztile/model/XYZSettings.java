package io.opensphere.xyztile.model;

import java.util.Observable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains user defined settings for a given xyz layer.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class XYZSettings extends Observable
{
    /**
     * The current maximum zoom level property.
     */
    public static final String MAX_ZOOM_LEVEL_CURRENT_PROP = "maxZoomLevelCurrent";

    /**
     * The id of the layer these settings belong to.
     */
    @XmlElement(name = "layerId")
    private String myLayerId;

    /**
     * The currently set maximum zoom level.
     */
    @XmlElement(name = "maxZoomLevel")
    private int myMaxZoomLevelCurrent;

    /**
     * The maximum level the current max zoom level can be set to.
     */
    private int myMaxZoomLevelDefault;

    /**
     * The minimum level for the zoom level.
     */
    private int myMinZoomLevel;

    /**
     * Gets the id of the layer these settings belong to.
     *
     * @return The id of the layer these settings belong to.
     */
    public String getLayerId()
    {
        return myLayerId;
    }

    /**
     * Gets the currently set maximum zoom level.
     *
     * @return The currently set maximum zoom level.
     */
    public int getMaxZoomLevelCurrent()
    {
        return myMaxZoomLevelCurrent;
    }

    /**
     * Gets the maximum level the current max zoom level can be set to.
     *
     * @return The maximum level the current max zoom level can be set to.
     */
    public int getMaxZoomLevelDefault()
    {
        return myMaxZoomLevelDefault;
    }

    /**
     * Gets the minimum zoom level for the layer.
     *
     * @return the minZoomLevel The layer's minimum zoom level.
     */
    public int getMinZoomLevel()
    {
        return myMinZoomLevel;
    }

    /**
     * Sets the id of the layer these settings belong to.
     *
     * @param layerId The id of the layer these settings belong to.
     */
    public void setLayerId(String layerId)
    {
        myLayerId = layerId;
    }

    /**
     * Sets the currently set maximum zoom level.
     *
     * @param currentMaxZoomLevel The currently set maximum zoom level.
     */
    public void setMaxZoomLevelCurrent(int currentMaxZoomLevel)
    {
        myMaxZoomLevelCurrent = currentMaxZoomLevel;
        setChanged();
        notifyObservers(MAX_ZOOM_LEVEL_CURRENT_PROP);
    }

    /**
     * Sets the maximum level the current max zoom level can be set to.
     *
     * @param defaultMaxZoomLevel The maximum level the current max zoom level
     *            can be set to.
     */
    public void setMaxZoomLevelDefault(int defaultMaxZoomLevel)
    {
        myMaxZoomLevelDefault = defaultMaxZoomLevel;
    }

    /**
     * Sets the minimum zoom level for the layer.
     *
     * @param minZoomLevel the minZoomLevel to set.
     */
    public void setMinZoomLevel(int minZoomLevel)
    {
        myMinZoomLevel = minZoomLevel;
    }
}
