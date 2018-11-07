package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/** A Default Map Point Geometry Support. */
public class DefaultMapLineOfBearingGeometrySupport extends DefaultMapPointGeometrySupport
        implements MapLineOfBearingGeometrySupport
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The line length (km). */
    private float myLineLength;

    /** The line orientation (degrees clockwise from north). */
    private float myOrientation;

    /** Default constructor. */
    public DefaultMapLineOfBearingGeometrySupport()
    {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    public DefaultMapLineOfBearingGeometrySupport(DefaultMapLineOfBearingGeometrySupport source)
    {
        super(source);

        myLineLength = source.myLineLength;
        myOrientation = source.myOrientation;
    }

    /**
     * CTOR with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public DefaultMapLineOfBearingGeometrySupport(LatLonAlt loc)
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
    public DefaultMapLineOfBearingGeometrySupport(LatLonAlt loc, float orient, float length)
    {
        super(loc);
        myOrientation = orient;
        myLineLength = length;
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultMapLineOfBearingGeometrySupport other = (DefaultMapLineOfBearingGeometrySupport)obj;
        return Float.floatToIntBits(myLineLength) == Float.floatToIntBits(other.myLineLength)
                && Float.floatToIntBits(myOrientation) == Float.floatToIntBits(other.myOrientation);
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
        return myLineLength;
    }

    @Override
    public float getOrientation()
    {
        return myOrientation;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return hasChildren() ? MapVisualizationType.COMPOUND_FEATURE_ELEMENTS : MapVisualizationType.LOB_ELEMENTS;
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myLineLength);
        result = prime * result + Float.floatToIntBits(myOrientation);
        return result;
    }

    @Override
    public void setLength(float length)
    {
        myLineLength = length;
    }

    @Override
    public void setOrientation(float orient)
    {
        myOrientation = orient;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport#createCopy()
     */
    @Override
    public DefaultMapLineOfBearingGeometrySupport createCopy()
    {
        return new DefaultMapLineOfBearingGeometrySupport(this);
    }
}
