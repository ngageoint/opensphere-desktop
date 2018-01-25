package io.opensphere.wfs.gml311;

import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Abstract SAX handler for individual GML geometries.
 */
public abstract class AbstractGmlGeometryHandler
{
    /** Flag that's true if position order is Lat/Lon, false if Lon/Lat. */
    private final boolean myIsLatBeforeLon;

    /** The GML Geometry tag. */
    private final String myTagName;

    /**
     * Abstract constructor for GML geometry handlers.
     *
     * @param tagName the GML geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public AbstractGmlGeometryHandler(String tagName, boolean isLatBeforeLon)
    {
        myTagName = tagName;
        myIsLatBeforeLon = isLatBeforeLon;
    }

    /**
     * Gets the name of the GML Geometry tag for this geometry type.
     *
     * @return the GML Geometry tag name
     */
    public String getTagName()
    {
        return myTagName;
    }

    /**
     * Gets the position ordering in GML points.
     *
     * @return true if position order is Lat/Lon, false if Lon/Lat.
     */
    public boolean isLatBeforeLong()
    {
        return myIsLatBeforeLon;
    }

    /**
     * Gets the {@link MapGeometrySupport} part of the {@link DataElement} that
     * this class was responsible for parsing from the GML.
     *
     * @return the mantle-formatted geometry
     */
    public abstract AbstractMapGeometrySupport getGeometry();

    /**
     * Handle a GML Geometry-related opening tag.
     *
     * @param tag the name of the GML tag to handle
     * @param value the value of the XML tag
     */
    public abstract void handleClosingTag(String tag, String value);

    /**
     * Handle a GML Geometry-related closing tag.
     *
     * @param tag the name of the GML tag to handle
     */
    public abstract void handleOpeningTag(String tag);
}
