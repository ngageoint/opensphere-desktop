package io.opensphere.core.util.collections;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.WeakReference;

/**
 * A weak map that can only contain one value.
 *
 * @param <K> The type of the key in the map.
 * @param <V> The type of the value in the map.
 * @see Collections#singletonMap
 */
public class WeakSingletonMap<K, V> extends AbstractMap<K, V>
{
    /** The reference to the key in the map. */
    private final Reference<K> myKeyRef;

    /**
     * The value in the map. This will be nulled once the key reference is
     * cleared.
     */
    private volatile V myValue;

    /**
     * Constructor.
     *
     * @param key The key.
     * @param value The value.
     */
    public WeakSingletonMap(K key, V value)
    {
        myKeyRef = new WeakReference<>(key);
        myValue = value;
    }

    @Override
    public boolean containsKey(Object key)
    {
        checkReference();
        return EqualsHelper.equals(key, myKeyRef.get());
    }

    @Override
    public boolean containsValue(Object value)
    {
        checkReference();
        return myKeyRef.get() != null && EqualsHelper.equals(value, myValue);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        checkReference();
        K key = myKeyRef.get();
        if (key == null)
        {
            return Collections.emptySet();
        }
        return Collections.<Map.Entry<K, V>>singleton(new SimpleImmutableEntry<>(key, myValue));
    }

    @Override
    public V get(Object key)
    {
        checkReference();
        return EqualsHelper.equals(key, myKeyRef.get()) ? myValue : null;
    }

    @Override
    public boolean isEmpty()
    {
        checkReference();
        return myValue == null;
    }

    @Override
    public Set<K> keySet()
    {
        K key = myKeyRef.get();
        return key == null ? Collections.<K>emptySet() : Collections.singleton(key);
    }

    @Override
    public int size()
    {
        checkReference();
        return myValue == null ? 0 : 1;
    }

    @Override
    public Collection<V> values()
    {
        checkReference();
        return myValue == null ? Collections.<V>emptySet() : Collections.singleton(myValue);
    }

    /**
     * If the key reference has been cleared, clear my value.
     */
    private void checkReference()
    {
        if (myKeyRef.get() == null)
        {
            myValue = null;
        }
    }
}
