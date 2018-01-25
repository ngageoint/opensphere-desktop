package io.opensphere.mantle.data.geom.impl;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.geom.MapPointGeometrySupport;

/**
 * The Class SimpleMapPointGeometrySupport.
 *
 * Does not support children, callouts, or tooltips.
 */
public class SimpleMapPointGeometrySupport extends AbstractSimpleLocationGeometrySupport implements MapPointGeometrySupport
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The scale. */
    private float myScale = 1f;

    /**
     * CTOR.
     */
    public SimpleMapPointGeometrySupport()
    {
        super();
    }

    /**
     * CTOR with {@link LatLonAlt}.
     *
     * @param loc - the location
     */
    public SimpleMapPointGeometrySupport(LatLonAlt loc)
    {
        super(loc);
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return 11 * super.hashCode();
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
}
