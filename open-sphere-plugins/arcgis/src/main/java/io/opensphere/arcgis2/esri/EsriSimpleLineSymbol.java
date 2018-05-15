package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Simple line symbols can be used to symbolize polyline geometries. The
 * <code>type</code> property for simple line symbols is <code>esriSLS</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriSimpleLineSymbol extends EsriSymbol
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My style. */
    @JsonProperty("style")
    private EsriSLSStyle myStyle;

    /** My width. */
    @JsonProperty("width")
    private int myWidth;

    /**
     * Gets the style.
     *
     * @return the style
     */
    public EsriSLSStyle getStyle()
    {
        return myStyle;
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
     * Sets the style.
     *
     * @param style the new style
     */
    public void setStyle(EsriSLSStyle style)
    {
        myStyle = style;
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

    /**
     * The Enum EsriSLSStyle.
     */
    public enum EsriSLSStyle
    {
        /** The ESRI SLS dashed-line style. */
        esriSLSDash,

        /** The ESRI SLS dash-dot line style. */
        esriSLSDashDot,

        /** The ESRI SLS dash-dot-dot line style. */
        esriSLSDashDotDot,

        /** The ESRI SLS dotted-line style. */
        esriSLSDot,

        /** The null ESRI SLS style. */
        esriSLSNull,

        /** The ESRI SLS solid line style. */
        esriSLSSolid
    }
}
