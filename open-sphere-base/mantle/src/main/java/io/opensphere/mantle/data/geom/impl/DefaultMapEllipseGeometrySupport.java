package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * A Default Map Point Geometry Support.
 */
public class DefaultMapEllipseGeometrySupport extends AbstractLocationGeometrySupport implements MapEllipseGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The orientation. */
    private float myOrientation;

    /** The Semi-Major Axis. */
    private float mySemiMajorAxis;

    /** The Semi-Minor Axis. */
    private float mySemiMinorAxis;

    /** Default constructor. */
    public DefaultMapEllipseGeometrySupport()
    {
        super();
    }

    /**
     * Default constructor.
     *
     * @param source the source object from which to copy data.
     */
    public DefaultMapEllipseGeometrySupport(DefaultMapEllipseGeometrySupport source)
    {
        super(source);
        myOrientation = source.myOrientation;
        mySemiMajorAxis = source.mySemiMajorAxis;
        mySemiMinorAxis = source.mySemiMinorAxis;
    }

    /**
     * CTOR with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public DefaultMapEllipseGeometrySupport(LatLonAlt loc)
    {
        super(loc);
    }

    /**
     * CTOR with location and line width.
     *
     * @param loc - the location
     * @param semiMajor - the Semi-Major axis in km
     * @param semiMinor - the Semi-Minor axis in km
     * @param orient - the orientation in degrees clockwise from north.
     */
    public DefaultMapEllipseGeometrySupport(LatLonAlt loc, float semiMajor, float semiMinor, float orient)
    {
        super(loc);
        mySemiMinorAxis = semiMinor;
        mySemiMajorAxis = semiMajor;
        myOrientation = orient;
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
        DefaultMapEllipseGeometrySupport other = (DefaultMapEllipseGeometrySupport)obj;
        return Float.floatToIntBits(myOrientation) == Float.floatToIntBits(other.myOrientation)
                && Float.floatToIntBits(mySemiMajorAxis) == Float.floatToIntBits(other.mySemiMajorAxis)
                && Float.floatToIntBits(mySemiMinorAxis) == Float.floatToIntBits(other.mySemiMinorAxis);
    }

    @Override
    public GeographicBoundingBox getBoundingBox(Projection projection)
    {
        GeographicBoundingBox bounds = MapGeometrySupportUtils.getBoundingBox(this, projection);
        GeographicBoundingBox childBB = MapGeometrySupportUtils.getMergedChildBounds(this, projection);
        if (childBB != null)
        {
            bounds = GeographicBoundingBox.merge(bounds, childBB);
        }
        return bounds;
    }

    @Override
    public float getOrientation()
    {
        return myOrientation;
    }

    @Override
    public float getSemiMajorAxis()
    {
        return mySemiMajorAxis;
    }

    @Override
    public float getSemiMinorAxis()
    {
        return mySemiMinorAxis;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return hasChildren() ? MapVisualizationType.COMPOUND_FEATURE_ELEMENTS : MapVisualizationType.ELLIPSE_ELEMENTS;
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myOrientation);
        result = prime * result + Float.floatToIntBits(mySemiMajorAxis);
        result = prime * result + Float.floatToIntBits(mySemiMinorAxis);
        return result;
    }

    @Override
    public void setOrientation(float orient)
    {
        myOrientation = orient;
    }

    @Override
    public void setSemiMajorAxis(float sma)
    {
        mySemiMajorAxis = sma;
    }

    @Override
    public void setSemiMinorAxis(float smi)
    {
        mySemiMinorAxis = smi;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public DefaultMapEllipseGeometrySupport createCopy()
    {
        return new DefaultMapEllipseGeometrySupport(this);
    }
}
