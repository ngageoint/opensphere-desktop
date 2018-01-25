package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class EsriSpatialReference.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriSpatialReference implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My Well Known ID (wkid). */
    @JsonProperty("wkid")
    private int myWkid;

    /** My Well Known Text (wkt). */
    @JsonProperty("wkt")
    private String myWkt;

    /**
     * Gets the Well Known ID.
     *
     * @return the Well Known ID
     */
    public int getWkid()
    {
        return myWkid;
    }

    /**
     * Gets the Well Known Text.
     *
     * @return the Well Known Text
     */
    public String getWkt()
    {
        return myWkt;
    }

    /**
     * Sets the Well Known ID.
     *
     * @param wkid the new Well Known ID
     */
    public void setWkid(int wkid)
    {
        myWkid = wkid;
    }

    /**
     * Sets the Well Known Text.
     *
     * @param wkt the new Well Known Text
     */
    public void setWkt(String wkt)
    {
        myWkt = wkt;
    }
}
