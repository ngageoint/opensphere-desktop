package io.opensphere.core.util.collections;

import java.util.Map;

/**
 * Interface for a facility that provides maps.
 *
 * @param <K> The type of keys in the provided maps.
 * @param <V> The type of values in the provided maps.
 */
public interface MapProvider<K, V>
{
    /**
     * Get a map.
     *
     * @return The map.
     */
    Map<K, V> get();

    /**
     * Get a map with a suggested size. The size may be ignored at the
     * discretion of the implementation.
     *
     * @param size The suggested size for the map.
     * @return The map.
     */
    Map<K, V> get(int size);

    /**
     * Get a map, specifying the contents of the map. The returned map must
     * contain the provided contents at a minimum, but may also have other
     * contents.
     *
     * @param contents The contents for the map.
     * @return The map.
     */
    Map<K, V> get(Map<? extends K, ? extends V> contents);

    /**
     * Get a map with the intention that the map will not need to contain
     * anything.
     *
     * @return The map.
     */
    Map<K, V> getEmpty();

    /**
     * Get a map with the intention that the map will only contain one mapping.
     *
     * @param key The key.
     * @param value The value.
     * @return The map.
     */
    Map<K, V> getSingleton(K key, V value);
}
