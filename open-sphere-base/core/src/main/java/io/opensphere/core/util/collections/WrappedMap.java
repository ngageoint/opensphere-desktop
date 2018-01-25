package io.opensphere.core.util.collections;

import java.util.Map;

/**
 * A map implementation that wraps another map and passes all methods to it.
 * This class may be extended to change the behavior of the map without having
 * the extend the map directly.
 *
 * @param <K> The type of the keys in the map.
 * @param <V> The type of the values in the map.
 */
public class WrappedMap<K, V> extends AbstractProxyMap<K, V>
{
    /** The wrapped map. */
    private final Map<K, V> myMap;

    /**
     * Static construction method.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The wrapped map.
     * @return The new map.
     */
    public static <K, V> WrappedMap<K, V> create(Map<K, V> map)
    {
        return new WrappedMap<K, V>(map);
    }

    /**
     * Constructor.
     *
     * @param map The wrapped map.
     */
    public WrappedMap(Map<K, V> map)
    {
        myMap = map;
    }

    /**
     * Accessor for the map.
     *
     * @return The map.
     */
    @Override
    protected final Map<K, V> getMap()
    {
        return myMap;
    }
}
