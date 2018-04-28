package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * <p>
 * Picture fill symbols can be used to symbolize polygon geometries. The
 * <code>type</code> property for simple marker symbols is <code>esriPFS</code>.
 * </p>
 * These symbols include the base64 encoded <code>imageData</code> as well as a
 * <code>url</code> that could be used to retrieve the image from the server.
 * Note that this is a relative URL. It can be dereferenced by accessing the
 * "map layer image resource" or the "feature layer image resource."
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriPictureFillSymbol extends EsriPictureMarkerSymbol
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1;

    /** My Outline. */
    @JsonProperty("outline")
    private EsriSimpleLineSymbol myOutline;

    /** My x scale. */
    @JsonProperty("xscale")
    private int myXScale;

    /** My y scale. */
    @JsonProperty("yscale")
    private int myYScale;

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
     * Gets the x scale.
     *
     * @return the x scale
     */
    public int getXScale()
    {
        return myXScale;
    }

    /**
     * Gets the y scale.
     *
     * @return the y scale
     */
    public int getYScale()
    {
        return myYScale;
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
     * Sets the x scale.
     *
     * @param xScale the new x scale
     */
    public void setXScale(int xScale)
    {
        myXScale = xScale;
    }

    /**
     * Sets the y scale.
     *
     * @param yScale the new y scale
     */
    public void setYScale(int yScale)
    {
        myYScale = yScale;
    }
}
