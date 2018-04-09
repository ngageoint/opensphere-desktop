package io.opensphere.core.util.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.opensphere.core.util.Immutable;
import io.opensphere.core.util.Utilities;

/**
 * Factory for standard classes. This allows easy switching of implementations
 * across the application.
 */
@SuppressWarnings("PMD.GodClass")
public final class New
{
    /** The singleton collection provider. */
    private static final CollectionProvider<Object> COLLECTION_FACTORY = new CollectionProvider<Object>()
    {
        @Override
        public Collection<Object> get()
        {
            return collection();
        }

        @Override
        public Collection<Object> get(Collection<? extends Object> initialContents)
        {
            return collection(initialContents);
        }

        @Override
        public Collection<Object> get(int initialSize)
        {
            return collection(initialSize);
        }

        @Override
        public Collection<Object> getEmpty()
        {
            return Collections.emptySet();
        }
    };

    /** The singleton insertion-order set provider. */
    private static final SetProvider<Object> INSERTION_ORDER_SET_FACTORY = new SetProvider<Object>()
    {
        @Override
        public Set<Object> get()
        {
            return insertionOrderSet();
        }

        @Override
        public Set<Object> get(Collection<? extends Object> initialContents)
        {
            return insertionOrderSet(initialContents);
        }

        @Override
        public Set<Object> get(int initialSize)
        {
            return insertionOrderSet(initialSize);
        }

        @Override
        public Set<Object> getEmpty()
        {
            return Collections.emptySet();
        }
    };

    /** The singleton linked list provider. */
    private static final ListProvider<Object> LINKED_LIST_FACTORY = new ListProvider<Object>()
    {
        @Override
        public List<Object> get()
        {
            return linkedList();
        }

        @Override
        public List<Object> get(Collection<? extends Object> initialContents)
        {
            return linkedList(initialContents);
        }

        @Override
        public List<Object> get(int initialSize)
        {
            return linkedList();
        }

        @Override
        public List<Object> getEmpty()
        {
            return Collections.emptyList();
        }
    };

    /** The singleton list provider. */
    private static final ListProvider<Object> LIST_FACTORY = new ListProvider<Object>()
    {
        @Override
        public List<Object> get()
        {
            return list();
        }

        @Override
        public List<Object> get(Collection<? extends Object> initialContents)
        {
            return list(initialContents);
        }

        @Override
        public List<Object> get(int initialSize)
        {
            return list(initialSize);
        }

        @Override
        public List<Object> getEmpty()
        {
            return Collections.emptyList();
        }
    };

    /** The singleton map provider. */
    private static final MapProvider<Object, Object> MAP_FACTORY = new MapProvider<Object, Object>()
    {
        @Override
        public Map<Object, Object> get()
        {
            return map();
        }

        @Override
        public Map<Object, Object> get(int initialSize)
        {
            return map(initialSize);
        }

        @Override
        public Map<Object, Object> get(Map<? extends Object, ? extends Object> initialContents)
        {
            return map(initialContents);
        }

        @Override
        public Map<Object, Object> getEmpty()
        {
            return Collections.emptyMap();
        }

        @Override
        public Map<Object, Object> getSingleton(Object key, Object value)
        {
            return Collections.singletonMap(key, value);
        }
    };

    /** The singleton natural-order set provider. */
    private static final SetProvider<Object> NATURAL_ORDER_SET_FACTORY = new SetProvider<Object>()
    {
        @Override
        public Set<Object> get()
        {
            return naturalOrderSet();
        }

        @Override
        public Set<Object> get(Collection<? extends Object> initialContents)
        {
            return naturalOrderSet(initialContents);
        }

        @Override
        public Set<Object> get(int initialSize)
        {
            return naturalOrderSet(initialSize);
        }

        @Override
        public Set<Object> getEmpty()
        {
            return Collections.emptySet();
        }
    };

    /** The singleton random-access list provider. */
    private static final ListProvider<Object> RANDOM_ACCESS_LIST_FACTORY = new ListProvider<Object>()
    {
        @Override
        public List<Object> get()
        {
            return randomAccessList();
        }

        @Override
        public List<Object> get(Collection<? extends Object> initialContents)
        {
            return randomAccessList(initialContents);
        }

        @Override
        public List<Object> get(int initialSize)
        {
            return randomAccessList(initialSize);
        }

        @Override
        public List<Object> getEmpty()
        {
            return Collections.emptyList();
        }
    };

    /** The singleton set provider. */
    private static final SetProvider<Object> SET_FACTORY = new SetProvider<Object>()
    {
        @Override
        public Set<Object> get()
        {
            return set();
        }

        @Override
        public Set<Object> get(Collection<? extends Object> initialContents)
        {
            return set(initialContents);
        }

        @Override
        public Set<Object> get(int initialSize)
        {
            return set(initialSize);
        }

        @Override
        public Set<Object> getEmpty()
        {
            return Collections.emptySet();
        }
    };

    /** The singleton synchronized list provider. */
    private static final ListProvider<Object> SYNCHRONIZED_LIST_FACTORY = new SynchronizedListProvider<>(LIST_FACTORY);

    /** The singleton synchronized set provider. */
    private static final SetProvider<Object> SYNCHRONIZED_SET_FACTORY = new SynchronizedSetProvider<>(SET_FACTORY);

