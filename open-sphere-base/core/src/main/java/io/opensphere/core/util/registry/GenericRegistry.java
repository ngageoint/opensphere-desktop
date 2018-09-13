package io.opensphere.core.util.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.messaging.DefaultGenericPublisher;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Facility for maintaining a set of objects along with where they come from,
 * along with a subscription service for entities that want to know when objects
 * are added or removed from the registry.
 *
 * @param <E> The type of object in the registry.
 */
public class GenericRegistry<E> extends DefaultGenericPublisher<E> implements GenericSubscriber<E>
{
    /** The map of sources to sets of objects. */
    private final Map<Object, Set<E>> mySourceToObjectMap = New.weakMap();

    /**
     * Add objects to the registry.
     *
     * @param source The source object.
     * @param objs The objects being added.
     */
    public void addObjectsForSource(Object source, Collection<? extends E> objs)
    {
        if (doAddObjectsForSource(source, objs))
        {
            sendObjects(source, objs, Collections.<E>emptySet());
        }
    }

    /**
     * Get a list of all the objects in the registry.
     *
     * @return The objects.
     */
    public List<E> getObjects()
    {
        Collection<Set<E>> sets;
        int size = 0;
        synchronized (mySourceToObjectMap)
        {
            sets = new ArrayList<>(mySourceToObjectMap.size());
            for (Set<E> set : mySourceToObjectMap.values())
            {
                sets.add(set);
                size += set.size();
            }
        }

        List<E> results = new ArrayList<>(size);
        for (Set<E> set : sets)
        {
            synchronized (set)
            {
                results.addAll(set);
            }
        }
        return results;
    }

    /**
     * Get all objects of a particular class and derivative types.
     *
     * @param <T> The class.
     * @param cl The class.
     * @return The objects.
     */
    public <T extends E> Collection<T> getObjectsAssignableToClass(Class<T> cl)
    {
        Collection<Object> sources;
        synchronized (mySourceToObjectMap)
        {
            sources = new ArrayList<>(mySourceToObjectMap.keySet());
        }

        Collection<T> results = new ArrayList<>();
        for (Object source : sources)
        {
            results.addAll(getObjectsForSourceAssignableToClass(source, cl));
        }

        return results;
    }

    /**
     * Get the objects that are associated with a particular source.
     *
     * @param source The source of interest.
     * @return The objects.
     */
    public Collection<E> getObjectsForSource(Object source)
    {
        Collection<E> result;
        Set<E> set;
        System.out.println("Objects in registry for source " + source.getClass() + ": " + mySourceToObjectMap);

        synchronized (mySourceToObjectMap)
        {
            set = mySourceToObjectMap.get(source);
        }
        if (set == null)
        {
            result = Collections.emptySet();
        }
        else
        {
            synchronized (set)
            {
                result = new ArrayList<>(set);
            }
        }
        return result;
    }

