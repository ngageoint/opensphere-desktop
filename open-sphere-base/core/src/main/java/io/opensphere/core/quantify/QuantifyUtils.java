package io.opensphere.core.quantify;

/**
 * A collection of utilities for use in the quantify plugin.
 */
public final class QuantifyUtils
{
    /**
     * Private constructor, preventing instantiation of utility classes.
     */
    private QuantifyUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Normalizes the supplied string for use as a metric name, replacing all
     * spaces with dashes, and removing any non-alphanumeric character
     * (excluding dots, dashes, and underscores).
     *
     * @param name the name to normalize.
     * @return a normalized name for use as a metric name.
     */
    public static String normalize(String name)
    {
        String normalizedName = name.toLowerCase().replaceAll(" ", "-");
        // after replacing all spaces, get rid of any non-alphanumeric
        // characters:
        normalizedName.replaceAll("[^a-zA-Z0-9.-_]+", "");

        return normalizedName;
    }
}
