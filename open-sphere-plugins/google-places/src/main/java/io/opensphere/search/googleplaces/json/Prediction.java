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

/** Class that bind predictions to objects.*/
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "description", "id", "matched_substrings", "place_id", "reference", "terms", "types" })
public class Prediction
{
    /** Contains the human-readable name for the returned result. */
    @JsonProperty("description")
    private String myDescription;

    /**
     * Contains a unique stable identifier denoting this place. This identifier
     * may not be used to retrieve information about this place, but can be used
     * to consolidate data about this place, and to verify the identity of a
     * place across separate searches.
     */
    @JsonProperty("id")
    private String myId;

    /**
     * Contains an offset value and a length. These describe the location of the
     * entered term in the prediction result text, so that the term can be
     * highlighted if desired.
     */
    @JsonProperty("matched_substrings")
    private List<MatchedSubstring> myMatchedSubstrings = new ArrayList<MatchedSubstring>();

    /** Is a textual identifier that uniquely identifies a place. */
    @JsonProperty("place_id")
    private String myPlaceId;

    /**
     * Contains a unique token that you can use to retrieve additional
     * information about this place in a Place Details request.
     */
    @JsonProperty("reference")
    private String myReference;

    /**
     * Contains an array of terms identifying each section of the returned
     * description.
     */
    @JsonProperty("terms")
    private List<Term> myTerms = new ArrayList<Term>();

    /** Contains an array of types that apply to this place. */
    @JsonProperty("types")
    private List<String> myTypes = new ArrayList<String>();

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Get the human readable name of place.
     *
     * @return The description
     */
    @JsonProperty("description")
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Set the human readable name of place.
     *
     * @param description The description.
     */
    @JsonProperty("description")
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Get the unique identifier of the place.
     *
     * @return The id
     */
    @JsonProperty("id")
    public String getId()
    {
        return myId;
    }

    /**
     * Set the unique identifier of the place.
     *
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Get the offset and length of text to hightlight string.
     *
     * @return The matchedSubstrings
     */
    @JsonProperty("matched_substrings")
    public List<MatchedSubstring> getMatchedSubstrings()
    {
        return myMatchedSubstrings;
    }

    /**
     * Set the offset and length of text.
     *
     * @param matchedSubstrings The matched_substrings
     */
    @JsonProperty("matched_substrings")
    public void setMatchedSubstrings(List<MatchedSubstring> matchedSubstrings)
    {
        myMatchedSubstrings = matchedSubstrings;
    }

    /**
     * Get the unique identifier for the place.
     *
     * @return The placeId
     */
    @JsonProperty("place_id")
    public String getPlaceId()
    {
        return myPlaceId;
    }

    /**
     * Set the unique identifier for the place.
     *
     * @param placeId The place_id
     */
    @JsonProperty("place_id")
    public void setPlaceId(String placeId)
    {
        myPlaceId = placeId;
    }

    /**
     * Get the unique token to retrieve more information.
     *
     * @return The reference
     */
    @JsonProperty("reference")
    public String getReference()
    {
        return myReference;
    }

    /**
     * Set the unique place token.
     *
     * @param reference The reference
     */
    @JsonProperty("reference")
    public void setReference(String reference)
    {
        myReference = reference;
    }

    /**
     * Get the terms identifying the description.
     *
     * @return The terms
     */
    @JsonProperty("terms")
    public List<Term> getTerms()
    {
        return myTerms;
    }

    /**
     * Set the terms identifying the description.
     *
     * @param terms The terms
     */
    @JsonProperty("terms")
    public void setTerms(List<Term> terms)
    {
        myTerms = terms;
    }

    /**
     * Get the array of types for place.
     *
     * @return The types
     */
    @JsonProperty("types")
    public List<String> getTypes()
    {
        return myTypes;
    }

    /**
     * Set array of types for place.
     *
     * @param types The types
     */
    @JsonProperty("types")
    public void setTypes(List<String> types)
    {
        myTypes = types;
    }

    /**
     * Additional objects in JSON response not assigned are stored in a map.
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
