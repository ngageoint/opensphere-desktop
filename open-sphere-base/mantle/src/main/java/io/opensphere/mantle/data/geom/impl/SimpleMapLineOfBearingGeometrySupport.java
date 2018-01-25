package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * A Simple Map Point Geometry Support. It does not support children, tooltips,
 * or callouts.
 */
public class SimpleMapLineOfBearingGeometrySupport extends SimpleMapPointGeometrySupport
        implements MapLineOfBearingGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The line length (km). */
    private float myLLength;

    /** The line orientation (degrees clockwise from north). */
    private float myOrient;

    /**
     * CTOR.
     */
    public SimpleMapLineOfBearingGeometrySupport()
    {
        super();
    }

    /**
     * CTOR with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public SimpleMapLineOfBearingGeometrySupport(LatLonAlt loc)
    {
        super(loc);
    }

    /**
     * CTOR with location position, orientation, and length.
     *
     * @param loc - the location
     * @param orient - the orientation in degrees clockwise from north.
     * @param length - the line length in km.
     */
    public SimpleMapLineOfBearingGeometrySupport(LatLonAlt loc, float orient, float length)
    {
        super(loc);
        myOrient = orient;
        myLLength = length;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        SimpleMapLineOfBearingGeometrySupport other = (SimpleMapLineOfBearingGeometrySupport)obj;
        return Float.floatToIntBits(myLLength) == Float.floatToIntBits(other.myLLength)
                && Float.floatToIntBits(myOrient) == Float.floatToIntBits(other.myOrient);
    }

    @Override
    public GeographicBoundingBox getBoundingBox(Projection projection)
    {
        GeographicBoundingBox bounds = MapGeometrySupportUtils.getBoundingBox(this);
        GeographicBoundingBox childBB = MapGeometrySupportUtils.getMergedChildBounds(this, projection);
        if (childBB != null)
        {
            bounds = GeographicBoundingBox.merge(bounds, childBB);
        }
        return bounds;
    }

    @Override
    public float getLength()
    {
        return myLLength;
    }

    @Override
    public float getOrientation()
    {
        return myOrient;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.LOB_ELEMENTS;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myLLength);
        result = prime * result + Float.floatToIntBits(myOrient);
        return result;
    }

    @Override
    public void setLength(float length)
    {
        myLLength = length;
    }

    @Override
    public void setOrientation(float orient)
    {
        myOrient = orient;
    }
}
