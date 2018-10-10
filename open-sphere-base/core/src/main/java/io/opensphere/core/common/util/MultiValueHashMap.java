package io.opensphere.core.common.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A MultiValueHashMap is an implementation of a hash map, in which more than
 * one value may be mapped to a single key.
 *
 * <p>
 * A MultiMap is a Map with slightly different semantics. Getting a value will
 * return a Collection, holding all the values put to that key.
 * </p>
 *
 * <p>
 * Note that MultiValueMap is not synchronized and is not thread-safe. If you
 * wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. This class may throw exceptions when accessed by
 * concurrent threads without synchronization.
 * </p>
 *
 * @param <KEY_TYPE> the datatype of the key.
 * @param <VALUE_TYPE> the datatype of individual values.
 */
public class MultiValueHashMap<KEY_TYPE, VALUE_TYPE> extends AbstractMultiMap<KEY_TYPE, VALUE_TYPE>
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -6562695874027464868L;

    /**
     * An internal storage class, used to place key / value pairs in.
     *
     * @param <ENTRY_KEY_TYPE> the datatype of the key.
     * @param <ENTRY_VALUE_TYPE> the datatype of the value.
     */
    static class Entry<ENTRY_KEY_TYPE, ENTRY_VALUE_TYPE> implements Map.Entry<ENTRY_KEY_TYPE, Collection<ENTRY_VALUE_TYPE>>
    {
        /**
         * The hash code of the key within the entry.
         */
        private final int hash;

        /**
         * The key of the entry.
         */
        private final ENTRY_KEY_TYPE key;

        /**
         * the value of the entry.
         */
        Entry<ENTRY_KEY_TYPE, ENTRY_VALUE_TYPE> next;

        /**
         * the next entry in the hash map (may be null).
         */
        Collection<ENTRY_VALUE_TYPE> value;

        /**
         * Creates new entry.
         *
         * @param pHash The hash code of the key within the entry.
         * @param pKey The key of the entry.
         * @param pValue the value of the entry.
         * @param pNextEntry the next entry in the hash map (may be null).
         */
        Entry(int pHash, ENTRY_KEY_TYPE pKey, Collection<ENTRY_VALUE_TYPE> pValue,
                Entry<ENTRY_KEY_TYPE, ENTRY_VALUE_TYPE> pNextEntry)
        {
            value = pValue;
            next = pNextEntry;
            key = pKey;
            hash = pHash;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public final boolean equals(Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            @SuppressWarnings("unchecked")
            Map.Entry<ENTRY_KEY_TYPE, ENTRY_VALUE_TYPE> e = (Map.Entry<ENTRY_KEY_TYPE, ENTRY_VALUE_TYPE>)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || k1 != null && k1.equals(k2))
            {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || v1 != null && v1.equals(v2))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map.Entry#getKey()
         */
        @Override
        public final ENTRY_KEY_TYPE getKey()
        {
            return key;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map.Entry#getValue()
         */
        @Override
        public final Collection<ENTRY_VALUE_TYPE> getValue()
        {
            return value;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public final int hashCode()
        {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * This method is invoked whenever the value in an entry is overwritten
         * by an invocation of put(k,v) for a key k that's already in the
         * HashMap.
         *
         * @param m
         */
        void recordAccess(MultiValueHashMap<ENTRY_KEY_TYPE, ENTRY_VALUE_TYPE> m)
        {
            /* intentionally blank */
        }

        /**
         * This method is invoked whenever the entry is removed from the table.
         *
         * @param m
         */
        void recordRemoval(MultiValueHashMap<ENTRY_KEY_TYPE, ENTRY_VALUE_TYPE> m)
        {
            /* intentionally blank */
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        @Override
        public final Collection<ENTRY_VALUE_TYPE> setValue(Collection<ENTRY_VALUE_TYPE> pNewValue)
        {
            Collection<ENTRY_VALUE_TYPE> oldValue = value;
            value = pNewValue;
            return oldValue;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public final String toString()
        {
            return getKey() + "=" + getValue();
        }
    }

    /**
     * An iterator implementation used to access the set of Key / Value entries
     * in the table.
     *
     */
    private final class EntryIterator extends HashIterator<Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>>
    {
        /**
         * {@inheritDoc}
         *
         * @see java.util.Iterator#next()
         */
        @Override
        public Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>> next()
        {
            return nextEntry();
        }
    }

    /**
     * A set implementation, providing access to the key / value entries stored
     * within the hash table.
     *
     */
    private final class EntrySet extends AbstractSet<Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>>
    {
        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#clear()
         */
        @Override
        public void clear()
        {
            MultiValueHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         */
        @Override
        public boolean contains(Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            @SuppressWarnings("unchecked")
            Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>> e = (Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>)o;
            Entry<KEY_TYPE, VALUE_TYPE> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>> iterator()
        {
            return newEntryIterator();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#remove(java.lang.Object)
         */
        @Override
        public boolean remove(Object o)
        {
            return removeMapping(o) != null;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size()
        {
            return size;
        }
    }

    /**
     * An abstract base iterator class, used to access a set of data points
     * (either keys, values, or entries) stored within a {@link MultiMap}.
     *
     * @param <E> the datatype of the item to which the iterator is providing
     *            access.
     */
    private abstract class HashIterator<E> implements Iterator<E>
    {
        /**
         * The current entry in the iterator.
         */
        private Entry<KEY_TYPE, VALUE_TYPE> current;

        /**
         * The number of modifications expected by the iterator, allowing a
         * "fast-fail" behavior if the expected modification count does not
         * match the actual modification count.
         */
        private int expectedModCount;

        /**
         * The current location in the hash table's backing store.
         */
        private int index;

        /**
         * The next entry in the iterator.
         */
        private Entry<KEY_TYPE, VALUE_TYPE> next;

        /**
         * Creates a new iterator.
         */
        public HashIterator()
        {
            expectedModCount = modCount;
            if (size > 0)
            {
                // advance to first entry
                Entry<KEY_TYPE, VALUE_TYPE>[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                {
                    /* intentionally blank */
                }
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public final boolean hasNext()
        {
            return next != null;
        }

        /**
         * Gets the next entry in the iterator. An underlying implementation
         * method, allowing subclasses to call this without overriding the
         * actual behavior of the method.
         *
         * @return the next entry in the iterator.
         */
        protected final Entry<KEY_TYPE, VALUE_TYPE> nextEntry()
        {
            if (modCount != expectedModCount)
            {
                throw new ConcurrentModificationException();
            }
            Entry<KEY_TYPE, VALUE_TYPE> entry = next;
            if (entry == null)
            {
                throw new NoSuchElementException();
            }

            if ((next = entry.next) == null)
            {
                Entry<KEY_TYPE, VALUE_TYPE>[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                {
                    /* intentionally blank */
                }
            }
            current = entry;
            return entry;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove()
        {
            if (current == null)
            {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount)
            {
                throw new ConcurrentModificationException();
            }
            Object k = current.key;
            current = null;
            MultiValueHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }

    }

    /**
     * An iterator implementation used to access the set of keys in the table.
     *
     */
    private final class KeyIterator extends HashIterator<KEY_TYPE>
    {
        /**
         * {@inheritDoc}
         *
         * @see java.util.Iterator#next()
         */
        @Override
        public KEY_TYPE next()
        {
            return nextEntry().getKey();
        }
    }

    /**
     * A set implementation, in which the keys stored in this hash table are
     * accessed.
     *
     */
    private final class KeySet extends AbstractSet<KEY_TYPE>
    {
        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#clear()
         */
        @Override
        public void clear()
        {
            MultiValueHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         */
        @Override
        public boolean contains(Object o)
        {
            return containsKey(o);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<KEY_TYPE> iterator()
        {
            return newKeyIterator();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#remove(java.lang.Object)
         */
        @Override
        public boolean remove(Object o)
        {
            return MultiValueHashMap.this.removeEntryForKey(o) != null;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size()
        {
            return size;
        }
    }

    /**
     * A collection implementation, in which the values stored in this hash
     * table are accessed.
     *
     */
    private final class ValueCollection extends AbstractCollection<Collection<VALUE_TYPE>>
    {
        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#clear()
         */
        @Override
        public void clear()
        {
            MultiValueHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         */
        @Override
        public boolean contains(Object o)
        {
            return containsValue(o);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<Collection<VALUE_TYPE>> iterator()
        {
            return newValueIterator();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size()
        {
            return size;
        }
    }

    /**
     * An iterator implementation used to access the set of values in the table.
     *
     */
    private final class ValueIterator extends HashIterator<Collection<VALUE_TYPE>>
    {
        /**
         * {@inheritDoc}
         *
         * @see java.util.Iterator#next()
         */
        @Override
        public Collection<VALUE_TYPE> next()
        {
            return nextEntry().value;
        }
    }

    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by
     * either of the constructors with arguments. MUST be a power of two <=
     * 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Applies a supplemental hash function to a given hashCode, which defends
     * against poor quality hash functions. This is critical because HashMap
     * uses power-of-two length hash tables, that otherwise encounter collisions
     * for hashCodes that do not differ in lower bits. Note: Null keys always
     * map to hash 0, thus index 0.
     *
     * @param pOriginalHash the original hash value.
     * @return an updated hash value, modified to protect against poor hash
     *         functions.
     */
    static int hash(int pOriginalHash)
    {
        int originalHash = pOriginalHash;
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        originalHash ^= originalHash >>> 20 ^ originalHash >>> 12;
        return originalHash ^ originalHash >>> 7 ^ originalHash >>> 4;
    }

    /**
     * Returns index for hash code pOriginalHash.
     *
     * @param pOriginalHash the has for which the index will be returned.
     * @param pLength the length of the internal table.
     * @return the index calculated for the supplied pOriginalHash value.
     */
    static int indexFor(int pOriginalHash, int pLength)
    {
        return pOriginalHash & pLength - 1;
    }

    /**
     * This field is initialized to contain an instance of the appropriate view
     * the first time this view is requested. The views are stateless, so
     * there's no reason to create more than one of each.
     */
    private transient Set<Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>> entrySet = null;

    /**
     * This field is initialized to contain an instance of the appropriate view
     * the first time this view is requested. The views are stateless, so
     * there's no reason to create more than one of each.
     */
    private transient volatile Set<KEY_TYPE> keySet = null;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    private final float loadFactor;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g., rehash).
     * This field is used to make iterators on Collection-views of the HashMap
     * fail-fast. (See ConcurrentModificationException).
     */
    private transient volatile int modCount;

    /**
     * The number of key-value mappings contained in this map.
     */
    private transient int size;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    private transient Entry<KEY_TYPE, VALUE_TYPE>[] table;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    private int threshold;

    /**
     * This field is initialized to contain an instance of the appropriate view
     * the first time this view is requested. The views are stateless, so
     * there's no reason to create more than one of each.
     */
    private transient volatile Collection<Collection<VALUE_TYPE>> values = null;

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    @SuppressWarnings("unchecked")
    public MultiValueHashMap()
    {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
        init();
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
     * and the default load factor (0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public MultiValueHashMap(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity
     * and load factor.
     *
     * @param pInitialCapacity the initial capacity
     * @param pLoadFactor the load factor
     * @throws IllegalArgumentException if the initial capacity is negative or
     *             the load factor is nonpositive
     */
    @SuppressWarnings("unchecked")
    public MultiValueHashMap(int pInitialCapacity, float pLoadFactor)
    {
        if (pInitialCapacity < 0)
        {
            throw new IllegalArgumentException("Illegal initial capacity: " + pInitialCapacity);
        }
        int initialCapacity = pInitialCapacity;
        if (initialCapacity > MAXIMUM_CAPACITY)
        {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (pLoadFactor <= 0 || Float.isNaN(pLoadFactor))
        {
            throw new IllegalArgumentException("Illegal load factor: " + pLoadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity)
        {
            capacity <<= 1;
        }

        this.loadFactor = pLoadFactor;
        threshold = (int)(capacity * loadFactor);
        table = new Entry[capacity];
        init();
    }

    /**
     * Constructs a new <tt>HashMap</tt> with the same mappings as the specified
     * <tt>Map</tt>. The <tt>HashMap</tt> is created with default load factor
     * (0.75) and an initial capacity sufficient to hold the mappings in the
     * specified <tt>Map</tt>.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
    public MultiValueHashMap(Map<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>> m)
    {
        this(Math.max((int)(m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAllForCreate(m);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#add(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public Collection<VALUE_TYPE> add(KEY_TYPE pKey, VALUE_TYPE pValue)
    {
        Collection<VALUE_TYPE> returnValue;

        Entry<KEY_TYPE, VALUE_TYPE> entry = getEntry(pKey);

        if (entry == null)
        {
            int hash = hash(pKey.hashCode());
            int index = indexFor(hash, table.length);

            Collection<VALUE_TYPE> valueCollection = new ArrayList<>();
            valueCollection.add(pValue);

            addEntry(hash, pKey, valueCollection, index);
            returnValue = Collections.unmodifiableCollection(valueCollection);
        }
        else
        {
            entry.getValue().add(pValue);

            returnValue = Collections.unmodifiableCollection(entry.getValue());
        }

        return returnValue;
    }

    /**
     * Adds a new entry with the specified key, value and hash code to the
     * specified bucket. It is the responsibility of this method to resize the
     * table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     *
     * @param pHash the hash code of the supplied key, used to calculate the
     *            location into which the value is stored.
     * @param pKey the key with which the new value will be stored.
     * @param pValue the value to store in the hash table.
     * @param bucketIndex
     */
    protected void addEntry(int pHash, KEY_TYPE pKey, Collection<VALUE_TYPE> pValue, int bucketIndex)
    {
        /* no need to do any checking to see if there's an existing entry here.
         * If it's null, the new entry will just assume there's no entry, and
         * overwrite it. If it is not null, the new entry will store the
         * existing entry as its next pointer, and overwrite the bucket
         * entry. */
        Entry<KEY_TYPE, VALUE_TYPE> existingEntry = table[bucketIndex];

        table[bucketIndex] = new Entry<>(pHash, pKey, pValue, existingEntry);
        // double check to see if the table needs to be resized:
        if (size++ >= threshold)
        {
            resize(2 * table.length);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#clear()
     */
    @Override
    public void clear()
    {
        modCount++;
        Entry<KEY_TYPE, VALUE_TYPE>[] tab = table;
        for (int i = 0; i < tab.length; i++)
        {
            tab[i] = null;
        }
        size = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#containsCollection(java.util.Collection)
     */
    @Override
    public boolean containsCollection(Collection<VALUE_TYPE> pCollection)
    {
        return containsValue(pCollection);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object pKey)
    {
        return getEntry(pKey) != null;
    }

    /**
     * Special-case code for containsValue with null argument
     *
     * @return true if the map contains a null value, false otherwise.
     */
    private boolean containsNullValue()
    {
        Entry<KEY_TYPE, VALUE_TYPE>[] tab = table;
        for (int i = 0; i < tab.length; i++)
        {
            for (Entry<KEY_TYPE, VALUE_TYPE> e = tab[i]; e != null; e = e.next)
            {
                if (e.value == null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#containsSingleValue(java.lang.Object)
     */
    @Override
    public boolean containsSingleValue(VALUE_TYPE pValue)
    {
        if (pValue == null)
        {
            return containsNullValue();
        }

        Entry<KEY_TYPE, VALUE_TYPE>[] tab = table;
        for (int i = 0; i < tab.length; i++)
        {
            for (Entry<KEY_TYPE, VALUE_TYPE> e = tab[i]; e != null; e = e.next)
            {
                if (e.value != null && e.value.contains(pValue))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object pValue)
    {
        if (pValue == null)
        {
            return containsNullValue();
        }

        Entry<KEY_TYPE, VALUE_TYPE>[] tab = table;
        for (int i = 0; i < tab.length; i++)
        {
            for (Entry<KEY_TYPE, VALUE_TYPE> e = tab[i]; e != null; e = e.next)
            {
                if (pValue.equals(e.value))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Like addEntry except that this version is used when creating entries as
     * part of Map construction or "pseudo-construction" (cloning,
     * de-serialization). This version needn't worry about resizing the table.
     * <p>
     * Subclass overrides this to alter the behavior of HashMap(Map), clone, and
     * readObject.
     * </p>
     *
     * @param hash
     * @param key
     * @param pValue the value to store in the hash table.
     * @param bucketIndex
     */
    protected void createEntry(int hash, KEY_TYPE key, Collection<VALUE_TYPE> pValue, int bucketIndex)
    {
        Entry<KEY_TYPE, VALUE_TYPE> e = table[bucketIndex];
        table[bucketIndex] = new Entry<>(hash, key, pValue, e);
        size++;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>> entrySet()
    {
        return entrySet0();
    }

    /**
     * A utility method used to generate the entry set for the
     * {@link #entrySet()} method.
     *
     * @return the entry set view used in the {@link #entrySet()} method.
     */
    protected Set<Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>> entrySet0()
    {
        Set<Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#get(java.lang.Object)
     */
    @Override
    public Collection<VALUE_TYPE> get(Object pKey)
    {
        if (pKey == null)
        {
            return getForNullKey();
        }
        int hash = hash(pKey.hashCode());
        for (Entry<KEY_TYPE, VALUE_TYPE> e = table[indexFor(hash, table.length)]; e != null; e = e.next)
        {
            Object k;
            if (e.hash == hash && ((k = e.key) == pKey || pKey.equals(k)))
            {
                return e.value;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#get(java.lang.Object)
     */
    @Override
    public VALUE_TYPE getFirst(Object pKey)
    {

        Collection<VALUE_TYPE> coll = get(pKey);
        if (null == coll || coll.isEmpty())
        {
            return null;
        }
        for (VALUE_TYPE val : coll)
        {
            return val;
        }
        return null;
    }

    /**
     * Returns the entry associated with the specified key in the HashMap.
     * Returns null if the HashMap contains no mapping for the key.
     *
     * @param pKey The key for which to get the entry.
     * @return the entry associated with the supplied key, or null if no entry
     *         is associated with the key.
     */
    protected Entry<KEY_TYPE, VALUE_TYPE> getEntry(Object pKey)
    {
        int hash = pKey == null ? 0 : hash(pKey.hashCode());
        for (Entry<KEY_TYPE, VALUE_TYPE> e = table[indexFor(hash, table.length)]; e != null; e = e.next)
        {
            Object k;
            if (e.hash == hash && ((k = e.key) == pKey || pKey != null && pKey.equals(k)))
            {
                return e;
            }
        }
        return null;
    }

    /**
     * Offloaded version of get() to look up null keys. Null keys map to index
     * 0. This null case is split out into separate methods for the sake of
     * performance in the two most commonly used operations (get and put), but
     * incorporated with conditionals in others.
     *
     * @return the value associated with the 'null' key, or null if none is
     *         defined.
     */
    protected Collection<VALUE_TYPE> getForNullKey()
    {
        for (Entry<KEY_TYPE, VALUE_TYPE> e = table[0]; e != null; e = e.next)
        {
            if (e.key == null)
            {
                return e.value;
            }
        }
        return null;
    }

    /**
     * Initialization hook for subclasses. This method is called in all
     * constructors and pseudo-constructors (clone, readObject) after HashMap
     * has been initialized but before any entries have been inserted. (In the
     * absence of this method, readObject would require explicit knowledge of
     * subclasses.)
     */
    protected void init()
    {
        /* intentionally blank */
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#isEmpty()
     */
    @Override
    public boolean isEmpty()
    {
        return size == 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#itemIterator(java.lang.Object)
     */
    @Override
    public Iterator<VALUE_TYPE> itemIterator(KEY_TYPE pKey)
    {
        Entry<KEY_TYPE, VALUE_TYPE> entry = getEntry(pKey);

        if (entry != null && entry.value != null)
        {
            return entry.value.iterator();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#keySet()
     */
    @Override
    public Set<KEY_TYPE> keySet()
    {
        Set<KEY_TYPE> ks = keySet;
        return ks != null ? ks : (keySet = new KeySet());
    }

    /**
     * Creates a new entry iterator. Provided as a hook to allow subclasses to
     * override the behavior of the entry iterators.
     *
     * @return a new entry iterator.
     */
    protected Iterator<Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>> newEntryIterator()
    {
        return new EntryIterator();
    }

    /**
     * Creates a new key iterator. Provided as a hook to allow subclasses to
     * override the behavior of the key iterators.
     *
     * @return a new key iterator.
     */
    protected Iterator<KEY_TYPE> newKeyIterator()
    {
        return new KeyIterator();
    }

    /**
     * Creates a new values iterator. Provided as a hook to allow subclasses to
     * override the behavior of the values iterators.
     *
     * @return a new values iterator.
     */
    protected Iterator<Collection<VALUE_TYPE>> newValueIterator()
    {
        return new ValueIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#put(java.lang.Object,
     *      java.util.Collection)
     */
    @Override
    public Collection<VALUE_TYPE> put(KEY_TYPE pKey, Collection<VALUE_TYPE> pValues)
    {
        if (pKey == null)
        {
            return putForNullKey(pValues);
        }
        int hash = hash(pKey.hashCode());
        int i = indexFor(hash, table.length);
        for (Entry<KEY_TYPE, VALUE_TYPE> e = table[i]; e != null; e = e.next)
        {
            Object k;
            if (e.hash == hash && ((k = e.key) == pKey || pKey.equals(k)))
            {
                Collection<VALUE_TYPE> oldValue = e.value;
                e.value = pValues;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(hash, pKey, pValues, i);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>> pMap)
    {
        int numKeysToBeAdded = pMap.size();
        if (numKeysToBeAdded == 0)
        {
            return;
        }

        /* Expand the map if the map if the number of mappings to be added is
         * greater than or equal to threshold. This is conservative; the obvious
         * condition is (m.size() + size) >= threshold, but this condition could
         * result in a map with twice the appropriate capacity, if the keys to
         * be added overlap with the keys already in this map. By using the
         * conservative calculation, we subject ourself to at most one extra
         * resize. */
        if (numKeysToBeAdded > threshold)
        {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
            {
                targetCapacity = MAXIMUM_CAPACITY;
            }
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
            {
                newCapacity <<= 1;
            }
            if (newCapacity > table.length)
            {
                resize(newCapacity);
            }
        }

        for (Iterator<? extends Map.Entry<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>>> i = pMap.entrySet().iterator(); i
                .hasNext();)
        {
            Map.Entry<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>> e = i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Adds the entries contained within the supplied argument to the map, when
     * generating the map from the constructor.
     *
     * @param pMap the collection with which to populate the map.
     */
    protected void putAllForCreate(Map<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>> pMap)
    {
        for (Iterator<? extends Map.Entry<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>>> i = pMap.entrySet().iterator(); i
                .hasNext();)
        {
            Map.Entry<? extends KEY_TYPE, ? extends Collection<VALUE_TYPE>> e = i.next();
            putForCreate(e.getKey(), e.getValue());
        }
    }

    /**
     * This method is used instead of put by constructors and
     * pseudo-constructors (clone, readObject). It does not resize the table,
     * check for co-modification, etc. It calls createEntry rather than
     * addEntry.
     *
     * @param pKey the key with which the supplied value will be associated.
     * @param pValue the value to associate with the supplied key.
     */
    protected void putForCreate(KEY_TYPE pKey, Collection<VALUE_TYPE> pValue)
    {
        int hash = pKey == null ? 0 : hash(pKey.hashCode());
        int i = indexFor(hash, table.length);

        /* Look for pre-existing entry for key. This will never happen for clone
         * or de-serialize. It will only happen for construction if the input
         * Map is a sorted map whose ordering is inconsistent w/ equals. */
        for (Entry<KEY_TYPE, VALUE_TYPE> e = table[i]; e != null; e = e.next)
        {
            Object k;
            if (e.hash == hash && ((k = e.key) == pKey || pKey != null && pKey.equals(k)))
            {
                e.value = pValue;
                return;
            }
        }

        createEntry(hash, pKey, pValue, i);
    }

    /**
     * Offloaded version of put for null keys
     *
     * @param pValue the value with which the null key will be associated.
     * @return the value (if any) previously associated with the null key.
     */
    protected Collection<VALUE_TYPE> putForNullKey(Collection<VALUE_TYPE> pValue)
    {
        for (Entry<KEY_TYPE, VALUE_TYPE> e = table[0]; e != null; e = e.next)
        {
            if (e.key == null)
            {
                Collection<VALUE_TYPE> oldValue = e.value;
                e.value = pValue;
                e.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
        addEntry(0, null, pValue, 0);
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#remove(java.lang.Object)
     */
    @Override
    public Collection<VALUE_TYPE> remove(Object pKey)
    {
        Entry<KEY_TYPE, VALUE_TYPE> e = removeEntryForKey(pKey);
        return e == null ? null : e.value;
    }

    /**
     * Removes and returns the entry associated with the specified key in the
     * HashMap. Returns null if the HashMap contains no mapping for this key.
     *
     * @param pKey the key corresponding to the entry to remove.
     * @return the entry that was associated with the supplied key.
     */
    private Entry<KEY_TYPE, VALUE_TYPE> removeEntryForKey(Object pKey)
    {
        int hash = pKey == null ? 0 : hash(pKey.hashCode());
        int i = indexFor(hash, table.length);
        Entry<KEY_TYPE, VALUE_TYPE> prev = table[i];
        Entry<KEY_TYPE, VALUE_TYPE> e = prev;

        while (e != null)
        {
            Entry<KEY_TYPE, VALUE_TYPE> next = e.next;
            Object k;
            if (e.hash == hash && ((k = e.key) == pKey || pKey != null && pKey.equals(k)))
            {
                modCount++;
                size--;
                if (prev == e)
                {
                    table[i] = next;
                }
                else
                {
                    prev.next = next;
                }
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Special version of remove for EntrySet.
     *
     * @param pEntry the entry to remove from the map.
     * @return the entry that was removed from the map.
     */
    final Entry<KEY_TYPE, VALUE_TYPE> removeMapping(Object pEntry)
    {
        if (!(pEntry instanceof Map.Entry))
        {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>> entry = (Map.Entry<KEY_TYPE, Collection<VALUE_TYPE>>)pEntry;
        Object key = entry.getKey();
        int hash = key == null ? 0 : hash(key.hashCode());
        int i = indexFor(hash, table.length);
        Entry<KEY_TYPE, VALUE_TYPE> prev = table[i];
        Entry<KEY_TYPE, VALUE_TYPE> e = prev;

        while (e != null)
        {
            Entry<KEY_TYPE, VALUE_TYPE> next = e.next;
            if (e.hash == hash && e.equals(entry))
            {
                modCount++;
                size--;
                if (prev == e)
                {
                    table[i] = next;
                }
                else
                {
                    prev.next = next;
                }
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#removeValue(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public VALUE_TYPE removeValue(KEY_TYPE pKey, VALUE_TYPE pValue)
    {
        VALUE_TYPE returnValue = null;

        Entry<KEY_TYPE, VALUE_TYPE> entry = getEntry(pKey);
        if (entry != null && entry.getValue() != null)
        {
            if (entry.getValue().remove(pValue) == true)
            {
                returnValue = pValue;
            }
        }

        return returnValue;
    }

    /**
     * Rehashes the contents of this map into a new array with a larger
     * capacity. This method is called automatically when the number of keys in
     * this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not resize the
     * map, but sets threshold to Integer.MAX_VALUE. This has the effect of
     * preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two; must be
     *            greater than current capacity unless current capacity is
     *            MAXIMUM_CAPACITY (in which case value is irrelevant).
     */
    void resize(int newCapacity)
    {
        Entry<KEY_TYPE, VALUE_TYPE>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY)
        {
            threshold = Integer.MAX_VALUE;
            return;
        }

        @SuppressWarnings("unchecked")
        Entry<KEY_TYPE, VALUE_TYPE>[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)(newCapacity * loadFactor);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#size()
     */
    @Override
    public int size()
    {
        return size;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#size(java.lang.Object)
     */
    @Override
    public Integer size(KEY_TYPE pKey)
    {
        Integer returnValue = null;

        Entry<KEY_TYPE, VALUE_TYPE> entry = getEntry(pKey);
        if (entry != null && entry.getValue() != null)
        {
            returnValue = Integer.valueOf(entry.getValue().size());
        }

        return returnValue;
    }

    /**
     * Transfers all entries from current table to newTable.
     *
     * @param newTable
     */
    void transfer(Entry<KEY_TYPE, VALUE_TYPE>[] newTable)
    {
        Entry<KEY_TYPE, VALUE_TYPE>[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++)
        {
            Entry<KEY_TYPE, VALUE_TYPE> e = src[j];
            if (e != null)
            {
                src[j] = null;
                do
                {
                    Entry<KEY_TYPE, VALUE_TYPE> next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                }
                while (e != null);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.util.MultiMap#values()
     */
    @Override
    public Collection<Collection<VALUE_TYPE>> values()
    {
        Collection<Collection<VALUE_TYPE>> vs = values;
        return vs != null ? vs : (values = new ValueCollection());
    }
}
