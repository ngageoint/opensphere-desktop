package io.opensphere.arcgis2.esri;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Parent class for EsriSymbol types with x,y offset and angle.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriSymbolWithOffset extends EsriSymbol
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My angle. */
    @JsonProperty("angle")
    private int myAngle;

    /** My x offset. */
    @JsonProperty("xoffset")
    private int myXOffset;

    /** My y offset. */
    @JsonProperty("yoffset")
    private int myYOffset;

    /**
     * Gets the angle.
     *
     * @return the angle
     */
    public int getAngle()
    {
        return myAngle;
    }

    /**
     * Gets the x offset.
     *
     * @return the x offset
     */
    public int getXOffset()
    {
        return myXOffset;
    }

    /**
     * Gets the y offset.
     *
     * @return the y offset
     */
    public int getYOffset()
    {
        return myYOffset;
    }

    /**
     * Sets the angle.
     *
     * @param angle the new angle
     */
    public void setAngle(int angle)
    {
        myAngle = angle;
    }

    /**
     * Sets the x offset.
     *
     * @param xOffset the new x offset
     */
    public void setXOffset(int xOffset)
    {
        myXOffset = xOffset;
    }

    /**
     * Sets the y offset.
     *
     * @param yOffset the new y offset
     */
    public void setYOffset(int yOffset)
    {
        myYOffset = yOffset;
    }
}
