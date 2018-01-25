package io.opensphere.mantle.data.geom;

import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;

/**
 * MapGeometrySupport for a multiple location based geometry of points connected
 * by a line.
 */
public interface MapPathGeometrySupport extends MapGeometrySupport
{
    /**
     * Adds a location to the list.
     *
     * @param loc - the location to add
     * @return true if added, false if not
     */
    boolean addLocation(LatLonAlt loc);

    /**
     * Clears out all the locations.
     */
    void clearLocations();

    /**
     * Gets the line type.
     *
     * @return the line type
     */
    LineType getLineType();

    /**
     * Gets the line width.
     *
     * @return the line width.
     */
    int getLineWidth();

    /**
     * Gets the location list.
     *
     * @return the List of locations.
     */
    List<LatLonAlt> getLocations();

    /**
     * Returns true if this geometry is closed, i.e. the last point should be
     * connected to the first point.
     *
     * @return true if close, false if open.
     */
    boolean isClosed();

    /**
     * Removes a location from the location list. Note: By Reference not
     * equality.
     *
     * @param loc - the location to remove.
     * @return true if removed, false if not
     */
    boolean removeLocation(LatLonAlt loc);

    /**
     * Sets the line type.
     *
     * @param type the new line type
     */
    void setLineType(LineType type);

    /**
     * Sets the line width.
     *
     * @param lineWidth - the line width.
     */
    void setLineWidth(int lineWidth);

    /**
     * Sets the location.
     *
     * @param locations - the locations to set
     */
    void setLocations(List<LatLonAlt> locations);
}
