package io.opensphere.search.mapzen.model.geojson;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/** A JSON object used in MapZen search results. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Geometry
{
    /** Describes the type of the geometry. */
    @JsonProperty("type")
    private String myTypeString;

    /** An enum mapping of the {@link #myTypeString}. */
    private GeoJSONType myType;

    /** The coordinates of the geometry. */
    @JsonProperty("coordinates")
    private double[] myCoordinates;

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
        for (GeoJSONType type : GeoJSONType.values())
        {
            if (StringUtils.equals(pTypeString, type.getType()))
            {
                myType = type;
                break;
            }
        }
    }

    /**
     * Gets the value of the coordinates field.
     *
     * @return the coordinates
     */
    public double[] getCoordinates()
    {
        return myCoordinates;
    }

    /**
     * Assigns the value of the coordinates field to the supplied value.
     *
     * @param pCoordinates the coordinates to set
     */
    public void setCoordinates(double[] pCoordinates)
    {
        myCoordinates = pCoordinates;
    }

    /**
     * Gets the value of the type field.
     *
     * @return the type
     */
    public GeoJSONType getType()
    {
        return myType;
    }
}