    /** The singleton weak map provider. */
    private static final MapProvider<Object, Object> WEAK_MAP_FACTORY = new MapProvider<Object, Object>()
    {
        @Override
        public Map<Object, Object> get()
        {
            return weakMap();
        }

        @Override
        public Map<Object, Object> get(int initialSize)
        {
            return weakMap(initialSize);
        }

        @Override
        public Map<Object, Object> get(Map<? extends Object, ? extends Object> initialContents)
        {
            return weakMap(initialContents);
        }

        @Override
        public Map<Object, Object> getEmpty()
        {
            return Collections.emptyMap();
        }

        @Override
        public Map<Object, Object> getSingleton(Object key, Object value)
        {
            return new WeakSingletonMap<Object, Object>(key, value);
        }
    };

    /**
     * Create an array with the contents of the given collection.
     *
     * @param <T> The type of object in the array.
     * @param col The input collection.
     * @param type The type of object in the array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws NullPointerException If type is {@code null}.
     * @throws IllegalArgumentException If type is {@link Void#TYPE}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] array(Collection<? extends T> col, Class<T> type)
    {
        if (col == null)
        {
            return null;
        }
        T[] arr = (T[])Array.newInstance(type, col.size());
        col.toArray(arr);
        return arr;
    }

    /**
     * Create an array with the contents of the given collection, adding
     * {@code null}s at the beginning and/or end of the array.
     *
     * @param <T> The type of object in the array.
     * @param col The input collection.
     * @param type The type of object in the array.
     * @param prefixCount The number of null elements to put at the beginning of
     *            the array.
     * @param postfixCount The number of null elements to put at the end of the
     *            array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If the prefixCount or postfixCount is
     *             negative.
     * @throws NullPointerException If type is {@code null}.
     * @throws IllegalArgumentException If type is {@link Void#TYPE}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] array(Collection<? extends T> col, Class<T> type, int prefixCount, int postfixCount)
        throws IllegalArgumentException
    {
        if (prefixCount < 0 || postfixCount < 0)
        {
            throw new IllegalArgumentException("prefixCount and postfixCount must be >= 0");
        }
        if (col == null)
        {
            return null;
        }
        T[] arr = (T[])Array.newInstance(type, col.size() + prefixCount + postfixCount);
        col.toArray(arr);
        if (prefixCount > 0)
        {
            System.arraycopy(arr, 0, arr, prefixCount, col.size());
            Arrays.fill(arr, 0, prefixCount, null);
        }
        return arr;
    }

    /**
     * Create an array with the contents of the given array.
     *
     * @param array The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws NullPointerException If type is {@code null}.
     */
    public static Integer[] array(int[] array)
    {
        return array(array, Integer.class);
    }

    /**
     * Create an array with the contents of the given array. This can be used to
     * create an array of wrapper types from an array of primitive types.
     *
     * @param <T> The type of object in the array.
     * @param array The input array.
     * @param type The type of object in the array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws NullPointerException If type is {@code null}.
     * @throws IllegalArgumentException If type is {@link Void#TYPE}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] array(Object array, Class<T> type)
    {
        if (array == null)
        {
            return null;
        }
        else if (!array.getClass().isArray())
        {
            throw new IllegalArgumentException("Argument is not an array: " + array);
        }
        else if (!type.isAssignableFrom(array.getClass().getComponentType())
                && Utilities.primitiveTypeFor(type) != array.getClass().getComponentType())
        {
            throw new IllegalArgumentException("Array component type [" + array.getClass().getComponentType()
                    + "] is not compatible with declared list type [" + type + "]");
        }
        else
        {
            T[] arr = (T[])Array.newInstance(type, Array.getLength(array));
            for (int index = 0; index < arr.length;)
            {
                arr[index] = (T)Array.get(array, index++);
            }
            return arr;
        }
    }

    /**
     * Concise way to create an array from some objects.
     *
     * @param <T> The type of the objects in the array.
     * @param objs The objects to go in the array.
     * @return The array.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> T[] array(T... objs)
    {
        return objs;
    }

    /**
     * Create a boolean array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static boolean[] booleanArray(Boolean[] arr)
    {
        return (boolean[])uncheckedArray(arr, boolean.class);
    }

    /**
     * Create a boolean array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static boolean[] booleanArray(Collection<? extends Boolean> col)
    {
        return (boolean[])uncheckedArray(col, boolean.class);
    }

    /**
     * Create a byte array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static byte[] byteArray(Byte[] arr)
    {
        return (byte[])uncheckedArray(arr, byte.class);
    }

    /**
     * Create a byte array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static byte[] byteArray(Collection<? extends Byte> col)
    {
        return (byte[])uncheckedArray(col, byte.class);
    }

    /**
     * Factory method for a collection that casts the contents.
     *
     * @param <T> The type of the output collection.
     * @param col The initial contents of the collection.
     * @return The new collection instance, or {@code null} if the input is
     *         {@code null}.
     * @throws ClassCastException If one of the elements cannot be cast to the
     *             new type.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> castCollection(Collection<?> col) throws ClassCastException
    {
        Collection<T> result;
        if (col == null)
        {
            result = null;
        }
        else
        {
            Object[] elementData = col.toArray();
            result = new ArrayList<>(elementData.length);
            for (int index = 0; index < elementData.length;)
            {
                result.add((T)elementData[index++]);
            }
        }
        return result;
    }

    /**
     * Create a char array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static char[] charArray(Character[] arr)
    {
        return (char[])uncheckedArray(arr, char.class);
    }

    /**
     * Create a char array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static char[] charArray(Collection<? extends Character> col)
    {
        return (char[])uncheckedArray(col, char.class);
    }

    /**
     * Factory method for a collection.
     *
     * @param <T> The type of the objects in the collection.
     * @return The new collection instance.
     */
    public static <T> Collection<T> collection()
    {
        return new ArrayList<T>();
    }

