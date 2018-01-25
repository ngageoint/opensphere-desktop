package io.opensphere.mantle.data.geom.impl;

import java.util.Objects;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * The Class SimpleLocationGeometrySupport.
 */
public abstract class AbstractSimpleLocationGeometrySupport extends AbstractSimpleGeometrySupport
        implements MapLocationGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The my location. */
    private LatLonAlt myLocation;

    /**
     * CTOR.
     */
    public AbstractSimpleLocationGeometrySupport()
    {
        super();
    }

    /**
     * CTOR with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public AbstractSimpleLocationGeometrySupport(LatLonAlt loc)
    {
        super();
        if (loc == null)
        {
            throw new IllegalArgumentException("Location Can Not Be Null");
        }
        myLocation = loc;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !super.equals(obj))
        {
            return false;
        }
        AbstractSimpleLocationGeometrySupport other = (AbstractSimpleLocationGeometrySupport)obj;
        return Objects.equals(myLocation, other.myLocation);
    }

    @Override
    public GeographicBoundingBox getBoundingBox(Projection projection)
    {
        GeographicBoundingBox bounds = new GeographicBoundingBox(getLocation(), getLocation());
        GeographicBoundingBox childBB = MapGeometrySupportUtils.getMergedChildBounds(this, projection);
        if (childBB != null)
        {
            bounds = GeographicBoundingBox.merge(bounds, childBB);
        }
        return bounds;
    }

    @Override
    public LatLonAlt getLocation()
    {
        return myLocation;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.POINT_ELEMENTS;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myLocation == null ? 0 : myLocation.hashCode());
        return result;
    }

    @Override
    public void setLocation(LatLonAlt loc)
    {
        if (loc == null)
        {
            throw new IllegalArgumentException("Location Can Not Be Null");
        }
        myLocation = loc;
    }
}
