package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

import io.opensphere.core.util.ColorUtilities;

/** Standard implementation of {@link ColorRenderProperties}. */
public class DefaultColorRenderProperties extends DefaultBaseRenderProperties implements ColorRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The color of this geometry, in ARGB bytes. */
    private volatile int myColor;

    /** The highlight color of this geometry, in ARGB bytes. */
    private volatile int myHighlightColor;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     */
    public DefaultColorRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant)
    {
        super(zOrder, drawable, pickable, obscurant);
        myColor = Color.WHITE.getRGB();
        myHighlightColor = DEFAULT_HIGHLIGHT_COLOR;
    }

    /**
     * Constructor that takes initial colors.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     * @param color The color of the geometry, in ARGB bytes.
     * @param highlightColor The highlight color of the geometry, in ARGB bytes.
     */
    public DefaultColorRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant, int color,
            int highlightColor)
    {
        super(zOrder, drawable, pickable, obscurant);
        myColor = color;
        myHighlightColor = highlightColor;
    }

    @Override
    public DefaultColorRenderProperties clone()
    {
        return (DefaultColorRenderProperties)super.clone();
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
        DefaultColorRenderProperties other = (DefaultColorRenderProperties)obj;
        return myColor == other.myColor && myHighlightColor == other.myHighlightColor;
    }

    @Override
    public Color getColor()
    {
        return new Color(myColor, true);
    }

    @Override
    public int getColorARGB()
    {
        return myColor;
    }

    @Override
    public Color getHighlightColor()
    {
        return new Color(myHighlightColor, true);
    }

    @Override
    public int getHighlightColorARGB()
    {
        return myHighlightColor;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myColor;
        result = prime * result + myHighlightColor;
        return result;
    }

    @Override
    public void opacitizeColor(float opacity)
    {
        setColor(ColorUtilities.opacitizeColor(getColor(), opacity));
    }

    @Override
    public void setColor(Color color)
    {
        setColorARGB(color.getRGB());
    }

    @Override
    public void setColorARGB(int color)
    {
        myColor = color;
        notifyChanged();
    }

    @Override
    public void setHighlightColor(Color color)
    {
        myHighlightColor = color.getRGB();
        notifyChanged();
    }

    @Override
    public void setHighlightColorARGB(int color)
    {
        myHighlightColor = color;
        notifyChanged();
    }
}
