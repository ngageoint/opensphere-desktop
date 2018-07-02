package io.opensphere.analysis.binning.bins;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import net.jcip.annotations.NotThreadSafe;

import io.opensphere.core.util.collections.New;

/**
 * A bin.
 *
 * @param <T> the type of the data in the bin
 */
@NotThreadSafe
public abstract class AbstractBin<T> implements Bin<T>
{
    /** The data in this bin. */
    private final List<T> myData = New.list();

    /**
     * The unique bin id.
     */
    private final UUID myBinId = UUID.randomUUID();

    @Override
    public boolean add(T data)
    {
        return myData.add(data);
    }

    @Override
    public boolean addAll(Collection<? extends T> dataItems)
    {
        return myData.addAll(dataItems);
    }

    @Override
    public UUID getBinId()
    {
        return myBinId;
    }

    @Override
    public boolean remove(T data)
    {
        return myData.remove(data);
    }

    @Override
    public boolean removeAll(Collection<? extends T> dataItems)
    {
        return myData.removeAll(dataItems);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter)
    {
        return myData.removeIf(filter);
    }

    @Override
    public int getSize()
    {
        return myData.size();
    }

    @Override
    public List<T> getData()
    {
        return myData;
    }

    @Override
    public Bin<T> getBin()
    {
        return this;
    }
}
