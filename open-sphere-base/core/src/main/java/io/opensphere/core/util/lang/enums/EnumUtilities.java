package io.opensphere.core.util.lang.enums;

import java.util.EnumSet;
import java.util.Objects;

/** Utilities for working with enums. */
public final class EnumUtilities
{
    /**
     * Retrieve the value for the enum whose toString() matches the given label.
     * This should only be used for enums which override toString(), otherwise
     * valueOf() should be used instead.
     *
     * @param <T> The type of the enum.
     * @param type The type of the enum.
     * @param label The label whose equivalent value is desired.
     * @return The matching value if found or {@code null} if no match is
     *         available.
     */
    public static <T extends Enum<T>> T fromString(Class<T> type, String label)
    {
        return EnumSet.allOf(type).stream().filter(v -> v != null && Objects.equals(v.toString(), label)).findAny().orElse(null);
    }

    /**
     * Gets the enum value of the text, or returns the default value.
     *
     * @param <T> The type of the enum.
     * @param type The type of the enum.
     * @param name The name text to convert
     * @param defaultValue The default value to use
     * @return the The converted enum value
     */
    public static <T extends Enum<T>> T valueOf(Class<T> type, String name, T defaultValue)
    {
        T value;
        try
        {
            value = Enum.valueOf(type, name);
        }
        catch (IllegalArgumentException e)
        {
            value = defaultValue;
        }
        return value;
    }

    /** Disallow instantiation. */
    private EnumUtilities()
    {
    }
}
