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
 * Contains an array of terms identifying each section of the returned
 * description.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "offset", "value" })
public class Term
{
    /**
     * The position, in the input term, of the last character that the service
     * uses to match predictions.
     */
    @JsonProperty("offset")
    private int myOffset;

    /** Term value. */
    @JsonProperty("value")
    private String myValue;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Get the position of the last character the service uses.
     *
     * @return The offset
     */
    @JsonProperty("offset")
    public int getOffset()
    {
        return myOffset;
    }

    /**
     * Set the last position of the last character the service uses.
     *
     * @param offset The offset
     */
    @JsonProperty("offset")
    public void setOffset(int offset)
    {
        myOffset = offset;
    }

    /**
     * Get the value of the term.
     *
     * @return The value
     */
    @JsonProperty("value")
    public String getValue()
    {
        return myValue;
    }

    /**
     * Set the value of the term.
     *
     * @param value The value
     */
    @JsonProperty("value")
    public void setValue(String value)
    {
        myValue = value;
    }

    /**
     * Get the additional properties not defined.
     *
     * @return additional properties
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return additionalProperties;
    }

    /**
     * Set additional properties.
     *
     * @param name set the name.
     * @param value set the value.
     */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        additionalProperties.put(name, value);
    }
}
