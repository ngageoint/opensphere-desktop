package io.opensphere.wfs.envoy;

/**
 * Enumeration for the supported WFS Request Types.
 */
public enum WFSRequestType
{
    /** OGC WFS GetCapabilities request. */
    GET_CAPABLITIES("GetCapabilities"),

    /** OGC WFS GetCapabilities request. */
    DESCRIBE_FEATURE_TYPE("DescribeFeatureType"),

    /** OGC WFS GetCapabilities request. */
    GET_FEATURE("GetFeature");

    /** The string representation of the request type. */
    private String myValue;

    /**
     * Instantiate a new request type.
     *
     * @param type the string representation of the request type
     */
    private WFSRequestType(String type)
    {
        myValue = type;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    protected String getValue()
    {
        return myValue;
    }
}
