package io.opensphere.core.util.swing;

/**
 * An enumeration over the types of changes available to the DateTimePickerPanel
 * class.
 */
public enum DateTimeChangeType
{
    /** The DATE_TIME_CHANGE_ACTION. */
    DATE_TIME_CHANGE_ACTION("DATE_TIME_CHANGE_ACTION"),

    /** The CALENDAR_DATE_CHANGE_ACTION. */
    CALENDAR_DATE_CHANGE_ACTION("CALENDAR_DATE_CHANGE_ACTION");

    /** The Type string. */
    private String myTypeString;

    /**
     * Instantiates a new change type.
     *
     * @param typeStr the type string.
     */
    DateTimeChangeType(String typeStr)
    {
        myTypeString = typeStr;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
        return myTypeString;
    }
}
