package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Function;

import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.util.lang.HashCodeHelper;

/** Standard implementation of {@link LabelRenderProperties}. */
public class DefaultLabelRenderProperties extends DefaultColorRenderProperties implements LabelRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The font for the text. */
    private volatile String myFont;

    /** Optional scaling function. */
    private transient volatile Function<Kilometers, Float> myScaleFunction;

    /** The color of the shadow, in ARGB bytes. */
    private volatile int myShadowColor;

    /** The shadow X offset, positive being toward the right of the screen. */
    private volatile float myShadowOffsetX;

    /** The shadow Y offset, positive being toward the top of the screen. */
    private volatile float myShadowOffsetY;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     */
    public DefaultLabelRenderProperties(int zOrder, boolean drawable, boolean pickable)
    {
        super(zOrder, drawable, pickable, true);
    }

    /**
     * Constructor that takes initial colors.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param color The color of the geometry, in ARGB bytes.
     * @param highlightColor The highlight color of the geometry, in ARGB bytes.
     */
    public DefaultLabelRenderProperties(int zOrder, boolean drawable, boolean pickable, int color, int highlightColor)
    {
        super(zOrder, drawable, pickable, true, color, highlightColor);
    }

    /**
     * Constructor that takes initial colors.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param color The color of the geometry, in ARGB bytes.
     * @param highlightColor The highlight color of the geometry, in ARGB bytes.
     * @param shadowColor The color of the label's shadow, in ARGB bytes.
     */
    public DefaultLabelRenderProperties(int zOrder, boolean drawable, boolean pickable, int color, int highlightColor,
            int shadowColor)
    {
        super(zOrder, drawable, pickable, true, color, highlightColor);
        myShadowColor = shadowColor;
    }

    @Override
    public DefaultLabelRenderProperties clone()
    {
        return (DefaultLabelRenderProperties)super.clone();
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
        DefaultLabelRenderProperties other = (DefaultLabelRenderProperties)obj;
        return myShadowColor == other.myShadowColor && Objects.equals(myScaleFunction, other.myScaleFunction);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myShadowColor;
        result = prime * result + HashCodeHelper.getHashCode(myScaleFunction);
        return result;
    }

    @Override
    public String getFont()
    {
        return myFont;
    }

    @Override
    public Function<Kilometers, Float> getScaleFunction()
    {
        return myScaleFunction;
    }

    @Override
    public Color getShadowColor()
    {
        return new Color(myShadowColor, true);
    }

    @Override
    public int getShadowColorARGB()
    {
        return myShadowColor;
    }

    @Override
    public float getShadowOffsetX()
    {
        return myShadowOffsetX;
    }

    @Override
    public float getShadowOffsetY()
    {
        return myShadowOffsetY;
    }

    @Override
    public void setFont(String font)
    {
        myFont = font;
        notifyChanged();
    }

    @Override
    public void setScaleFunction(Function<Kilometers, Float> scaleFunction)
    {
        myScaleFunction = scaleFunction;
        notifyChanged();
    }

    @Override
    public void setShadowColor(Color color)
    {
        setShadowColorARGB(color.getRGB());
    }

    @Override
    public void setShadowColorARGB(int color)
    {
        myShadowColor = color;
        notifyChanged();
    }

    @Override
    public void setShadowOffset(float x, float y)
    {
        myShadowOffsetX = x;
        myShadowOffsetY = y;
    }

    /**
     * Mutator for the shadowOffsetY.
     *
     * @param shadowOffsetY The shadowOffsetY to set.
     */
    public void setShadowOffsetY(float shadowOffsetY)
    {
        myShadowOffsetY = shadowOffsetY;
    }
}
