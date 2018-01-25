package io.opensphere.wms.sld.ui;

import javax.swing.JPanel;
import javax.xml.bind.JAXBElement;

import net.opengis.sld._100.SymbolizerType;

/**
 * The Class AbstractSymbolizer.
 */
public abstract class AbstractSymbolizer extends JPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Well known name. */
    private WellKnownName myWellKnownName;

    /** The Stroke color. */
    private String myStrokeColor;

    /** The Stroke width. */
    private String myStrokeWidth;

    /** The Opacity. */
    private float myOpacity;

//    /** The Size. */
//    private int mySize;

    /**
     * Gets the opacity.
     *
     * @return the opacity
     */
    public float getOpacity()
    {
        return myOpacity;
    }

    /**
     * Gets the stroke color.
     *
     * @return the stroke color
     */
    public String getStrokeColor()
    {
        return myStrokeColor;
    }

    /**
     * Gets the stroke width.
     *
     * @return the stroke width
     */
    public String getStrokeWidth()
    {
        return myStrokeWidth;
    }

    /**
     * Gets the well known name.
     *
     * @return the well known name
     */
    public WellKnownName getWellKnownName()
    {
        return myWellKnownName;
    }

    /**
     * Sets the opacity.
     *
     * @param opacity the new opacity
     */
    public void setOpacity(float opacity)
    {
        myOpacity = opacity;
    }

    /**
     * Sets the stroke color.
     *
     * @param strokeColor the new stroke color
     */
    public void setStrokeColor(String strokeColor)
    {
        myStrokeColor = strokeColor;
    }

    /**
     * Sets the stroke width.
     *
     * @param strokeWidth the new stroke width
     */
    public void setStrokeWidth(String strokeWidth)
    {
        myStrokeWidth = strokeWidth;
    }

//    /**
//     * Gets the size.
//     *
//     * @return the size
//     */
//    @Override
//    public int getSize()
//    {
//        return mySize;
//    }
//
//    /**
//     * Sets the size.
//     *
//     * @param size the new size
//     */
//    public void setSize(int size)
//    {
//        mySize = size;
//    }

    /**
     * Sets the well known name.
     *
     * @param wellKnownName the new well known name
     */
    public void setWellKnownName(WellKnownName wellKnownName)
    {
        myWellKnownName = wellKnownName;
    }

    /**
     * Validate inputs.
     *
     * @return the new symbolizer type
     */
    public abstract JAXBElement<? extends SymbolizerType> validateInputs();

    /**
     * Gets the rgb color and returns the hex equivalent prepended with '0xX'.
     *
     * @param intColor the int rgb color value
     * @return the hex string equivalent
     */
    protected String getRGBtoHexColor(int intColor)
    {
        return String.format("#%06X", Integer.valueOf(0XFFFFFF & intColor));
    }

    /**
     * The Enum WellKnownName.
     */
    public enum WellKnownName
    {
        /** The SQUARE. */
        SQUARE("Square"),

        /** The CIRCLE. */
        CIRCLE("Circle"),

        /** The TRIANGLE. */
        TRIANGLE("Triangle"),

        /** The STAR. */
        STAR("Star"),

        /** The CROSS. */
        CROSS("Cross"),

        /** The ECKS. */
        ECKS("X");

//        /** The SLASH. */
//        SLASH_("Slash"),
//
//        /** The BACKSLASH. */
//        BACKSLASH_("Backslash"),
//
//        /** The PLUS. */
//        PLUS_("Plus"),
//
//        /** The TIMES. */
//        TIMES_("Times"),
//
//        /** The HLINE. */
//        HLINE("Horizontal Line"),
//
//        /** The VLINE. */
//        VLINE("Vertical Line"),
//
//        /** The HORIZLINE. */
//        HORIZLINE_("horizline"),
//
//        /** The VERTLINE. */
//        VERTLINE_("vertline");

        /** The Title. */
        private final String myTitle;

        /**
         * Instantiates a new well known name.
         *
         * @param title the title
         */
        WellKnownName(String title)
        {
            myTitle = title;
        }

        @Override
        public String toString()
        {
            return myTitle;
        }
    }
}
