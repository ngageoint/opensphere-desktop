package io.opensphere.search.googleplaces.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Generated;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/** An array of photo objects, each containing a reference to an image. */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "height", "html_attributions", "photo_reference", "width" })
public class Photo
{
    /** Photo reference height. */
    @JsonProperty("height")
    private int myHeight;

    /** API attributions response. */
    @JsonProperty("html_attributions")
    private List<String> myHtmlAttributions = new ArrayList<String>();

    /** Photo attributions. */
    @JsonProperty("photo_reference")
    private String myPhotoReference;

    /** Photo reference width. */
    @JsonProperty("width")
    private int myWidth;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * The maximum height of the image.
     *
     * @return The height
     */
    @JsonProperty("height")
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Set height.
     *
     * @param height The height
     */
    @JsonProperty("height")
    public void setHeight(int height)
    {
        myHeight = height;
    }

    /**
     * Contains any required attributions. This field will always be present,
     * but may be empty.
     *
     * @return The htmlAttributions
     */
    @JsonProperty("html_attributions")
    public List<String> getHtmlAttributions()
    {
        return myHtmlAttributions;
    }

    /**
     * Set html attributions.
     *
     * @param htmlAttributions The html_attributions
     */
    @JsonProperty("html_attributions")
    public void setHtmlAttributions(List<String> htmlAttributions)
    {
        myHtmlAttributions = htmlAttributions;
    }

    /**
     * A string used to identify the photo when you perform a Photo request.
     *
     * @return The photoReference
     */
    @JsonProperty("photo_reference")
    public String getPhotoReference()
    {
        return myPhotoReference;
    }

    /**
     * Set photo reference.
     *
     * @param photoReference The photo_reference
     */
    @JsonProperty("photo_reference")
    public void setPhotoReference(String photoReference)
    {
        myPhotoReference = photoReference;
    }

    /**
     * The maximum width of the image.
     *
     * @return The width
     */
    @JsonProperty("width")
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Set the width.
     *
     * @param width The width
     */
    @JsonProperty("width")
    public void setWidth(int width)
    {
        myWidth = width;
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
