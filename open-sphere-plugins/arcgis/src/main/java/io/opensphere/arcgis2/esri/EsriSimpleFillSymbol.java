package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Simple fill symbols can be used to symbolize polygon geometries. The
 * <code>type</code> property for simple fill symbols is <code>esriSFS</code>.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriSimpleFillSymbol extends EsriSymbol
{
    /** My outline. */
    @JsonProperty("outline")
    private EsriSimpleLineSymbol myOutline;

    /** My style. */
    @JsonProperty("style")
    private EsriSFSStyle myStyle;

    /**
     * Gets the outline.
     *
     * @return the outline
     */
    public EsriSimpleLineSymbol getOutline()
    {
        return myOutline;
    }

    /**
     * Gets the style.
     *
     * @return the style
     */
    public EsriSFSStyle getStyle()
    {
        return myStyle;
    }

    /**
     * Sets the outline.
     *
     * @param outline the new outline
     */
    public void setOutline(EsriSimpleLineSymbol outline)
    {
        myOutline = outline;
    }

    /**
     * Sets the style.
     *
     * @param style the new style
     */
    public void setStyle(EsriSFSStyle style)
    {
        myStyle = style;
    }

    /**
     * The Enum EsriSFSStyle.
     */
    public enum EsriSFSStyle
    {
        /** The ESRI SFS backward-diagonal-line fill style. */
        esriSFSBackwardDiagonal,

        /** The ESRI SFS cross-hatched fill style. */
        esriSFSCross,

        /** The ESRI SFS diagonal cross-hatched fill style. */
        esriSFSDiagonalCross,

        /** The ESRI SFS forward-diagonal-line fill style. */
        esriSFSForwardDiagonal,

        /** The ESRI SFS horizontal-line fill style. */
        esriSFSHorizontal,

        /** The null SFS style. */
        esriSFSNull,

        /** The ESRI SFS solid fill style. */
        esriSFSSolid,

        /** The ESRI SFS vertical-line fill style. */
        esriSFSVertical
    }
}
