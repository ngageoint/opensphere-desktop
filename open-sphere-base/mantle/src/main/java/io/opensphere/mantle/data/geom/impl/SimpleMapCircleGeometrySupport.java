package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * A Simple Map Circle Geometry Support. It does not support children, tooltips,
 * or callouts.
 */
public class SimpleMapCircleGeometrySupport extends AbstractSimpleLocationGeometrySupport implements MapCircleGeometrySupport
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The radius. */
    private float myRadius;

    /** Constructor. */
    public SimpleMapCircleGeometrySupport()
    {
        super();
    }

    /**
     * Copy Constructor.
     *
     * @param source the source object from which to copy data.
     */
    public SimpleMapCircleGeometrySupport(SimpleMapCircleGeometrySupport source)
    {
        super(source);

        myRadius = source.myRadius;
    }

    /**
     * Constructor with {@link LatLonAlt}.
     *
     * @param loc the location
     */
    public SimpleMapCircleGeometrySupport(LatLonAlt loc)
    {
        super(loc);
    }

    /**
     * Constructor with location and line width.
     *
     * @param loc - the location
     * @param radius the radius in km
     */
    public SimpleMapCircleGeometrySupport(LatLonAlt loc, float radius)
    {
        super(loc);
        myRadius = radius;
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
        SimpleMapCircleGeometrySupport other = (SimpleMapCircleGeometrySupport)obj;
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
        return MapVisualizationType.CIRCLE_ELEMENTS;
    }

    @Override
    public int hashCode()
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
    public SimpleMapCircleGeometrySupport createCopy()
    {
        return new SimpleMapCircleGeometrySupport(this);
    }
}
