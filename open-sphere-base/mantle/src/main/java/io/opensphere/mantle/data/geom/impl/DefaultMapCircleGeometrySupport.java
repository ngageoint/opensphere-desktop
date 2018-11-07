package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * Support for circle geometries.
 */
public class DefaultMapCircleGeometrySupport extends AbstractLocationGeometrySupport implements MapCircleGeometrySupport
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The radius. */
    private float myRadius;

    /** Constructor. */
    public DefaultMapCircleGeometrySupport()
    {
        super();
    }

    /**
     * Copy Constructor.
     *
     * @param source the source object from which to copy data.
     */
    public DefaultMapCircleGeometrySupport(DefaultMapCircleGeometrySupport source)
    {
        super(source);
        myRadius = source.myRadius;
    }

    /**
     * Constructor with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public DefaultMapCircleGeometrySupport(LatLonAlt loc)
    {
        super(loc);
    }

    /**
     * CTOR with location and line width.
     *
     * @param loc - the location
     * @param radius - the Semi-Major axis in km
     */
    public DefaultMapCircleGeometrySupport(LatLonAlt loc, float radius)
    {
        super(loc);
        myRadius = radius;
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
        DefaultMapCircleGeometrySupport other = (DefaultMapCircleGeometrySupport)obj;
        return Float.floatToIntBits(myRadius) == Float.floatToIntBits(other.myRadius);
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
    public float getRadius()
    {
        return myRadius;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return hasChildren() ? MapVisualizationType.COMPOUND_FEATURE_ELEMENTS : MapVisualizationType.CIRCLE_ELEMENTS;
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myRadius);
        return result;
    }

    @Override
    public void setRadius(float radius)
    {
        myRadius = radius;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public DefaultMapCircleGeometrySupport createCopy()
    {
        return new DefaultMapCircleGeometrySupport(this);
    }
}
