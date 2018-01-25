package io.opensphere.core.util.lang;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection of null constants. This is for convenience when passing typed
 * <code>null</code>s. Typed <code>null</code>s are useful for getting compiler
 * warnings when a method signature changes and <code>null</code>s being passed
 * are no longer for the correct arguments.
 */
public final class Nulls
{
    /** Null string. */
    public static final String STRING = null;

    /**
     * Null typed collection.
     *
     * @param <T> The type of collection.
     * @return A <code>null</code> collection.
     */
    public static <T> Collection<T> collection()
    {
        return null;
    }

    /**
     * Null typed list.
     *
     * @param <T> The type of list.
     * @return A <code>null</code> list.
     */
    public static <T> List<T> list()
    {
        return null;
    }

    /**
     * Null typed map.
     *
     * @param <K> The type of the map keys.
     * @param <V> The type of the map values.
     * @return A <code>null</code> map.
     */
    public static <K, V> Map<K, V> map()
    {
        return null;
    }

    /**
     * Null typed set.
     *
     * @param <T> The type of set.
     * @return A <code>null</code> set.
     */
    public static <T> Set<T> set()
    {
        return null;
    }

    /** Disallow construction. */
    private Nulls()
    {
    }
}
