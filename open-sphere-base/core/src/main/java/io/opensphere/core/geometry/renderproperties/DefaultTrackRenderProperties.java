package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;

/** Standard implementation of {@link TrackRenderProperties}. */
public class DefaultTrackRenderProperties extends DefaultPolylineRenderProperties implements TrackRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The arrow color. */
    private volatile int myArrowColor = Color.YELLOW.getRGB();

    /** The arrow length scale. (0 - 1). */
    private volatile float myArrowLengthScale = 15 / 100;

    /** The arrow width. */
    private volatile float myArrowWidth = 2;

    /** The node color. */
    private volatile int myNodeColor = Color.YELLOW.getRGB();

    /** The node size. */
    private volatile float myNodeSize = 3;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     */
    public DefaultTrackRenderProperties(int zOrder, boolean drawable, boolean pickable)
    {
        super(zOrder, drawable, pickable);
    }

    @Override
    public DefaultTrackRenderProperties clone()
    {
        return (DefaultTrackRenderProperties)super.clone();
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
        DefaultTrackRenderProperties other = (DefaultTrackRenderProperties)obj;
        return myNodeColor == other.myNodeColor && myNodeSize == other.myNodeSize && myArrowColor == other.myArrowColor
                && myArrowWidth == other.myArrowWidth && myArrowLengthScale == other.myArrowLengthScale;
    }

    @Override
    public Color getArrowColor()
    {
        return new Color(myArrowColor, true);
    }

    @Override
    public float getArrowLengthScale()
    {
        return myArrowLengthScale;
    }

    @Override
    public float getArrowWidth()
    {
        return myArrowWidth;
    }

    @Override
    public Color getNodeColor()
    {
        return new Color(myNodeColor, true);
    }

    @Override
    public float getNodeSize()
    {
        return myNodeSize;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myNodeColor;
        result = prime * result + Float.floatToIntBits(myNodeSize);
        result = prime * result + myArrowColor;
        result = prime * result + Float.floatToIntBits(myArrowWidth);
        result = prime * result + Float.floatToIntBits(myArrowLengthScale);
        return result;
    }

    @Override
    public void setArrowColor(Color color)
    {
        myArrowColor = color.getRGB();
        notifyChanged();
    }

    @Override
    public void setArrowLengthScale(float scale)
    {
        myArrowLengthScale = scale;
        notifyChanged();
    }

    @Override
    public void setArrowWidth(float width)
    {
        myArrowWidth = width;
        notifyChanged();
    }

    @Override
    public void setNodeColor(Color color)
    {
        myNodeColor = color.getRGB();
        notifyChanged();
    }

    @Override
    public void setNodeSize(float size)
    {
        myNodeSize = size;
        notifyChanged();
    }
}
