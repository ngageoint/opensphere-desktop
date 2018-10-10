package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.util.Utilities;

/**
 * A map implementation that wraps another map and passes most methods to it,
 * except if a value is requested for a key that the wrapped map does not
 * contain. In that case, if the key is assignable to my key type, a value is
 * generated using a factory and inserted into the wrapped map.
 * <p>
 * Null values are treated the same as non-existent values.
 * <p>
 * If a value is inserted into the map externally at the same time a get
 * operation is performed on the lazy map, it is undefined which value the get
 * operation will return.
 *
 * @param <K> The type of the keys in the map.
 * @param <V> The type of the values in the map.
 */
public class LazyMap<K, V> extends WrappedMap<K, V>
{
    /** The value factory. */
    private final Factory<? super K, ? extends V> myFactory;

    /** The type of the keys in the map. */
    private final Class<? extends K> myKeyType;

    /**
     * Create a lazy map using a collection provider for the values in the map.
     *
     * @param <K> The type of the keys in the map.
     * @param <E> The type of the elements in the collections that are the
     *            values of the map.
     * @param map The wrapped map.
     * @param keyType The key type.
     * @param provider The provider for the collections that are the values of
     *            the map.
     * @return The new map.
     */
    public static <K, E> LazyMap<K, Collection<E>> create(Map<K, Collection<E>> map, Class<K> keyType,
            CollectionProvider<E> provider)
    {
        return create(map, keyType, LazyMap.providerToFactory(provider));
    }

    /**
     * Create a lazy map using a list provider for the values in the map.
     *
     * @param <K> The type of the keys in the map.
     * @param <E> The type of the elements in the collections that are the
     *            values of the map.
     * @param map The wrapped map.
     * @param keyType The key type.
     * @param provider The provider for the lists that are the values of the
     *            map.
     * @return The new map.
     */
    public static <K, E> LazyMap<K, List<E>> create(Map<K, List<E>> map, Class<K> keyType, ListProvider<E> provider)
    {
        return create(map, keyType, LazyMap.providerToFactory(provider));
    }

    /**
     * Create a lazy map using a set provider for the values in the map.
     *
     * @param <K> The type of the keys in the map.
     * @param <E> The type of the elements in the collections that are the
     *            values of the map.
     * @param map The wrapped map.
     * @param keyType The key type.
     * @param provider The provider for the sets that are the values of the map.
     * @return The new map.
     */
    public static <K, E> LazyMap<K, Set<E>> create(Map<K, Set<E>> map, Class<K> keyType, SetProvider<E> provider)
    {
        return create(map, keyType, LazyMap.providerToFactory(provider));
    }

    /**
     * Create a lazy map with no default factory. A lazy map created in this way
     * must be used with {@link #get(Object, Factory)} rather than
     * {@link #get(Object)} so that a factory can be provided if the value does
     * not exist.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The wrapped map.
     * @param keyType The type of the keys in the map.
     * @return The new map.
     */
    public static <K, V> LazyMap<K, V> create(Map<K, V> map, Class<? extends K> keyType)
    {
        return new LazyMap<>(map, keyType);
    }

    /**
     * Create a lazy map with a default factory.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The wrapped map.
     * @param keyType The type of the keys in the map.
     * @param factory The factory to provide new values in the map.
     * @return The new map.
     */
    public static <K, V> LazyMap<K, V> create(Map<K, V> map, Class<? extends K> keyType, Factory<? super K, ? extends V> factory)
    {
        return new LazyMap<>(map, keyType, factory);
    }

    /**
     * Get a factory that returns a new collection from a collection provider
     * for any requested key.
     *
     * @param <V> The type of the values in the collections produced by the
     *            factory.
     * @param provider The collection provider.
     * @return The factory.
     */
    public static <V> Factory<Object, Collection<V>> providerToFactory(final CollectionProvider<V> provider)
    {
        return key -> provider.get();
    }

