package io.opensphere.arcgis2.model;

import org.codehaus.jackson.annotate.JsonProperty;

/** A service. */
public class Service
{
    /** The name. */
    @JsonProperty("name")
    private String myName;

    /** The type. */
    @JsonProperty("type")
    private String myType;

    /**
     * Get the name.
     *
     * @return The name.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Get the type.
     *
     * @return The type.
     */
    public String getType()
    {
        return myType;
    }

    /**
     * Set the name.
     *
     * @param name The name.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Set the type.
     *
     * @param type The type.
     */
    public void setType(String type)
    {
        myType = type;
    }
}
