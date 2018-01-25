package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class EsriDrawingInfo.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriDrawingInfo implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My brightness. */
    @JsonProperty("brightness")
    private int myBrightness;

    /** My contrast. */
    @JsonProperty("contrast")
    private int myContrast;

    /** My labeling information. */
    @JsonProperty("labelingInfo")
    private Object myLabelingInfo;

    /** My renderer. */
    @JsonProperty("renderer")
    private EsriRenderer myRenderer;

    /** My scale symbols boolean. */
    @JsonProperty("scaleSymbols")
    private boolean myScaleSymbols;

    /** My transparency. */
    @JsonProperty("transparency")
    private int myTransparency;

    /**
     * Gets the brightness.
     *
     * @return the brightness
     */
    public int getBrightness()
    {
        return myBrightness;
    }

    /**
     * Gets the contrast.
     *
     * @return the contrast
     */
    public int getContrast()
    {
        return myContrast;
    }

    /**
     * Gets the labeling info.
     *
     * @return the labeling info
     */
    public Object getLabelingInfo()
    {
        return myLabelingInfo;
    }

    /**
     * Gets the renderer.
     *
     * @return the renderer
     */
    public EsriRenderer getRenderer()
    {
        return myRenderer;
    }

    /**
     * Gets the transparency.
     *
     * @return the transparency
     */
    public int getTransparency()
    {
        return myTransparency;
    }

    /**
     * Checks if scale symbols flag is set.
     *
     * @return true, if symbols are scaled
     */
    public boolean isScaleSymbols()
    {
        return myScaleSymbols;
    }

    /**
     * Sets the brightness.
     *
     * @param brightness the new brightness
     */
    public void setBrightness(int brightness)
    {
        myBrightness = brightness;
    }

    /**
     * Sets the contrast.
     *
     * @param contrast the new contrast
     */
    public void setContrast(int contrast)
    {
        myContrast = contrast;
    }

    /**
     * Sets the labeling info.
     *
     * @param labelingInfo the new labeling info
     */
    public void setLabelingInfo(Object labelingInfo)
    {
        myLabelingInfo = labelingInfo;
    }

    /**
     * Sets the renderer.
     *
     * @param renderer the new renderer
     */
    public void setRenderer(EsriRenderer renderer)
    {
        myRenderer = renderer;
    }

    /**
     * Sets the scale symbols flag.
     *
     * @param scaleSymbols the new scale symbols flag
     */
    public void setScaleSymbols(boolean scaleSymbols)
    {
        myScaleSymbols = scaleSymbols;
    }

    /**
     * Sets the transparency.
     *
     * @param transparency the new transparency
     */
    public void setTransparency(int transparency)
    {
        myTransparency = transparency;
    }
}
