package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Text symbols are used to add text to a feature (labeling). The
 * <code>type</code> property for text symbols is <code>esriTS</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriTextSymbol extends EsriSymbolWithOffset
{
    /** My background color. */
    @JsonProperty("backgroundColor")
    private EsriColor myBackgroundColor;

    /** My border line color. */
    @JsonProperty("borderLineColor")
    private EsriColor myBorderLineColor;

    /** My font. */
    @JsonProperty("font")
    private EsriFont myFont;

    /** My horizontal alignment. */
    @JsonProperty("horizontalAlignment")
    private EsriHorizontalAlignment myHorizontalAlignment;

    /** My kerning flag. */
    @JsonProperty("kerning")
    private boolean myKerning = true;

    /** My right-to-left flag. */
    @JsonProperty("rightToLeft")
    private boolean myRightToLeft;

    /** My vertical alignment. */
    @JsonProperty("verticalAlignment")
    private EsriVerticalAlignment myVerticalAlignment;

    /**
     * Gets the Background Color.
     *
     * @return the background color
     */
    public EsriColor getBackgroundColor()
    {
        return myBackgroundColor;
    }

    /**
     * Gets the Border Line Color.
     *
     * @return the border line color
     */
    public EsriColor getBorderLineColor()
    {
        return myBorderLineColor;
    }

    /**
     * Gets the font.
     *
     * @return the font
     */
    public EsriFont getFont()
    {
        return myFont;
    }

    /**
     * Gets the horizontal alignment.
     *
     * @return the horizontal alignment
     */
    public EsriHorizontalAlignment getHorizontalAlignment()
    {
        return myHorizontalAlignment;
    }

    /**
     * Gets the vertical alignment.
     *
     * @return the vertical alignment
     */
    public EsriVerticalAlignment getVerticalAlignment()
    {
        return myVerticalAlignment;
    }

    /**
     * Gets the flag indicating if text is kerned.
     *
     * @return the kerning flag
     */
    public boolean isKerning()
    {
        return myKerning;
    }

    /**
     * Gets the flag indicating if text should read right-to-left.
     *
     * @return the right-to-left flag
     */
    public boolean isRightToLeft()
    {
        return myRightToLeft;
    }

    /**
     * Sets the Background Color.
     *
     * @param backgroundColor the new background color
     */
    public void setBackgroundColor(EsriColor backgroundColor)
    {
        myBackgroundColor = backgroundColor;
    }

    /**
     * Sets the Border Line Color.
     *
     * @param borderLineColor the new border line color
     */
    public void setBorderLineColor(EsriColor borderLineColor)
    {
        myBorderLineColor = borderLineColor;
    }

    /**
     * Sets the font.
     *
     * @param font the new font
     */
    public void setFont(EsriFont font)
    {
        myFont = font;
    }

    /**
     * Sets the Horizontal Alignment.
     *
     * @param horizontalAlignment the new horizontal alignment
     */
    public void setHorizontalAlignment(EsriHorizontalAlignment horizontalAlignment)
    {
        myHorizontalAlignment = horizontalAlignment;
    }

    /**
     * Sets the kerning flag.
     *
     * @param kerning the new kerning flag
     */
    public void setKerning(boolean kerning)
    {
        myKerning = kerning;
    }

    /**
     * Sets the right-to-left flag.
     *
     * @param rightToLeft the new right-to-left flag
     */
    public void setRightToLeft(boolean rightToLeft)
    {
        myRightToLeft = rightToLeft;
    }

    /**
     * Sets the Vertical Alignment.
     *
     * @param verticalAlignment the new vertical alignment
     */
    public void setVerticalAlignment(EsriVerticalAlignment verticalAlignment)
    {
        myVerticalAlignment = verticalAlignment;
    }

    /**
     * The Enum EsriHorizontalAlignment.
     */
    public enum EsriHorizontalAlignment
    {
        /** Horizontal text alignment to center. */
        center,

        /** Horizontal text alignment to full justification. */
        justify,

        /** Horizontal text alignment to left. */
        left,

        /** Horizontal text alignment to right. */
        right
    }

    /**
     * The Enum EsriVerticalAlignment.
     */
    public enum EsriVerticalAlignment
    {
        /** Vertical text alignment to baseline. */
        baseline,

        /** Vertical text alignment to bottom. */
        bottom,

        /** Vertical text alignment to middle. */
        middle,

        /** Vertical text alignment to top. */
        top
    }
}