    /**
     * Factory method for a collection.
     *
     * @param <T> The type of the objects in the collection.
     * @param col The initial contents of the collection.
     * @return The new collection instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <T> Collection<T> collection(Collection<? extends T> col)
    {
        return col == null ? null : new ArrayList<T>(col);
    }

    /**
     * Factory method for a collection.
     *
     * @param <T> The type of the objects in the collection.
     * @param initialCapacity The initial capacity for the collection. This may
     *            be ignored by the implementation.
     * @return The new collection instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <T> Collection<T> collection(int initialCapacity) throws IllegalArgumentException
    {
        return new ArrayList<T>(initialCapacity);
    }

    /**
     * Create a collection from an iterable.
     *
     * @param <T> The type of objects in the collection.
     * @param iter The iterable.
     * @return The new collection.
     */
    public static <T> Collection<T> collection(Iterable<? extends T> iter)
    {
        return collection(iter, New.<T>listFactory());
    }

    /**
     * Create a collection from an iterable.
     *
     * @param <T> The type of objects in the collection.
     * @param iter The iterable.
     * @param provider The collection provider.
     * @return The new collection.
     */
    public static <T> Collection<T> collection(Iterable<? extends T> iter, CollectionProvider<T> provider)
    {
        Collection<T> col = provider.get();
        for (Iterator<? extends T> it = iter.iterator(); it.hasNext();)
        {
            col.add(it.next());
        }
        return col;
    }

    /**
     * Get a factory that creates collections.
     *
     * @param <E> The type of objects contained by the collections produced by
     *            the provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <E> CollectionProvider<E> collectionFactory()
    {
        return (CollectionProvider<E>)COLLECTION_FACTORY;
    }

    /**
     * Factory method for a concurrent map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @return The new map instance.
     */
    public static <K, V> ConcurrentMap<K, V> concurrentMap()
    {
        return new ConcurrentHashMap<K, V>();
    }

    /**
     * Factory method for a concurrent map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param initialCapacity The initial capacity of the map.
     * @return The new map instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <K, V> Map<K, V> concurrentMap(int initialCapacity)
    {
        return new ConcurrentHashMap<K, V>(initialCapacity);
    }

    /**
     * Factory method for a concurrent map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The initial contents of the map.
     * @return The new map instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <K, V> Map<K, V> concurrentMap(Map<? extends K, ? extends V> map)
    {
        return map == null ? null : new ConcurrentHashMap<K, V>(map);
    }

    /**
     * Factory method for a double ended queue.
     *
     * @param <T> The type of the objects in the queue.
     * @return The new deque instance.
     */
    public static <T> Deque<T> deque()
    {
        return new LinkedList<T>();
    }

    /**
     * Factory method for a double ended queue that takes a collection of
     * initial values.
     *
     * @param <T> The type of the objects in the queue.
     * @param col The initial contents of the queue.
     * @return The new deque instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <T> Deque<T> deque(Collection<? extends T> col)
    {
        return col == null ? null : new LinkedList<T>(col);
    }

    /**
     * Create a double array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static double[] doubleArray(Collection<? extends Double> col)
    {
        return (double[])uncheckedArray(col, double.class);
    }

    /**
     * Create a double array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static double[] doubleArray(Double[] arr)
    {
        return (double[])uncheckedArray(arr, double.class);
    }

    /**
     * Get an empty array.
     *
     * @param <T> The type of object in the array.
     * @param type The type of object in the array.
     * @return The empty array.
     * @throws NullPointerException If type is {@code null}.
     * @throws IllegalArgumentException If type is {@link Void#TYPE}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(Class<T> type)
    {
        return (T[])Array.newInstance(type, 0);
    }

    /**
     * Create a float array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static float[] floatArray(Collection<? extends Float> col)
    {
        return (float[])uncheckedArray(col, float.class);
    }

    /**
     * Create a float array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static float[] floatArray(Float[] arr)
    {
        return (float[])uncheckedArray(arr, float.class);
    }

    /**
     * Factory method for a map that maintains insertion order.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @return The new map instance.
     */
    public static <K, V> Map<K, V> insertionOrderMap()
    {
        return new LinkedHashMap<K, V>();
    }

    /**
     * Factory method for a map that maintains insertion order.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param initialCapacity The initial capacity of the map.
     * @return The new map instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <K, V> Map<K, V> insertionOrderMap(int initialCapacity)
    {
        return new LinkedHashMap<K, V>(initialCapacity);
    }

    /**
     * Factory method for a map that maintains insertion order.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The initial contents of the map.
     * @return The new map instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <K, V> Map<K, V> insertionOrderMap(Map<? extends K, ? extends V> map)
    {
        return map == null ? null : new LinkedHashMap<K, V>(map);
    }

    /**
     * Factory method for a set that maintains insertion order.
     *
     * @param <E> The type of the elements in the set.
     * @return The new set instance.
     */
    public static <E> Set<E> insertionOrderSet()
    {
        return new LinkedHashSet<E>();
    }

