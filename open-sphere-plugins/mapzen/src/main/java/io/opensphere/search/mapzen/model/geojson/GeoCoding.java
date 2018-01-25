package io.opensphere.search.mapzen.model.geojson;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/** A JSON object used in MapZen search results. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoCoding
{
    /** The version of the search provider. */
    @JsonProperty("version")
    private String myVersion;

    /** The provider of the search to which the results are attributed. */
    @JsonProperty("attribution")
    private String myAttribution;

    /** The query submitted to the remote endpoint. */
    @JsonProperty("query")
    private Query myQuery;

    /** The engine used to encode the results. */
    @JsonProperty("engine")
    private Engine myEngine;

    /** The time at which the search was executed. */
    @JsonProperty("timestamp")
    private long myTimestamp;

    /**
     * Gets the value of the version field.
     *
     * @return the version
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Assigns the value of the version field to the supplied value.
     *
     * @param pVersion the version to set
     */
    public void setVersion(String pVersion)
    {
        myVersion = pVersion;
    }

    /**
     * Gets the value of the attribution field.
     *
     * @return the attribution
     */
    public String getAttribution()
    {
        return myAttribution;
    }

    /**
     * Assigns the value of the attribution field to the supplied value.
     *
     * @param pAttribution the attribution to set
     */
    public void setAttribution(String pAttribution)
    {
        myAttribution = pAttribution;
    }

    /**
     * Gets the value of the query field.
     *
     * @return the query
     */
    public Query getQuery()
    {
        return myQuery;
    }

    /**
     * Assigns the value of the query field to the supplied value.
     *
     * @param pQuery the query to set
     */
    public void setQuery(Query pQuery)
    {
        myQuery = pQuery;
    }

    /**
     * Gets the value of the engine field.
     *
     * @return the engine
     */
    public Engine getEngine()
    {
        return myEngine;
    }

    /**
     * Assigns the value of the engine field to the supplied value.
     *
     * @param pEngine the engine to set
     */
    public void setEngine(Engine pEngine)
    {
        myEngine = pEngine;
    }

    /**
     * Gets the value of the timestamp field.
     *
     * @return the timestamp
     */
    public long getTimestamp()
    {
        return myTimestamp;
    }

    /**
     * Assigns the value of the timestamp field to the supplied value.
     *
     * @param pTimestamp the timestamp to set
     */
    public void setTimestamp(long pTimestamp)
    {
        myTimestamp = pTimestamp;
    }
}
