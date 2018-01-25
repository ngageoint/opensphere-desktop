package io.opensphere.csvcommon.common;

/**
 * Enum for CSV column separation format.
 */
public enum ColumnFormat
{
    /** The columns are separated by specific characters. */
    DELIMITED("Delimited"),

    /** The columns are fixed width. */
    FIXED_WIDTH("Fixed Width");

    /** The display name. */
    private final String myDisplayName;

    /**
     * Constructor.
     *
     * @param displayName The display name
     */
    ColumnFormat(String displayName)
    {
        myDisplayName = displayName;
    }

    @Override
    public String toString()
    {
        return myDisplayName;
    }
}
