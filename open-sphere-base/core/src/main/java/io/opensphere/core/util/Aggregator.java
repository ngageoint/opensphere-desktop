package io.opensphere.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import io.opensphere.core.util.collections.New;
import net.jcip.annotations.NotThreadSafe;

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
     * Helper method to process some data in batches.
     *
     * @param items the items to process
     * @param batchSize the batch size
     * @param processor the processor
     * @param <T> the items type
     */
    public static <T> void process(Collection<? extends T> items, int batchSize, Consumer<List<T>> processor)
    {
        Aggregator<T> aggregator = new Aggregator<>(batchSize, processor);
        aggregator.addItems(items);
        aggregator.processAll();
    }

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
        int totalSize = myItems.size() + items.size();
        if (totalSize < myBatchSize)
        {
            myItems.addAll(items);
        }
        else if (totalSize == myBatchSize)
        {
            myItems.addAll(items);
            processAll();
        }
        else
        {
            Iterator<? extends T> iter = items.iterator();
            while (iter.hasNext())
            {
                int countToAdd = myBatchSize - myItems.size();
                int i = 0;
                while (iter.hasNext())
                {
                    if (i++ >= countToAdd)
                    {
                        break;
                    }
                    T item = iter.next();
                    myItems.add(item);
                }
                if (myItems.size() >= myBatchSize)
                {
                    processAll();
                }
            }
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
