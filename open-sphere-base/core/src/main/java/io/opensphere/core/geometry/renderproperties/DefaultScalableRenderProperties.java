package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

/** Standard implementation of {@link ScalableRenderProperties}. */
public class DefaultScalableRenderProperties extends DefaultBaseAltitudeRenderProperties implements ScalableRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The base color of this geometry, in ARGB bytes. */
    private volatile int myBaseColor = Color.WHITE.getRGB();

    /** Width of the geometry. */
    private volatile float myWidth = 1f;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     */
    public DefaultScalableRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant)
    {
        super(zOrder, drawable, pickable, obscurant);
    }

    @Override
    public DefaultScalableRenderProperties clone()
    {
        return (DefaultScalableRenderProperties)super.clone();
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
        DefaultScalableRenderProperties other = (DefaultScalableRenderProperties)obj;
        return myBaseColor == other.myBaseColor && myWidth == other.myWidth;
    }

    @Override
    public Color getBaseColor()
    {
        return new Color(myBaseColor, true);
    }

    @Override
    public float getWidth()
    {
        return myWidth;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myBaseColor;
        result = prime * result + Float.floatToIntBits(myWidth);
        return result;
    }

    @Override
    public void setBaseColor(Color baseColor)
    {
        myBaseColor = baseColor.getRGB();
        notifyChanged();
    }

    @Override
    public void setWidth(float width)
    {
        myWidth = width;
        notifyChanged();
    }
}
