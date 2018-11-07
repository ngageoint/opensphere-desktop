package io.opensphere.mantle.data.geom.impl;

import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;

/**
 * Default polyline geometry support. Not a closed geometry.
 */
public class DefaultMapPolylineGeometrySupport extends AbstractMapPathGeometrySupport implements MapPolylineGeometrySupport
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Default constructor. */
    public DefaultMapPolylineGeometrySupport()
    {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param source the object from which to copy data.
     */
    public DefaultMapPolylineGeometrySupport(DefaultMapPolylineGeometrySupport source)
    {
        super(source);
        // nothing to copy here.
    }

    /**
     * CTOR with initial location list.
     *
     * @param locations - the locations
     */
    public DefaultMapPolylineGeometrySupport(List<LatLonAlt> locations)
    {
        super(locations);
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        return this == obj || super.equals(obj);
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return hasChildren() ? MapVisualizationType.COMPOUND_FEATURE_ELEMENTS : MapVisualizationType.POLYLINE_ELEMENTS;
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 13;
        int result = prime * super.hashCode();
        return result;
    }

    @Override
    public boolean isClosed()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public MapGeometrySupport createCopy()
    {
        return new DefaultMapPolylineGeometrySupport(this);
    }
}
