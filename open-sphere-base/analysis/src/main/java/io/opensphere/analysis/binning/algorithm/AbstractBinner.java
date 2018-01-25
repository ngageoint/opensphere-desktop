package io.opensphere.analysis.binning.algorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;

/**
 * Bins generic data.
 *
 * @param <T> the type of data to bin
 */
public abstract class AbstractBinner<T> implements Binner<T>
{
    /** The bins. */
    private final Map<Object, Bin<T>> myBins = New.map();

    /**
     * The list of bins sorted, this is only populated if the bin is comparable.
     */
    private final List<Bin<T>> mySortedBins = New.list();

    /** The optional listener. */
    private ListDataListener<Bin<T>> myListener;

    /** Whether to sort bins if they are comparable. */
    private boolean mySortBins;

    /** Whether to attempt to create empty bins. */
    private boolean myCreateEmptyBins;

    /** Whether to create the N/A bin. */
    private boolean myCreateNABin = true;

    /** Whether to fire events. */
    private boolean myFireEvents = true;

    /** The list of bins to be fired in an event. */
    private final Collection<Bin<T>> myPendingAddRemoveBins = New.list();

    /** The list of bins to be fired in an event. */
    private final Collection<Bin<T>> myPendingChangeBins = New.set();

    /**
     * Creates a binner that bins based on bin criteria.
     */
    public AbstractBinner()
    {
    }

    /**
     * Creates a binner with predefined bins. No new bins are created.
     *
     * @param bins the predefined bins to use
     */
    public AbstractBinner(List<Bin<T>> bins)
    {
        for (Bin<T> bin : bins)
        {
            myBins.put(bin.getValueObject(), bin);
        }
    }

    @Override
    public Bin<T> add(T data)
    {
        Bin<T> bin = findBin(data);
        if (bin == null)
        {
            bin = createBin(data);
            if (bin != null)
            {
                bin.add(data);
                addBin(bin);
            }
            // TODO handle dropping data
        }
        else
        {
            bin.add(data);
            fireChanged(bin);
        }
        return bin;
    }

    @Override
    public void addAll(Collection<? extends T> dataItems)
    {
        myFireEvents = false;
        try
        {
            for (T data : dataItems)
            {
                add(data);
            }
            firePendingEvents(true);
        }
        finally
        {
            myFireEvents = true;
        }
    }

    @Override
    public Bin<T> remove(T data)
    {
        Bin<T> bin = findBin(data);
        if (bin != null)
        {
            boolean removed = bin.remove(data);
            if (removed)
            {
                fireChanged(bin);
                if (!myCreateEmptyBins && bin.getSize() == 0)
                {
                    myBins.remove(bin.getValueObject());
                    if (mySortBins && bin instanceof Comparable)
                    {
                        int index = getIndex(bin);
                        mySortedBins.remove(index);
                    }

                    fireRemoved(bin);
                }
            }
            else
            {
                bin = null;
            }
        }

        return bin;
    }

    @Override
    public void removeAll(Collection<? extends T> dataItems)
    {
        remove(b -> b.removeAll(dataItems));
    }

    @Override
    public void removeIf(Predicate<? super T> filter)
    {
        remove(b -> b.removeIf(filter));
    }

    @Override
    public void clear()
    {
        List<Bin<T>> tmpBins = New.list(myBins.values());
        mySortedBins.clear();
        myBins.clear();

        myFireEvents = false;
        try
        {
            for (Bin<T> bin : tmpBins)
            {
                fireRemoved(bin);
            }
            firePendingEvents(false);
        }
        finally
        {
            myFireEvents = true;
        }
    }

    @Override
    public void addBin(Bin<T> bin)
    {
        if (mySortBins && bin instanceof Comparable)
        {
            int index = getIndex(bin);
            mySortedBins.add(index, bin);

            List<Bin<T>> emptyBins = myCreateEmptyBins && bin.getValueObject() != null ? createEmptyBins(index, bin)
                    : Collections.emptyList();
            if (!emptyBins.isEmpty())
            {
                for (Bin<T> emptyBin : emptyBins)
                {
                    myBins.put(emptyBin.getValueObject(), emptyBin);
                }
                int emptyIndex = index == 0 ? 1 : index;
                mySortedBins.addAll(emptyIndex, emptyBins);
                emptyBins.forEach(this::fireAdded);
            }
        }

        myBins.put(bin.getValueObject(), bin);
        fireAdded(bin);
    }

    @Override
    public void rebin()
    {
        List<T> data = myBins.values().stream().flatMap(b -> b.getData().stream()).collect(Collectors.toList());
        clear();
        // Immediately setting the bins here doesn't cause the UI to update.
        // Seems like a bug in JavaFX.
        ThreadUtilities.sleep(100);
        addAll(data);
    }

    @Override
    public List<Bin<T>> getBins()
    {
        List<Bin<T>> bins = mySortedBins;
        if (mySortedBins.isEmpty())
        {
            bins = New.list(myBins.values());
        }
        return bins;
    }

    @Override
    public Map<Object, Bin<T>> getBinsMap()
    {
        return myBins;
    }

    @Override
    public void setListener(ListDataListener<Bin<T>> listener)
    {
        myListener = listener;
    }

    /**
     * Sets the sortBins.
     *
     * @param sortBins the sortBins
     */
    public void setSortBins(boolean sortBins)
    {
        mySortBins = sortBins;
    }

