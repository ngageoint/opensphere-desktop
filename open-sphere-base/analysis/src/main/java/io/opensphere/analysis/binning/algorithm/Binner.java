package io.opensphere.analysis.binning.algorithm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.core.util.ListDataListener;

/**
 * Interface for a generic binner.
 *
 * @param <T> the type of data to bin
 */
public interface Binner<T>
{
    /**
     * Adds the data to the binner.
     *
     * @param data the data
     *
     * @return The bin the data was added to.
     */
    Bin<T> add(T data);

    /**
     * Adds the data to the binner.
     *
     * @param dataItems the data items
     */
    void addAll(Collection<? extends T> dataItems);

    /**
     * Removes the data from the binner.
     *
     * @param data the data
     *
     * @return The bin the data was removed from, or null if it was not in a
     *         bin.
     */
    Bin<T> remove(T data);

    /**
     * Removes the data from the binner.
     *
     * @param dataItems the data items
     */
    void removeAll(Collection<? extends T> dataItems);

    /**
     * Removes the data that match the filter.
     *
     * @param filter the filter
     */
    void removeIf(Predicate<? super T> filter);

    /**
     * Clears all the bins.
     */
    void clear();

    /**
     * Adds the a bin to the binner.
     *
     * @param bin the bin
     */
    void addBin(Bin<T> bin);

    /**
     * Rebins using the current data and criteria.
     */
    void rebin();

    /**
     * Gets the bins.
     *
     * @return the bins
     */
    List<Bin<T>> getBins();

    /**
     * Gets the bins mapped by their bin values.
     *
     * @return The bins.
     */
    Map<Object, Bin<T>> getBinsMap();

    /**
     * Sets the listener.
     *
     * @param listener the listener
     */
    void setListener(ListDataListener<Bin<T>> listener);
}
