package io.opensphere.mantle.data.geom;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.LatLonAlt;

/**
 * Polygon geometry support ( i.e. closed path ). Nothing new to add here, its
 * more a drawing hint.
 */
public interface MapPolygonGeometrySupport extends MapPathGeometrySupport
{
    /**
     * Gets the fill color.
     *
     * @return the fill color ( null indicates fill color same as main color )
     */
    Color getFillColor();

    /**
     * Get the holes.
     *
     * @return the holes
     */
    Collection<? extends List<? extends LatLonAlt>> getHoles();

    /**
     * Checks if is filled.
     *
     * @return true, if is filled
     */
    boolean isFilled();

    /**
     * Checks to see if the line for the polygon is drawn.
     *
     * @return true if drawn, false if not
     */
    boolean isLineDrawn();

    /**
     * Sets the fill color. (null indicates fill color same as main color)
     *
     * @param fillColor the new fill color
     */
    void setFillColor(Color fillColor);

    /**
     * Sets if the polygon is filled.
     *
     * @param filled - true if filled, false if not
     */
    void setFilled(boolean filled);

    /**
     * Sets if the polygon has its line drawn.
     *
     * @param drawn - true to draw, false to not draw
     */
    void setLineDrawn(boolean drawn);
}
