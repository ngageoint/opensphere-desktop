package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * Abstract implementation of a {@link MapLocationGeometrySupport}.
 */
public abstract class AbstractLocationGeometrySupport extends AbstractDefaultMapGeometrySupport
        implements MapLocationGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The Constant ZERO_ZERO_ZERO. */
    private static final LatLonAlt ZERO_ZERO_ZERO = LatLonAlt.createFromDegrees(0.0, 0.0);

    /**
     * Default constructor.
     */
    public AbstractLocationGeometrySupport()
    {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    public AbstractLocationGeometrySupport(AbstractLocationGeometrySupport source)
    {
        super(source);
        // nothing unique here to copy
    }

    /**
     * Constructor with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public AbstractLocationGeometrySupport(LatLonAlt loc)
    {
        super();
        if (loc == null)
        {
            throw new IllegalArgumentException("Location Can Not Be Null");
        }

        putItemInDynamicStorage(loc);
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        return this == obj || super.equals(obj) && getClass() == obj.getClass();
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
        LatLonAlt loc = (LatLonAlt)getItemFromDynamicStorage(LatLonAlt.class);
        return loc == null ? ZERO_ZERO_ZERO : loc;
    }

    @Override
    public abstract MapVisualizationType getVisualizationType();

    @Override
    public synchronized int hashCode()
    {
        return 31 * super.hashCode();
    }

    @Override
    public void setLocation(LatLonAlt loc)
    {
        if (loc == null)
        {
            throw new IllegalArgumentException("Location Can Not Be Null");
        }

        putItemInDynamicStorage(loc);
    }
}
