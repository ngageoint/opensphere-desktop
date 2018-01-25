package io.opensphere.wps.envoy;

import org.apache.commons.lang.StringUtils;

/**
 * An enumeration over the set of known parameters used in WPS calls.
 */
public enum WpsParameter
{
    /** The name of the parameter in which the service is specified. */
    SERVICE("service"),

    /** The name of the parameter in which the REQUEST type is specified. */
    REQUEST("REQUEST"),

    /** The name of the parameter in which the WPS version is specified. */
    VERSION("version"),

    /** The name of the parameter in which the set of data inputs are supplied for the process. */
    DATA_INPUTS("datainputs"),

    /** The name of the parameter in which the process ID is specified. */
    PROCESS_ID("identifier");

    /**
     * The name of the variable, as submitted in the WPS request.
     */
    private String myVariableName;

    /**
     * Creates a new WpsParameter enum instance, configured with the supplied variable name.
     *
     * @param pVariableName The name of the variable, as submitted in the WPS request.
     */
    private WpsParameter(String pVariableName)
    {
        myVariableName = pVariableName;
    }

    /**
     * Gets the value of the {@link #myVariableName} field.
     *
     * @return the value stored in the {@link #myVariableName} field.
     */
    public String getVariableName()
    {
        return myVariableName;
    }

    /**
     * Locates the enum value with a variable name corresponding to the supplied value.
     *
     * @param pVariableName the name of the variable for which to search.
     * @return the {@link WpsParameter} with a variable name corresponding to the supplied value, or null if none could be found.
     */
    public static WpsParameter fromVariableName(String pVariableName)
    {
        WpsParameter returnValue = null;
        for (WpsParameter enumValue : values())
        {
            if (StringUtils.equals(pVariableName, enumValue.getVariableName()))
            {
                returnValue = enumValue;
                break;
            }
        }

        return returnValue;
    }
}
