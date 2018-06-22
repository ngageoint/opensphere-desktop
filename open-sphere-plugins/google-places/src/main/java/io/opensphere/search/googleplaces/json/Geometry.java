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

/**
 * Jackson Annotation.
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "location" })
public class Geometry
{
    /** API response for location array. */
    @JsonProperty("location")
    private Location myLocation;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Geocode of the place.
     *
     * @return The location in Lat/Long
     */
    @JsonProperty("location")
    public Location getLocation()
    {
        return myLocation;
    }

    /**
     * set location field.
     *
     * @param location The location
     */
    @JsonProperty("location")
    public void setLocation(Location location)
    {
        myLocation = location;
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
     * Set values for additional properties is JSON response.
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
