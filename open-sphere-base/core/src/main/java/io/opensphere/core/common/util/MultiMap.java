package io.opensphere.core.common.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Defines a map that holds a collection of values against each key.
 * <p>
 * A MultiMap is a Map with slightly different semantics. Putting a value into
 * the map will add the value to a Collection at that key. Getting a value will
 * return a Collection, holding all the values put to that key.
 * </p>
 * <p>
 * For example:
 *
 * <pre>
 * MultiMap&lt;KEY_TYPE, VALUE_TYPE&gt; mhm = new MultiHashMap&lt;KEY_TYPE, VALUE_TYPE&gt;();
 * mhm.put(key, &quot;A&quot;);
 * mhm.put(key, &quot;B&quot;);
 * mhm.put(key, &quot;C&quot;);
 * Collection&lt;VALUE_TYPE&gt; coll = (Collection&lt;VALUE_TYPE&gt;)mhm.get(key);
 * </pre>
 *
 * </p>
 * <p>
 * coll will be a collection containing "A", "B", "C".
 * </p>
 *
 * @param <KEY_TYPE> the generic type of the key, with which values are
 *            associated.
 * @param <VALUE_TYPE> the generic type of the values, which are associated with
 *            keys.
 */
public interface MultiMap<KEY_TYPE, VALUE_TYPE> extends Map<KEY_TYPE, Collection<VALUE_TYPE>>
{
    /**
     * Adds the supplied value to the collection associated with the supplied
     * key. If no collection is associated with the supplied key, a new
     * collection is created. The collection into which the value is placed is
     * returned <b>AS AN UNMODIFIABLE COLLECTION</b>.
     *
     * @param pKey the key with which the value will be associated.
     * @param pValue the value to store.
     * @return the collection into which the value is placed <b>AS AN
     *         UNMODIFIABLE COLLECTION</b>.
     * @throws UnsupportedOperationException if the <code>add</code> operation
     *             is not supported by this map.
     * @throws ClassCastException if the class of the specified key or value
     *             prevents it from being stored in this map.
     * @throws NullPointerException if the specified key or value is null, and
     *             this map does not permit null keys or values.
     * @throws IllegalArgumentException if some property of the specified key or
     *             value prevents it from being stored in this map
     */
    Collection<VALUE_TYPE> add(KEY_TYPE pKey, VALUE_TYPE pValue);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#clear()
     */
    @Override
    void clear();

    /**
     * Returns <code>true</code> if this map maps one or more keys to the
     * specified value. More formally, returns <code>true</code> if and only if
     * this map contains at least one mapping to a value <code>v</code> such
     * that <code>(value==null ? v==null : value.equals(v))</code>. This
     * operation will probably require time linear in the map size for most
     * implementations of the Map interface.
     *
     * @param pCollection the collection for which to search within the map.
     * @return <code>true</code> if this map maps one or more keys to the
     *         specified value, false otherwise.
     * @throws ClassCastException if the value is of an inappropriate type for
     *             this map (optional)
     * @throws NullPointerException if the specified value is null and this map
     *             does not permit null values (optional)
     */
    boolean containsCollection(Collection<VALUE_TYPE> pCollection);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    boolean containsKey(Object pKey);

    /**
     * Tests to determine if the supplied value is stored in a collection
     * somewhere within the map.
     *
     * <p>
     * WARNING: This method may be extremely slow, as a direct linear-timed
     * comparison is not possible. In the worst case scenario, ALL collections
     * stored in the map will have to be searched.
     * </p>
     *
     * @param pValue the value for which to search.
     * @return <code>true</code> if the map contains at least one collection in
     *         which the supplied value is stored, false otherwise.
     * @throws ClassCastException if the value is of an inappropriate type for
     *             this map (optional).
     * @throws NullPointerException if the specified value is null and this map
     *             does not permit null values (optional)
     * @throws UnsupportedOperationException if this map implementation does not
     *             support searching for individual values within the map.
     */
    boolean containsSingleValue(VALUE_TYPE pValue);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    boolean containsValue(Object pValue);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#entrySet()
     */
    @Override
    Set<java.util.Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>> entrySet();

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#get(Object)
     */
    @Override
    Collection<VALUE_TYPE> get(Object pKey);

    /**
     * Returns the first value from the Collection that would be returned by
     * calling get
     *
     * @see io.opensphere.core.common.util.MultiMap#get(java.lang.Object)
     */
    VALUE_TYPE getFirst(Object pKey);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#isEmpty()
     */
    @Override
    boolean isEmpty();

    /**
     * Gets an iterator over the collection identified by the supplied key.
     * Guaranteed to never return null. If the supplied key does not correspond
     * to a give collection, an empty, non-null iterator will be returned.
     *
     * @param pKey the key for which the iterator will be returned.
     * @return an iterator over the collection associated with the supplied key.
     */
    Iterator<VALUE_TYPE> itemIterator(KEY_TYPE pKey);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#keySet()
     */
    @Override
    Set<KEY_TYPE> keySet();

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    Collection<VALUE_TYPE> put(KEY_TYPE pKey, Collection<VALUE_TYPE> pValues);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    void putAll(Map<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>> pMap);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    Collection<VALUE_TYPE> remove(Object pKey);

    /**
     * Removes the supplied value from the collection specified by the supplied
     * key. If no collection is mapped using the supplied key, or if the mapped
     * collection does not contain the supplied key, no action is taken, and a
     * null value is returned. Otherwise, the value removed from the map is
     * returned.
     *
     * @param pKey the identifier of the collection in which the target value is
     *            stored.
     * @param pValue the value to remove from the identified collection.
     * @return the value removed from the map, or null if no action was taken.
     * @throws UnsupportedOperationException if the <code>removeValue</code>
     *             operation is not supported by this map.
     * @throws ClassCastException if the class of the specified key or value
     *             prevents it from being stored in this map.
     * @throws NullPointerException if the specified key or value is null, and
     *             this map does not permit null keys or values.
     * @throws IllegalArgumentException if some property of the specified key or
     *             value prevents it from being removed from this map
     */
    VALUE_TYPE removeValue(KEY_TYPE pKey, VALUE_TYPE pValue);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#size()
     */
    @Override
    int size();

    /**
     * Determines the size of the collection associated with the supplied key.
     * If no collection is associated with the supplied key, a null value is
     * returned.
     *
     * @param pKey the key for which to get the size of the associated
     *            collection.
     * @return an integer specifying the number of items associated with the
     *         identified collection, or null if no collection is associated
     *         with the supplied key.
     */
    Integer size(KEY_TYPE pKey);

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map#values()
     */
    @Override
    Collection<Collection<VALUE_TYPE>> values();
}
