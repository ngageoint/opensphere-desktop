package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map implementation that proxies map calls through the map provided by the
 * subclass.
 *
 * @param <K> The type of the keys in the map.
 * @param <V> The type of the values in the map.
 */
public abstract class AbstractProxyMap<K, V> implements Map<K, V>
{
    @Override
    public void clear()
    {
        getMap().clear();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return getMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return getMap().containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        return getMap().entrySet();
    }

    @Override
    public boolean equals(Object obj)
    {
        return getMap().equals(obj);
    }

    @Override
    public V get(Object key) throws IllegalStateException
    {
        return getMap().get(key);
    }

    @Override
    public int hashCode()
    {
        return getMap().hashCode();
    }

    @Override
    public boolean isEmpty()
    {
        return getMap().isEmpty();
    }

    @Override
    public Set<K> keySet()
    {
        return getMap().keySet();
    }

    @Override
    public V put(K key, V value)
    {
        return getMap().put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        getMap().putAll(m);
    }

    @Override
    public V remove(Object key)
    {
        return getMap().remove(key);
    }

    @Override
    public int size()
    {
        return getMap().size();
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append(getClass().getSimpleName()).append(" [").append(getMap().toString()).append(']')
                .toString();
    }

    @Override
    public Collection<V> values()
    {
        return getMap().values();
    }

    /**
     * Accessor for the map.
     *
     * @return The map.
     */
    protected abstract Map<K, V> getMap();
}
