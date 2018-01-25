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
 * Results Array From JSON response.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "formatted_address", "geometry", "icon", "id", "name", "opening_hours", "photos", "place_id", "rating",
    "reference", "types", "price_level" })
public class Result
{
    /** API response for postal address. */
    @JsonProperty("formatted_address")
    private String myFormattedAddress;

    /** Geometry array. */
    @JsonProperty("geometry")
    private Geometry myGeometry;

    /** URL of a recommended icon. */
    @JsonProperty("icon")
    private String myIcon;

    /** Unique stable identifier denoting this place. */
    @JsonProperty("id")
    private String myId;

    /** Location Name. */
    @JsonProperty("name")
    private String myName;

    /** Array of a boolean values. */
    @JsonProperty("opening_hours")
    private OpeningHours myOpeningHours;

    /** An List of photo objects. */
    @JsonProperty("photos")
    private List<Photo> myPhotos = new ArrayList<Photo>();

    /** Unique identifier for a place. */
    @JsonProperty("place_id")
    private String myPlaceId;

    /** Locations rating from 1 to 5. */
    @JsonProperty("rating")
    private float myRating;

    /** Unique token that you can use to retrieve additional information. */
    @JsonProperty("reference")
    private String myReference;

    /** List of feature types describing the given result. */
    @JsonProperty("types")
    private List<String> myTypes = new ArrayList<String>();

    /** Results price level from 0 to 4. */
    @JsonProperty("price_level")
    private int myPriceLevel;

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Is a string containing the human-readable address of this place. Often
     * this address is equivalent to the "postal address".
     *
     * @return The formattedAddress
     */
    @JsonProperty("formatted_address")
    public String getFormattedAddress()
    {
        return myFormattedAddress;
    }

    /**
     * Set formatted_address field value.
     *
     * @param formattedAddress The formatted_address
     */
    @JsonProperty("formatted_address")
    public void setFormattedAddress(String formattedAddress)
    {
        myFormattedAddress = formattedAddress;
    }

    /**
     * Geometry information about the result, including the location (geocode)
     * of the place and the viewport identifying its general area of coverage.
     *
     * @return The geometry
     */
    @JsonProperty("geometry")
    public Geometry getGeometry()
    {
        return myGeometry;
    }

    /**
     * Sets geometry field.
     *
     * @param geometry The geometry
     */
    @JsonProperty("geometry")
    public void setGeometry(Geometry geometry)
    {
        myGeometry = geometry;
    }

    /**
     * Contains the URL of a recommended icon which may be displayed to the user
     * when indicating this result.
     *
     * @return The icon
     */
    @JsonProperty("icon")
    public String getIcon()
    {
        return myIcon;
    }

    /**
     * Sets icon field.
     *
     * @param icon The icon
     */
    @JsonProperty("icon")
    public void setIcon(String icon)
    {
        myIcon = icon;
    }

    /**
     * Contains a unique stable identifier denoting this place. The id is now
     * deprecated in favor of place_id.
     *
     * @return The id
     */
    @JsonProperty("id")
    public String getId()
    {
        return myId;
    }

    /**
     * Sets id field.
     *
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Contains the human-readable name for the returned result.
     *
     * @return The name
     */
    @JsonProperty("name")
    public String getName()
    {
        return myName;
    }

    /**
     * Sets name field.
     *
     * @param name The name
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Is Array of a boolean values indicating if the place is open at the
     * current time.
     *
     * @return The openingHours
     */
    @JsonProperty("opening_hours")
    public OpeningHours getOpeningHours()
    {
        return myOpeningHours;
    }

    /**
     * Sets opening hours.
     *
     * @param openingHours The opening_hours
     */
    @JsonProperty("opening_hours")
    public void setOpeningHours(OpeningHours openingHours)
    {
        myOpeningHours = openingHours;
    }

    /**
     * An array of photo objects, each containing a reference to an image. A
     * Place Search will return at most one photo object.
     *
     * @return The photos
     */
    @JsonProperty("photos")
    public List<Photo> getPhotos()
    {
        return myPhotos;
    }

    /**
     * An array of photo objects, each containing a reference to an image. A
     * Place Search will return at most one photo object.
     *
     * @param photos The photos
     */
    @JsonProperty("photos")
    public void setPhotos(List<Photo> photos)
    {
        myPhotos = photos;
    }

    /**
     * A textual identifier that uniquely identifies a place.
     *
     * @return The placeId
     */
    @JsonProperty("place_id")
    public String getPlaceId()
    {
        return myPlaceId;
    }

    /**
     * Set placeId.
     *
     * @param placeId The place_id
     */
    @JsonProperty("place_id")
    public void setPlaceId(String placeId)
    {
        myPlaceId = placeId;
    }

    /**
     * Contains the place's rating, from 1.0 to 5.0, based on aggregated user
     * reviews.
     *
     * @return The rating
     */
    @JsonProperty("rating")
    public float getRating()
    {
        return myRating;
    }

    /**
     * Set rating.
     *
     * @param rating The rating
     */
    @JsonProperty("rating")
    public void setRating(float rating)
    {
        myRating = rating;
    }

    /**
     * Contains a unique token that you can use to retrieve additional
     * information about this place in a Place Details request.
     *
     * @return The reference
     */
    @JsonProperty("reference")
    public String getReference()
    {
        return myReference;
    }

    /**
     * Set reference.
     *
     * @param reference The reference
     */
    @JsonProperty("reference")
    public void setReference(String reference)
    {
        myReference = reference;
    }

    /**
     * Contains an array of feature types describing the given result.
     *
     * @return The types
     */
    @JsonProperty("types")
    public List<String> getTypes()
    {
        return myTypes;
    }

    /**
     * Set types.
     *
     * @param types The types
     */
    @JsonProperty("types")
    public void setTypes(List<String> types)
    {
        myTypes = types;
    }

    /**
     * The price level of the place, on a scale of 0 to 4.
     *
     * @return The priceLevel
     */
    @JsonProperty("price_level")
    public int getPriceLevel()
    {
        return myPriceLevel;
    }

    /**
     * Set priceLevel.
     *
     * @param priceLevel The price_level
     */
    @JsonProperty("price_level")
    public void setPriceLevel(int priceLevel)
    {
        myPriceLevel = priceLevel;
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
