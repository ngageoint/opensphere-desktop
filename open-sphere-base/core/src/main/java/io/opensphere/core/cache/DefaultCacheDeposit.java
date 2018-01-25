package io.opensphere.core.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Default implementation of {@link CacheDeposit}.
 *
 * @param <S> The type of objects being inserted.
 */
public class DefaultCacheDeposit<S> implements CacheDeposit<S>
{
    /** A collection of accessors to be used on the input objects. */
    private final Collection<? extends PropertyAccessor<? super S, ?>> myAccessors;

    /** The category for the objects. */
    private final DataModelCategory myCategory;

    /**
     * Indicates if these models are <i>critical</i>; i.e., they should not be
     * deleted from the database by cleanup routines.
     */
    private final boolean myCritical;

    /**
     * The expiration date of this deposit.
     */
    private final Date myExpirationDate;

    /** The objects. */
    private final Collection<? extends S> myInput;

    /** Flag indicating if these objects are new to the cache. */
    private final boolean myNewInsert;

    /**
     * Construct the cache deposit.
     *
     * @param category The category of the objects.
     * @param accessors The property accessors that define what should be stored
     *            from the objects.
     * @param input The input objects.
     * @param newInsert If the objects are new to the database,
     *            <code>true</code>.
     * @param expirationDate The date after which the objects can be discarded.
     *            A value of {@link CacheDeposit#SESSION_END} indicates the
     *            models should be discarded at the end of the current session.
     *            Disparate expiration dates may result in a separate database
     *            table for each one, so data for the same
     *            {@link DataModelCategory} should have the same expiration date
     *            as much as possible.
     * @param critical Indicates if these models are <i>critical</i>; i.e., they
     *            should not be deleted from the database by cleanup routines.
     */
    public DefaultCacheDeposit(DataModelCategory category, Collection<? extends PropertyAccessor<? super S, ?>> accessors,
            Collection<? extends S> input, boolean newInsert, Date expirationDate, boolean critical)
    {
        if (accessors == null)
        {
            myAccessors = Collections.emptySet();
        }
        else
        {
            Set<String> propertyKeys = New.set(accessors.size());
            for (PropertyAccessor<? super S, ?> accessor : accessors)
            {
                if (!propertyKeys.add(accessor.getPropertyDescriptor().getPropertyName()))
                {
                    throw new IllegalArgumentException(
                            "Cannot have more than one accessor for the same property name. Found more than once accessor for property ["
                                    + accessor.getPropertyDescriptor().getPropertyName() + "]");
                }
            }
            myAccessors = accessors;
        }
        myCategory = category;
        myInput = input;
        myNewInsert = newInsert;
        Utilities.checkNull(expirationDate, "expirationDate");
        myExpirationDate = Utilities.sameInstance(SESSION_END, expirationDate) ? SESSION_END : (Date)expirationDate.clone();
        myCritical = critical;
    }

    @Override
    public Collection<? extends PropertyAccessor<? super S, ?>> getAccessors()
    {
        return myAccessors;
    }

    @Override
    public DataModelCategory getCategory()
    {
        return myCategory;
    }

    @Override
    public Date getExpirationDate()
    {
        return myExpirationDate == null || Utilities.sameInstance(myExpirationDate, SESSION_END) ? myExpirationDate
                : (Date)myExpirationDate.clone();
    }

    @Override
    public Collection<? extends S> getInput()
    {
        return myInput;
    }

    @Override
    public boolean isCritical()
    {
        return myCritical;
    }

    @Override
    public boolean isNew()
    {
        return myNewInsert;
    }
}
