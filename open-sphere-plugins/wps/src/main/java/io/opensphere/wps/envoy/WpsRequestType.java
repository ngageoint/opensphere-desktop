package io.opensphere.wps.envoy;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration over the supported WPS Request Types.
 */
public enum WpsRequestType
{
    /** OGC WPS GetCapabilities request. */
    GET_CAPABLITIES("GetCapabilities"),

    /** OGC WPS DescribeProcessType request. */
    DESCRIBE_PROCESS_TYPE("DescribeProcess"),

    /** The OGC WPS Execute request type. */
    EXECUTE("Execute");

    /** The string representation of the request type. */
    private String myValue;

    /**
     * Instantiate a new request type.
     *
     * @param pType the string representation of the request type
     */
    WpsRequestType(String pType)
    {
        myValue = pType;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue()
    {
        return myValue;
    }

    /**
     * Gets the request type corresponding to the supplied value. If none is
     * found, a null value is returned.F
     *
     * @param pValue the textual value of the request type to return.
     * @return a {@link WpsRequestType} instance corresponding to the supplied
     *         text.
     */
    public static WpsRequestType fromValue(String pValue)
    {
        for (WpsRequestType requestType : values())
        {
            if (StringUtils.equals(pValue, requestType.getValue()))
            {
                return requestType;
            }
        }
        return null;
    }
}
