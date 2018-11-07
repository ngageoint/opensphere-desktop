package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapPointGeometrySupport;

/**
 * A Default Map Point Geometry Support.
 */
public class DefaultMapPointGeometrySupport extends AbstractLocationGeometrySupport implements MapPointGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The scale. */
    private float myScale = 1f;

    /** Default constructor. */
    public DefaultMapPointGeometrySupport()
    {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param source the source from which to copy data.
     */
    public DefaultMapPointGeometrySupport(DefaultMapPointGeometrySupport source)
    {
        super(source);

        myScale = source.myScale;
    }

    /**
     * CTOR with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public DefaultMapPointGeometrySupport(LatLonAlt loc)
    {
        super(loc);
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        return this == obj || super.equals(obj);
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return hasChildren() ? MapVisualizationType.COMPOUND_FEATURE_ELEMENTS : MapVisualizationType.POINT_ELEMENTS;
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = prime * super.hashCode();
        return result;
    }

    @Override
    public float getScale()
    {
        return myScale;
    }

    @Override
    public void setScale(float scale)
    {
        myScale = scale;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public DefaultMapPointGeometrySupport createCopy()
    {
        return new DefaultMapPointGeometrySupport(this);
    }
}
