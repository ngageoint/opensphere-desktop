package io.opensphere.core.util.collections;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A set that accepts objects of different concrete types and allows for quick
 * retrieval of a subset of homogeneous objects.
 *
 * @param <E> The superclass of all objects allowed in the set.
 */
@SuppressWarnings("PMD.GodClass")
public class HeterogeneousSet<E> extends AbstractSet<E>
{
    /**
     * The map of object types to lists of objects.
     */
    private final Map<Class<?>, List<? extends E>> myClassToObjectMap = new HashMap<>();

    @Override
    public synchronized boolean add(E e)
    {
        return addToClassToObjectMap(e, null);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c)
    {
        return addToClassToObjectMap(c, null);
    }

    /**
     * Add objects to this set, ordered in accordance with the given comparator.
     *
     * @param c The collection of objects.
     * @param comparator The comparator.
     * @return If the set was changed.
     */
    public synchronized boolean addAll(Collection<? extends E> c, Comparator<E> comparator)
    {
        return addToClassToObjectMap(c, comparator);
    }

    @Override
    public synchronized void clear()
    {
        synchronized (myClassToObjectMap)
        {
            myClassToObjectMap.clear();
        }
    }

    @Override
    public boolean contains(Object o)
    {
        List<? extends E> list;
        synchronized (myClassToObjectMap)
        {
            list = myClassToObjectMap.get(o.getClass());
        }
        if (list != null)
        {
            synchronized (list)
            {
                return list.contains(o);
            }
        }
        return false;
    }

    /**
     * Get all the objects in this set as a list.
     *
     * @return The objects in this set.
     */
    @SuppressWarnings("unchecked")
    public List<E> getObjectsAsList()
    {
        int size = 0;
        Collection<List<? extends E>> values = myClassToObjectMap.values();
        for (List<? extends E> list : values)
        {
            size += list.size();
        }
        Object[] arr = new Object[size];
        int pos = 0;
        for (List<? extends E> list : values)
        {
            System.arraycopy(list.toArray(), 0, arr, pos, list.size());
            pos += list.size();
        }
        return (List<E>)Arrays.asList(arr);
    }

    /**
     * Retrieve the objects in this set of an exact concrete type. Sub-types are
     * <b>not</b> returned.
     *
     * @param <T> The type of object.
     * @param cl The type of object.
     * @return A list of objects of the given type that are a part of this set.
     */
    public <T extends E> List<T> getObjectsOfClass(Class<T> cl)
    {
        List<T> result;
        synchronized (myClassToObjectMap)
        {
            List<? extends T> objs = classToObjectMapAccess(cl);
            if (objs == null)
            {
                result = Collections.emptyList();
            }
            else
            {
                synchronized (objs)
                {
                    result = new ArrayList<>(objs);
                }
            }
        }
        return result;
    }

    @Override
    public Iterator<E> iterator()
    {
        return new Itr();
    }

