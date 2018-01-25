package io.opensphere.search.mapzen.model.geojson;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import io.opensphere.core.common.geojson.GeoJSON;
import io.opensphere.core.common.geojson.GeoJSON.Type;

/** A JSON object used in MapZen search results. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature
{
    /** The field in which the result type is expressed. */
    @JsonProperty("type")
    private String myTypeString;

    /** An enum-mapping of the {@link #getTypeString()} field. */
    private GeoJSON.Type myType;

    /** The properties describing the feature. */
    @JsonProperty("properties")
    private Properties myProperties;

    /** The geometry describing the feature. */
    @JsonProperty("geometry")
    private Geometry myGeometry;

    /** The bounding box around the feature. */
    @JsonProperty("bbox")
    private double[] myBoundingBox;

    /**
     * Gets the value of the typeString field.
     *
     * @return the typeString
     */
    public String getTypeString()
    {
        return myTypeString;
    }

    /**
     * Assigns the value of the typeString field to the supplied value.
     *
     * @param pTypeString the typeString to set
     */
    public void setTypeString(String pTypeString)
    {
        myTypeString = pTypeString;
        for (Type type : GeoJSON.Type.values())
        {
            if (StringUtils.equals(pTypeString, type.getType()))
            {
                myType = type;
                break;
            }
        }
    }

    /**
     * Gets the value of the type field.
     *
     * @return the type
     */
    public GeoJSON.Type getType()
    {
        return myType;
    }

    /**
     * Gets the value of the properties field.
     *
     * @return the properties
     */
    public Properties getProperties()
    {
        return myProperties;
    }

    /**
     * Assigns the value of the properties field to the supplied value.
     *
     * @param pProperties the properties to set
     */
    public void setProperties(Properties pProperties)
    {
        myProperties = pProperties;
    }

    /**
     * Gets the value of the geometry field.
     *
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return myGeometry;
    }

    /**
     * Assigns the value of the geometry field to the supplied value.
     *
     * @param pGeometry the geometry to set
     */
    public void setGeometry(Geometry pGeometry)
    {
        myGeometry = pGeometry;
    }

    /**
     * Gets the value of the boundingBox field.
     *
     * @return the boundingBox
     */
    public double[] getBoundingBox()
    {
        return myBoundingBox;
    }

    /**
     * Assigns the value of the boundingBox field to the supplied value.
     *
     * @param pBoundingBox the boundingBox to set
     */
    public void setBoundingBox(double[] pBoundingBox)
    {
        myBoundingBox = pBoundingBox;
    }
}
