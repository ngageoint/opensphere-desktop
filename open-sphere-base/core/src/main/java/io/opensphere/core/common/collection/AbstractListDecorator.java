package io.opensphere.core.common.collection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * This class decorates a Java <code>List</code>.
 */
@SuppressWarnings("serial")
public abstract class AbstractListDecorator<E> extends AbstractCollectionDecorator<E> implements List<E>
{
    /**
     * Creates a new instance that decorates the given <code>List</code>.
     *
     * @param list the <code>List</code> to be decorated.
     */
    public AbstractListDecorator(List<E> list)
    {
        super(list);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        return getCollection().addAll(index, c);
    }

    @Override
    public E get(int index)
    {
        return getCollection().get(index);
    }

    @Override
    public E set(int index, E element)
    {
        return getCollection().set(index, element);
    }

    @Override
    public void add(int index, E element)
    {
        getCollection().add(index, element);
    }

    @Override
    public E remove(int index)
    {
        return getCollection().remove(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return getCollection().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return getCollection().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator()
    {
        return getCollection().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index)
    {
        return getCollection().listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex)
    {
        return getCollection().subList(fromIndex, toIndex);
    }

    /**
     * Returns the decorated list.
     *
     * @return the decorated list.
     */
    @Override
    protected List<E> getCollection()
    {
        return (List<E>)super.getCollection();
    }
}
