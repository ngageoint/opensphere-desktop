package io.opensphere.analysis.binning.bins;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A bin.
 *
 * @param <T> the type of the data in the bin
 */
public interface Bin<T>
{
    /**
     * Returns whether the bin accepts the given data.
     *
     * @param data the data
     * @return whether the data is accepted
     */
    boolean accepts(T data);

    /**
     * Gets the value object for this bin.
     *
     * @return the comparable
     */
    Object getValueObject();

    /**
     * Adds the data to the bin.
     *
     * @param data the data
     * @return whether the data was added
     */
    boolean add(T data);

    /**
     * Adds the data to the bin.
     *
     * @param dataItems the data items
     * @return whether the data was added
     */
    boolean addAll(Collection<? extends T> dataItems);

    /**
     * Gets the bin id.
     *
     * @return The id of the bin.
     */
    UUID getBinId();

    /**
     * Removes the data from the bin.
     *
     * @param data the data
     * @return whether the data was added
     */
    boolean remove(T data);

    /**
     * Removes the data from the bin.
     *
     * @param dataItems the data items
     * @return whether the data was added
     */
    boolean removeAll(Collection<? extends T> dataItems);

    /**
     * Removes the data that match the filter.
     *
     * @param filter the filter
     * @return whether the data was added
     */
    boolean removeIf(Predicate<? super T> filter);

    /**
     * Gets the number of elements in the bin.
     *
     * @return the number of elements in the bin
     */
    int getSize();

    /**
     * Gets the data.
     *
     * @return the data
     */
    List<T> getData();

    /**
     * Gets the underlying bin.
     *
     * @return the underlying bin
     */
    Bin<T> getBin();
}
