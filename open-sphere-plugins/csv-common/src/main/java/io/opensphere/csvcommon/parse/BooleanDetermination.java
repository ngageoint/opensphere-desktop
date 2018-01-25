package io.opensphere.csvcommon.parse;

/** The Enum BooleanDetermination. */
public enum BooleanDetermination
{
    /** BOOLEAN_TRUE. */
    BOOLEAN_TRUE,

    /** BOOLEAN_FALSE. */
    BOOLEAN_FALSE,

    /** UNDETERMINED. */
    UNDETERMINED;

    /**
     * Checks if is boolean.
     *
     * @return true, if is boolean
     */
    public boolean isBoolean()
    {
        return this == BOOLEAN_FALSE || this == BOOLEAN_TRUE;
    }
}
