package io.opensphere.merge.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ModifiableObservableListBase;

/**
 * An modifiable observable list that is backed by an ArrayList.
 * 
 * @param <E> the type the list can contain
 */
public class ObservableMergeList<E> extends ModifiableObservableListBase<E>
{
    /** The ArrayList providing the internals of the ObservableList. */
    private final List<E> delegate = new ArrayList<>();

    @Override
    public E get(int index)
    {
        return delegate.get(index);
    }

    @Override
    public int size()
    {
        return delegate.size();
    }

    @Override
    protected void doAdd(int index, E element)
    {
        delegate.add(index, element);
    }

    @Override
    protected E doSet(int index, E element)
    {
        return delegate.set(index, element);
    }

    @Override
    protected E doRemove(int index)
    {
        return delegate.remove(index);
    }

}
