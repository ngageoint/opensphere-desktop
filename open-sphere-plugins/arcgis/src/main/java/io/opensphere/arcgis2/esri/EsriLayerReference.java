package io.opensphere.arcgis2.esri;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class EsriLayerReference. A short reference that provides the name and ID
 * needed to uniquely identify a layer.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriLayerReference implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My id. */
    @JsonProperty("id")
    private int myId;

    /** My max scale. */
    @JsonProperty("maxScale")
    private int myMaxScale;

    /** My min scale. */
    @JsonProperty("minScale")
    private int myMinScale;

    /** My name. */
    @JsonProperty("name")
    private String myName;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId()
    {
        return myId;
    }

    /**
     * Gets the max scale.
     *
     * @return the max scale
     */
    public int getMaxScale()
    {
        return myMaxScale;
    }

    /**
     * Gets the min scale.
     *
     * @return the min scale
     */
    public int getMinScale()
    {
        return myMinScale;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(int id)
    {
        myId = id;
    }

    /**
     * Sets the max scale.
     *
     * @param maxScale the new max scale
     */
    public void setMaxScale(int maxScale)
    {
        myMaxScale = maxScale;
    }

    /**
     * Sets the min scale.
     *
     * @param minScale the new min scale
     */
    public void setMinScale(int minScale)
    {
        myMinScale = minScale;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name)
    {
        myName = name;
    }
}