    /**
     * Get the objects associated with a source of a particular concrete type.
     * <p>
     * Objects of a sub-type will <b>not</b> be returned.
     *
     * @param <T> The concrete type.
     *
     * @param source The source of interest.
     * @param type The specific type to be retrieved.
     * @return The collection of objects.
     */
    @SuppressWarnings("unchecked")
    public <T extends E> Collection<T> getObjectsForSource(Object source, Class<T> type)
    {
        Collection<T> results;
        Collection<E> objectsForSource;
        synchronized (mySourceToObjectMap)
        {
            objectsForSource = mySourceToObjectMap.get(source);
        }
        if (objectsForSource == null)
        {
            results = Collections.emptySet();
        }
        else
        {
            results = new ArrayList<>();
            synchronized (objectsForSource)
            {
                for (E obj : objectsForSource)
                {
                    if (type.isAssignableFrom(obj.getClass()))
                    {
                        results.add((T)obj);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Get the objects associated with a source that are assignable to a
     * particular class.
     *
     * @param <T> The class.
     *
     * @param source The source of interest.
     * @param type The type to be retrieved.
     * @return The collection of objects.
     */
    @SuppressWarnings("unchecked")
    public <T extends E> Collection<T> getObjectsForSourceAssignableToClass(Object source, Class<T> type)
    {
        Collection<T> results;
        Collection<E> objectsForSource;
        synchronized (mySourceToObjectMap)
        {
            objectsForSource = mySourceToObjectMap.get(source);
        }
        if (objectsForSource == null)
        {
            results = Collections.emptySet();
        }
        else
        {
            results = new ArrayList<>();
            synchronized (objectsForSource)
            {
                for (E obj : objectsForSource)
                {
                    if (type.isAssignableFrom(obj.getClass()))
                    {
                        results.add((T)obj);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Get all objects of a particular concrete class. Objects of a sub-type of
     * the class will <b>not</b> be returned.
     *
     * @param <T> The class.
     * @param cl The class.
     * @return The objects.
     */
    public <T extends E> Collection<T> getObjectsOfClass(Class<T> cl)
    {
        Collection<Object> sources;
        synchronized (mySourceToObjectMap)
        {
            sources = new ArrayList<>(mySourceToObjectMap.keySet());
        }

        Collection<T> results = new ArrayList<>();
        for (Object source : sources)
        {
            results.addAll(getObjectsForSource(source, cl));
        }

        return results;
    }

    @Override
    public void receiveObjects(Object source, Collection<? extends E> adds, Collection<? extends E> removes)
    {
        boolean removed = doRemoveObjectsForSource(source, removes);
        boolean added = doAddObjectsForSource(source, adds);
        if (added || removed)
        {
            sendObjects(source, adds, removes);
        }
    }

    /**
     * Remove some objects from the registry without knowledge of the objects'
     * sources. This forces iteration over all sources, so it should be used
     * with care.
     *
     * @param removes The objects being removed.
     */
    public void removeObjects(Collection<? extends E> removes)
    {
        Collection<Object> sources;
        synchronized (mySourceToObjectMap)
        {
            sources = new ArrayList<>(mySourceToObjectMap.keySet());
        }

        for (Object source : sources)
        {
            boolean removed = doRemoveObjectsForSource(source, removes);
            if (removed)
            {
                sendObjects(source, Collections.<E>emptySet(), removes);
            }
        }
    }

    /**
     * Remove the objects associated with a source.
     *
     * @param source The source of interest.
     * @return The removed objects.
     */
    public Collection<E> removeObjectsForSource(Object source)
    {
        Collection<E> removed = doRemoveObjectsForSource(source);
        sendObjects(source, Collections.<E>emptySet(), removed);
        return removed;
    }

    /**
     * Remove the objects associated with a source of a particular concrete
     * type. Objects of a sub-type will <b>not</b> be removed.
     *
     * @param <T> The concrete type.
     *
     * @param source The source of interest.
     * @param type The specific type to be removed.
     * @return The removed objects.
     */
    public <T extends E> Collection<T> removeObjectsForSource(Object source, Class<T> type)
    {
        Collection<T> removes = getObjectsForSource(source, type);
        if (doRemoveObjectsForSource(source, removes))
        {
            sendObjects(source, Collections.<E>emptySet(), removes);
            return removes;
        }
        return Collections.emptySet();
    }

    /**
     * Remove specific objects associated with a source.
     *
     * @param source The source of interest.
     * @param objs The objects to be removed.
     * @return If any objects were removed.
     */
    public boolean removeObjectsForSource(Object source, Collection<? extends E> objs)
    {
        if (doRemoveObjectsForSource(source, objs))
        {
            sendObjects(source, Collections.<E>emptySet(), objs);
            return true;
        }
        return false;
    }

    /**
     * Helper method that adds objects for a particular source. Duplicate
     * objects are ignored.
     *
     * @param source The source of interest.
     * @param objs The objects being added.
     * @return If any objects were added.
     */
    protected boolean doAddObjectsForSource(Object source, Collection<? extends E> objs)
    {
        if (objs.isEmpty())
        {
            return false;
        }
        else
        {
            Set<E> objsForSource = getObjectsForSourceOrCreate(source);
            boolean added;
            synchronized (objsForSource)
            {
                added = objsForSource.addAll(objs);
            }
            return added;
        }
    }

    /**
     * Helper method that removes objects associated with a particular source.
     *
     * @param source The source of interest.
     * @return The collection of objects removed.
     */
    protected Collection<E> doRemoveObjectsForSource(Object source)
    {
        Set<E> objectsForSource;
        synchronized (mySourceToObjectMap)
        {
            objectsForSource = mySourceToObjectMap.get(source);
        }

        Collection<E> removed;
        if (objectsForSource == null)
        {
            removed = Collections.emptySet();
        }
        else
        {
            synchronized (objectsForSource)
            {
                removed = new ArrayList<>(objectsForSource);
                objectsForSource.clear();
            }
        }

        return removed;
    }

    /**
     * Helper method that removes objects associated with a particular source.
     *
     * @param source The source of interest.
     * @param objs The objects being removed.
     * @return If any objects were found and removed.
     */
    protected boolean doRemoveObjectsForSource(Object source, Collection<? extends E> objs)
    {
        boolean result;

        if (objs.isEmpty())
        {
            result = false;
        }
        else
        {
            Set<E> set;
            synchronized (mySourceToObjectMap)
            {
                set = mySourceToObjectMap.get(source);
            }
            if (set == null)
            {
                result = false;
            }
            else
            {
                synchronized (set)
                {
                    if (objs.size() < 10)
                    {
                        result = set.removeAll(objs);
                    }
                    else
                    {
                        result = CollectionUtilities.subtract(set, new ArrayList<E>(objs));
                    }

                    // This does not remove the set if it is empty; this is to
                    // avoid recreating the set every time objects are exchanged
                    // for sets with single objects, and to avoid
                    // synchronization issues resulting from one thread trying
                    // to add objects as another thread is removing them.
                }
            }
        }

        return result;
    }

    /**
     * Helper method that gets the already-created set of objects for a
     * particular source or creates one if it hasn't been created already.
     *
     * @param source The source of interest.
     * @return The set of the objects.
     */
    private Set<E> getObjectsForSourceOrCreate(Object source)
    {
        synchronized (mySourceToObjectMap)
        {
            Set<E> set = mySourceToObjectMap.get(source);
            if (set == null)
            {
                set = new HashSet<>();
                mySourceToObjectMap.put(source, set);
            }
            return set;
        }
    }
}
