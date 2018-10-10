package io.opensphere.core.util.collections;

import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.Nullable;
import gnu.trove.TIntCollection;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ActuallyCloneable;

/**
 * Utilities for Java collections.
 */
@SuppressWarnings("PMD.GodClass")
public final class CollectionUtilities
{
    /**
     * Empirically determined tuning parameter that determines how large the
     * collection of objects being <i>removed</i> from an array list should be
     * before it's worth the cost to do a bulk array copy rather than using the
     * iterator.
     */
    private static final int ARRAY_LIST_THRESHOLD = 100;

    /**
     * Empirically determined tuning parameter that determines how large a
     * non-linked-list collection should be before it's worth the cost to create
     * a linked-list out of it.
     */
    private static final int LINKED_LIST_THRESHOLD1 = 2500;

    /**
     * Empirically determined tuning parameter that determines how large the
     * collection of objects being <i>removed</i> from a non-linked-list
     * collection should be before it's worth the cost to create a linked-list
     * out of the larger collection.
     */
    private static final int LINKED_LIST_THRESHOLD2 = 50;

    /**
     * Empirically determined tuning parameter that determines how large a
     * non-set collection should be before it's worth the cost to create a set
     * out of it.
     */
    private static final int SET_THRESHOLD = 50;

    /**
     * Adds the items to the collection if the equality predicate fails.
     *
     * @param <T> the type of the items
     * @param items the collection to add to
     * @param toAdd the items to possibly add
     * @param equalityPredicate the predicate to determine equality with the items
     */
    public static <T> void addIfNotContained(Collection<T> items, Collection<? extends T> toAdd,
            BiPredicate<? super T, ? super T> equalityPredicate)
    {
        for (T item : toAdd)
        {
            addIfNotContained(items, item, equalityPredicate);
        }
    }

    /**
     * Adds the item to the collection if the equality predicate fails.
     *
     * @param <T> the type of the items
     * @param items the collection
     * @param item the item to possibly add
     * @param equalityPredicate the predicate to determine equality with the items
     */
    public static <T> void addIfNotContained(Collection<T> items, T item, BiPredicate<? super T, ? super T> equalityPredicate)
    {
        if (!items.stream().anyMatch(i -> equalityPredicate.test(i, item)))
        {
            items.add(item);
        }
    }

    /**
     * Adds the item to the collection if it's not already contained in the collection.
     *
     * @param <T> the type of the items
     * @param items the collection
     * @param item the item to possibly add
     */
    public static <T> void addIfNotContained(Collection<T> items, T item)
    {
        if (!items.contains(item))
        {
            items.add(item);
        }
    }

    /**
     * Adds naturally sorted elements to a naturally sorted list. If the two
     * collections are not naturally sorted, the behavior is undefined.
     *
     * @param <T> the type of the items
     * @param list the (naturally sorted) list
     * @param added the (naturally sorted) elements to add
     */
    public static <T extends Comparable<T>> void addSorted(List<T> list, Collection<? extends T> added)
    {
        if (list.isEmpty())
        {
            list.addAll(added);
        }
        else
        {
            for (T bin : added)
            {
                int index = indexOf(bin, list);
                list.add(index, bin);
            }
        }
    }

    /**
     * Adds sorted elements to a sorted list. If the two collections are not
     * sorted by the given comparator, the behavior is undefined.
     *
     * @param <T> the type of the items
     * @param list the sorted list
     * @param added the sorted elements to add
     * @param comparator the comparator
     */
    public static <T extends Comparable<T>> void addSorted(List<T> list, Collection<? extends T> added,
            Comparator<? super T> comparator)
    {
        if (list.isEmpty())
        {
            list.addAll(added);
        }
        else
        {
            for (T bin : added)
            {
                int index = indexOf(bin, list, comparator);
                list.add(index, bin);
            }
        }
    }