    /**
     * Factory method for a set that maintains insertion order.
     *
     * @param <E> The type of the elements in the set.
     * @param set The initial contents of the set.
     * @return The new set instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <E> Set<E> insertionOrderSet(Collection<? extends E> set)
    {
        return set == null ? null : new LinkedHashSet<E>(set);
    }

    /**
     * Factory method for a set that maintains insertion order.
     *
     * @param <E> The type of the elements in the set.
     * @param initialCapacity The initial capacity of the set.
     * @return The new set instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <E> Set<E> insertionOrderSet(int initialCapacity)
    {
        return new LinkedHashSet<E>(initialCapacity);
    }

    /**
     * Get a factory that creates insertion-order sets.
     *
     * @param <T> The type of objects contained by the sets produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> SetProvider<T> insertionOrderSetFactory()
    {
        return (SetProvider<T>)INSERTION_ORDER_SET_FACTORY;
    }

    /**
     * Create a int array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static int[] intArray(Collection<? extends Integer> col)
    {
        return (int[])uncheckedArray(col, int.class);
    }

    /**
     * Create an int array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static int[] intArray(Integer[] arr)
    {
        return (int[])uncheckedArray(arr, int.class);
    }

    /**
     * Create a collection provider that will use the wrapped collection
     * provider to get the first requested collection, but then reuses that
     * collection for subsequent requests. If only empty collections are
     * requested, the empty collection from the wrapped provider will be used.
     *
     * @param <T> The type of elements in the collection.
     * @param wrapped The collection provider used to get the first collection.
     * @return The new collection provider.
     */
    public static <T> LazyCollectionProvider<T> lazyCollectionProvider(final CollectionProvider<T> wrapped)
    {
        return new LazyCollectionProvider<T>()
        {
            /** The collection once it has been retrieved. */
            private Collection<T> myCollection;

            /** Flag indicating if only empty collections have been provided. */
            private boolean myOnlyEmpty = true;

            @Override
            public synchronized Collection<T> get()
            {
                if (myCollection == null || myOnlyEmpty)
                {
                    myCollection = wrapped.get();
                    myOnlyEmpty = false;
                }
                return myCollection;
            }

            @Override
            public synchronized Collection<T> get(Collection<? extends T> initialContents)
            {
                if (myCollection == null || myOnlyEmpty)
                {
                    myCollection = wrapped.get(initialContents);
                    myOnlyEmpty = false;
                }
                else
                {
                    myCollection.addAll(initialContents);
                }
                return myCollection;
            }

            @Override
            public synchronized Collection<T> get(int size)
            {
                if (myCollection == null || myOnlyEmpty)
                {
                    myCollection = wrapped.get(size);
                    myOnlyEmpty = false;
                }
                return myCollection;
            }

            @Override
            public synchronized Collection<T> getEmpty()
            {
                if (myCollection == null)
                {
                    myCollection = wrapped.getEmpty();
                    myOnlyEmpty = true;
                }
                return myCollection;
            }

            @Override
            public Collection<? extends T> getUnmodifiable()
            {
                if (myOnlyEmpty || myCollection == null)
                {
                    return wrapped.getEmpty();
                }
                else
                {
                    return unmodifiableCollection(myCollection);
                }
            }
        };
    }

    /**
     * Factory method for a linked list.
     *
     * @param <T> The type of the objects in the list.
     * @return The new list instance.
     */
    public static <T> List<T> linkedList()
    {
        return new LinkedList<T>();
    }

    /**
     * Factory method for a linked list.
     *
     * @param <T> The type of the objects in the list.
     * @param col The initial contents of the list.
     * @return The new list instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <T> List<T> linkedList(Collection<? extends T> col)
    {
        return col == null ? null : new LinkedList<T>(col);
    }

    /**
     * Get a factory that creates linked lists.
     *
     * @param <T> The type of objects contained by the lists produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> ListProvider<T> linkedListFactory()
    {
        return (ListProvider<T>)LINKED_LIST_FACTORY;
    }

    /**
     * Factory method for a list.
     *
     * @param <T> The type of the objects in the list.
     * @return The new list instance.
     */
    public static <T> List<T> list()
    {
        return new ArrayList<T>();
    }

    /**
     * Factory method for a list.
     *
     * @param <T> The type of the objects in the list.
     * @param col The initial contents of the list.
     * @return The new list instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <T> List<T> list(Collection<? extends T> col)
    {
        return col == null ? null : new ArrayList<T>(col);
    }

    /**
     * Factory method for a list.
     *
     * @param <T> The type of the objects in the list.
     * @param initialCapacity The initial capacity for the list.
     * @return The new list instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <T> List<T> list(int initialCapacity) throws IllegalArgumentException
    {
        return new ArrayList<T>(initialCapacity);
    }

    /**
     * Factory method for a list given an single or array of values as a
     * parameter.
     *
     * @param <T> The type of the objects in the list.
     * @param values the array or values (or one value).
     * @return the list containing the values, or null if input array is null.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> list(T... values)
    {
        return values == null ? null : list(Arrays.asList(values));
    }

    /**
     * Get a factory that creates lists.
     *
     * @param <T> The type of objects contained by the lists produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> ListProvider<T> listFactory()
    {
        return (ListProvider<T>)LIST_FACTORY;
    }

    /**
     * Create a fixed-size list that contains some number of {@code null}s.
     *
     * @param <T> The type of the list.
     * @param size The number of items to put in the list.
     * @return The list.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> listOfNulls(int size)
    {
        return (List<T>)Arrays.asList(new Object[size]);
    }

    /**
     * Create a long array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static long[] longArray(Collection<? extends Long> col)
    {
        return (long[])uncheckedArray(col, long.class);
    }

    /**
     * Create a long array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static long[] longArray(Long[] arr)
    {
        return (long[])uncheckedArray(arr, long.class);
    }

    /**
     * Factory method for a map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @return The new map instance.
     */
    public static <K, V> Map<K, V> map()
    {
        return new HashMap<K, V>();
    }

