package io.opensphere.core.geometry.renderproperties;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import io.opensphere.core.util.SharedObjectPool;

/** Standard implementation of {@link PolylineRenderProperties}. */
public class DefaultPolylineRenderProperties extends DefaultScalableRenderProperties implements PolylineRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** An object pool for the stipple models. */
    private static final SharedObjectPool<StippleModelConfig> STIPPLE_POOL = new SharedObjectPool<>();

    /**
     * The line stipple configuration. When this is null, a solid line will be
     * given.
     */
    private volatile StippleModelConfig myStipple;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     */
    public DefaultPolylineRenderProperties(int zOrder, boolean drawable, boolean pickable)
    {
        super(zOrder, drawable, pickable, false);
    }

    @Override
    public DefaultPolylineRenderProperties clone()
    {
        return (DefaultPolylineRenderProperties)super.clone();
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
        DefaultPolylineRenderProperties other = (DefaultPolylineRenderProperties)obj;
        return Objects.equals(myStipple, other.myStipple);
    }

    @Override
    public StippleModelConfig getStipple()
    {
        return myStipple;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myStipple == null ? 0 : myStipple.hashCode());
        return result;
    }

    @Override
    public void setStipple(StippleModelConfig stipple)
    {
        AtomicReferenceFieldUpdater<DefaultPolylineRenderProperties, StippleModelConfig> updater = AtomicReferenceFieldUpdater
                .newUpdater(DefaultPolylineRenderProperties.class, StippleModelConfig.class, "myStipple");
        StippleModelConfig oldStipple = updater.getAndSet(this, STIPPLE_POOL.get(stipple));
        if (!Objects.equals(stipple, oldStipple))
        {
            notifyChanged();
        }
    }
}
