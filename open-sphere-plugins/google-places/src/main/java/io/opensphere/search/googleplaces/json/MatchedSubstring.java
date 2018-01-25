package io.opensphere.search.googleplaces.json;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Describes the location of the entered term in the prediction result text, so
 * that the term can be highlighted if desired.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "length", "offset" })
public class MatchedSubstring
{
    /** Size of String. */
    @JsonProperty("length")
    private int myLength;

    /**
     * The position, in the input term, of the last character that the service
     * uses to match predictions.
     */
    @JsonProperty("offset")
    private int myOffset;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Get the length of the string.
     *
     * @return The length
     */
    @JsonProperty("length")
    public int getLength()
    {
        return myLength;
    }

    /**
     * Set the length of the string.
     *
     * @param length The length
     */
    @JsonProperty("length")
    public void setLength(int length)
    {
        myLength = length;
    }

    /**
     * Get the offset.
     *
     * @return The offset
     */
    @JsonProperty("offset")
    public int getOffset()
    {
        return myOffset;
    }

    /**
     * Set the offset.
     *
     * @param offset The offset
     */
    @JsonProperty("offset")
    public void setOffset(int offset)
    {
        myOffset = offset;
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