    /**
     * Factory method for a map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param initialCapacity The initial capacity of the map.
     * @return The new map instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <K, V> Map<K, V> map(int initialCapacity)
    {
        return new HashMap<K, V>(initialCapacity);
    }

    /**
     * Factory method for a map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The initial contents of the map.
     * @return The new map instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <K, V> Map<K, V> map(Map<? extends K, ? extends V> map)
    {
        return map == null ? null : new HashMap<K, V>(map);
    }

    /**
     * Get a factory that creates maps.
     *
     * @param <K> The type of keys contained in the maps produced by the
     *            provider.
     * @param <V> The type of values contained in the maps produced by the
     *            provider.
     * @return The map provider.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> MapProvider<K, V> mapFactory()
    {
        return (MapProvider<K, V>)MAP_FACTORY;
    }

    /**
     * Factory method for a map whose iteration order is based on the natural
     * ordering of the keys in the map. The keys must be mutually
     * {@link Comparable}.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @return The new map instance.
     */
    public static <K, V> SortedMap<K, V> naturalOrderMap()
    {
        return new TreeMap<K, V>();
    }

    /**
     * Factory method for a map whose iteration order is based on the natural
     * ordering of the keys in the map. The keys must be mutually
     * {@link Comparable}.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param initialCapacity The initial capacity of the map.
     * @return The new map instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <K, V> SortedMap<K, V> naturalOrderMap(int initialCapacity)
    {
        return new TreeMap<K, V>();
    }

    /**
     * Factory method for a map whose iteration order is based on the natural
     * ordering of the keys in the map. The keys must be mutually
     * {@link Comparable}.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The initial contents of the map.
     * @return The new map instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <K, V> SortedMap<K, V> naturalOrderMap(Map<? extends K, ? extends V> map)
    {
        return map == null ? null : new TreeMap<K, V>(map);
    }

    /**
     * Factory method for a set whose iteration order is based on the natural
     * ordering of the elements in the set. The elements must be mutually
     * {@link Comparable}.
     *
     * @param <E> The type of the elements in the set.
     * @return The new set instance.
     */
    public static <E> SortedSet<E> naturalOrderSet()
    {
        return new TreeSet<E>();
    }

    /**
     * Factory method for a set whose iteration order is based on the natural
     * ordering of the elements in the set. The elements must be mutually
     * {@link Comparable}.
     *
     * @param <E> The type of the elements in the set.
     * @param set The initial contents of the set.
     * @return The new set instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <E> SortedSet<E> naturalOrderSet(Collection<? extends E> set)
    {
        return set == null ? null : new TreeSet<E>(set);
    }

    /**
     * Factory method for a set whose iteration order is based on the natural
     * ordering of the elements in the set. The elements must be mutually
     * {@link Comparable}.
     *
     * @param <E> The type of the elements in the set.
     * @param initialCapacity The initial capacity of the set.
     * @return The new set instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <E> SortedSet<E> naturalOrderSet(int initialCapacity)
    {
        return new TreeSet<E>();
    }

    /**
     * Get a factory that creates natural-order sets.
     *
     * @param <T> The type of objects contained by the sets produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> SetProvider<T> naturalOrderSetFactory()
    {
        return (SetProvider<T>)NATURAL_ORDER_SET_FACTORY;
    }

    /**
     * Returns a singleton list if the object is not null, otherwise returns an
     * empty list.
     *
     * @param <T> the type of the object
     * @param o the object
     * @return the list
     */
    public static <T> List<T> noNullsList(T o)
    {
        return o == null ? Collections.<T>emptyList() : Collections.singletonList(o);
    }

    /**
     * Get an {@link Iterable} that can be iterated once using the given
     * iterator.
     *
     * @param <T> The element type.
     * @param iterator The iterator.
     * @return The iterable.
     */
    public static <T> Iterable<T> oneTimeIterable(final Iterator<T> iterator)
    {
        return () -> iterator;
    }

    /**
     * Factory method for an ordered map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param comparator The comparator used to order the map.
     * @return The new map instance.
     */
    public static <K, V> SortedMap<K, V> orderedMap(Comparator<? super K> comparator)
    {
        return new TreeMap<K, V>(comparator);
    }

