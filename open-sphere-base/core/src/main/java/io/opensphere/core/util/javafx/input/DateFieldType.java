package io.opensphere.core.util.javafx.input;

import org.apache.commons.lang3.StringUtils;

/**
 * An enumeration over the set of temporal types assigned to a given date field.
 */
public enum DateFieldType
{
    /**
     * The enum type for a start field.
     */
    START,

    /**
     * The enum type for an end field.
     */
    END,

    /**
     * The enum type for a field that is neither a start nor an end field.
     */
    OTHER;

    /**
     * A factory method used to determine the date field type from the supplied
     * string. The type is inferred from a temporal qualifier, such as "START",
     * "BEGIN", "STOP", "END", etc. If the supplied text does not contain a
     * temporal qualifier, then {@link #OTHER} is returned.
     *
     * @param pType the String to process to determine the date field type.
     * @return a date field type determined based on the supplied String.
     */
    public static DateFieldType fromString(String pType)
    {
        if (StringUtils.equals(StringUtils.upperCase(StringUtils.trim(pType)), "START")
                || StringUtils.equals(StringUtils.upperCase(StringUtils.trim(pType)), "BEGIN"))
        {
            return START;
        }
        else if (StringUtils.equals(StringUtils.upperCase(StringUtils.trim(pType)), "STOP")
                || StringUtils.equals(StringUtils.upperCase(StringUtils.trim(pType)), "END"))
        {
            return END;
        }
        return OTHER;
    }
}
