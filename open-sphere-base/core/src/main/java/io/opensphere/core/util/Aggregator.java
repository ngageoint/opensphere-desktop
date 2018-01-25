package io.opensphere.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.concurrent.NotThreadSafe;

import io.opensphere.core.util.collections.New;

/**
 * Aggregates items.
 *
 * @param <T> the type of the items
 */
@NotThreadSafe
public class Aggregator<T>
{
    /** The size at which to process the items. */
    private final int myBatchSize;

    /** The items. */
    private final List<T> myItems;

    /** The processor. */
    private final Consumer<List<T>> myProcessor;

    /**
     * Constructor.
     *
     * @param batchSize the size at which to process the items
     * @param processor the item processor
     */
    public Aggregator(int batchSize, Consumer<List<T>> processor)
    {
        myBatchSize = batchSize;
        myItems = New.list(batchSize);
        myProcessor = processor;
    }

    /**
     * Adds an item.
     *
     * @param item the item
     */
    public void addItem(T item)
    {
        if (myBatchSize > 1)
        {
            myItems.add(item);
            if (myItems.size() >= myBatchSize)
            {
                processAll();
            }
        }
        else
        {
            myProcessor.accept(Collections.singletonList(item));
        }
    }

    /**
     * Adds items.
     *
     * @param items the items
     */
    public void addItems(Collection<? extends T> items)
    {
        if (myBatchSize > 1)
        {
            myItems.addAll(items);
            if (myItems.size() >= myBatchSize)
            {
                processAll();
            }
        }
        else
        {
            myProcessor.accept(New.list(items));
        }
    }

    /**
     * Processes all the items.
     */
    public void processAll()
    {
        if (!myItems.isEmpty())
        {
            myProcessor.accept(New.list(myItems));
            myItems.clear();
        }
    }
}