    /**
     * Sets the createEmptyBins.
     *
     * @param createEmptyBins the createEmptyBins
     */
    public void setCreateEmptyBins(boolean createEmptyBins)
    {
        myCreateEmptyBins = createEmptyBins;
        if (createEmptyBins)
        {
            mySortBins = true;
        }
    }

    /**
     * Gets the createEmptyBins.
     *
     * @return the createEmptyBins
     */
    public boolean isCreateEmptyBins()
    {
        return myCreateEmptyBins;
    }

    /**
     * Sets the createNABin.
     *
     * @param createNABin the createNABin
     */
    public void setCreateNABin(boolean createNABin)
    {
        myCreateNABin = createNABin;
    }

    /**
     * Gets the createNABin.
     *
     * @return the createNABin
     */
    public boolean isCreateNABin()
    {
        return myCreateNABin;
    }

    /**
     * Creates a new bin for the given data.
     *
     * @param data the data
     * @return the new bin
     */
    protected abstract Bin<T> createBin(T data);

    /**
     * Indicates if we are auto binning or manual/custom binning.
     *
     * @return True if we are custom binning, false if we are auto binning.
     */
    protected abstract boolean isCustomBins();

    /**
     * Gets the bin value for the data.
     *
     * @param data The data to get the bin value for.
     * @return The bin value or null.
     */
    protected abstract Object getBinValue(T data);

    /**
     * Creates any necessary bins between the given bin and the adjacent one.
     *
     * @param index the index of the bin
     * @param bin the bin
     * @return the new empty bins
     */
    protected abstract List<Bin<T>> createEmptyBins(int index, Bin<T> bin);

    /**
     * Finds the bin containing the given data.
     *
     * @param data the data
     * @return the matching bin, or null if none found
     */
    private Bin<T> findBin(T data)
    {
        Bin<T> matchingBin = null;
        if (!isCustomBins())
        {
            matchingBin = myBins.get(getBinValue(data));
        }
        else
        {
            for (Bin<T> bin : myBins.values())
            {
                if (bin.accepts(data))
                {
                    matchingBin = bin;
                    break;
                }
            }
        }
        return matchingBin;
    }

    /**
     * Gets the index of this bin in the sorted bin.
     *
     * @param bin The bin to find the index for.
     * @return Either the index of the bin within the list, or the index the bin
     *         should be inserted at in order to keep it sorted.
     */
    private int getIndex(Bin<T> bin)
    {
        @SuppressWarnings("unchecked")
        Comparator<Bin<T>> comparator = (o1, o2) -> ((Comparable<Bin<T>>)o1).compareTo(o2);
        return CollectionUtilities.indexOf(bin, mySortedBins, comparator);
    }

    /**
     * Removes data from bins.
     *
     * @param remover the remover
     */
    private void remove(Predicate<Bin<T>> remover)
    {
        myFireEvents = false;
        try
        {
            for (Iterator<Bin<T>> iter = myBins.values().iterator(); iter.hasNext();)
            {
                Bin<T> bin = iter.next();
                // Perform the remove
                if (remover.test(bin))
                {
                    fireChanged(bin);
                }
                if (!myCreateEmptyBins && bin.getSize() == 0)
                {
                    iter.remove();
                    fireRemoved(bin);
                }
            }
            firePendingEvents(false);
        }
        finally
        {
            myFireEvents = true;
        }
    }

    /**
     * Fires an event for an added bin.
     *
     * @param bin the bin
     */
    private void fireAdded(Bin<T> bin)
    {
        if (myListener != null)
        {
            if (myFireEvents)
            {
                myListener.elementsAdded(new ListDataEvent<>(this, bin));
            }
            else
            {
                myPendingAddRemoveBins.add(bin);
            }
        }
    }

    /**
     * Fires an event for an changed bin.
     *
     * @param bin the bin
     */
    private void fireChanged(Bin<T> bin)
    {
        if (myListener != null)
        {
            if (myFireEvents)
            {
                myListener.elementsChanged(new ListDataEvent<>(this, bin));
            }
            else
            {
                myPendingChangeBins.add(bin);
            }
        }
    }

    /**
     * Fires an event for an removed bin.
     *
     * @param bin the bin
     */
    private void fireRemoved(Bin<T> bin)
    {
        if (myListener != null)
        {
            if (myFireEvents)
            {
                myListener.elementsRemoved(new ListDataEvent<>(this, bin));
            }
            else
            {
                myPendingAddRemoveBins.add(bin);
            }
        }
    }

    /**
     * Fires pending events.
     *
     * @param isAdd true for add, false for remove
     */
    private void firePendingEvents(boolean isAdd)
    {
        if (myListener != null)
        {
            if (isAdd && !myPendingAddRemoveBins.isEmpty())
            {
                myListener.elementsAdded(new ListDataEvent<>(this, New.list(myPendingAddRemoveBins)));
            }
            if (!myPendingChangeBins.isEmpty())
            {
                myListener.elementsChanged(new ListDataEvent<>(this, New.list(myPendingChangeBins)));
            }
            if (!isAdd && !myPendingAddRemoveBins.isEmpty())
            {
                myListener.elementsRemoved(new ListDataEvent<>(this, New.list(myPendingAddRemoveBins)));
            }

            myPendingAddRemoveBins.clear();
            myPendingChangeBins.clear();
        }
    }
}
