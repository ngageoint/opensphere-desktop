package io.opensphere.infinity.json;

import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonIgnore;

/** JSON bean for an arbitrary property/value. */
public class Any
{
    @JsonIgnore
    private String myPropertyName;

    @JsonIgnore
    private Object myPropertyValue;

    public Any()
    {
    }

    public Any(String propertyName, Object propertyValue)
    {
        myPropertyName = propertyName;
        myPropertyValue = propertyValue;
    }

    @JsonAnyGetter
    public Map<String, Object> getAny()
    {
        return Collections.singletonMap(myPropertyName, myPropertyValue);
    }
}
