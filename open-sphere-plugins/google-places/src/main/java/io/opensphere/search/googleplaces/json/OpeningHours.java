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

/** Class indicating if the place is open at the current time. */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "open_now", "weekday_text" })
public class OpeningHours
{
    /** API response for locations operating hours. */
    @JsonProperty("open_now")
    private Boolean myOpenNow;

    /** API response for locations operating days. */
    @JsonProperty("weekday_text")
    private List<Object> myWeekdayText = new ArrayList<Object>();

    /** Attribute for additional fields not defined. */
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Boolean value indicating if location is open at given time.
     *
     * @return The openNow
     */
    @JsonProperty("open_now")
    public Boolean getOpenNow()
    {
        return myOpenNow;
    }

    /**
     * Sets boolean value.
     *
     * @param openNow The open_now
     */
    @JsonProperty("open_now")
    public void setOpenNow(Boolean openNow)
    {
        myOpenNow = openNow;
    }

    /**
     * Days of the week business is open.
     *
     * @return The weekdayText
     */
    @JsonProperty("weekday_text")
    public List<Object> getWeekdayText()
    {
        return myWeekdayText;
    }

    /**
     * Sets weekdayText.
     *
     * @param weekdayText The weekday_text
     */
    @JsonProperty("weekday_text")
    public void setWeekdayText(List<Object> weekdayText)
    {
        myWeekdayText = weekdayText;
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
