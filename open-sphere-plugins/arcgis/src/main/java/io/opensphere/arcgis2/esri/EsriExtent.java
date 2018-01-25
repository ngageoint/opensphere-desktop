package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class EsriExtent.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriExtent implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My spatial reference. */
    @JsonProperty("spatialReference")
    private EsriSpatialReference mySpatialReference;

    /** My x max. */
    @JsonProperty("xmax")
    private double myXMax;

    /** My x min. */
    @JsonProperty("xmin")
    private double myXMin;

    /** My y max. */
    @JsonProperty("ymax")
    private double myYMax;

    /** My y min. */
    @JsonProperty("ymin")
    private double myYMin;

    /**
     * Gets the spatial reference.
     *
     * @return the spatial reference
     */
    public EsriSpatialReference getSpatialReference()
    {
        return mySpatialReference;
    }

    /**
     * Gets the x max.
     *
     * @return the x max
     */
    public double getXMax()
    {
        return myXMax;
    }

    /**
     * Gets the x min.
     *
     * @return the x min
     */
    public double getXMin()
    {
        return myXMin;
    }

    /**
     * Gets the y max.
     *
     * @return the y max
     */
    public double getYMax()
    {
        return myYMax;
    }

    /**
     * Gets the y min.
     *
     * @return the y min
     */
    public double getYMin()
    {
        return myYMin;
    }

    /**
     * Sets the spatial reference.
     *
     * @param reference the new spatial reference
     */
    public void setSpatialReference(EsriSpatialReference reference)
    {
        mySpatialReference = reference;
    }

    /**
     * Sets the x max.
     *
     * @param xmax the new x max
     */
    public void setXMax(double xmax)
    {
        myXMax = xmax;
    }

    /**
     * Sets the x min.
     *
     * @param xmin the new x min
     */
    public void setXMin(double xmin)
    {
        myXMin = xmin;
    }

    /**
     * Sets the y max.
     *
     * @param ymax the new y max
     */
    public void setYMax(double ymax)
    {
        myYMax = ymax;
    }

    /**
     * Sets the y min.
     *
     * @param ymin the new y min
     */
    public void setYMin(double ymin)
    {
        myYMin = ymin;
    }
}