    /**
     * Factory method for an ordered map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param initialCapacity The initial capacity of the map.
     * @param comparator The comparator used to order the map.
     * @return The new map instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <K, V> SortedMap<K, V> orderedMap(int initialCapacity, Comparator<? super K> comparator)
    {
        return new TreeMap<K, V>(comparator);
    }

    /**
     * Factory method for an ordered map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The initial contents of the map.
     * @param comparator The comparator used to order the map.
     * @return The new map instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <K, V> SortedMap<K, V> orderedMap(Map<? extends K, ? extends V> map, Comparator<? super K> comparator)
    {
        if (map == null)
        {
            return null;
        }
        TreeMap<K, V> m = new TreeMap<>(comparator);
        m.putAll(map);
        return m;
    }

    /**
     * Factory method for an ordered map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The initial contents of the map.
     * @return The new map instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <K, V> SortedMap<K, V> orderedMap(SortedMap<K, ? extends V> map)
    {
        return map == null ? null : new TreeMap<K, V>(map);
    }

    /**
     * Factory method for an ordered set.
     *
     * @param <E> The type of the elements in the set.
     * @param set The initial contents of the set.
     * @return The new set instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <E> SortedSet<E> orderedSet(Collection<? extends E> set)
    {
        return set == null ? null : new TreeSet<>(set);
    }

    /**
     * Factory method for an ordered set.
     *
     * @param <E> The type of the elements in the set.
     * @param set The initial contents of the set.
     * @param comparator The comparator used to order the set.
     * @return The new set instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <E> SortedSet<E> orderedSet(Collection<? extends E> set, Comparator<? super E> comparator)
    {
        if (set == null)
        {
            return null;
        }
        TreeSet<E> result = new TreeSet<>(comparator);
        result.addAll(set);
        return result;
    }

    /**
     * Factory method for an ordered set.
     *
     * @param <E> The type of the elements in the set.
     * @param comparator The comparator used to order the set.
     * @return The new set instance.
     */
    public static <E> SortedSet<E> orderedSet(Comparator<? super E> comparator)
    {
        return new TreeSet<E>(comparator);
    }

    /**
     * Factory method for an ordered set.
     *
     * @param <E> The type of the elements in the set.
     * @param initialCapacity The initial capacity of the set.
     * @param comparator The comparator used to order the set.
     * @return The new set instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <E> SortedSet<E> orderedSet(int initialCapacity, Comparator<? super E> comparator)
    {
        return new TreeSet<E>(comparator);
    }

    /**
     * Factory method for an ordered set.
     *
     * @param <E> The type of the elements in the set.
     * @param set The initial contents of the set.
     * @return The new set instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <E> SortedSet<E> orderedSet(SortedSet<E> set)
    {
        return set == null ? null : new TreeSet<E>(set);
    }

    /**
     * Factory method for a queue.
     *
     * @param <T> The type of the objects in the queue.
     * @return The new queue instance.
     */
    public static <T> Queue<T> queue()
    {
        return new LinkedList<T>();
    }

    /**
     * Factory method for a queue that takes a collection of initial values.
     *
     * @param <T> The type of the objects in the queue.
     * @param col The initial contents of the queue.
     * @return The new queue instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <T> Queue<T> queue(Collection<? extends T> col)
    {
        return col == null ? null : new LinkedList<T>(col);
    }

    /**
     * Factory method for a random access list.
     *
     * @param <T> The type of the objects in the list.
     * @return The new list instance.
     */
    public static <T> List<T> randomAccessList()
    {
        return new ArrayList<T>();
    }

    /**
     * Factory method for a random access list.
     *
     * @param <T> The type of the objects in the list.
     * @param col The initial contents of the list.
     * @return The new list instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <T> List<T> randomAccessList(Collection<? extends T> col)
    {
        return col == null ? null : new ArrayList<T>(col);
    }

    /**
     * Factory method for a random access list.
     *
     * @param <T> The type of the objects in the list.
     * @param initialCapacity The initial capacity for the list.
     * @return The new list instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <T> List<T> randomAccessList(int initialCapacity) throws IllegalArgumentException
    {
        return new ArrayList<T>(initialCapacity);
    }

    /**
     * Get a factory that creates random-access lists.
     *
     * @param <T> The type of objects contained by the lists produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> ListProvider<T> randomAccessListFactory()
    {
        return (ListProvider<T>)RANDOM_ACCESS_LIST_FACTORY;
    }

    /**
     * Create an int array of length <code>to</code> - <code>from</code> that
     * contains integers in order from <code>from</code> to <code>to</code>
     * exclusive.
     *
     * @param from The first value in the return array.
     * @param to One more than the last value in the return array.
     * @return The array.
     * @throws NegativeArraySizeException If {@code from} &gt; {@code to}.
     */
    public static int[] sequentialIntArray(int from, int to) throws NegativeArraySizeException
    {
        int[] arr = new int[to - from];
        for (int index = 0; index < arr.length; ++index)
        {
            arr[index] = index + from;
        }
        return arr;
    }

    /**
     * Factory method for a set.
     *
     * @param <T> The type of the objects in the set.
     * @return The new set instance.
     */
    public static <T> Set<T> set()
    {
        return new HashSet<T>();
    }

    /**
     * Factory method for a set.
     *
     * @param <T> The type of the objects in the set.
     * @param col The initial contents of the set.
     * @return The new set instance, or {@code null} if the input is
     *         {@code null}.
     */
    public static <T> Set<T> set(Collection<? extends T> col)
    {
        return col == null ? null : new HashSet<T>(col);
    }

