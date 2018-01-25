package io.opensphere.search.mapzen.model.geojson;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import io.opensphere.core.common.geojson.GeoJSON;
import io.opensphere.core.common.geojson.GeoJSON.Type;

/** A JSON object used in MapZen search results. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoJsonObject
{
    /** The GeoCoding information of the search results. */
    @JsonProperty("geocoding")
    private GeoCoding myGeoCoding;

    /** The type of search result. Typically "FeatureCollection". */
    @JsonProperty("type")
    private String myTypeString;

    /** An enum mapping of the {@link #myTypeString} field. */
    private GeoJSON.Type myType;

    /** The set of features in the results. */
    @JsonProperty("features")
    private Feature[] myFeatures;

    /** The bounding box encapsulating the results. */
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
     * Gets the value of the geoCoding field.
     *
     * @return the geoCoding
     */
    public GeoCoding getGeoCoding()
    {
        return myGeoCoding;
    }

    /**
     * Assigns the value of the geoCoding field to the supplied value.
     *
     * @param pGeoCoding the geoCoding to set
     */
    public void setGeoCoding(GeoCoding pGeoCoding)
    {
        myGeoCoding = pGeoCoding;
    }

    /**
     * Gets the value of the features field.
     *
     * @return the features
     */
    public Feature[] getFeatures()
    {
        return myFeatures;
    }

    /**
     * Assigns the value of the features field to the supplied value.
     *
     * @param pFeatures the features to set
     */
    public void setFeatures(Feature[] pFeatures)
    {
        myFeatures = pFeatures;
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
