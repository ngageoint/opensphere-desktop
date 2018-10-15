package io.opensphere.core.geometry.renderproperties;

import java.util.Objects;
import java.util.function.Function;

import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Implementation of {@link PointSizeRenderProperty} where size represents a
 * scale of the actual geometry size.
 */
public class PointScaleRenderProperty extends AbstractRenderProperties implements PointSizeRenderProperty
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Size to render the geometry when the geometry is highlighted. */
    private volatile float myHighlightSize = DEFAULT_HIGHLIGHT_SIZE;

    /** Scale to render the geometry. */
    private volatile float myScale = 1f;

    /** Optional scaling function. */
    private transient volatile Function<Kilometers, Float> myScaleFunction;

    @Override
    public float getHighlightSize()
    {
        return myHighlightSize;
    }

    @Override
    public float getSize()
    {
        return myScale;
    }

    /**
     * Gets the scale function.
     *
     * @return the scale function, or null
     */
    public Function<Kilometers, Float> getScaleFunction()
    {
        return myScaleFunction;
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
        myScale = size;
        notifyChanged();
    }

    /**
     * Sets the scale function. The argument is the viewer altitude, the return
     * value is the scale.
     *
     * @param scaleFunction the scale function
     */
    public void setScaleFunction(Function<Kilometers, Float> scaleFunction)
    {
        myScaleFunction = scaleFunction;
        notifyChanged();
    }

    @Override
    public int compareTo(PointSizeRenderProperty o)
    {
        if (myScale == o.getSize())
        {
            return Float.compare(myHighlightSize, o.getHighlightSize());
        }
        return myScale < o.getSize() ? -1 : 1;
    }

    @Override
    public PointScaleRenderProperty clone()
    {
        return (PointScaleRenderProperty)super.clone();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + HashCodeHelper.getHashCode(myHighlightSize);
        result = prime * result + HashCodeHelper.getHashCode(myScale);
        result = prime * result + HashCodeHelper.getHashCode(myScaleFunction);
        return result;
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
        PointScaleRenderProperty other = (PointScaleRenderProperty)obj;
        return EqualsHelper.floatEquals(myHighlightSize, other.myHighlightSize, myScale, other.myScale)
                && Objects.equals(myScaleFunction, other.myScaleFunction);
    }
}
