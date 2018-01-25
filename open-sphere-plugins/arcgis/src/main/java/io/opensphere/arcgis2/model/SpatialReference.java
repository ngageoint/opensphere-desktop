package io.opensphere.arcgis2.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Contains the spatial reference of a layer.
 */
public class SpatialReference
{
    /**
     * The newest reference code.
     */
    @JsonProperty("latestWkid")
    private int myLatestWkid;

    /**
     * The reference code.
     */
    @JsonProperty("wkid")
    private int myWkid;

    /**
     * Gets The newest reference code.
     *
     * @return The newest reference code.
     */
    public int getLatestWkid()
    {
        return myLatestWkid;
    }

    /**
     * Gets The reference code.
     *
     * @return The reference code.
     */
    public int getWkid()
    {
        return myWkid;
    }

    /**
     * Sets The newest reference code.
     *
     * @param latestWkid The newest reference code.
     */
    public void setLatestWkid(int latestWkid)
    {
        myLatestWkid = latestWkid;
    }

    /**
     * Sets The reference code.
     *
     * @param wkid The reference code.
     */
    public void setWkid(int wkid)
    {
        myWkid = wkid;
    }
}