    /**
     * Get a new collection that contains the contents of the input collections.
     *
     * @param <T> The type of the return collection.
     * @param provider A collection provider to create the return collection.
     * @param cols The collections to be added to the result.
     * @return The result collection.
     */
    @SafeVarargs
    public static <T> Collection<T> concat(CollectionProvider<T> provider, Iterable<? extends T>... cols)
    {
        int size = 0;
        for (Iterable<? extends T> col : cols)
        {
            size += col instanceof Collection ? ((Collection<?>)col).size() : 10;
        }

        Collection<T> result = provider.get(size);
        for (Iterable<? extends T> col : cols)
        {
            for (T value : col)
            {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Get a new collection that contains the contents of the input collections.
     *
     * @param <T> The type of the return collection.
     * @param cols The collections to be added to the result.
     * @return The result collection.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Collection<T> concat(Iterable<? extends T>... cols)
    {
        return concat(New.<T>collectionFactory(), cols);
    }

    /**
     * Get a new collection that contains the contents of the input collections.
     *
     * @param <T> The type of the return collection.
     * @param col1 The first collection to be added to the result.
     * @param col2 The second collection to be added to the result.
     * @return The result collection.
     */
    public static <T> Collection<T> concat(Iterable<? extends T> col1, Iterable<? extends T> col2)
    {
        return concat(New.<T>collectionFactory(), col1, col2);
    }

    /**
     * Get a new list that contains the contents of the input collections.
     *
     * @param <T> The type of the return collection.
     * @param provider A collection provider to create the return collection.
     * @param cols The collections to be added to the result.
     * @return The result collection.
     */
    @SafeVarargs
    public static <T> List<T> concat(ListProvider<T> provider, Collection<? extends T>... cols)
    {
        int size = 0;
        for (Collection<? extends T> col : cols)
        {
            size += col.size();
        }

        List<T> result = provider.get(size);
        for (Collection<? extends T> col : cols)
        {
            result.addAll(col);
        }
        return result;
    }

    /**
     * Get a new collection that contains the contents of the input collections.
     *
     * @param <T> The type of the return collection.
     * @param provider A collection provider to create the return collection.
     * @param cols The collections to be added to the result.
     * @return The result collection.
     */
    @SafeVarargs
    public static <T> List<T> concat(ListProvider<T> provider, Iterable<? extends T>... cols)
    {
        int size = 0;
        for (Iterable<? extends T> col : cols)
        {
            size += col instanceof Collection ? ((Collection<?>)col).size() : 10;
        }

        List<T> result = provider.get(size);
        for (Iterable<? extends T> col : cols)
        {
            for (T value : col)
            {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Get a new set that contains the contents of the input collections.
     *
     * @param <T> The type of the return collection.
     * @param provider A collection provider to create the return collection.
     * @param cols The collections to be added to the result.
     * @return The result collection.
     */
    @SafeVarargs
    public static <T> Set<T> concat(SetProvider<T> provider, Collection<? extends T>... cols)
    {
        int size = 0;
        for (Collection<? extends T> col : cols)
        {
            size += col.size();
        }

        Set<T> result = provider.get(size);
        for (Collection<? extends T> col : cols)
        {
            result.addAll(col);
        }
        return result;
    }

    /**
     * Get a new collection that contains the contents of the input collection
     * plus the input objects.
     *
     * @param <T> The type of the return collection.
     * @param provider A collection provider to create the return collection.
     * @param col The collection to be added to the result.
     * @param objs The additional objects.
     * @return The result collection.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Collection<T> concatObjs(CollectionProvider<T> provider, Iterable<? extends T> col, T... objs)
    {
        return concat(provider, col, Arrays.asList(objs));
    }

    /**
     * Get a new collection that contains the contents of the input collection
     * plus the input objects.
     *
     * @param <T> The type of the return collection.
     * @param col The collection to be added to the result.
     * @param objs The additional objects.
     * @return The result collection.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Collection<T> concatObjs(Iterable<? extends T> col, T... objs)
    {
        return concat(col, Arrays.asList(objs));
    }

    /**
     * Get a new collection that contains the contents of the input collection
     * plus the input objects.
     *
     * @param <T> The type of the return collection.
     * @param provider A collection provider to create the return collection.
     * @param col The collection to be added to the result.
     * @param objs The additional objects.
     * @return The result collection.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> concatObjs(ListProvider<T> provider, Iterable<? extends T> col, T... objs)
    {
        return concat(provider, col, Arrays.asList(objs));
    }

    /**
     * Determine if one collection contains all the elements of another
     * collection. This implementation provides a shortcut to avoid any
     * iteration or object creation if the size of the object collection is
     * larger than the subject collection.
     * <p>
     * <b>This assumes that the collections do not contain duplicate
     * objects.</b>
     *
     * @param subject The collection that must contain the object elements.
     * @param object The elements that must be contained.
     * @return {@code true} if the subject collection contains all of the object
     *         elements.
     */
    public static boolean containsAll(Collection<?> subject, Collection<?> object)
    {
        return subject.size() >= object.size() && subject.containsAll(object);
    }

    /**
     * Determine if one collection contains any of the elements of another
     * collection.
     *
     * @param subject The collection that must contain an object element.
     * @param object The elements, one of which must be contained.
     * @return {@code true} if the subject collection contains any of the object
     *         elements.
     */
    public static boolean containsAny(Collection<?> subject, Collection<?> object)
    {
        for (Object o : object)
        {
            if (subject.contains(o))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Make a deep copy of the input list, cloning each element.
     *
     * @param <T> The type of the elements.
     * @param input The input list.
     * @return The new list.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ActuallyCloneable> List<T> deepCopy(Collection<? extends T> input)
    {
        List<T> result = New.list(input.size());
        for (T obj : input)
        {
            result.add((T)obj.clone());
        }
        return result;
    }

    /**
     * Given a collection of items, determines the total set of different class
     * types in the collection and returns it.
     *
     * @param addToSet - the set to add the types to ( may be null )
     * @param items - the collection to check
     * @return the {@link Set} of {@link Class} found in the collection. This
     *         will be the addToSet with new types appended if addToSet is not
     *         null.
     */
    public static Set<Class<?>> determineClassTypesInCollection(Set<Class<?>> addToSet, Collection<?> items)
    {
        Set<Class<?>> types = addToSet;
        if (types == null)
        {
            types = New.set();
        }

        Class<?> lastClass = null;
        for (Object item : items)
        {
            Class<?> currentClass = item.getClass();
            if (!Utilities.sameInstance(lastClass, currentClass))
            {
                types.add(currentClass);
                lastClass = currentClass;
            }
        }

        return types;
    }

    /**
     * Returns a new collection of the items that are in the first collection
     * but not the second.
     *
     * @param <T> The type of the returned objects.
     * @param col1 The first collection
     * @param col2 The second collection
     * @return The items that are in the first collection but not the second
     */
    public static <T> Collection<T> difference(Collection<? extends T> col1, Collection<? extends T> col2)
    {
        Collection<T> diff;
        if (col2.isEmpty())
        {
            diff = New.list(col1);
        }
        else
        {
            diff = New.list(col1.size());
            if (!col1.isEmpty())
            {
                for (T item : col1)
                {
                    if (!col2.contains(item))
                    {
                        diff.add(item);
                    }
                }
            }
        }
        return diff;
    }

    /**
     * Filter a collection into a new collection that only contains objects that
     * are instances of the given type.
     *
     * @param <T> The type of the returned objects.
     * @param input The input collection.
     * @param type The desired result type.
     * @return The collection of objects of type T.
     */
    public static <T> Collection<T> filterDowncast(Collection<?> input, Class<T> type)
    {
        Collection<T> output = New.collection(input.size());
        filterDowncast(input, type, output);
        return output;
    }

    /**
     * Filter a collection into a new collection that only contains objects that
     * are instances of the given type.
     *
     * @param <T> The type of the returned objects.
     * @param input The input collection.
     * @param type The desired result type.
     * @param output The output collection.
     */
    public static <T> void filterDowncast(Collection<?> input, Class<T> type, Collection<? super T> output)
    {
        for (Object obj : input)
        {
            if (type.isInstance(obj))
            {
                @SuppressWarnings("unchecked")
                T cast = (T)obj;
                output.add(cast);
            }
        }
    }

    /**
     * Create an iterator that wraps a given iterator, but only returns elements
     * that are instances of the given type.
     *
     * @param <T> The type of the returned objects.
     * @param iter The input iterator.
     * @param type The desired result type.
     * @return The iterator over objects of type T.
     */
    public static <T> Iterator<T> filterDowncast(final Iterator<?> iter, final Class<T> type)
    {
        return new FilterDowncastIterator<>(type, iter);
    }

    /**
     * Iterate over an iterable to get the item at the specified index.
     *
     * @param <T> The type of objects in the collection.
     * @param col The collection.
     * @param index The index of the desired item (0-based).
     * @return The item.
     */
    public static <T> T getItem(Iterable<? extends T> col, int index)
    {
        if (col instanceof List)
        {
            List<? extends T> cast = (List<? extends T>)col;
            return cast.get(index);
        }
        int i = 0;
        for (Iterator<? extends T> iter = col.iterator(); iter.hasNext();)
        {
            T item = iter.next();
            if (i++ == index)
            {
                return item;
            }
        }
        throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for collection " + col);
    }

    /**
     * Iterate over an iterable to get the item at the specified index.
     *
     * @param <T> The type of objects in the collection.
     * @param col The collection.
     * @param index The index of the desired item (0-based).
     * @return The item, or {@code null} if the iterable does not have an item
     *         at the specified index.
     */
    public static <T> T getItemOrNull(Iterable<? extends T> col, int index)
    {
        if (col instanceof List)
        {
            List<? extends T> cast = (List<? extends T>)col;
            return cast.size() > index ? cast.get(index) : null;
        }
        int i = 0;
        for (Iterator<? extends T> iter = col.iterator(); iter.hasNext();)
        {
            T item = iter.next();
            if (i++ == index)
            {
                return item;
            }
        }
        return null;
    }

    /**
     * Get the last item in a list, or {@code null} if the list is empty.
     *
     * @param <T> The types of objects in the list.
     * @param col The list.
     * @return The last item.
     */
    @Nullable
    public static <T> T getLastItemOrNull(List<? extends T> col)
    {
        Utilities.checkNull(col, "col");
        return col.isEmpty() ? null : col.get(col.size() - 1);
    }

    /**
     * If the list contains the item, the item is returned. Otherwise the first
     * item is returned, or null if the list is empty.
     *
     * @param col The list
     * @param item the item that might be in the list
     * @param <T> the type of the items
     * @return the returned item per the description above
     */
    public static <T> T getItemOrFirst(List<T> col, T item)
    {
        T returnItem = null;
        if (col.contains(item))
        {
            returnItem = item;
        }
        else if (!col.isEmpty())
        {
            returnItem = col.get(0);
        }
        return returnItem;
    }

    /**
     * Get a list containing the contents of the input collection, either by
     * casting the collection or creating an array list.
     *
     * @param <T> The type for the collection.
     * @param col The input collection.
     * @return A list.
     */
    public static <T> List<T> getList(Collection<T> col)
    {
        if (col instanceof List)
        {
            return (List<T>)col;
        }
        return New.list(col);
    }

    /**
     * Get a set containing the contents of the input collection, either by
     * casting the collection or creating a hash set.
     *
     * @param <T> The type for the collection.
     * @param col The input collection.
     * @return A set.
     */
    public static <T> Set<T> getSet(Collection<T> col)
    {
        if (col instanceof Set)
        {
            return (Set<T>)col;
        }
        return New.set(col);
    }

    /**
     * Method that determines if a collection is not <code>null</code> and is
     * not empty.
     *
     * @param collection The collection.
     * @return If the collection has contents.
     */
    public static boolean hasContent(Collection<?> collection)
    {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Method that determines if an iterable is not <code>null</code> and is not
     * empty.
     *
     * @param iterable The iterable.
     * @return If the iterable has contents.
     */
    public static boolean hasContent(Iterable<?> iterable)
    {
        return iterable != null
                && (iterable instanceof Collection ? !((Collection<?>)iterable).isEmpty() : iterable.iterator().hasNext());
    }

    /**
     * Finds the index where the search item would go assuming the list is
     * naturally sorted.
     *
     * @param <T> the type of the elements in the list
     * @param searchItem the search item
     * @param list the list
     * @return the index
     */
    public static <T extends Comparable<T>> int indexOf(T searchItem, List<? extends T> list)
    {
        int index = Collections.binarySearch(list, searchItem);
        if (index < 0)
        {
            index = -1 * (index + 1);
        }
        return index;
    }

    /**
     * Finds the index where the search item would go assuming the list is
     * sorted by the given comparator.
     *
     * @param <T> the type of the elements in the list
     * @param searchItem the search item
     * @param list the list
     * @param comparator the comparator
     * @return the index
     */
    public static <T> int indexOf(T searchItem, List<? extends T> list, Comparator<? super T> comparator)
    {
        int index = Collections.binarySearch(list, searchItem, comparator);
        if (index < 0)
        {
            index = -1 * (index + 1);
        }
        return index;
    }

    /**
     * Finds the intersection of a set with a collection. The resultant list
     * will only contain values that occurred in both the set and collection. Or
     * empty list if nothing is found. Result list is returned in the order of
     * the collectionToIntersect.
     *
     * @param <T> the generic type of set/collection
     * @param setToIntersect the set to intersect
     * @param collectionToIntersect the collection to intersect
     * @return the intersection
     */
    public static <T> List<T> intersectionAsList(Set<T> setToIntersect, Collection<T> collectionToIntersect)
    {
        Utilities.checkNull(setToIntersect, "setToIntersect");
        Utilities.checkNull(collectionToIntersect, "collectionToIntersect");
        int maxValues = Math.max(setToIntersect.size(), collectionToIntersect.size());
        List<T> result = New.list(maxValues);
        for (T val : collectionToIntersect)
        {
            if (setToIntersect.contains(val))
            {
                result.add(val);
            }
        }

        // Trim the resultant array if we didn't use all the values.
        if (result.size() < maxValues)
        {
            result = result.isEmpty() ? Collections.<T>emptyList() : New.list(result);
        }
        return result;
    }

    /**
     * Finds the intersection of a set with a collection. The resultant set will
     * only contain values that occurred in both the set and collection. Or
     * empty set if nothing is found.
     *
     * @param <T> the generic type of set/collection
     * @param setToIntersect the set to intersect
     * @param collectionToIntersect the collection to intersect
     * @return the intersection
     */
    public static <T> Set<T> intersectionAsSet(Set<? extends T> setToIntersect, Collection<? extends T> collectionToIntersect)
    {
        Utilities.checkNull(setToIntersect, "setToIntersect");
        Utilities.checkNull(collectionToIntersect, "collectionToIntersect");
        Set<T> result = New.set();
        for (T val : collectionToIntersect)
        {
            if (setToIntersect.contains(val))
            {
                result.add(val);
            }
        }
        return result;
    }

    /**
     * Get an iterator that will iterate over the combined values of two
     * iterators.
     *
     * @param <T> The common type of the iterators.
     * @param iter1 The first iterator.
     * @param iter2 The second iterator.
     * @return The combined iterator.
     */
    public static <T> Iterator<T> iterate(final Iterator<? extends T> iter1, final Iterator<? extends T> iter2)
    {
        return new Iterator<>()
        {
            @Override
            public boolean hasNext()
            {
                return iter1.hasNext() || iter2.hasNext();
            }

            @Override
            public T next()
            {
                if (iter1.hasNext())
                {
                    return iter1.next();
                }
                return iter2.next();
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Method that creates a collection if necessary and adds an object to it,
     * then returns the collection.
     *
     * @param <T> The type of object in the collection.
     * @param obj The object to add.
     * @param col The collection if it has already been created, or
     *            <code>null</code>
     * @return Either a new collection or the collection passed in, with the
     *         object added.
     */
    public static <T> Collection<T> lazyAdd(T obj, Collection<T> col)
    {
        return lazyAdd(obj, col, New.<T>collectionFactory());
    }

    /**
     * Method that creates a collection if necessary and adds an object to it,
     * then returns the collection.
     *
     * @param <T> The type of object in the collection.
     * @param obj The object to add.
     * @param col The collection if it has already been created, or
     *            <code>null</code>
     * @param provider The collection provider.
     * @return Either a new collection or the collection passed in, with the
     *         object added.
     */
    public static <T> Collection<T> lazyAdd(T obj, Collection<T> col, CollectionProvider<T> provider)
    {
        Collection<T> result;
        if (col == null)
        {
            result = provider.get();
        }
        else
        {
            result = col;
        }
        result.add(obj);
        return result;
    }

    /**
     * Method that creates a list if necessary and adds an object to it, then
     * returns the list.
     *
     * @param <T> The type of object in the collection.
     * @param obj The object to add.
     * @param list The list if it has already been created, or <code>null</code>
     * @return Either a new list or the list passed in, with the object added.
     */
    public static <T> List<T> lazyAdd(T obj, List<T> list)
    {
        return lazyAdd(obj, list, New.<T>listFactory());
    }

    /**
     * Method that creates a list if necessary and adds an object to it, then
     * returns the list.
     *
     * @param <T> The type of object in the collection.
     * @param obj The object to add.
     * @param list The list if it has already been created, or <code>null</code>
     * @param provider The provider for the result list.
     * @return Either a new list or the list passed in, with the object added.
     */
    public static <T> List<T> lazyAdd(T obj, List<T> list, ListProvider<T> provider)
    {
        List<T> result;
        if (list == null)
        {
            result = provider.get();
        }
        else
        {
            result = list;
        }
        result.add(obj);
        return result;
    }

    /**
     * Returns a fixed-size, read-only list backed by the specified array.
     *
     * @param values The array to wrap in a {@link List} interface.
     * @return The {@link List}.
     */
    public static List<Integer> listView(int[] values)
    {
        return listView(values, Integer.class);
    }

    /**
     * Returns a fixed-size, read-only list backed by the specified array.
     *
     * @param values The array to wrap in a {@link List} interface.
     * @return The {@link List}.
     */
    public static List<Long> listView(long[] values)
    {
        return listView(values, Long.class);
    }

    /**
     * Returns a fixed-size, read-only list backed by the specified array.
     *
     * @param <T> The expected type of the objects in the list.
     * @param values The array to wrap in a {@link List} interface.
     * @param type The type of the list.
     * @return The {@link List}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    public static <T> List<T> listView(Object values, Class<T> type)
    {
        return new PrimitiveArrayListWrapper<>(values, type);
    }

    /**
     * Returns a fixed-size, read-only list backed by the specified array.
     *
     * @param <T> The type of the values in the list.
     * @param values The array to wrap in a {@link List} interface.
     * @return The {@link List}.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> listView(T... values)
    {
        return (List<T>)listView(values, values.getClass().getComponentType());
    }

    /**
     * Returns a fixed-size, read-only list backed by the specified array.
     *
     * @param values The array to wrap in a {@link List} interface.
     * @return The {@link List}.
     */
    public static List<Integer> listViewInt(int... values)
    {
        return listView(values, Integer.class);
    }

    /**
     * Returns a fixed-size, read-only list backed by the specified array.
     *
     * @param values The array to wrap in a {@link List} interface.
     * @return The {@link List}.
     */
    public static List<Long> listViewLong(long... values)
    {
        return listView(values, Long.class);
    }

    /**
     * Add an item to an object-to-object multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     * @param syncList Indicates if the lists in the map should be created using
     *            {@link Collections#synchronizedList(List)}.
     * @param <K> The key type.
     * @param <V> The value type.
     */
    public static <K, V> void multiMapAdd(Map<K, List<V>> map, K key, V value, boolean syncList)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = new ArrayList<>();
            map.put(key, syncList ? Collections.synchronizedList(list) : list);
        }
        list.add(value);
    }

    /**
     * Add a long value to a Trove object-to-long multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     * @param <K> The key type.
     */
    public static <K> void multiMapAdd(Map<K, TLongArrayList> map, K key, long value)
    {
        TLongArrayList col = map.get(key);
        if (col == null)
        {
            col = new TLongArrayList();
            map.put(key, col);
        }
        col.add(value);
    }

    /**
     * Add an item to a Trove int-to-object multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     * @param syncList Indicates if the lists in the map should be created using
     *            {@link Collections#synchronizedList(List)}.
     * @param <V> The value type.
     */
    public static <V> void multiMapAdd(TIntObjectHashMap<List<V>> map, int key, V value, boolean syncList)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = new ArrayList<>();
            map.put(key, syncList ? Collections.synchronizedList(list) : list);
        }
        list.add(value);
    }

    /**
     * Add an item to a Trove int-to-object multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     */
    public static void multiMapAdd(TIntObjectHashMap<TIntArrayList> map, int key, int value)
    {
        TIntArrayList list = map.get(key);
        if (list == null)
        {
            list = new TIntArrayList();
            map.put(key, list);
        }
        list.add(value);
    }

    /**
     * Add an item to a Trove long-to-object multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     * @param syncList Indicates if the lists in the map should be created using
     *            {@link Collections#synchronizedList(List)}.
     * @param <V> The value type.
     */
    public static <V> void multiMapAdd(TLongObjectHashMap<List<V>> map, long key, V value, boolean syncList)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = new ArrayList<>();
            map.put(key, syncList ? Collections.synchronizedList(list) : list);
        }
        list.add(value);
    }

    /**
     * Add items to an object-to-object multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values.
     * @param syncList Indicates if the lists in the map should be created using
     *            {@link Collections#synchronizedList(List)}.
     * @param <K> The key type.
     * @param <V> The value type.
     */
    public static <K, V> void multiMapAddAll(Map<K, List<V>> map, K key, Collection<? extends V> values, boolean syncList)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = new ArrayList<>();
            map.put(key, syncList ? Collections.synchronizedList(list) : list);
        }
        list.addAll(values);
    }

    /**
     * Add items to a Trove int-to-object multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values.
     * @param syncList Indicates if the lists in the map should be created using
     *            {@link Collections#synchronizedList(List)}.
     * @param <V> The value type.
     */
    public static <V> void multiMapAddAll(TIntObjectHashMap<List<V>> map, int key, Collection<? extends V> values,
            boolean syncList)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = new ArrayList<>();
            map.put(key, syncList ? Collections.synchronizedList(list) : list);
        }
        list.addAll(values);
    }

    /**
     * Add items to a Trove int-to-object multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values.
     */
    public static void multiMapAddAll(TIntObjectHashMap<TIntArrayList> map, int key, int[] values)
    {
        TIntArrayList list = map.get(key);
        if (list == null)
        {
            list = new TIntArrayList();
            map.put(key, list);
        }
        list.add(values);
    }

    /**
     * Add items to a Trove long-to-object multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values.
     * @param syncList Indicates if the lists in the map should be created using
     *            {@link Collections#synchronizedList(List)}.
     * @param <V> The value type.
     */
    public static <V> void multiMapAddAll(TLongObjectHashMap<List<V>> map, long key, Collection<? extends V> values,
            boolean syncList)
    {
        List<V> list = map.get(key);
        if (list == null)
        {
            list = new ArrayList<>();
            map.put(key, syncList ? Collections.synchronizedList(list) : list);
        }
        list.addAll(values);
    }

    /**
     * Remove an item from an object-to-object multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     *
     * @param <K> The key type.
     * @param <V> The value type.
     */
    public static <K, V> void multiMapRemove(Map<K, List<V>> map, K key, V value)
    {
        List<V> list = map.get(key);
        if (list != null)
        {
            list.remove(value);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Remove an item from a Trove int-to-object multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     *
     * @param <V> The value type.
     */
    public static <V> void multiMapRemove(TIntObjectHashMap<List<V>> map, int key, V value)
    {
        List<V> list = map.get(key);
        if (list != null)
        {
            list.remove(value);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Remove an item from a Trove int-to-int multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     */
    public static void multiMapRemove(TIntObjectHashMap<TIntArrayList> map, int key, int value)
    {
        TIntArrayList list = map.get(key);
        if (list != null)
        {
            list.remove(value);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Remove an item from a Trove long-to-object multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param value The value.
     *
     * @param <V> The value type.
     */
    public static <V> void multiMapRemove(TLongObjectHashMap<List<V>> map, long key, V value)
    {
        List<V> list = map.get(key);
        if (list != null)
        {
            list.remove(value);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Remove items from an object-to-object multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values.
     *
     * @param <K> The key type.
     * @param <V> The value type.
     */
    public static <K, V> void multiMapRemoveAll(Map<K, List<V>> map, K key, Collection<? extends V> values)
    {
        List<V> list = map.get(key);
        if (list != null)
        {
            list.removeAll(values);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Remove items from a Trove int-to-object multi-map (i.e., a map with
     * multiple values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values.
     *
     * @param <V> The value type.
     */
    public static <V> void multiMapRemoveAll(TIntObjectHashMap<List<V>> map, int key, Collection<? extends V> values)
    {
        List<V> list = map.get(key);
        if (list != null)
        {
            list.removeAll(values);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Remove items from a Trove int-to-int multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values array.
     */
    public static void multiMapRemoveAll(TIntObjectHashMap<TIntArrayList> map, int key, int[] values)
    {
        TIntArrayList list = map.get(key);
        if (list != null)
        {
            list.removeAll(values);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Remove items from a Trove int-to-int multi-map (i.e., a map with multiple
     * values per key).
     *
     * @param map The map.
     * @param key The key.
     * @param values The values.
     */
    public static void multiMapRemoveAll(TIntObjectHashMap<TIntArrayList> map, int key, TIntCollection values)
    {
        TIntArrayList list = map.get(key);
        if (list != null)
        {
            list.removeAll(values);
            if (list.isEmpty())
            {
                map.remove(key);
            }
        }
    }

    /**
     * Partitions the collection into a map based on the partitioner.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param values the collection of values
     * @param partitioner the partitioner function
     * @return the partitioned map
     */
    public static <K, V> Map<K, List<V>> partition(Collection<? extends V> values, Function<? super V, ? extends K> partitioner)
    {
        Map<K, List<V>> map = New.map();
        for (V value : values)
        {
            K key = partitioner.apply(value);
            map.computeIfAbsent(key, h -> New.list()).add(value);
        }
        return map;
    }

    /**
     * Creates a map from the collection based on the partitioner. If there are
     * multiple values for the same key, the last one wins.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param values the collection of values
     * @param partitioner the partitioner function
     * @return the partitioned map
     */
    public static <K, V> Map<K, V> map(Collection<? extends V> values, Function<? super V, ? extends K> partitioner)
    {
        Map<K, V> map = New.map();
        for (V value : values)
        {
            K key = partitioner.apply(value);
            map.put(key, value);
        }
        return map;
    }

    /**
     * Optimized version of {@link Collection#removeAll(Collection)} that checks
     * the implementation types of the input collections and changes the way the
     * removes are done to be more efficient.
     * <p>
     * Specifically, if the {@code removeFrom} collection is a {@link Set} and
     * the {@code toRemove} collection is not a {@link Set} and {@code toRemove}
     * isn't smaller than {@code removeFrom}, it will be faster to iterate over
     * the {@code toRemove} collection and remove each element individually
     * rather than using {@link AbstractSet#removeAll(Collection)} which will
     * call {@link Collection#contains(Object)} on {@code toRemove} for each
     * element of {@code removeFrom}.
     *
     * @param removeFrom The collection to remove objects from.
     * @param toRemove The objects to be removed.
     */
    public static void removeAll(Collection<?> removeFrom, Collection<?> toRemove)
    {
        if (removeFrom instanceof Set)
        {
            removeAll((Set<?>)removeFrom, toRemove);
        }
        else if (!toRemove.isEmpty())
        {
            removeFrom.removeAll(toRemove);
        }
    }

    /**
     * Special version of {@link #removeAll(Collection, Collection)} that
     * identifies the first argument as a {@link Set}.
     *
     * @param removeFrom The set to remove objects from.
     * @param toRemove The objects to be removed.
     */
    public static void removeAll(Set<?> removeFrom, Collection<?> toRemove)
    {
        if (removeFrom.size() > toRemove.size())
        {
            if (toRemove instanceof List)
            {
                for (int index = 0; index < toRemove.size();)
                {
                    removeFrom.remove(((List<?>)toRemove).get(index++));
                }
            }
            else if (!toRemove.isEmpty())
            {
                for (Iterator<?> iter = toRemove.iterator(); iter.hasNext();)
                {
                    removeFrom.remove(iter.next());
                }
            }
        }
        else
        {
            if (toRemove instanceof Set || !(toRemove instanceof List))
            {
                doRemoveAll(removeFrom, toRemove);
            }
            else
            {
                for (int index = 0; index < toRemove.size();)
                {
                    removeFrom.remove(((List<?>)toRemove).get(index++));
                }
            }
        }
    }

    /**
     * Special version of {@link #removeAll(Collection, Collection)} that
     * identifies the first argument as a {@link Set} and the second argument as
     * a {@link List}.
     *
     * @param removeFrom The set to remove objects from.
     * @param toRemove The list of objects to be removed.
     */
    public static void removeAll(Set<?> removeFrom, List<?> toRemove)
    {
        if (removeFrom.size() > toRemove.size())
        {
            for (int index = 0; index < toRemove.size();)
            {
                removeFrom.remove(((List<?>)toRemove).get(index++));
            }
        }
        else
        {
            if (toRemove instanceof Set)
            {
                doRemoveAll(removeFrom, toRemove);
            }
            else
            {
                for (int index = 0; index < toRemove.size();)
                {
                    removeFrom.remove(((List<?>)toRemove).get(index++));
                }
            }
        }
    }

    /**
     * Removes the first element in the iterable that matches the given
     * predicate.
     *
     * @param <T> the element type
     * @param iterable the iterable
     * @param predicate the predicate
     * @return the removed element or null if nothing matched the predicate
     */
    public static <T> T removeFirst(Iterable<? extends T> iterable, Predicate<? super T> predicate)
    {
        for (Iterator<? extends T> iter = iterable.iterator(); iter.hasNext();)
        {
            T elem = iter.next();
            if (predicate.test(elem))
            {
                iter.remove();
                return elem;
            }
        }
        return null;
    }

    /**
     * Sorts a copy of the given collection and returns it.
     *
     * @param <T> the type of the values in the collection
     * @param col the collection
     * @return the sorted collection
     */
    public static <T extends Comparable<? super T>> List<T> sort(Collection<? extends T> col)
    {
        List<T> groupList = new ArrayList<>(col);
        Collections.sort(groupList);
        return groupList;
    }

    /**
     * Sorts a copy of the given collection and returns it.
     *
     * @param <T> the type of the values in the collection
     * @param col the collection
     * @param comparator the comparator
     * @return the sorted collection
     */
    public static <T> List<T> sort(Collection<? extends T> col, Comparator<? super T> comparator)
    {
        List<T> groupList = new ArrayList<>(col);
        Collections.sort(groupList, comparator);
        return groupList;
    }

    /**
     * Remove the objects that exist in both collections from both collections.
     * This attempts various optimizations to handle large collections. This
     * assumes that the objects in each collection are unique.
     *
     * @param col1 The first collection.
     * @param col2 The second collection.
     * @return <code>true</code> if the collections were changed.
     */
    @SuppressWarnings("unchecked")
    public static boolean subtract(Collection<?> col1, Collection<?> col2)
    {
        boolean changed = false;

        // Try assuming that the collections have objects in the same order.
        Iterator<?> iter1 = col1.iterator();
        Iterator<?> iter2 = col2.iterator();
        int removeCount1 = 0;
        int removeCount2 = 0;
        if (col1 instanceof HashSet<?>)
        {
            if (col2 instanceof HashSet<?>)
            {
                subtractSequential(iter1, iter2, iter1, iter2);
            }
            else
            {
                removeCount2 = subtractSequential(iter1, iter2, iter1);
            }
        }
        else if (col2 instanceof HashSet<?>)
        {
            removeCount1 = subtractSequential(iter1, iter2, iter2);
        }
        else
        {
            removeCount1 = subtractSequential(iter1, iter2);
            removeCount2 = removeCount1;
        }
        if (removeCount1 > 0)
        {
            if (removeCount1 == col1.size())
            {
                col1.clear();
            }
            else
            {
                if (col1 instanceof ArrayList<?> && removeCount1 > ARRAY_LIST_THRESHOLD)
                {
                    removeCount((ArrayList<Object>)col1, removeCount1);
                }
                else
                {
                    removeCount(col1, removeCount1);
                }
            }
        }
        if (removeCount2 > 0)
        {
            if (removeCount2 == col2.size())
            {
                col2.clear();
            }
            else
            {
                if (col2 instanceof ArrayList<?> && removeCount2 > ARRAY_LIST_THRESHOLD)
                {
                    removeCount((ArrayList<Object>)col2, removeCount2);
                }
                else
                {
                    removeCount(col2, removeCount2);
                }
            }
            changed = true;
        }
        if (!col1.isEmpty() && !col2.isEmpty())
        {
            changed |= subtractRandom(col1, col2);
        }
        return changed;
    }

    /**
     * Create an array of ints from a collection of {@link Number}s.
     *
     * @param col The collection of {@link Number}s.
     * @return The array of ints.
     */
    public static int[] toIntArray(Collection<? extends Number> col)
    {
        int[] result = new int[col.size()];
        int index = 0;
        for (Number val : col)
        {
            result[index++] = val.intValue();
        }
        return result;
    }

    /**
     * Create an array of longs from a collection of {@link Number}s.
     *
     * @param col The collection of {@link Number}s.
     * @return The array of longs.
     */
    public static long[] toLongArray(Collection<? extends Number> col)
    {
        long[] result = new long[col.size()];
        int index = 0;
        for (Number val : col)
        {
            result[index++] = val.longValue();
        }
        return result;
    }

    /**
     * Get a {@link Set} view of a map's keys. Unlike {@link Map#keySet()}, the
     * returned set passes all set operations through to the map.
     *
     * @param <K> The type of the keys in the map.
     * @param <V> The type of the values in the map.
     * @param map The input map.
     * @param value The value to use for add operations on the set. This may be
     *            {@code null} if the map supports {@code null} values.
     * @return The set view.
     */
    public static <K, V> Set<K> toSetView(Map<K, V> map, V value)
    {
        return new WrappedMapSet<>(map, value);
    }

    /**
     * Reduce a collection's memory footprint as much as possible.
     *
     * @param col The collection.
     */
    public static void trimToSize(Collection<?> col)
    {
        if (col instanceof ArrayList)
        {
            ((ArrayList<?>)col).trimToSize();
        }
    }

    /**
     * Get an unmodifiable view of the given collection, similar to
     * {@link Collections#unmodifiableCollection(Collection)}, except this will
     * check if the input collection is a {@link Set} or a {@link List} and
     * create the appropriate wrapper that implements that type.
     *
     * @param <T> The type of objects in the collection.
     * @param col The collection to be wrapped.
     * @return The view.
     */
    public static <T> Collection<? extends T> unmodifiableCollection(Collection<? extends T> col)
    {
        return col instanceof List ? Collections.unmodifiableList((List<? extends T>)col) : col instanceof Set
                ? Collections.unmodifiableSet((Set<? extends T>)col) : Collections.unmodifiableCollection(col);
    }

    /**
     * Remove the objects from <b>to</b> that are not in <b>from</b>. In other
     * words, manipulate <b>to</b> to look like <b>from</b>.
     *
     * @param to The collection being changed.
     * @param from The collection to mimic.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void copyProxyBack(Collection<?> to, Collection from)
    {
        Collection smallerRemoveProxy;
        if (to instanceof ArrayList && to.size() >= LINKED_LIST_THRESHOLD1)
        {
            smallerRemoveProxy = New.linkedList(to);
        }
        else
        {
            smallerRemoveProxy = to;
        }
        for (Iterator iter = smallerRemoveProxy.iterator(); iter.hasNext();)
        {
            Object object = iter.next();
            if (!from.contains(object))
            {
                iter.remove();
            }
        }
        if (!Utilities.sameInstance(smallerRemoveProxy, to))
        {
            to.clear();
            if (!smallerRemoveProxy.isEmpty())
            {
                to.addAll(smallerRemoveProxy);
            }
        }
    }

    /**
     * Remove the objects in {@code toRemove} from {@code removeFrom} assuming
     * that {@code removeFrom} is not larger than {@code toRemove}.
     *
     * @param removeFrom The set to remove objects from.
     * @param toRemove The objects to be removed.
     */
    private static void doRemoveAll(Set<?> removeFrom, Collection<?> toRemove)
    {
        if (!removeFrom.isEmpty())
        {
            for (Iterator<?> iter = removeFrom.iterator(); iter.hasNext();)
            {
                if (toRemove.contains(iter.next()))
                {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Remove a number of sequential objects from the beginning of an array list
     * using a bulk array copy.
     *
     * @param list The list from which to remove the objects.
     * @param count The number of objects to remove.
     */
    @SuppressWarnings("PMD.LooseCoupling")
    private static void removeCount(ArrayList<Object> list, int count)
    {
        Object[] arr = list.toArray();
        Object[] range = Arrays.copyOfRange(arr, count, arr.length);
        list.clear();
        list.addAll(Arrays.asList(range));
    }

    /**
     * Remove a number of sequential objects from the beginning of a collection.
     *
     * @param col The collection from which to remove the objects.
     * @param count The number of objects to remove.
     */
    private static void removeCount(Collection<?> col, int count)
    {
        Iterator<?> iter = col.iterator();
        for (int i = 0; i < count; i++)
        {
            iter.next();
            iter.remove();
        }
    }

    /**
     * Remove the common portions of two collections, assuming random access is
     * required.
     *
     * @param col1 The first collection.
     * @param col2 The second collection.
     * @return <code>true</code> if the collections were changed.
     */
    private static boolean subtractRandom(Collection<?> col1, Collection<?> col2)
    {
        boolean changed = false;
        Collection<?> bigger;
        Collection<?> smaller;
        if (col1.size() > col2.size())
        {
            bigger = col1;
            smaller = col2;
        }
        else
        {
            bigger = col2;
            smaller = col1;
        }

        // Figure out which way the removes will be fastest.
        if (bigger instanceof Set<?>)
        {
            if (smaller instanceof Set<?>)
            {
                changed |= subtractRandom((Set<?>)bigger, (Set<?>)smaller);
            }
            else
            {
                changed |= subtractRandom((Set<?>)bigger, smaller);
            }
        }
        else
        {
            changed |= subtractRandomNonSet(bigger, smaller);
        }
        return changed;
    }

    /**
     * Remove the common portions of a set and a collection, assuming random
     * access is required.
     *
     * @param bigger The bigger set.
     * @param smaller The smaller collection.
     * @return <code>true</code> if the collections were changed.
     */
    @SuppressWarnings("unchecked")
    private static boolean subtractRandom(Set<?> bigger, Collection<?> smaller)
    {
        boolean changed = false;
        @SuppressWarnings("rawtypes")
        Collection smaller2;
        if (smaller instanceof AbstractSequentialList<?> || smaller.size() < LINKED_LIST_THRESHOLD1)
        {
            smaller2 = smaller;
        }
        else
        {
            smaller2 = New.linkedList(smaller);
        }
        Iterator<?> e = smaller2.iterator();
        while (e.hasNext())
        {
            if (bigger.remove(e.next()))
            {
                e.remove();
                changed = true;
            }
        }
        if (Utilities.notSameInstance(smaller2, smaller) && changed)
        {
            smaller.clear();
            if (!smaller2.isEmpty())
            {
                smaller.addAll(smaller2);
            }
        }
        return changed;
    }

    /**
     * Remove the common portions of two sets, assuming random access is
     * required.
     *
     * @param bigger The bigger set.
     * @param smaller The smaller set.
     * @return <code>true</code> if the collections were changed.
     */
    private static boolean subtractRandom(Set<?> bigger, Set<?> smaller)
    {
        boolean changed = false;
        for (Iterator<?> i = smaller.iterator(); i.hasNext();)
        {
            if (bigger.remove(i.next()))
            {
                i.remove();
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Remove the common portions of two collections, assuming random access is
     * required, and that the bigger collection is not a set.
     *
     * @param bigger The bigger collection.
     * @param smaller The smaller collection.
     * @return <code>true</code> if the collections were changed.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean subtractRandomNonSet(Collection<?> bigger, Collection<?> smaller)
    {
        boolean changed = false;
        Collection biggerProxy;
        if (bigger instanceof AbstractSequentialList<?> || bigger.size() < LINKED_LIST_THRESHOLD1
                || smaller.size() < LINKED_LIST_THRESHOLD2)
        {
            biggerProxy = bigger;
        }
        else
        {
            biggerProxy = New.linkedList(bigger);
        }
        Collection smallerSetProxy;
        if (smaller instanceof Set<?> || smaller.size() < SET_THRESHOLD)
        {
            smallerSetProxy = smaller;
        }
        else
        {
            smallerSetProxy = New.set(smaller);
        }
        for (Iterator<?> i = biggerProxy.iterator(); i.hasNext();)
        {
            if (smallerSetProxy.remove(i.next()))
            {
                i.remove();
                changed = true;
                if (smallerSetProxy.isEmpty())
                {
                    break;
                }
            }
        }
        if (!Utilities.sameInstance(smallerSetProxy, smaller) && changed)
        {
            copyProxyBack(smaller, smallerSetProxy);
        }
        if (!Utilities.sameInstance(biggerProxy, bigger) && changed)
        {
            bigger.clear();
            if (!biggerProxy.isEmpty())
            {
                bigger.addAll(biggerProxy);
            }
        }
        return changed;
    }

    /**
     * Iterate through two collections, finding all the objects that occur in
     * both collections. If remove iterators are provided, call remove on each
     * one for each matching object.
     *
     * @param iter1 The iterator over the set.
     * @param iter2 The iterator over the non-set.
     * @param removeIters Optional iterators on which to call
     *            {@link Iterator#remove()}.
     * @return The number of objects that can be removed from the non-set (but
     *         have not been removed by this method).
     */
    private static int subtractSequential(Iterator<?> iter1, Iterator<?> iter2, Iterator<?>... removeIters)
    {
        int removeCount = 0;
        while (iter1.hasNext() && iter2.hasNext())
        {
            Object obj1 = iter1.next();
            Object obj2 = iter2.next();
            if (obj1.equals(obj2))
            {
                for (Iterator<?> removeIter : removeIters)
                {
                    removeIter.remove();
                }
                removeCount++;
            }
            else
            {
                // Give up.
                break;
            }
        }
        return removeCount;
    }

    /** Disallow construction. */
    private CollectionUtilities()
    {
    }

    /**
     * An iterator that filters the results from a nested iterator so it only
     * returns elements that are instances of a particular type.
     *
     * @param <T> The type returned by the iterator.
     */
    private static final class FilterDowncastIterator<T> extends PredicateIterator<Object, T>
    {
        /**
         * Constructor.
         *
         * @param type The type to return from this iterator.
         * @param iter The wrapped iterator.
         */
        public FilterDowncastIterator(final Class<T> type, Iterator<?> iter)
        {
            super(iter, input -> type.isInstance(input));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected T convert(Object obj)
        {
            return (T)obj;
        }
    }
}
