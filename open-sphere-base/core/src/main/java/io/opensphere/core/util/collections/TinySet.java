package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A set that is optimized to have a small memory footprint.
 *
 * @param <E> The type of objects in the set.
 */
public class TinySet<E> extends AbstractProxySet<E>
{
    /** The wrapped set. */
    private Set<E> mySet = Collections.emptySet();

    /**
     * Return a set that contains the elements in the input set as well as the
     * given element.
     *
     * @param <T> The type of the elements in the input set.
     * @param input The input set.
     * @param e The element to be added.
     * @return The result set, which may or may not be the same as the input
     *         set.
     */
    public static <T> Set<T> add(Set<T> input, T e)
    {
        if (input.isEmpty())
        {
            return Collections.<T>singleton(e);
        }
        else if (input.size() == 1)
        {
            Set<T> result = New.set(2);
            result.addAll(input);
            if (result.add(e))
            {
                return result;
            }
            return input;
        }
        else
        {
            input.add(e);
            return input;
        }
    }

    /**
     * Return a set that contains all the elements from the input collections.
     *
     * @param <T> The type of the elements in the input set.
     * @param input The input set.
     * @param c The collection of elements to be added.
     * @return The result set, which may or may not be the same as the input
     *         set.
     */
    public static <T> Set<T> addAll(Set<T> input, Collection<? extends T> c)
    {
        if (c.isEmpty())
        {
            return input;
        }
        else if (input.isEmpty())
        {
            if (c.size() == 1)
            {
                return Collections.<T>singleton(c.iterator().next());
            }
            Set<T> result = New.set(c);
            if (result.size() == 1)
            {
                result = Collections.<T>singleton(c.iterator().next());
            }
            return result;
        }
        else if (input.size() == 1)
        {
            Set<T> result = New.set(c.size() + 1);
            result.addAll(input);
            if (result.addAll(c))
            {
                return result;
            }
            return input;
        }
        else
        {
            input.addAll(c);
            return input;
        }
    }

    /**
     * Return a set that contains the elements of the input set except the given
     * object. This may be accomplished by changing the input set or returning a
     * new set.
     *
     * @param <T> The type of elements in the set.
     * @param set The input set.
     * @param o The object to exclude.
     * @return The result set, which may or may not be the same as the input
     *         set.
     */
    public static <T> Set<T> remove(Set<T> set, Object o)
    {
        if (set.isEmpty())
        {
            return set;
        }
        else if (set.size() == 1)
        {
            if (set.contains(o))
            {
                return Collections.emptySet();
            }
            return set;
        }
        else
        {
            if (set.remove(o))
            {
                return resetStorage(set);
            }
            return set;
        }
    }

    /**
     * Return a set that contains the elements of the input set except the
     * objects in the given collection. This may be accomplished by changing the
     * input set or returning a new set.
     *
     * @param <T> The type of elements in the set.
     * @param set The input set.
     * @param c The objects to exclude.
     * @return The result set, which may or may not be the same as the input
     *         set.
     */
    public static <T> Set<T> removeAll(Set<T> set, Collection<T> c)
    {
        if (set.isEmpty())
        {
            return set;
        }
        else if (set.size() == 1)
        {
            if (c.contains(set.iterator().next()))
            {
                return Collections.emptySet();
            }
            return set;
        }
        else
        {
            if (set.removeAll(c))
            {
                return resetStorage(set);
            }
            return set;
        }
    }

    /**
     * Return a set that contains the elements of the input set that are also
     * contained in the given collection. This may be accomplished by changing
     * the input set or returning a new set.
     *
     * @param <T> The type of elements in the set.
     * @param mySet The input set.
     * @param c The objects to retain.
     * @return The result set, which may or may not be the same as the input
     *         set.
     */
    public static <T> Set<T> retainAll(Set<T> mySet, Collection<?> c)
    {
        if (mySet.isEmpty())
        {
            return mySet;
        }
        else if (mySet.size() == 1)
        {
            if (c.contains(mySet.iterator().next()))
            {
                return mySet;
            }
            return Collections.emptySet();
        }
        else if (mySet.retainAll(c))
        {
            return resetStorage(mySet);
        }
        else
        {
            return mySet;
        }
    }

    /**
     * Check the current size of the input set and return a smaller object if
     * possible.
     *
     * @param <T> The type of elements in the set.
     * @param set The input set.
     * @return The result set.
     */
    private static <T> Set<T> resetStorage(Set<T> set)
    {
        if (set.isEmpty())
        {
            return Collections.emptySet();
        }
        else if (set.size() == 1)
        {
            return Collections.singleton(set.iterator().next());
        }
        else
        {
            return set;
        }
    }

    @Override
    public boolean add(E e)
    {
        if (mySet.isEmpty())
        {
            mySet = Collections.<E>singleton(e);
            return true;
        }
        else if (mySet.size() == 1)
        {
            Set<E> set = New.set(2);
            set.addAll(mySet);
            if (set.add(e))
            {
                mySet = set;
                return true;
            }
            return false;
        }
        else
        {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        if (c.isEmpty())
        {
            return false;
        }
        else if (mySet.isEmpty())
        {
            if (c.size() == 1)
            {
                mySet = Collections.<E>singleton(c.iterator().next());
            }
            else
            {
                mySet = New.set(c);
                if (mySet.size() == 1)
                {
                    mySet = Collections.<E>singleton(c.iterator().next());
                }
            }
            return true;
        }
        else if (mySet.size() == 1)
        {
            Set<E> set = New.set(c.size() + 1);
            set.addAll(mySet);
            if (set.addAll(c))
            {
                mySet = set;
                return true;
            }
            return false;
        }
        else
        {
            return super.addAll(c);
        }
    }

    @Override
    public void clear()
    {
        mySet = Collections.emptySet();
    }

    @Override
    public boolean remove(Object o)
    {
        if (isEmpty())
        {
            return false;
        }
        else if (mySet.size() == 1)
        {
            if (mySet.contains(o))
            {
                mySet = Collections.emptySet();
                return true;
            }
            return false;
        }
        else
        {
            if (super.remove(o))
            {
                resetStorage();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        if (mySet.isEmpty())
        {
            return false;
        }
        else if (mySet.size() == 1)
        {
            if (c.contains(mySet.iterator().next()))
            {
                mySet = Collections.emptySet();
                return true;
            }
            return false;
        }
        else if (super.removeAll(c))
        {
            resetStorage();
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        if (mySet.isEmpty())
        {
            return false;
        }
        else if (mySet.size() == 1)
        {
            if (c.contains(mySet.iterator().next()))
            {
                return false;
            }
            mySet = Collections.emptySet();
            return true;
        }
        else if (super.retainAll(c))
        {
            resetStorage();
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    protected Set<E> getSet()
    {
        return mySet;
    }

    /**
     * Check the current size of the wrapped set and replace it with a smaller
     * object if possible.
     */
    private void resetStorage()
    {
        mySet = resetStorage(mySet);
    }
}
