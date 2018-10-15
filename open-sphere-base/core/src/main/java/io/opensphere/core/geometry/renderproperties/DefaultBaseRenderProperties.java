package io.opensphere.core.geometry.renderproperties;

import java.util.Objects;

/** Standard implementation of {@link BaseRenderProperties}. */
public class DefaultBaseRenderProperties extends DefaultZOrderRenderProperties implements BaseRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The lighting model to activate before rendering. */
    private transient volatile BlendingConfigGL myBlending;

    /** Indicates the whether the geometry may be drawn. */
    private final boolean myDrawable;

    /** True when the geometry should not be rendered even when in view. */
    private volatile boolean myHidden;

    /** The lighting model to activate before rendering. */
    private volatile LightingModelConfigGL myLighting;

    /** Indicates if this geometry is sensitive to the mouse cursor. */
    private final boolean myPickable;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     */
    public DefaultBaseRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant)
    {
        super(zOrder, obscurant);
        myDrawable = drawable;
        myPickable = pickable;
    }

    @Override
    public DefaultBaseRenderProperties clone()
    {
        return (DefaultBaseRenderProperties)super.clone();
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
        DefaultBaseRenderProperties other = (DefaultBaseRenderProperties)obj;
        return myHidden == other.myHidden && myDrawable == other.myDrawable && myPickable == other.myPickable
                && Objects.equals(myLighting, other.myLighting);
    }

    @Override
    public BlendingConfigGL getBlending()
    {
        return myBlending;
    }

    @Override
    public LightingModelConfigGL getLighting()
    {
        return myLighting;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myHidden ? 1231 : 1237);
        result = prime * result + (myDrawable ? 1231 : 1237);
        result = prime * result + (myPickable ? 1231 : 1237);
        result = prime * result + (myLighting == null ? 0 : myLighting.hashCode());
        return result;
    }

    @Override
    public boolean isDrawable()
    {
        return myDrawable;
    }

    @Override
    public boolean isHidden()
    {
        return myHidden;
    }

    @Override
    public boolean isPickable()
    {
        return myPickable;
    }

    @Override
    public void setBlending(BlendingConfigGL blend)
    {
        myBlending = blend;
        notifyChanged();
    }

    @Override
    public void setHidden(boolean hidden)
    {
        if (myHidden != hidden)
        {
            myHidden = hidden;
            notifyChanged();
        }
    }

    @Override
    public void setLighting(LightingModelConfigGL lighting)
    {
        myLighting = lighting;
        notifyChanged();
    }
}
