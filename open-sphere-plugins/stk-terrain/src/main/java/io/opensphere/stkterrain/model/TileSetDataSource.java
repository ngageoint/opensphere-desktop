package io.opensphere.stkterrain.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonSetter;

/**
 * Contains information about a data source that helped make a terrain tile set.
 */
public class TileSetDataSource implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The attribution string that credits the source of the raw input data.
     */
    private String myAttribution;

    /**
     * A text description of the data source.
     */
    private String myDescription;

    /**
     * A name describing the data source.
     */
    private String myName;

    /**
     * Revision number that is incremented each time this data source is
     * modified.
     */
    private String myRevisionNumber;

    /**
     * Gets the attribution string that credits the source of the raw input
     * data.
     *
     * @return The attribution string that credits the source of the raw input
     *         data.
     */
    public String getAttribution()
    {
        return myAttribution;
    }

    /**
     * Gets a text description of the data source.
     *
     * @return A text description of the data source.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets a name describing the data source.
     *
     * @return A name describing the data source.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the revision number that is incremented each time this data source
     * is modified.
     *
     * @return Revision number that is incremented each time this data source is
     *         modified.
     */
    public String getRevisionNumber()
    {
        return myRevisionNumber;
    }

    /**
     * Sets the attribution string that credits the source of the raw input
     * data.
     *
     * @param attribution The attribution string that credits the source of the
     *            raw input data.
     */
    public void setAttribution(String attribution)
    {
        myAttribution = attribution;
    }

    /**
     * Sets a text description of the data source.
     *
     * @param description A text description of the data source.
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets a name describing the data source.
     *
     * @param name A name describing the data source.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Sets the revision number that is incremented each time this data source
     * is modified.
     *
     * @param revisionNumber Revision number that is incremented each time this
     *            data source is modified.
     */
    @JsonSetter("_rev")
    public void setRevisionNumber(String revisionNumber)
    {
        myRevisionNumber = revisionNumber;
    }
}
