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

/** Class that binds JSON response to java objects. */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "html_attributions", "next_page_token", "results", "status" })
public class GeoJsonObject
{
    /** API response for html_attributions. */
    @JsonProperty("html_attributions")
    private List<Object> myHtmlAttributions = new ArrayList<Object>();

    /** API response for next_page_token of results. */
    @JsonProperty("next_page_token")
    private String myNextPageToken;

    /** API response for all locations searched. */
    @JsonProperty("results")
    private List<Result> myResults = new ArrayList<Result>();

    /** API response for status of search. */
    @JsonProperty("status")
    private String myStatus;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /** The current string being searched for. */
    private String searchTerm;

    /**
     * html_attributions field from JSON response.
     *
     * @return The htmlAttributions
     */
    @JsonProperty("html_attributions")
    public List<Object> getHtmlAttributions()
    {
        return myHtmlAttributions;
    }

    /**
     * Sets html_attributions field.
     *
     * @param htmlAttributions The html_attributions
     */
    @JsonProperty("html_attributions")
    public void setHtmlAttributions(List<Object> htmlAttributions)
    {
        myHtmlAttributions = htmlAttributions;
    }

    /**
     * next_page_token from JSON response.
     *
     * @return The nextPageToken
     */
    @JsonProperty("next_page_token")
    public String getNextPageToken()
    {
        return myNextPageToken;
    }

    /**
     * Sets next_page_token field.
     *
     * @param nextPageToken The next_page_token
     */
    @JsonProperty("next_page_token")
    public void setNextPageToken(String nextPageToken)
    {
        myNextPageToken = nextPageToken;
    }

    /**
     * Google Places service returns JSON results from a search, it places them
     * within a results array.
     *
     * @return The results
     */
    @JsonProperty("results")
    public List<Result> getResults()
    {
        return myResults;
    }

    /**
     * sets results field.
     *
     * @param results The results
     */
    @JsonProperty("results")
    public void setResults(List<Result> results)
    {
        myResults = results;
    }

    /**
     * Status of the request, and may contain debugging information to help you
     * track down why the request failed.
     *
     * @return The status
     */
    @JsonProperty("status")
    public String getStatus()
    {
        return myStatus;
    }

    /**
     * sets status field.
     *
     * @param status The status
     */
    @JsonProperty("status")
    public void setStatus(String status)
    {
        myStatus = status;
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

    /**
     * Set the search term.
     *
     * @param term The search term
     */
    public void setSearchTerm(String term)
    {
        searchTerm = term;
    }

    /**
     * searchTerm field from JSON response.
     *
     * @return searchTerm
     */
    public String getSearchTerm()
    {
        return searchTerm;
    }
}
