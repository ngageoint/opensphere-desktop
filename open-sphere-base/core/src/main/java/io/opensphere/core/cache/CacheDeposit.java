package io.opensphere.core.cache;

import java.util.Collection;
import java.util.Date;

import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.data.util.DataModelCategory;

/**
 * A construct that specifies sets of properties to be inserted into the cache.
 *
 * @param <S> The type of objects that contain the properties.
 *
 * @see Cache#put(CacheDeposit, CacheModificationListener)
 */
public interface CacheDeposit<S>
{
    /**
     * Special date for use as an expiration time that indicates data expires at
     * the end of the current session.
     */
    Date SESSION_END = new Date();

    /**
     * Get the accessors for the objects' properties.
     *
     * @return The property accessors.
     */
    Collection<? extends PropertyAccessor<? super S, ?>> getAccessors();

    /**
     * Get the category of the objects.
     *
     * @return The category of the objects.
     */
    DataModelCategory getCategory();

    /**
     * Get the expiration date for this deposit. Disparate expiration dates may
     * result in a separate database table for each one, so data for the same
     * {@link DataModelCategory} should have the same expiration date as much as
     * possible.
     *
     * @return The date after which the objects can be discarded. A value of
     *         {@link #SESSION_END} indicates the models should be discarded at
     *         the end of the current session.
     */
    Date getExpirationDate();

    /**
     * Get the collection of input objects.
     *
     * @return The input objects.
     */
    Iterable<? extends S> getInput();

    /**
     * Get if these models are <i>critical</i>; i.e., they should not be deleted
     * from the database by the cleanup routines.
     *
     * @return If these are critical data.
     */
    boolean isCritical();

    /**
     * Get if the models represented by this deposit are new to the database. If
     * they are new, new ids will be generated for them; otherwise the database
     * will be queried to get the existing ids. If this returns
     * <code>true</code> and the keys already exist in the database, or this
     * returns <code>false</code> and the keys do not exist in the database, an
     * exception will be generated when
     * {@link Cache#put(CacheDeposit, CacheModificationListener)} is called.
     *
     * @return If the deposit is an insert, <code>true</code>; if the deposit is
     *         an update, <code>false</code>.
     */
    boolean isNew();
}
