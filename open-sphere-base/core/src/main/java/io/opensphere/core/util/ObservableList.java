package io.opensphere.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.opensphere.core.util.collections.New;

/**
 * An observable list.
 *
 * @param <E> the type of elements in this list
 */
public class ObservableList<E> implements List<E>, Serializable
{
    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /** The change support. */
    private transient AbstractChangeSupport<ListDataListener<E>> myChangeSupport = new WeakChangeSupport<>();

    /** The delegate. */
    private final List<E> myDelegate = new ArrayList<>();

    @Override
    public boolean add(E e)
    {
        boolean result = myDelegate.add(e);
        if (result)
        {
            fireElementAdded(this, e);
        }
        return result;
    }

    @Override
    public void add(int index, E element)
    {
        myDelegate.add(index, element);
        fireElementAdded(this, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        boolean result = myDelegate.addAll(c);
        if (result)
        {
            fireElementsAdded(this, c);
        }
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        boolean result = myDelegate.addAll(index, c);
        if (result)
        {
            fireElementsAdded(this, c);
        }
        return result;
    }

    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    public void addChangeListener(ListDataListener<E> listener)
    {
        getChangeSupport().addListener(listener);
    }

    @Override
    public void clear()
    {
        List<E> elements = new ArrayList<>(myDelegate);
        myDelegate.clear();
        if (!elements.isEmpty())
        {
            fireElementsRemoved(this, elements);
        }
    }

    @Override
    public boolean contains(Object o)
    {
        return myDelegate.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return myDelegate.containsAll(c);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof List))
        {
            return false;
        }

        ListIterator<E> e1 = listIterator();
        ListIterator<?> e2 = ((List<?>)o).listIterator();
        while (e1.hasNext() && e2.hasNext())
        {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2)))
            {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    @Override
    public E get(int index)
    {
        return myDelegate.get(index);
    }

    @Override
    public int hashCode()
    {
        int hashCode = 1;
        for (E e : this)
        {
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    @Override
    public int indexOf(Object o)
    {
        return myDelegate.indexOf(o);
    }

    @Override
    public boolean isEmpty()
    {
        return myDelegate.isEmpty();
    }

    @Override
    public Iterator<E> iterator()
    {
        // TODO fire event when remove() is called on the iterator
        return myDelegate.iterator();
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return myDelegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator()
    {
        // TODO fire event when remove() is called on the iterator
        return myDelegate.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index)
    {
        // TODO fire event when remove() is called on the iterator
        return myDelegate.listIterator(index);
    }

    @Override
    public E remove(int index)
    {
        E element = myDelegate.remove(index);
        fireElementRemoved(this, element);
        return element;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o)
    {
        boolean result = myDelegate.remove(o);
        if (result)
        {
            fireElementRemoved(this, (E)o);
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("removeAll is not supported by ObservableList");
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    public void removeChangeListener(ListDataListener<E> listener)
    {
        getChangeSupport().removeListener(listener);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("retainAll is not supported by ObservableList");
    }

    @Override
    public E set(int index, E element)
    {
        E oldElement = myDelegate.set(index, element);
        fireElementChanged(this, oldElement, element);
        return oldElement;
    }

    /**
     * Replaces the element at the supplied index with the supplied element. The
     * supplied collection of elements are removed, but a single event is fired
     * with the collection of items specified as the "previous" elements in the
     * event. The replacement at the specified index will return the original
     * element. The collection is tested to determine if it contains the
     * original element, and if not, it is added to the collection.
     *
     * @param index the index at which to place the new element.
     * @param element the element to store in the list.
     * @param itemsToReplace the items to remove from the list.
     * @return true if the list was modified as a result of this operation.
     */
    public boolean replaceMultipleWithOne(int index, E element, Collection<E> itemsToReplace)
    {
        boolean modified = false;
        E oldElement = myDelegate.set(index, element);
        modified |= oldElement != null;
        modified |= myDelegate.removeAll(itemsToReplace);
        List<E> replacedItems = New.list(itemsToReplace);
        if (!replacedItems.contains(oldElement))
        {
            replacedItems.add(oldElement);
        }
        fireElementsChanged(this, replacedItems, Collections.singleton(element));
        return modified;
    }

    @Override
    public int size()
    {
        return myDelegate.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        return myDelegate.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray()
    {
        return myDelegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return myDelegate.toArray(a);
    }

    /**
     * Fires that an element was added.
     *
     * @param source the <code>ObservableList</code> that changed, typically
     *            "this"
     * @param element the element being added
     */
    protected void fireElementAdded(Object source, E element)
    {
        final ListDataEvent<E> event = new ListDataEvent<>(source, element);
        getChangeSupport().notifyListeners(listener -> listener.elementsAdded(event));
    }

    /**
     * Fires that an element was changed.
     *
     * @param source the <code>ObservableList</code> that changed, typically
     *            "this"
     * @param previous The element before the change.
     * @param element the element being changed
     */
    protected void fireElementChanged(Object source, E previous, E element)
    {
        final ListDataEvent<E> event = new ListDataEvent<>(source, previous, element);
        getChangeSupport().notifyListeners(listener -> listener.elementsChanged(event));
    }

    /**
     * Fires that an element was changed.
     *
     * @param source the <code>ObservableList</code> that changed, typically
     *            "this"
     * @param previous The element before the change.
     * @param element the element being changed
     */
    protected void fireElementsChanged(Object source, Collection<E> previous, Collection<E> element)
    {
        final ListDataEvent<E> event = new ListDataEvent<>(source, previous, element);
        getChangeSupport().notifyListeners(listener -> listener.elementsChanged(event));
    }

    /**
     * Fires that an element was removed.
     *
     * @param source the <code>ObservableList</code> that changed, typically
     *            "this"
     * @param element the element being removed
     */
    protected void fireElementRemoved(Object source, E element)
    {
        final ListDataEvent<E> event = new ListDataEvent<>(source, element);
        getChangeSupport().notifyListeners(listener -> listener.elementsRemoved(event));
    }

    /**
     * Fires that elements were added.
     *
     * @param source the <code>ObservableList</code> that changed, typically
     *            "this"
     * @param elements the elements being added
     */
    protected void fireElementsAdded(Object source, Collection<? extends E> elements)
    {
        final ListDataEvent<E> event = new ListDataEvent<>(source, elements);
        getChangeSupport().notifyListeners(listener -> listener.elementsAdded(event));
    }

    /**
     * Fires that elements were removed.
     *
     * @param source the <code>ObservableList</code> that changed, typically
     *            "this"
     * @param elements the elements being removed
     */
    protected void fireElementsRemoved(Object source, Collection<? extends E> elements)
    {
        final ListDataEvent<E> event = new ListDataEvent<>(source, elements);
        getChangeSupport().notifyListeners(listener -> listener.elementsRemoved(event));
    }

    @Override
    public String toString()
    {
        Iterator<E> it = iterator();
        if (!it.hasNext())
        {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;)
        {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
            {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

    /**
     * Gets the change support.
     *
     * @return The change support.
     */
    private AbstractChangeSupport<ListDataListener<E>> getChangeSupport()
    {
        if (myChangeSupport == null)
        {
            myChangeSupport = new WeakChangeSupport<>();
        }

        return myChangeSupport;
    }
}
