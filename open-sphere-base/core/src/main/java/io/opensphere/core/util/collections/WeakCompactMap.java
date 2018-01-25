package io.opensphere.core.util.collections;

/**
 * A weak map that tries to minimize its memory footprint by replacing its map
 * implementation based on the number of mappings it contains.
 *
 * @param <K> The type of the keys in the map.
 * @param <V> The type of the values in the map.
 */
public class WeakCompactMap<K, V> extends CompactMap<K, V>
{
    /**
     * Default constructor.
     */
    public WeakCompactMap()
    {
    }

    /**
     * Constructor to be used when the number of items in the map can be
     * estimated.
     *
     * @param expectedSize The expected size of the map.
     */
    public WeakCompactMap(int expectedSize)
    {
        super(expectedSize);
    }

    @Override
    protected MapProvider<K, V> getMapProvider()
    {
        return New.weakMapFactory();
    }
}
