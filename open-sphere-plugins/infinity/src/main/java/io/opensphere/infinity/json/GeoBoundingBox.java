package io.opensphere.infinity.json;

import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonIgnore;

public class GeoBoundingBox
{
    private final boolean myIgnoreUnmapped = true;

    @JsonIgnore
    private String myGeometryName;

    @JsonIgnore
    private Object myGeometry;

    /**
     * Gets the ignoreUnmapped.
     *
     * @return the ignoreUnmapped
     */
    public boolean isIgnore_unmapped()
    {
        return myIgnoreUnmapped;
    }

    /**
     * Sets the geometryName.
     *
     * @param geometryName the geometryName
     */
    public void setGeometryName(String geometryName)
    {
        myGeometryName = geometryName;
    }

    /**
     * Sets the geometry.
     *
     * @param geometry the geometry
     */
    public void setGeometry(Object geometry)
    {
        myGeometry = geometry;
    }

    @JsonAnyGetter
    public Map<String, Object> getAny()
    {
        return Collections.singletonMap(myGeometryName, myGeometry);
    }
}
