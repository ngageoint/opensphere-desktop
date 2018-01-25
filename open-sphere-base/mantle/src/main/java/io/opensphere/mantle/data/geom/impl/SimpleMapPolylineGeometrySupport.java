package io.opensphere.mantle.data.geom.impl;

import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;

/**
 * Simple polyline geometry support. Not a closed geometry. Does not support
 * children.
 */
public class SimpleMapPolylineGeometrySupport extends AbstractSimpleMapPathGeometrySupport implements MapPolylineGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 22L;

    /**
     * Basic Constructor.
     */
    public SimpleMapPolylineGeometrySupport()
    {
        super();
    }

    /**
     * CTOR with location list.
     *
     * @param locs - the locations
     */
    public SimpleMapPolylineGeometrySupport(List<LatLonAlt> locs)
    {
        super(locs);
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || super.equals(obj);
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.POLYLINE_ELEMENTS;
    }

    @Override
    public int hashCode()
    {
        final int primeVal = 13;
        int result = primeVal * super.hashCode();
        return result;
    }

    @Override
    public boolean isClosed()
    {
        return false;
    }
}