    /**
     * Factory method for a set.
     *
     * @param <T> The type of the objects in the set.
     * @param initialCapacity The initial capacity for the set.
     * @return The new set instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <T> Set<T> set(int initialCapacity) throws IllegalArgumentException
    {
        return new HashSet<T>(initialCapacity);
    }

    /**
     * Factory method for a set given an single or array of values as a
     * parameter.
     *
     * @param <T> The type of the objects in the set.
     * @param values the array or values (or one value).
     * @return the set containing the values, or null if input array is null.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Set<T> set(T... values)
    {
        return values == null ? null : set(Arrays.asList(values));
    }

    /**
     * Get a factory that creates sets.
     *
     * @param <T> The type of objects contained by the sets produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> SetProvider<T> setFactory()
    {
        return (SetProvider<T>)SET_FACTORY;
    }

    /**
     * Create a short array with the contents of the given collection.
     *
     * @param col The input collection.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static short[] shortArray(Collection<? extends Short> col)
    {
        return (short[])uncheckedArray(col, short.class);
    }

    /**
     * Create a short array with the contents of the given array.
     *
     * @param arr The input array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static short[] shortArray(Short[] arr)
    {
        return (short[])uncheckedArray(arr, short.class);
    }

    /**
     * Create a collection provider that always provides the given collection.
     *
     * @param <T> The type of elements in the collection.
     * @param col The single collection to be provided.
     * @return The collection provider.
     */
    public static <T> CollectionProvider<T> singletonCollectionProvider(final Collection<T> col)
    {
        return new CollectionProvider<T>()
        {
            @Override
            public Collection<T> get()
            {
                return col;
            }

            @Override
            public Collection<T> get(Collection<? extends T> initialContents)
            {
                col.addAll(initialContents);
                return col;
            }

            @Override
            public Collection<T> get(int size)
            {
                return col;
            }

            @Override
            public Collection<T> getEmpty()
            {
                return col;
            }
        };
    }

    /**
     * Create a list provider that always provides the given list.
     *
     * @param <T> The type of elements in the list.
     * @param list The single list to be provided.
     * @return The list provider.
     */
    public static <T> ListProvider<T> singletonListProvider(final List<T> list)
    {
        return new ListProvider<T>()
        {
            @Override
            public List<T> get()
            {
                return list;
            }

            @Override
            public List<T> get(Collection<? extends T> initialContents)
            {
                list.addAll(initialContents);
                return list;
            }

            @Override
            public List<T> get(int size)
            {
                return list;
            }

            @Override
            public List<T> getEmpty()
            {
                return list;
            }
        };
    }

    /**
     * Return an immutable singleton map containing the first entry in the given
     * map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map Input map containing the contents of the new map.
     * @return The new singleton map.
     * @throws NoSuchElementException If the map contains less than one entry.
     * @throws NullPointerException If the input map is {@code null}.
     */
    public static <K, V> Map<K, V> singletonMap(Map<? extends K, ? extends V> map)
    {
        Map.Entry<? extends K, ? extends V> entry = map.entrySet().iterator().next();
        return Collections.<K, V>singletonMap(entry.getKey(), entry.getValue());
    }

    /**
     * Create a set provider that always provides the given set.
     *
     * @param <T> The type of elements in the set.
     * @param set The single set to be provided.
     * @return The set provider.
     */
    public static <T> SetProvider<T> singletonSetProvider(final Set<T> set)
    {
        return new SetProvider<T>()
        {
            @Override
            public Set<T> get()
            {
                return set;
            }

            @Override
            public Set<T> get(Collection<? extends T> initialContents)
            {
                set.addAll(initialContents);
                return set;
            }

            @Override
            public Set<T> get(int size)
            {
                return set;
            }

            @Override
            public Set<T> getEmpty()
            {
                return set;
            }
        };
    }

    /**
     * Get a factory that creates synchronized lists.
     *
     * @param <T> The type of objects contained by the lists produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> ListProvider<T> synchronizedListFactory()
    {
        return (ListProvider<T>)SYNCHRONIZED_LIST_FACTORY;
    }

    /**
     * Get a factory that creates synchronized sets.
     *
     * @param <T> The type of objects contained by the sets produced by the
     *            provider.
     * @return The collection provider.
     */
    @SuppressWarnings("unchecked")
    public static <T> SetProvider<T> synchronizedSetFactory()
    {
        return (SetProvider<T>)SYNCHRONIZED_SET_FACTORY;
    }

    /**
     * Create an array with the contents of the given collection. This method
     * allows the creation of a primitive array. For {@link Object} arrays,
     * {@link #array(Collection, Class)} is preferable for better type safety.
     *
     * @param col The input collection.
     * @param type The type of object in the array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If an  object in the collection cannot be
     *             cast or un-boxed into the result array.
     */
    public static Object uncheckedArray(Collection<?> col, Class<?> type)
    {
        if (col == null)
        {
            return null;
        }
        Object arr = Array.newInstance(type, col.size());
        int index = 0;
        for (Object obj : col)
        {
            Array.set(arr, index++, obj);
        }
        return arr;
    }

    /**
     * Create an array with the contents of the given array. This method allows
     * the creation of a primitive array.
     *
     * @param arr The input array.
     * @param type The type of object in the array.
     * @return The new array instance, or {@code null} if the input is
     *         {@code null}.
     * @throws IllegalArgumentException If the input is not an array or if an
     *             object in the array cannot be cast or un-boxed into the
     *             result.
     */
    public static Object uncheckedArray(Object arr, Class<?> type)
    {
        if (arr == null)
        {
            return null;
        }
        Object result = Array.newInstance(type, Array.getLength(arr));
        for (int index = 0; index < Array.getLength(arr); ++index)
        {
            Array.set(result, index, Array.get(arr, index));
        }
        return result;
    }

