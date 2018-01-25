package io.opensphere.core.geometry.renderproperties;

/** Standard implementation of {@link ScalableMeshRenderProperties}. */
public class DefaultMeshScalableRenderProperties extends DefaultScalableRenderProperties implements ScalableMeshRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Height of the geometry. */
    private volatile float myHeight = 1f;

    /** Length of the geometry. */
    private volatile float myLength = 1f;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     */
    public DefaultMeshScalableRenderProperties(int zOrder, boolean drawable, boolean pickable)
    {
        super(zOrder, drawable, pickable, true);
    }

    @Override
    public DefaultMeshScalableRenderProperties clone()
    {
        return (DefaultMeshScalableRenderProperties)super.clone();
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
        DefaultMeshScalableRenderProperties other = (DefaultMeshScalableRenderProperties)obj;
        return myLength == other.myLength && myHeight == other.myHeight;
    }

    @Override
    public float getHeight()
    {
        return myHeight;
    }

    @Override
    public float getLength()
    {
        return myLength;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myLength);
        result = prime * result + Float.floatToIntBits(myHeight);
        return result;
    }

    @Override
    public void setHeight(float height)
    {
        myHeight = height;
        notifyChanged();
    }

    @Override
    public void setLength(float length)
    {
        myLength = length;
        notifyChanged();
    }
}
