package io.opensphere.core.util.collections.observable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * A "mapped" observable list, in which a child list is wrapped, and each
 * element transformed to another type.
 * 
 * @param <SOURCE_TYPE> The source type of the child list.
 * @param <DESTINATION_TYPE> The destination type of the list (this list can be
 *            cast to an instance of ObservableList<DESTINATION_TYPE>).
 */
public class MappedObservableList<SOURCE_TYPE, DESTINATION_TYPE> extends TransformationList<SOURCE_TYPE, DESTINATION_TYPE>
{
    /** The function applied to the list to generate the list mapping. */
    final Function<DESTINATION_TYPE, SOURCE_TYPE> myMapper;

    /**
     * Creates a new mapped observable list, in which this list will observe the
     * wrapped list, and apply the supplied function as a map to any elements
     * within the list.
     * 
     * @param source the source list to wrap in this instance.
     * @param mapper the function to apply to the source list.
     */
    public MappedObservableList(ObservableList<? extends DESTINATION_TYPE> source, Function<DESTINATION_TYPE, SOURCE_TYPE> mapper)
    {
        super(source);
        this.myMapper = mapper;
    }

    @Override
    public int getSourceIndex(int index)
    {
        return index;
    }

    @Override
    public int getViewIndex(int index)
    {
        return index;
    }

    @Override
    public SOURCE_TYPE get(int index)
    {
        return myMapper.apply(getSource().get(index));
    }

    @Override
    public int size()
    {
        return getSource().size();
    }

    @Override
    protected void sourceChanged(Change<? extends DESTINATION_TYPE> c)
    {
        fireChange(new Change<>(this)
        {
            @Override
            public boolean wasAdded()
            {
                return c.wasAdded();
            }

            @Override
            public boolean wasRemoved()
            {
                return c.wasRemoved();
            }

            @Override
            public boolean wasReplaced()
            {
                return c.wasReplaced();
            }

            @Override
            public boolean wasUpdated()
            {
                return c.wasUpdated();
            }

            @Override
            public boolean wasPermutated()
            {
                return c.wasPermutated();
            }

            @Override
            public int getPermutation(int i)
            {
                return c.getPermutation(i);
            }

            @Override
            protected int[] getPermutation()
            {
                // This method is only called by the superclass methods
                // wasPermutated() and getPermutation(int), which are
                // both overriden by this class. There is no other way
                // this method can be called.
                throw new AssertionError("Unreachable code");
            }

            @Override
            public List<SOURCE_TYPE> getRemoved()
            {
                ArrayList<SOURCE_TYPE> res = new ArrayList<>(c.getRemovedSize());
                for (DESTINATION_TYPE e : c.getRemoved())
                {
                    res.add(myMapper.apply(e));
                }
                return res;
            }

            @Override
            public int getFrom()
            {
                return c.getFrom();
            }

            @Override
            public int getTo()
            {
                return c.getTo();
            }

            @Override
            public boolean next()
            {
                return c.next();
            }

            @Override
            public void reset()
            {
                c.reset();
            }
        });
    }
}