    /**
     * Return a new unmodifiable collection that contains the contents of the
     * given list.
     *
     * @param <T> The type of the objects in the collection.
     * @param col Input collection containing the contents of the new
     *            collection.
     * @return The new unmodifiable collection, or {@code null} if the input is
     *         {@code null}.
     */
    @SuppressWarnings({ "unchecked" /* suppresses javac warning */, "cast" })
    public static <T> Collection<? extends T> unmodifiableCollection(Collection<? extends T> col)
    {
        return col == null ? null
                : col.getClass().getAnnotation(Immutable.class) != null ? col
                        : col.isEmpty() ? Collections.<T>emptySet()
                                : col.size() == 1
                                        ? Collections.<T>singletonList(
                                                col instanceof List ? ((List<? extends T>)col).get(0) : col.iterator().next())
                                        : Collections.unmodifiableCollection(collection(col));
    }

    /**
     * Return a new unmodifiable list that contains the contents of the given
     * collection.
     *
     * @param <T> The type of the objects in the list.
     * @param col Input collection containing the contents of the new list.
     * @return The new unmodifiable list, or {@code null} if the input is
     *         {@code null}.
     */
    @SuppressWarnings({ "unchecked" /* suppresses javac warning */, "cast" })
    public static <T> List<T> unmodifiableList(Collection<T> col)
    {
        return col == null ? null
                : col.getClass().getAnnotation(Immutable.class) != null && col instanceof List ? (List<T>)col
                        : col.isEmpty() ? Collections.<T>emptyList()
                                : col.size() == 1
                                        ? Collections.<T>singletonList(
                                                col instanceof List ? ((List<? extends T>)col).get(0) : col.iterator().next())
                                        : Collections.unmodifiableList(list(col));
    }

    /**
     * Return a new unmodifiable list that contains the contents of the given
     * array.
     *
     * @param <T> The type of the objects in the list.
     * @param arr Input array containing the contents of the new list.
     * @return The new unmodifiable list, or {@code null} if the input is
     *         {@code null}.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> unmodifiableList(T... arr)
    {
        return arr == null ? null : unmodifiableList(Arrays.asList(arr));
    }

    /**
     * Return a new unmodifiable map that contains the contents of the given
     * map. The order of the returned map is undefined.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map Input map containing the contents of the new map.
     * @return The new unmodifiable map, or {@code null} if the input is
     *         {@code null}.
     */
    public static <K, V> Map<K, V> unmodifiableMap(Map<K, V> map)
    {
        return map == null ? null : map.getClass().getAnnotation(Immutable.class) != null ? map : map.isEmpty()
                ? Collections.<K, V>emptyMap() : map.size() == 1 ? singletonMap(map) : Collections.unmodifiableMap(map(map));
    }

    /**
     * Return a new unmodifiable set that contains the contents of the given
     * collection. The order of the returned set is undefined.
     *
     * @param <T> The type of the objects in the set.
     * @param col Input collection containing the contents of the new set.
     * @return The new unmodifiable set, or {@code null} if the input is
     *         {@code null}.
     */
    @SuppressWarnings({ "unchecked" /* suppresses javac warning */, "cast" })
    public static <T> Set<? extends T> unmodifiableSet(Collection<? extends T> col)
    {
        return col == null ? null
                : col.getClass().getAnnotation(Immutable.class) != null && col instanceof Set ? (Set<? extends T>)col
                        : col.isEmpty() ? Collections.<T>emptySet()
                                : col.size() == 1
                                        ? Collections.<T>singleton(
                                                col instanceof List ? ((List<? extends T>)col).get(0) : col.iterator().next())
                                        : Collections.unmodifiableSet(set(col));
    }

    /**
     * Factory method for a map that uses weak references for the keys.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @return The new map instance.
     */
    public static <K, V> Map<K, V> weakMap()
    {
        return new WeakHashMap<K, V>();
    }

    /**
     * Factory method for a map that uses weak references for the keys.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param initialCapacity The initial capacity for the map.
     * @return The new map instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <K, V> Map<K, V> weakMap(int initialCapacity) throws IllegalArgumentException
    {
        return new WeakHashMap<K, V>(initialCapacity);
    }

    /**
     * Factory method for a map that uses weak references for the keys.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param initialContents The initial contents for the map.
     * @return The new map instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <K, V> Map<K, V> weakMap(Map<? extends K, ? extends V> initialContents)
    {
        return new WeakHashMap<K, V>(initialContents);
    }

    /**
     * Get a factory that creates weak maps.
     *
     * @param <K> The type of keys contained in the maps produced by the
     *            provider.
     * @param <V> The type of values contained in the maps produced by the
     *            provider.
     * @return The map provider.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> MapProvider<K, V> weakMapFactory()
    {
        return (MapProvider<K, V>)WEAK_MAP_FACTORY;
    }

    /**
     * Factory method for a set that uses weak references.
     *
     * @param <T> The type of the objects in the set.
     * @return The new set instance.
     */
    public static <T> Set<T> weakSet()
    {
        return new WeakHashSet<T>();
    }

    /**
     * Factory method for a set that uses weak references.
     *
     * @param <T> The type of the objects in the set.
     * @param initialCapacity The initial capacity for the set.
     * @return The new set instance.
     * @throws IllegalArgumentException If the initial capacity is negative.
     */
    public static <T> Set<T> weakSet(int initialCapacity) throws IllegalArgumentException
    {
        return new WeakHashSet<T>(initialCapacity);
    }

    /** Disallow instantiation. */
    private New()
    {
    }
}
