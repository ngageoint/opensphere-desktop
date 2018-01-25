package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * A Simple Map Point Geometry Support. It does not support children, tooltips,
 * or callouts.
 */
public class SimpleMapEllipseGeometrySupport extends AbstractSimpleLocationGeometrySupport implements MapEllipseGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The orientation. */
    private float myOrient;

    /** The Semi-Major Axis. */
    private float mySmaAxis;

    /** The Semi-Minor Axis. */
    private float mySmnAxis;

    /**
     * CTOR.
     */
    public SimpleMapEllipseGeometrySupport()
    {
        super();
    }

    /**
     * CTOR with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public SimpleMapEllipseGeometrySupport(LatLonAlt loc)
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
    public SimpleMapEllipseGeometrySupport(LatLonAlt loc, float semiMajor, float semiMinor, float orient)
    {
        super(loc);
        mySmnAxis = semiMinor;
        mySmaAxis = semiMajor;
        myOrient = orient;
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
        SimpleMapEllipseGeometrySupport other = (SimpleMapEllipseGeometrySupport)obj;
        return Float.floatToIntBits(myOrient) == Float.floatToIntBits(other.myOrient)
                && Float.floatToIntBits(mySmaAxis) == Float.floatToIntBits(other.mySmaAxis)
                && Float.floatToIntBits(mySmnAxis) == Float.floatToIntBits(other.mySmnAxis);
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
        return myOrient;
    }

    @Override
    public float getSemiMajorAxis()
    {
        return mySmaAxis;
    }

    @Override
    public float getSemiMinorAxis()
    {
        return mySmnAxis;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.ELLIPSE_ELEMENTS;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myOrient);
        result = prime * result + Float.floatToIntBits(mySmaAxis);
        result = prime * result + Float.floatToIntBits(mySmnAxis);
        return result;
    }

    @Override
    public void setOrientation(float orient)
    {
        myOrient = orient;
    }

    @Override
    public void setSemiMajorAxis(float sma)
    {
        mySmaAxis = sma;
    }

    @Override
    public void setSemiMinorAxis(float smi)
    {
        mySmnAxis = smi;
    }
}