    @Override
    public synchronized boolean remove(Object obj)
    {
        boolean changed = false;
        synchronized (myClassToObjectMap)
        {
            Class<?> objClass = obj.getClass();
            List<? extends E> objsForClass = myClassToObjectMap.get(objClass);
            if (objsForClass != null)
            {
                synchronized (objsForClass)
                {
                    if (objsForClass.remove(obj))
                    {
                        changed = true;
                    }
                    if (objsForClass.isEmpty())
                    {
                        myClassToObjectMap.remove(objClass);
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c)
    {
        boolean changed = false;
        synchronized (myClassToObjectMap)
        {
            Class<?> objClass = null;
            List<? extends E> objsForClass = null;
            Collection<Object> objsToRemove = new ArrayList<>();
            Map<Class<?>, List<? extends E>> changedLists = new HashMap<>();
            for (Object obj : c)
            {
                if (objClass != obj.getClass())
                {
                    if (objsForClass != null)
                    {
                        synchronized (objsForClass)
                        {
                            if (CollectionUtilities.subtract(objsForClass, objsToRemove))
                            {
                                changed = true;
                            }
                        }
                    }
                    objsToRemove.clear();
                    objClass = obj.getClass();
                    objsForClass = myClassToObjectMap.get(objClass);
                    changedLists.put(objClass, objsForClass);
                }

                objsToRemove.add(obj);
            }
            if (objsForClass != null)
            {
                synchronized (objsForClass)
                {
                    if (CollectionUtilities.subtract(objsForClass, objsToRemove))
                    {
                        changed = true;
                    }
                }
            }
            // Remove empty lists.
            if (changed)
            {
                for (Entry<Class<?>, List<? extends E>> entry : changedLists.entrySet())
                {
                    Class<?> type = entry.getKey();
                    List<? extends E> list = entry.getValue();
                    if (list == null || list.isEmpty())
                    {
                        myClassToObjectMap.remove(type);
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public int size()
    {
        int size = 0;
        synchronized (myClassToObjectMap)
        {
            for (List<? extends E> col : myClassToObjectMap.values())
            {
                size += col.size();
            }
        }
        return size;
    }

    /**
     * Helper method that adds objects to the set.
     *
     * @param objs The objects being added.
     * @param comparator An optional comparator to order the objects. Order is
     *            only relevant for objects of the same concrete type.
     * @return If the set is changed.
     */
    private boolean addToClassToObjectMap(Collection<? extends E> objs, Comparator<E> comparator)
    {
        boolean changed = false;
        if (!objs.isEmpty())
        {
            Iterator<? extends E> iter = objs.iterator();
            E obj = iter.next();
            do
            {
                Class<E> objClass = getClassUnsafe(obj);
                List<E> objsForClass = getObjectsOfClassOrCreate(objClass);
                synchronized (objsForClass)
                {
                    Set<E> set = new HashSet<>(objsForClass);
                    do
                    {
                        if (set.add(obj))
                        {
                            changed = true;
                        }

                        obj = null;
                    }
                    while (iter.hasNext() && (obj = iter.next()).getClass() == objClass);

                    // If the last object is not the same type as the previous,
                    // we have gotten the last object from the iterator, but not
                    // processed it. In this case, the obj reference will not be
                    // null and the loop will be repeated to process the last
                    // object.

                    objsForClass.clear();
                    objsForClass.addAll(set);
                    if (comparator != null)
                    {
                        Collections.sort(objsForClass, comparator);
                    }
                }
            }
            while (obj != null);
        }

        return changed;
    }

    /**
     * Helper method that adds a single object to the set.
     *
     * @param <T> The type of the object being added.
     * @param obj The object being added.
     * @param comparator An optional comparator to order the objects. Order is
     *            only relevant for objects of the same concrete type.
     * @return If the set is changed.
     */
    private <T extends E> boolean addToClassToObjectMap(T obj, Comparator<E> comparator)
    {
        List<T> objsForClass = getObjectsOfClassOrCreate(getClassUnsafe(obj));
        synchronized (objsForClass)
        {
            Set<T> set = new HashSet<>(objsForClass);
            boolean changed = set.add(obj);
            if (changed)
            {
                objsForClass.clear();
                objsForClass.addAll(set);
                if (comparator != null)
                {
                    Collections.sort(objsForClass, comparator);
                }
            }
            return changed;
        }
    }

    /**
     * Helper method that squelches the unchecked warning when accessing
     * {@link #myClassToObjectMap}. This is safe because the map is populated
     * such that the objects in the list match the type of the key.
     *
     * @param <T> The class of interest.
     * @param objClass The class of interest.
     * @return The objects of the class.
     */
    @SuppressWarnings("unchecked")
    private <T extends E> List<T> classToObjectMapAccess(Class<T> objClass)
    {
        return (List<T>)myClassToObjectMap.get(objClass);
    }

    /**
     * Helper method that squelches the unchecked warning when casting the class
     * returned by {@link Class#getClass()}. This is not safe, but in this class
     * the generic type of the result of this method is never actually used.
     *
     * @param <T> The class of the object.
     * @param obj The object of interest.
     * @return The cast class of the object.
     */
    @SuppressWarnings("unchecked")
    private <T extends E> Class<T> getClassUnsafe(T obj)
    {
        return (Class<T>)obj.getClass();
    }

    /**
     * Helper method that retrieves or creates the list of objects for a
     * particular class.
     *
     * @param <T> The class of interest.
     * @param objClass The class of interest.
     * @return The list associated with the class of interest.
     */
    private <T extends E> List<T> getObjectsOfClassOrCreate(Class<T> objClass)
    {
        synchronized (myClassToObjectMap)
        {
            List<T> objsForClass = classToObjectMapAccess(objClass);
            if (objsForClass == null)
            {
                objsForClass = new ArrayList<>();
                myClassToObjectMap.put(objClass, objsForClass);
            }
            return objsForClass;
        }
    }

    /**
     * Iterator implementation.
     */
    private class Itr implements Iterator<E>
    {
        /** Iterator over the entry set of {@link #myClassToObjectMap}. */
        private final Iterator<Entry<Class<?>, List<? extends E>>> myEntryIter = myClassToObjectMap.entrySet().iterator();

        /** Iterator over the values in the current entry. */
        private Iterator<? extends E> myValueIter;

        @Override
        public boolean hasNext()
        {
            Iterator<? extends E> valueIter = getValueIter();
            return valueIter != null && valueIter.hasNext();
        }

        @Override
        public E next()
        {
            Iterator<? extends E> valueIter = getValueIter();
            return valueIter.next();
        }

        @Override
        public void remove()
        {
            if (myValueIter == null)
            {
                throw new IllegalStateException();
            }
            myValueIter.remove();
        }

        /**
         * Helper method to create an iterator over the values in the next entry
         * from the entry iterator.
         *
         * @return The next value iterator.
         */
        private Iterator<? extends E> getValueIter()
        {
            if ((myValueIter == null || !myValueIter.hasNext()) && myEntryIter.hasNext())
            {
                myValueIter = myEntryIter.next().getValue().iterator();
            }
            return myValueIter;
        }
    }
}
