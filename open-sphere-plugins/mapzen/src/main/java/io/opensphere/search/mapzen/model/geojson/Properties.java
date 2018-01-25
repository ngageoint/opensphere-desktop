package io.opensphere.search.mapzen.model.geojson;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import io.opensphere.core.util.collections.New;

/**
 * A JSON-bound POJO in which the GeoJSON "Properties" section of a feature is
 * encapsulated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Properties
{
    /** The ID field of the feature. */
    @JsonProperty("id")
    private String myId;

    /** The GID field of the feature. */
    @JsonProperty("gid")
    private String myGid;

    /** The name of the MapZen layer in which the feature is defined. */
    @JsonProperty("layer")
    private String myLayer;

    /** The source of the result. */
    @JsonProperty("source")
    private String mySource;

    /** The name of the result. */
    @JsonProperty("name")
    private String myName;

    /** The abbreviation of the country in which the result resides. */
    @JsonProperty("country_a")
    private String myCountryAbbreviation;

    /** The full name of the country in which the result resides. */
    @JsonProperty("country")
    private String myCountry;

    /** The name of the spatial region in which the result resides. */
    @JsonProperty("region")
    private String myRegion;

    /** The abbreviation of the spatial region in which the result resides. */
    @JsonProperty("region_a")
    private String myRegionAbbreviation;

    /** The name of the county in which the result was found. */
    @JsonProperty("county")
    private String myCounty;

    /** The local administrative area in which the result was found. */
    @JsonProperty("localadmin")
    private String myLocalAdmin;

    /** The locality in which the result was found. */
    @JsonProperty("locality")
    private String myLocality;

    /** The confidence score of the result, as a decimal percentage. */
    @JsonProperty("confidence")
    private double myConfidence;

    /** A human readable label applied to the result. */
    @JsonProperty("label")
    private String myLabel;

    /** The name of the neighborhood in which the result is found. */
    @JsonProperty("neighbourhood")
    private String myNeighborhood;

    /** The numeric portion of the address of the result. */
    @JsonProperty("housenumber")
    private String myHouseNumber;

    /** The street portion of the address of the result. */
    @JsonProperty("street")
    private String myStreet;

    /** The postal / ZIP code of the address of the result. */
    @JsonProperty("postalcode")
    private String myPostalCode;

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
     * Gets the value of the postalCode field.
     *
     * @return the postalCode
     */
    public String getPostalCode()
    {
        return myPostalCode;
    }

    /**
     * Assigns the value of the postalCode field to the supplied value.
     *
     * @param pPostalCode the postalCode to set
     */
    public void setPostalCode(String pPostalCode)
    {
        myPostalCode = pPostalCode;
    }

    /**
     * Gets the value of the id field.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Assigns the value of the id field to the supplied value.
     *
     * @param pId the id to set
     */
    public void setId(String pId)
    {
        myId = pId;
    }

    /**
     * Gets the value of the GID field.
     *
     * @return the GID
     */
    public String getGid()
    {
        return myGid;
    }

    /**
     * Assigns the value of the GID field to the supplied value.
     *
     * @param pGid the GID to set
     */
    public void setGid(String pGid)
    {
        myGid = pGid;
    }

    /**
     * Gets the value of the layer field.
     *
     * @return the layer
     */
    public String getLayer()
    {
        return myLayer;
    }

    /**
     * Assigns the value of the layer field to the supplied value.
     *
     * @param pLayer the layer to set
     */
    public void setLayer(String pLayer)
    {
        myLayer = pLayer;
    }

    /**
     * Gets the value of the source field.
     *
     * @return the source
     */
    public String getSource()
    {
        return mySource;
    }

    /**
     * Assigns the value of the source field to the supplied value.
     *
     * @param pSource the source to set
     */
    public void setSource(String pSource)
    {
        mySource = pSource;
    }

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
     * Gets the value of the countryAbbreviation field.
     *
     * @return the countryAbbreviation
     */
    public String getCountryAbbreviation()
    {
        return myCountryAbbreviation;
    }

    /**
     * Assigns the value of the countryAbbreviation field to the supplied value.
     *
     * @param pCountryAbbreviation the countryAbbreviation to set
     */
    public void setCountryAbbreviation(String pCountryAbbreviation)
    {
        myCountryAbbreviation = pCountryAbbreviation;
    }

    /**
     * Gets the value of the country field.
     *
     * @return the country
     */
    public String getCountry()
    {
        return myCountry;
    }

    /**
     * Assigns the value of the country field to the supplied value.
     *
     * @param pCountry the country to set
     */
    public void setCountry(String pCountry)
    {
        myCountry = pCountry;
    }

    /**
     * Gets the value of the region field.
     *
     * @return the region
     */
    public String getRegion()
    {
        return myRegion;
    }

    /**
     * Assigns the value of the region field to the supplied value.
     *
     * @param pRegion the region to set
     */
    public void setRegion(String pRegion)
    {
        myRegion = pRegion;
    }

    /**
     * Gets the value of the regionAbbreviation field.
     *
     * @return the regionAbbreviation
     */
    public String getRegionAbbreviation()
    {
        return myRegionAbbreviation;
    }

    /**
     * Assigns the value of the regionAbbreviation field to the supplied value.
     *
     * @param pRegionAbbreviation the regionAbbreviation to set
     */
    public void setRegionAbbreviation(String pRegionAbbreviation)
    {
        myRegionAbbreviation = pRegionAbbreviation;
    }

    /**
     * Gets the value of the county field.
     *
     * @return the county
     */
    public String getCounty()
    {
        return myCounty;
    }

    /**
     * Assigns the value of the county field to the supplied value.
     *
     * @param pCounty the county to set
     */
    public void setCounty(String pCounty)
    {
        myCounty = pCounty;
    }

    /**
     * Gets the value of the localAdmin field.
     *
     * @return the localAdmin
     */
    public String getLocalAdmin()
    {
        return myLocalAdmin;
    }

    /**
     * Assigns the value of the localAdmin field to the supplied value.
     *
     * @param pLocalAdmin the localAdmin to set
     */
    public void setLocalAdmin(String pLocalAdmin)
    {
        myLocalAdmin = pLocalAdmin;
    }

    /**
     * Gets the value of the locality field.
     *
     * @return the locality
     */
    public String getLocality()
    {
        return myLocality;
    }

    /**
     * Assigns the value of the locality field to the supplied value.
     *
     * @param pLocality the locality to set
     */
    public void setLocality(String pLocality)
    {
        myLocality = pLocality;
    }

    /**
     * Gets the value of the confidence field.
     *
     * @return the confidence
     */
    public double getConfidence()
    {
        return myConfidence;
    }

    /**
     * Assigns the value of the confidence field to the supplied value.
     *
     * @param pConfidence the confidence to set
     */
    public void setConfidence(double pConfidence)
    {
        myConfidence = pConfidence;
    }

    /**
     * Gets the value of the label field.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Assigns the value of the label field to the supplied value.
     *
     * @param pLabel the label to set
     */
    public void setLabel(String pLabel)
    {
        myLabel = pLabel;
    }

    /**
     * Gets the value of the neighborhood field.
     *
     * @return the neighborhood
     */
    public String getNeighborhood()
    {
        return myNeighborhood;
    }

    /**
     * Assigns the value of the neighborhood field to the supplied value.
     *
     * @param pNeighborhood the neighborhood to set
     */
    public void setNeighborhood(String pNeighborhood)
    {
        myNeighborhood = pNeighborhood;
    }

    /**
     * Gets the value of the houseNumber field.
     *
     * @return the houseNumber
     */
    public String getHouseNumber()
    {
        return myHouseNumber;
    }

    /**
     * Assigns the value of the houseNumber field to the supplied value.
     *
     * @param pHouseNumber the houseNumber to set
     */
    public void setHouseNumber(String pHouseNumber)
    {
        myHouseNumber = pHouseNumber;
    }

    /**
     * Gets the value of the street field.
     *
     * @return the street
     */
    public String getStreet()
    {
        return myStreet;
    }

    /**
     * Assigns the value of the street field to the supplied value.
     *
     * @param pStreet the street to set
     */
    public void setStreet(String pStreet)
    {
        myStreet = pStreet;
    }
}