    /**
     * Get a factory that returns a new list from a list provider for any
     * requested key.
     *
     * @param <V> The type of the values in the collections produced by the
     *            factory.
     * @param provider The list provider.
     * @return The factory.
     */
    public static <V> Factory<Object, List<V>> providerToFactory(final ListProvider<V> provider)
    {
        return key -> provider.get();
    }

    /**
     * Get a factory that returns a new set from a set provider for any
     * requested key.
     *
     * @param <V> The type of the values in the collections produced by the
     *            factory.
     * @param provider The set provider.
     * @return The factory.
     */
    public static <V> Factory<Object, Set<V>> providerToFactory(final SetProvider<V> provider)
    {
        return key -> provider.get();
    }

    /**
     * Constructor with no default factory. If this constructor is used,
     * {@link #get(Object, Factory)} must be used rather than
     * {@link #get(Object)} so that a factory can be provided.
     *
     * @param map The wrapped map.
     * @param keyType The type of the keys in the map.
     */
    public LazyMap(Map<K, V> map, Class<? extends K> keyType)
    {
        this(map, keyType, (Factory<K, V>)null);
    }

    /**
     * Constructor that takes a factory. The provided factory will be used if
     * {@link #get(Object)} is called and a value does not exist.
     *
     * @param map The wrapped map.
     * @param keyType The type of the keys in the map.
     * @param factory The factory to provide new values in the map.
     */
    public LazyMap(Map<K, V> map, Class<? extends K> keyType, Factory<? super K, ? extends V> factory)
    {
        super(map);
        myFactory = factory;
        myKeyType = keyType;
    }

    /**
     * Get a value from the map, using the factory specified in the constructor
     * if the value does not exist.
     *
     * @param key The key whose value is to be returned.
     * @return The value.
     * @throws IllegalStateException If no factory was specified when this map
     *             was constructed.
     */
    @Override
    public V get(Object key) throws IllegalStateException
    {
        Factory<? super K, ? extends V> factory = getFactory();
        if (factory == null)
        {
            throw new IllegalStateException("No factory installed.");
        }
        return get(key, factory);
    }

    /**
     * Get a value from the map, specifying a factory to use if the value does
     * not exist.
     *
     * @param key The key whose value is to be returned.
     * @param factory The factory to use to generate the value if it doesn't
     *            exist.
     * @return The value.
     */
    public V get(Object key, Factory<? super K, ? extends V> factory)
    {
        Utilities.checkNull(factory, "factory");
        V value = getIfExists(key);
        if (value != null || key != null && !getKeyType().isInstance(key))
        {
            return value;
        }

        synchronized (getMap())
        {
            value = getIfExists(key);
            if (value != null)
            {
                return value;
            }

            @SuppressWarnings("unchecked")
            K k = (K)key;
            value = factory.create(k);
            V old = getMap().put(k, value);
            assert old == null;
            return value;
        }
    }

    /**
     * Get a value if the key exists in the map, but do not create a value.
     *
     * @param key The key.
     * @return The associated value, or {@code null} if the entry does not
     *         exist.
     */
    public V getIfExists(Object key)
    {
        return getMap().get(key);
    }

    @Override
    public V put(K key, V value)
    {
        synchronized (getMap())
        {
            return super.put(key, value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        synchronized (getMap())
        {
            super.putAll(m);
        }
    }

    /**
     * Accessor for the factory.
     *
     * @return The factory.
     */
    protected final Factory<? super K, ? extends V> getFactory()
    {
        return myFactory;
    }

    /**
     * Accessor for the keyType.
     *
     * @return The keyType.
     */
    protected final Class<? extends K> getKeyType()
    {
        return myKeyType;
    }

    /**
     * Interface for the factory that provides the values in the map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     */
    @FunctionalInterface
    public interface Factory<K, V>
    {
        /**
         * Create a new value for the map.
         *
         * @param key The key for the value.
         * @return The value.
         */
        V create(K key);
    }
}
