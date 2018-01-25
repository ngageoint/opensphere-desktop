package io.opensphere.core.util;

import java.nio.Buffer;

/**
 * Collection of constants.
 */
public final class Constants
{
    /** Size of an empty array in bytes. */
    public static final int ARRAY_SIZE_BYTES = is32Bit() ? 12 : 16;

    /** Bits per byte. */
    public static final int BITS_PER_BYTE = 8;

    /** Size of a double in bytes. */
    public static final int BOOLEAN_SIZE_BYTES = 1;

    /** Size of a {@link Buffer}, not counting the array and its contents. */
    public static final int BUFFER_SIZE_BYTES;

    /** Bytes in a kilobyte. */
    public static final int BYTES_PER_KILOBYTE = 1024;

    /** Bytes in a megabyte. */
    public static final int BYTES_PER_MEGABYTE = 1048576;

    /** The size of a char in bytes. */
    public static final int CHAR_SIZE_BYTES = 2;

    /** Degrees in a circle. */
    public static final int CIRCLE_DEGREES = 360;

    /** Number of days in a week. */
    public static final int DAYS_PER_WEEK = 7;

    /** Size of a double in bytes. */
    public static final int DOUBLE_SIZE_BYTES;

    /** Feet per meter. */
    public static final double FEET_PER_METER;

    /** Feet per mile. */
    public static final int FEET_PER_MILE = 5280;

    /** Size of a float in bytes. */
    public static final int FLOAT_SIZE_BYTES;

    /** Degrees in a half-circle. */
    public static final int HALF_CIRCLE_DEGREES = 180;

    /** Size of an int in bytes. */
    public static final int INT_SIZE_BYTES;

    /** Size of an int in bytes. */
    public static final int LONG_SIZE_BYTES;

    /** Maximum days per month. */
    public static final int MAX_DAYS_PER_MONTH = 31;

    /** Maximum days in a year. */
    public static final int MAX_DAYS_PER_YEAR = 366;

    /** The smallest block of memory allocated by Java is 8 bytes. */
    public static final int MEMORY_BLOCK_SIZE_BYTES = 8;

    /** Meters per feet. */
    public static final double METERS_PER_FEET = 0.3048;

    /** Meters per nautical mile. */
    public static final int METERS_PER_NAUTICAL_MILE = 1852;

    /** Number of micro-units in a unit. */
    public static final int MICRO_PER_UNIT = 1000000;

    /** Number of milli-units in a unit. */
    public static final int MILLI_PER_UNIT = 1000;

    /** Number of milli-units in a unit as a double. */
    public static final double MILLI_PER_UNIT_DOUBLE = 1000.0;

    /** The number of milliseconds in a day. */
    public static final int MILLIS_PER_DAY;

    /** The number of milliseconds in an hour. */
    public static final int MILLIS_PER_HOUR;

    /** The number of milliseconds in a minute. */
    public static final int MILLIS_PER_MINUTE;

    /** Number of milliseconds in a week. */
    public static final long MILLIS_PER_WEEK;

    /** Minimum days per month. */
    public static final int MIN_DAYS_PER_MONTH = 28;

    /** Minimum days in a year. */
    public static final int MIN_DAYS_PER_YEAR = 365;

    /** Minimum milliseconds in a month. */
    public static final int MIN_MILLIS_PER_MONTH;

    /** Number of minutes in a degree. */
    public static final int MINUTES_PER_DEGREE = 60;

    /** Number of minutes in an hour. */
    public static final int MINUTES_PER_HOUR = 60;

    /** Months in a year. */
    public static final long MONTHS_PER_YEAR = 12;

    /** Number of nano-units in a milli-unit. */
    public static final int NANO_PER_MILLI;

    /** Number of nano-units in a unit. */
    public static final int NANO_PER_UNIT = 1000000000;

    /** How much memory an Object with no fields takes. */
    public static final int OBJECT_SIZE_BYTES = is32Bit() ? 8 : 12;

    /** A quarter circle in degrees. */
    public static final int QUARTER_CIRCLE_DEGREES = 90;

    /** Size of a reference in bytes. */
    public static final int REFERENCE_SIZE_BYTES = 4;

    /** Seconds in a (non-leap) day. */
    public static final int SECONDS_PER_DAY;

    /** Number of seconds in a degree. */
    public static final int SECONDS_PER_DEGREE = 3600;

    /** Seconds in an hour. */
    public static final int SECONDS_PER_HOUR;

    /** Number of seconds in a minute. */
    public static final int SECONDS_PER_MINUTE = 60;

    /** Number of seconds in a week. */
    public static final int SECONDS_PER_WEEK;

    /** The size of a short in bytes. */
    public static final int SHORT_SIZE_BYTES = 2;

    /** The number of units in a hecto-unit. */
    public static final int UNIT_PER_HECTO = 100;

    /** The number of units in a kilo-unit. */
    public static final int UNIT_PER_KILO = 1000;

    /** Hours in a (non-leap) day. */
    private static final int HOURS_PER_DAY = 24;

    static
    {
        DOUBLE_SIZE_BYTES = Double.SIZE / BITS_PER_BYTE;
        FLOAT_SIZE_BYTES = Float.SIZE / BITS_PER_BYTE;
        INT_SIZE_BYTES = Integer.SIZE / BITS_PER_BYTE;
        LONG_SIZE_BYTES = Long.SIZE / BITS_PER_BYTE;
        NANO_PER_MILLI = NANO_PER_UNIT / MILLI_PER_UNIT;
        SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
        SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
        SECONDS_PER_WEEK = SECONDS_PER_DAY * DAYS_PER_WEEK;
        MILLIS_PER_MINUTE = SECONDS_PER_MINUTE * MILLI_PER_UNIT;
        MILLIS_PER_HOUR = SECONDS_PER_HOUR * MILLI_PER_UNIT;
        MILLIS_PER_DAY = SECONDS_PER_DAY * MILLI_PER_UNIT;
        MILLIS_PER_WEEK = SECONDS_PER_WEEK * MILLI_PER_UNIT;
        MIN_MILLIS_PER_MONTH = MIN_DAYS_PER_MONTH * MILLIS_PER_DAY;
        FEET_PER_METER = 1. / METERS_PER_FEET;
        BUFFER_SIZE_BYTES = MathUtil.roundUpTo(OBJECT_SIZE_BYTES + REFERENCE_SIZE_BYTES + INT_SIZE_BYTES,
                MEMORY_BLOCK_SIZE_BYTES);
    }

    /**
     * Get if this is a 32-bit VM.
     *
     * @return {@code true} if this is a 32-bit VM.
     */
    private static boolean is32Bit()
    {
        return "x86".equals(System.getProperty("os.arch"));
    }

    /** Disallow instantiation. */
    private Constants()
    {
    }
}
