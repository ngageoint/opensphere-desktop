package io.opensphere.core.common.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This class decorates a Java <code>Map</code>.
 */
@SuppressWarnings("serial")
public abstract class AbstractMapDecorator<K, V> implements Map<K, V>, Serializable
{
    /**
     * the decorated <code>Map</code>.
     */
    private Map<K, V> map;

    /**
     * Creates a new instance that decorates the given <code>Map</code>.
     *
     * @param map the <code>Map</code> to be decorated.
     */
    public AbstractMapDecorator(Map<K, V> map)
    {
        this.map = map;
    }

    @Override
    public int size()
    {
        return getMap().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getMap().isEmpty();
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
    public V get(Object key)
    {
        return getMap().get(key);
    }

    @Override
    public V put(K key, V value)
    {
        return getMap().put(key, value);
    }

    @Override
    public V remove(Object key)
    {
        return getMap().remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m)
    {
        getMap().putAll(m);
    }

    @Override
    public void clear()
    {
        getMap().clear();
    }

    @Override
    public Set<K> keySet()
    {
        return getMap().keySet();
    }

    @Override
    public Collection<V> values()
    {
        return getMap().values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet()
    {
        return getMap().entrySet();
    }

    /**
     * Returns the decorated map.
     *
     * @return the decorated map.
     */
    protected Map<K, V> getMap()
    {
        return map;
    }
}
