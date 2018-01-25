package io.opensphere.search.mapzen.model.geojson;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import io.opensphere.core.util.collections.New;

/** A JSON encoding of a query. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Query
{
    /** The text of the query. */
    @JsonProperty("text")
    private String myText;

    /** the number of results in the query response. */
    @JsonProperty("size")
    private int mySize;

    /** A flag used to mark the results as private. */
    @JsonProperty("private")
    private boolean myPrivate;

    /** The size of the query. */
    @JsonProperty("querySize")
    private int myQuerySize;

    /** A map of other fields, to contain unknown or new values. */
    private final Map<String, Object> myOtherFields = New.map();

    /**
     * A setter used to populate unrecognized fields.
     *
     * @param name the name of the field.
     * @param value the value of the field.
     */
    @JsonAnySetter
    public void handleOtherField(String name, Object value)
    {
        myOtherFields.put(name, value);
    }

    /**
     * Gets the value of the otherFields field.
     *
     * @return the otherFields
     */
    public Map<String, Object> getOtherFields()
    {
        return myOtherFields;
    }

    /**
     * Gets the value of the text field.
     *
     * @return the text
     */
    public String getText()
    {
        return myText;
    }

    /**
     * Assigns the value of the text field to the supplied value.
     *
     * @param pText the text to set
     */
    public void setText(String pText)
    {
        myText = pText;
    }

    /**
     * Gets the value of the size field.
     *
     * @return the size
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * Assigns the value of the size field to the supplied value.
     *
     * @param pSize the size to set
     */
    public void setSize(int pSize)
    {
        mySize = pSize;
    }

    /**
     * Gets the value of the private field.
     *
     * @return the private
     */
    public boolean isPrivate()
    {
        return myPrivate;
    }

    /**
     * Assigns the value of the private field to the supplied value.
     *
     * @param pPrivate the private to set
     */
    public void setPrivate(boolean pPrivate)
    {
        myPrivate = pPrivate;
    }

    /**
     * Gets the value of the querySize field.
     *
     * @return the querySize
     */
    public int getQuerySize()
    {
        return myQuerySize;
    }

    /**
     * Assigns the value of the querySize field to the supplied value.
     *
     * @param pQuerySize the querySize to set
     */
    public void setQuerySize(int pQuerySize)
    {
        myQuerySize = pQuerySize;
    }
}
