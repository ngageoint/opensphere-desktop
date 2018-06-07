package io.opensphere.infinity.json;

import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonIgnore;

/** JSON bean for an arbitrary property/value. */
public class Any
{
    /** The property name. */
    @JsonIgnore
    private String myPropertyName;

    /** The property value. */
    @JsonIgnore
    private Object myPropertyValue;

    /** Constructor. */
    public Any()
    {
    }

    /**
     * Constructor.
     *
     * @param propertyName The property name
     * @param propertyValue The property value
     */
    public Any(String propertyName, Object propertyValue)
    {
        myPropertyName = propertyName;
        myPropertyValue = propertyValue;
    }

    /**
     * Gets the any map.
     *
     * @return the any map
     */
    @JsonAnyGetter
    public Map<String, Object> getAny()
    {
        return Collections.singletonMap(myPropertyName, myPropertyValue);
    }
}
