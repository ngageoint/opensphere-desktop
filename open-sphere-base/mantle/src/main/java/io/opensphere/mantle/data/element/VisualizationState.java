package io.opensphere.mantle.data.element;

import java.awt.Color;
import java.io.Serializable;
import java.util.Objects;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.lang.BitArrays;

/**
 * The Class VisualizationState.
 */
public class VisualizationState implements Serializable
{
    /** The VisualizationState property descriptor. */
    public static final PropertyDescriptor<VisualizationState> PROPERTY_DESCRIPTOR = new PropertyDescriptor<VisualizationState>(
            VisualizationState.class.getName(), VisualizationState.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The DEFAULT_COLOR. */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    /** Mask for Selection flag. */
    private static final byte SELECTED_MASK = 1;

    /** Mask for visible flag. */
    private static final byte VISIBLE_MASK = 2;

    /** Mask for lob visible flag. */
    private static final byte LOB_VISIBLE_MASK = 4;

    /** Mask for is map data element visible flag. */
    private static final byte IS_MAP_DATA_ELEMENT = 8;

    /** The Constant HAS_ALTERNATE_GEOMETRY_SUPPORT. */
    private static final byte HAS_ALTERNATE_GEOMETRY_SUPPORT = 16;

    /**
     * The value of the highest bit defined by this class. This is to be used as
     * an offset by subclasses that need to define their own bits.
     */
    protected static final byte HIGH_BIT = HAS_ALTERNATE_GEOMETRY_SUPPORT;

    /** The altitude adjust in meters. */
    private float myAltitudeAdjust;

    /** Current color of the data element. */
    private Color myColor = DEFAULT_COLOR;

    /** Bit Field for flag storage. 8 bits max. */
    private volatile byte myFlagsBitField;

    /**
     * Instantiates a new data element visualization state.
     *
     * @param isMapDataElement the is map data element
     */
    public VisualizationState(boolean isMapDataElement)
    {
        this(isMapDataElement, true, false, true);
        setHasAlternateGeometrySupport(false);
    }

    /**
     * Instantiates a new visualization state.
     *
     * @param isMapDataElement the is map data element
     * @param visibility the visibility
     * @param selected the selected
     */
    public VisualizationState(boolean isMapDataElement, boolean visibility, boolean selected)
    {
        this(isMapDataElement, visibility, selected, false);
    }

    /**
     * Instantiates a new visualization state.
     *
     * @param isMapDataElement the is map data element
     * @param visibility the visibility
     * @param selected the selected
     * @param lobVisible the lob visible
     */
    public VisualizationState(boolean isMapDataElement, boolean visibility, boolean selected, boolean lobVisible)
    {
        setFlag(IS_MAP_DATA_ELEMENT, isMapDataElement);
        setVisible(visibility);
        setLobVisible(lobVisible);
        setSelected(selected);
    }

    /**
     * Instantiates a new visualization state. Copy Constructor.
     *
     * @param other the other
     */
    public VisualizationState(VisualizationState other)
    {
        myFlagsBitField = other.myFlagsBitField;
        myColor = other.myColor;
        myAltitudeAdjust = other.myAltitudeAdjust;
    }

    /**
     * Gets the altitude adjust( in meters ).
     *
     * @return the altitude adjust ( in meters )
     */
    public final float getAltitudeAdjust()
    {
        return myAltitudeAdjust;
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public final Color getColor()
    {
        return myColor;
    }

    /**
     * Checks for alternate geometry support flag.
     *
     * @return true, if has an alternate geometry support.
     */
    public final boolean hasAlternateGeometrySupport()
    {
        return isFlagSet(HAS_ALTERNATE_GEOMETRY_SUPPORT);
    }

    /**
     * Checks if is default color.
     *
     * @return true, if is default color
     */
    public boolean isDefaultColor()
    {
        return DEFAULT_COLOR.equals(myColor);
    }

    /**
     * Checks if is lob visible.
     *
     * @return true, if is lob visible
     */
    public final boolean isLobVisible()
    {
        return isFlagSet(LOB_VISIBLE_MASK);
    }

    /**
     * Checks if is a map data element.
     *
     * @return true, if is a map data element
     */
    public final boolean isMapDataElement()
    {
        return isFlagSet(IS_MAP_DATA_ELEMENT);
    }

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     */
    public final boolean isSelected()
    {
        return isFlagSet(SELECTED_MASK);
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public final boolean isVisible()
    {
        return isFlagSet(VISIBLE_MASK);
    }

    /**
     * Sets the altitude adjust. (m)
     *
     * @param adjust the adjust (m)
     * @return true, if changed
     */
    public final boolean setAltitudeAdjust(float adjust)
    {
        boolean changed = myAltitudeAdjust != adjust;
        myAltitudeAdjust = adjust;
        return changed;
    }

    /**
     * Sets the color.
     *
     * @param c the c
     * @return true if changed
     */
    public final boolean setColor(Color c)
    {
        if (c == null)
        {
            throw new IllegalArgumentException("Color cannot be null");
        }

        boolean changed = !myColor.equals(c);
        myColor = c;
        return changed;
    }

    /**
     * Sets (or un-sets) a flag in the internal bit field.
     *
     * @param mask - the mask to use
     * @param on - true to set on, false to set off
     * @return true if changed.
     */
    public final boolean setFlag(byte mask, boolean on)
    {
        byte oldBitField = myFlagsBitField;
        byte newBitField = BitArrays.setFlag(mask, on, oldBitField);
        boolean changed = newBitField != oldBitField;
        myFlagsBitField = newBitField;
        return changed;
    }

    /**
     * Sets if this CacheEntry has an alternate geometry support.
     *
     * @param hasAlternate the has alternate geometry support.
     * @return true if this VisualizationState has an alternate geometry
     *         support.
     */
    public final boolean setHasAlternateGeometrySupport(boolean hasAlternate)
    {
        return setFlag(HAS_ALTERNATE_GEOMETRY_SUPPORT, hasAlternate);
    }

    /**
     * Sets the lob visible flag.
     *
     * @param on the on
     * @return true, if changed
     */
    public final boolean setLobVisible(boolean on)
    {
        return setFlag(LOB_VISIBLE_MASK, on);
    }

    /**
     * Sets the opacity. Range 0 - 255.
     *
     * Note that this sets the alpha channel of the color, it is not an
     * independent parameter.
     *
     * @param alpha - the new type opacity
     * @return true, if changed
     */
    public final boolean setOpacity(int alpha)
    {
        boolean changed = false;
        Color c = getColor();
        if (c.getAlpha() != alpha)
        {
            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            setColor(new Color(r, g, b, alpha));
            changed = true;
        }
        return changed;
    }

    /**
     * Sets selected.
     *
     * @param selected - true to select
     * @return true, if changed state
     */
    public final boolean setSelected(boolean selected)
    {
        return setFlag(SELECTED_MASK, selected);
    }

    /**
     * Sets the visible.
     *
     * @param on the on
     * @return true, if changed
     */
    public final boolean setVisible(boolean on)
    {
        return setFlag(VISIBLE_MASK, on);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        VisualizationState other = (VisualizationState)obj;
        return Float.floatToIntBits(myAltitudeAdjust) == Float.floatToIntBits(other.myAltitudeAdjust)
                && myFlagsBitField == other.myFlagsBitField && Objects.equals(myColor, other.myColor);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(myAltitudeAdjust);
        result = prime * result + myFlagsBitField;
        result = prime * result + Objects.hashCode(myColor);
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("VisulaizationState: \n  Selected[").append(isSelected()).append("]\n  Visible[").append(isVisible())
                .append("]\n  LOBVisible[").append(isLobVisible()).append("]\n  Color[").append(getColor()).append("]\n  AltAdj[")
                .append(getAltitudeAdjust()).append(']');
        return sb.toString();
    }

    /**
     * Checks to see if a flag is set in the internal bit field.
     *
     * @param mask - the mask to check
     * @return true if set, false if not
     */
    private boolean isFlagSet(byte mask)
    {
        return BitArrays.isFlagSet(mask, myFlagsBitField);
    }
}
