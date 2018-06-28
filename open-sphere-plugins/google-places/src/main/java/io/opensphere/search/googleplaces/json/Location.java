package io.opensphere.search.googleplaces.json;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/** Location Array [lat,lon] From JSON response. */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "lat", "lng" })
public class Location
{
    /** Location latitude coordinate. */
    @JsonProperty("lat")
    private double myLat;

    /** Location longitude coordinate. */
    @JsonProperty("lng")
    private double myLng;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Latitude coordinate.
     *
     * @return Location latitude coordinate.
     */
    @JsonProperty("lat")
    public double getLat()
    {
        return myLat;
    }

    /**
     * Set latitude.
     *
     * @param lat The lat
     */
    @JsonProperty("lat")
    public void setLat(double lat)
    {
        myLat = lat;
    }

    /**
     * Longitude coordinate.
     *
     * @return The location longitude coordinate.
     */
    @JsonProperty("lng")
    public double getLng()
    {
        return myLng;
    }

    /**
     * Set longitude.
     *
     * @param lng The lng
     */
    @JsonProperty("lng")
    public void setLng(double lng)
    {
        myLng = lng;
    }

    /**
     * Additional objects in JSON response not assigned stored in map.
     *
     * @return additionalProperties
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return additionalProperties;
    }

    /**
     * Set values for additional objects is JSON response.
     *
     * @param name name of object
     * @param value value of object
     */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        additionalProperties.put(name, value);
    }
}
