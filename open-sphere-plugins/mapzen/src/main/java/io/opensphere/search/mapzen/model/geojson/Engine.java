package io.opensphere.search.mapzen.model.geojson;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/** A JSON object used in MapZen search results. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Engine
{
    /** The name of the search engine. */
    @JsonProperty("name")
    private String myName;

    /** The author of the search engine. */
    @JsonProperty("author")
    private String myAuthor;

    /** The version of the search engine. */
    @JsonProperty("version")
    private String myVersion;

    /**
     * Gets the value of the name field.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Assigns the value of the name field to the supplied value.
     *
     * @param pName the name to set
     */
    public void setName(String pName)
    {
        myName = pName;
    }

    /**
     * Gets the value of the author field.
     *
     * @return the author
     */
    public String getAuthor()
    {
        return myAuthor;
    }

    /**
     * Assigns the value of the author field to the supplied value.
     *
     * @param pAuthor the author to set
     */
    public void setAuthor(String pAuthor)
    {
        myAuthor = pAuthor;
    }

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

}
