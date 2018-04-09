package io.opensphere.core.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;

/**
 * Aggregates items.
 *
 * @param <T> the type of the items
 */
public class TimeAggregator<T>
{
    /** The items. */
    private final List<T> myItems;

    /** The processor. */
    private final Consumer<List<T>> myProcessor;

    /** The executor. */
    private final ProcrastinatingExecutor myExecutor;

    /**
     * Constructor.
     *
     * @param processor the item processor
     * @param minDelayMilliseconds The minimum delay between when a task is
     *            submitted and when it is executed.
     * @param maxDelayMilliseconds The (best effort) maximum delay between when
     *            one task is submitted and when the latest task is executed.
     *            This will not cause multiple tasks to be executed
     *            concurrently, regardless of how long they take.
     */
    public TimeAggregator(Consumer<List<T>> processor, int minDelayMilliseconds, int maxDelayMilliseconds)
    {
        myItems = New.list();
        myProcessor = processor;
        myExecutor = new ProcrastinatingExecutor("TimeAggregator", minDelayMilliseconds, maxDelayMilliseconds);
    }

    /**
     * Adds an item.
     *
     * @param item the item
     */
    public synchronized void addItem(T item)
    {
        myItems.add(item);
        myExecutor.execute(this::processAll);
    }

    /**
     * Adds items.
     *
     * @param items the items
     */
    public synchronized void addItems(Collection<? extends T> items)
    {
        myItems.addAll(items);
        myExecutor.execute(this::processAll);
    }

    /**
     * Processes all the items.
     */
    public synchronized void processAll()
    {
        if (!myItems.isEmpty())
        {
            myProcessor.accept(New.list(myItems));
            myItems.clear();
        }
    }
}
