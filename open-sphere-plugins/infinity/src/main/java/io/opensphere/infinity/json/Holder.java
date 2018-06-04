package io.opensphere.infinity.json;

import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonIgnore;

/** Elasticsearch range holder JSON bean. */
public class Holder
{
    @JsonIgnore
    private String myPropertyName;

    @JsonIgnore
    private Object myPropertyValue;

    public Holder()
    {
    }

    public Holder(String propertyName, Object propertyValue)
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
