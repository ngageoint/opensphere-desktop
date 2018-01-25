package io.opensphere.core.util.collections;

/**
 * A map that tries to minimize its memory footprint by replacing its map
 * implementation based on the number of mappings it contains, using a custom
 * map provider.
 *
 * @param <K> The type of the keys in the map.
 * @param <V> The type of the values in the map.
 */
public class CustomCompactMap<K, V> extends CompactMap<K, V>
{
    /** The custom map provider. */
    private final MapProvider<K, V> myMapProvider;

    /**
     * Constructor that takes a custom map provider.
     *
     * @param mapProvider The map provider.
     */
    public CustomCompactMap(MapProvider<K, V> mapProvider)
    {
        myMapProvider = mapProvider;
    }

    @Override
    protected MapProvider<K, V> getMapProvider()
    {
        return myMapProvider;
    }
}
