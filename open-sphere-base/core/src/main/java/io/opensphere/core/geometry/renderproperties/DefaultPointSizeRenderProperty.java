package io.opensphere.core.geometry.renderproperties;

/** Standard implementation of {@link PointSizeRenderProperty}. */
public class DefaultPointSizeRenderProperty extends AbstractRenderProperties implements PointSizeRenderProperty
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Size to render the geometry when the geometry is highlighted. */
    private volatile float myHighlightSize = DEFAULT_HIGHLIGHT_SIZE;

    /** Size to render the geometry. */
    private volatile float mySize = 1f;

    @Override
    public DefaultPointSizeRenderProperty clone()
    {
        return (DefaultPointSizeRenderProperty)super.clone();
    }

    @Override
    public int compareTo(PointSizeRenderProperty o)
    {
        if (mySize == o.getSize())
        {
            return Float.compare(myHighlightSize, o.getHighlightSize());
        }
        return mySize < o.getSize() ? -1 : 1;
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
        DefaultPointSizeRenderProperty other = (DefaultPointSizeRenderProperty)obj;
        return Float.floatToIntBits(myHighlightSize) == Float.floatToIntBits(other.myHighlightSize)
                && Float.floatToIntBits(mySize) == Float.floatToIntBits(other.mySize);
    }

    @Override
    public float getHighlightSize()
    {
        return myHighlightSize;
    }

    @Override
    public float getSize()
    {
        return mySize;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(myHighlightSize);
        result = prime * result + Float.floatToIntBits(mySize);
        return result;
    }

    @Override
    public void setHighlightSize(float size)
    {
        myHighlightSize = size;
        notifyChanged();
    }

    @Override
    public void setSize(float size)
    {
        mySize = size;
        notifyChanged();
    }
}
