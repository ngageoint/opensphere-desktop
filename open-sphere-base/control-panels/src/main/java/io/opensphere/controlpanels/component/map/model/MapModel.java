package io.opensphere.controlpanels.component.map.model;

import java.util.Observable;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.model.ScreenBoundingBox;

/**
 * The model containing data global to the map component.
 */
public class MapModel extends Observable
{
    /**
     * The region property name.
     */
    public static final String REGION_PROP = "region";

    /**
     * The size property.
     */
    public static final String SIZE_PROP = "size";

    /**
     * The height of the map.
     */
    private int myHeight;

    /**
     * The geographic region to show.
     */
    private Quadrilateral<GeographicPosition> myRegion;

    /**
     * The x and y coordinates in screen coordinates of the maps viewport.
     */
    private ScreenBoundingBox myViewport;

    /**
     * The width of the map.
     */
    private int myWidth;

    /**
     * Gets the height of the map.
     *
     * @return The height of the map.
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Gets the region to show.
     *
     * @return The region to show.
     */
    public Quadrilateral<GeographicPosition> getRegion()
    {
        return myRegion;
    }

    /**
     * Gets the viewport of where on the map we will be drawing.
     *
     * @return The map viewport.
     */
    public ScreenBoundingBox getViewport()
    {
        return myViewport;
    }

    /**
     * Gets the width.
     *
     * @return The width of the map.
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Sets the height and width.
     *
     * @param height The height of the map.
     * @param width The width of the map.
     */
    public void setHeightWidth(int height, int width)
    {
        if (myHeight != height || myWidth != width)
        {
            myHeight = height;
            myWidth = width;
            setChanged();
            notifyObservers(SIZE_PROP);
        }
    }

    /**
     * Sets the region to show.
     *
     * @param region the region to show.
     */
    public void setRegion(Quadrilateral<GeographicPosition> region)
    {
        myRegion = region;
        setChanged();
        notifyObservers(REGION_PROP);
    }

    /**
     * Sets the viewport of where on the map we will be drawing.
     *
     * @param viewport The new map viewport.
     */
    public void setViewport(ScreenBoundingBox viewport)
    {
        myViewport = viewport;
    }
}
