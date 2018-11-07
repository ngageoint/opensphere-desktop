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

    /** Default constructor. */
    public SimpleMapPointGeometrySupport()
    {
        super();
    }

    /**
     * Default constructor.
     *
     * @param source the source object from which to copy data.
     */
    public SimpleMapPointGeometrySupport(SimpleMapPointGeometrySupport source)
    {
        super(source);

        myScale = source.myScale;
    }

    /**
     * Constructor with {@link LatLonAlt}.
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

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public SimpleMapPointGeometrySupport createCopy()
    {
        return new SimpleMapPointGeometrySupport(this);
    }
}
