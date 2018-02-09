package io.opensphere.mantle.data.impl.specialkey;

import java.util.Arrays;
import java.util.regex.Pattern;

/** An enumeration of speed units. */
public enum SpeedUnit
{
    /** Meters/second. */
    METERS_PER_SECOND
    {
        /** The pattern. */
        private final Pattern myPattern = Pattern.compile(".*m(eters)?\\s*/\\s*s(ec(ond)?)?\\s*\\).*", Pattern.CASE_INSENSITIVE);

        @Override
        protected boolean isUnit(String columnName)
        {
            return myPattern.matcher(columnName).matches();
        }
    },

    /** Kilometers/hour. */
    KILOMETERS_PER_HOUR
    {
        /** The pattern. */
        private final Pattern myPattern = Pattern.compile(".*((km|kilometers)\\s*/\\s*(h|hr|hour)|kph)\\s*\\).*",
                Pattern.CASE_INSENSITIVE);

        @Override
        protected boolean isUnit(String columnName)
        {
            return myPattern.matcher(columnName).matches();
        }
    },

    /** Miles/hour. */
    MILES_PER_HOUR
    {
        /** The pattern. */
        private final Pattern myPattern = Pattern.compile(".*(mi(les)?\\s*/\\s*(h|hr|hour)|mph)\\s*\\).*",
                Pattern.CASE_INSENSITIVE);

        @Override
        protected boolean isUnit(String columnName)
        {
            return myPattern.matcher(columnName).matches();
        }
    },

    /** Knots. */
    KNOTS
    {
        /** The pattern. */
        private final Pattern myPattern = Pattern.compile(".*kn(ots)?\\s*\\).*", Pattern.CASE_INSENSITIVE);

        @Override
        protected boolean isUnit(String columnName)
        {
            return myPattern.matcher(columnName).matches();
        }
    };

    /**
     * Attempts to detect the speed unit from the column name.
     *
     * @param columnName the name of the column to inspect
     * @return the detected speed unit, or null if it couldn't be detected
     */
    public static SpeedUnit detectUnit(String columnName)
    {
        return Arrays.stream(SpeedUnit.values()).filter(u -> u.isUnit(columnName)).findFirst().orElse(null);
    }

    /**
     * Attempts to detect if the column name is of the current unit.
     *
     * @param columnName the name of the column to inspect
     * @return whether it's of the current unit
     */
    protected abstract boolean isUnit(String columnName);
}
