package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Simple marker symbols can be used to symbolize point geometries. The
 * <code>type</code> property for simple marker symbols is <code>esriSMS</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriSimpleMarkerSymbol extends EsriSymbolWithOffset
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My outline. */
    @JsonProperty("outline")
    private EsriOutline myOutline;

    /** My size. */
    @JsonProperty("size")
    private int mySize;

    /** My style. */
    @JsonProperty("style")
    private EsriSMSStyle myStyle;

    /**
     * Gets the outline.
     *
     * @return the outline
     */
    public EsriOutline getOutline()
    {
        return myOutline;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * Gets the style.
     *
     * @return the style
     */
    public EsriSMSStyle getStyle()
    {
        return myStyle;
    }

    /**
     * Sets the outline.
     *
     * @param outline the new outline
     */
    public void setOutline(EsriOutline outline)
    {
        myOutline = outline;
    }

    /**
     * Sets the size.
     *
     * @param size the new size
     */
    public void setSize(int size)
    {
        mySize = size;
    }

    /**
     * Sets the style.
     *
     * @param style the new style
     */
    public void setStyle(EsriSMSStyle style)
    {
        myStyle = style;
    }

    /** The Class EsriOutline. */
    @JsonAutoDetect(JsonMethod.NONE)
    public static class EsriOutline implements Serializable
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** My color. */
        @JsonProperty("color")
        private EsriColor myColor;

        /** My width. */
        @JsonProperty("width")
        private int myWidth;

        /**
         * Gets the color.
         *
         * @return the color
         */
        public EsriColor getColor()
        {
            return myColor;
        }

        /**
         * Gets the width.
         *
         * @return the width
         */
        public int getWidth()
        {
            return myWidth;
        }

        /**
         * Sets the color.
         *
         * @param color the new color
         */
        public void setColor(EsriColor color)
        {
            myColor = color;
        }

        /**
         * Sets the width.
         *
         * @param width the new width
         */
        public void setWidth(int width)
        {
            myWidth = width;
        }
    }

    /**
     * The Enum EsriSMSStyle.
     */
    public enum EsriSMSStyle
    {
        /** The ESRI SMS circle style. */
        esriSMSCircle,

        /** The ESRI SMS cross style. */
        esriSMSCross,

        /** The ESRI SMS diamond style. */
        esriSMSDiamond,

        /** The ESRI SMS square style. */
        esriSMSSquare,

        /** The ESRI SMS "X" style. */
        esriSMSX
    }
}
