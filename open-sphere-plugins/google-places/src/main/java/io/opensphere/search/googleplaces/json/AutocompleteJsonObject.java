package io.opensphere.search.googleplaces.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Class that binds Google Places Autocomplete JSON response to java objects.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "predictions", "status" })
public class AutocompleteJsonObject
{
    /** Contains an array of places, with information about the place. */
    @JsonProperty("predictions")
    private List<Prediction> myPredictions = new ArrayList<Prediction>();

    /** Contains metadata on the request. */
    @JsonProperty("status")
    private String myStatus;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Places service returns JSON results from a search.
     *
     * @return The predictions
     */
    @JsonProperty("predictions")
    public List<Prediction> getPredictions()
    {
        return myPredictions;
    }

    /**
     * Set the predictions.
     *
     * @param predictions The predictions
     */
    @JsonProperty("predictions")
    public void setPredictions(List<Prediction> predictions)
    {
        myPredictions = predictions;
    }

    /**
     * The status of the request. Useful for debugging responses.
     *
     * @return The status
     */
    @JsonProperty("status")
    public String getStatus()
    {
        return myStatus;
    }

    /**
     * Set the status of the response.
     *
     * @param status The status
     */
    @JsonProperty("status")
    public void setStatus(String status)
    {
        myStatus = status;
    }

    /**
     * Additional objects in JSON response not assigned are stored in a map.
     *
     * @return addtionalProperties.
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return additionalProperties;
    }

    /**
     * Set additional objects in JSON response.
     *
     * @param name -- name.
     * @param value -- value.
     */
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        additionalProperties.put(name, value);
    }
}
