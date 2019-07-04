package io.opensphere.core.units;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ImpossibleException;

/**
 * Common functionality for {@link UnitsProvider}s.
 *
 * @param <T> The supertype of the unit types in this provider.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractUnitsProvider<T> implements UnitsProvider<T>
{
    /** The collection of available units. */
    private final List<Class<? extends T>> myAvailableUnits = New.list();

    /** Change support for when the units change. */
    private final ChangeSupport<UnitsChangeListener<T>> myChangeSupport = new WeakChangeSupport<>();

    /** Lock used to synchronize changes to the units. */
    private final Lock myLock = new ReentrantLock();

    /**
     * The preferences, which may be {@code null}. This may be used to persist
     * the preferred units.
     */
    private volatile Preferences myPreferences;

    /** The preferences listener. */
    private final PreferenceChangeListener myPreferencesListener = evt ->
    {
        if (evt.getSource() != AbstractUnitsProvider.this)
        {
            String className = evt.getValueAsString(null);
            if (className != null)
            {
                setPreferredUnits(className);
            }
        }
    };

    /** The preferred units. */
    private final AtomicReference<Class<? extends T>> myPreferredUnits = new AtomicReference<>();

    /** The key for the preferences. */
    private final String myPrefsKey = getClass().getSimpleName() + ".preferredUnits";

    /** The previously preferred units. */
    private final AtomicReference<Class<? extends T>> myPrevPreferredUnits = new AtomicReference<>();

    @Override
    public void addListener(UnitsChangeListener<T> listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void addUnits(Class<? extends T> type) throws InvalidUnitsException
    {
        testUnits(type);

        final Collection<Class<? extends T>> newUnits;
        myLock.lock();
        try
        {
            boolean changed = false;
            if (!myAvailableUnits.contains(type))
            {
                myAvailableUnits.add(type);
                changed = true;
            }
            if (changed)
            {
                newUnits = getAvailableUnits(true);
            }
            else
            {
                newUnits = null;
            }
        }
        finally
        {
            myLock.unlock();
        }
        if (newUnits != null)
        {
            notifyChanges(newUnits);
        }
    }

    @Override
    public T fromLongLabelString(String label) throws UnitsParseException
    {
        return UnitsUtilities.createFromLongLabelString(this, getValueType(), label);
    }

    @Override
    public T fromMagnitudeAndLongLabel(Number magnitude, String longLabel) throws InvalidUnitsException, UnitsParseException
    {
        Class<? extends T> type = getUnitsWithLongLabel(longLabel);
        if (type == null)
        {
            throw new UnitsParseException("Failed to find appropriate units for string: " + longLabel);
        }
        return fromUnitsAndMagnitude(type, magnitude);
    }

    @Override
    public T fromMagnitudeAndSelectionLabel(Number magnitude, String selectionLabel)
        throws InvalidUnitsException, UnitsParseException
    {
        Class<? extends T> type = getUnitsWithSelectionLabel(selectionLabel);
        if (type == null)
        {
            throw new UnitsParseException("Failed to find appropriate units for string: " + selectionLabel);
        }
        return fromUnitsAndMagnitude(type, magnitude);
    }

    @Override
    public T fromMagnitudeAndShortLabel(Number magnitude, String shortLabel) throws InvalidUnitsException, UnitsParseException
    {
        Class<? extends T> type = getUnitsWithLongLabel(shortLabel);
        if (type == null)
        {
            throw new UnitsParseException("Failed to find appropriate units for string: " + shortLabel);
        }
        return fromUnitsAndMagnitude(type, magnitude);
    }

    @Override
    public T fromShortLabelString(String label) throws UnitsParseException
    {
        return UnitsUtilities.createFromShortLabelString(this, getValueType(), label);
    }

    @Override
    public Collection<Class<? extends T>> getAvailableUnits(boolean allowAutoscale)
    {
        myLock.lock();
        try
        {
            Collection<Class<? extends T>> result = New.collection(myAvailableUnits.size());
            for (Class<? extends T> type : myAvailableUnits)
            {
                if (allowAutoscale || !isAutoscale(type))
                {
                    result.add(type);
                }
            }
            return result;
        }
        finally
        {
            myLock.unlock();
        }
    }

    @Override
    public String[] getAvailableUnitsSelectionLabels(boolean allowAutoscale)
    {
        Collection<Class<? extends T>> availableUnits = getAvailableUnits(allowAutoscale);
        Collection<String> result = New.collection(availableUnits.size());
        for (Class<? extends T> type : availableUnits)
        {
            result.add(getSelectionLabel(type));
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public Class<? extends T> getFixedScaleUnits(Class<? extends T> type, T value)
    {
        if (isAutoscale(type))
        {
            return getDisplayClass(convert(type, value));
        }
        return type;
    }

    @Override
    public Class<? extends T> getPreferredFixedScaleUnits(T value)
    {
        return getFixedScaleUnits(getPreferredUnits(), value);
    }

    @Override
    public Class<? extends T> getPreferredUnits()
    {
        return myPreferredUnits.get();
    }
    
    public Class<? extends T> getPrevPreferredUnits()
    {
        return myPrevPreferredUnits.get();
    }
    

    @Override
    public Class<? extends T> getUnitsWithLongLabel(String label)
    {
        Lock lock = getLock();
        lock.lock();
        try
        {
            for (Class<? extends T> type : getAvailableUnitsUnsync())
            {
                try
                {
                    if (!isAutoscale(type) && (getLongLabel(type, true).equalsIgnoreCase(label)
                            || getLongLabel(type, false).equalsIgnoreCase(label)))
                    {
                        return type;
                    }
                }
                catch (InvalidUnitsException e)
                {
                    throw new ImpossibleException(e);
                }
            }
            return null;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public Class<? extends T> getUnitsWithSelectionLabel(String label)
    {
        Lock lock = getLock();
        lock.lock();
        try
        {
            for (Class<? extends T> type : getAvailableUnitsUnsync())
            {
                try
                {
                    if (getSelectionLabel(type).equalsIgnoreCase(label))
                    {
                        return type;
                    }
                }
                catch (InvalidUnitsException e)
                {
                    throw new ImpossibleException(e);
                }
            }
            return null;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public Class<? extends T> getUnitsWithShortLabel(String label)
    {
        Lock lock = getLock();
        lock.lock();
        try
        {
            for (Class<? extends T> type : getAvailableUnitsUnsync())
            {
                try
                {
                    if (!isAutoscale(type) && (getShortLabel(type, true).equalsIgnoreCase(label)
                            || getShortLabel(type, false).equalsIgnoreCase(label)))
                    {
                        return type;
                    }
                }
                catch (InvalidUnitsException e)
                {
                    throw new ImpossibleException(e);
                }
            }
            return null;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public boolean isAutoscale(Class<? extends T> type)
    {
        return UnitsUtilities.isAutoscale(type);
    }

    @Override
    public void removeListener(UnitsChangeListener<T> listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void removeUnits(Class<? extends T> type)
    {
        final Collection<Class<? extends T>> newUnits;
        myLock.lock();
        try
        {
            if (myAvailableUnits.remove(type))
            {
                newUnits = getAvailableUnits(true);
            }
            else
            {
                newUnits = null;
            }
        }
        finally
        {
            myLock.unlock();
        }
        if (newUnits != null)
        {
            notifyChanges(newUnits);
        }
    }

    @Override
    public void setPreferences(Preferences preferences)
    {
        Utilities.checkNull(preferences, "preferences");

        @SuppressWarnings("rawtypes")
        AtomicReferenceFieldUpdater<AbstractUnitsProvider, Preferences> updater = AtomicReferenceFieldUpdater
                .newUpdater(AbstractUnitsProvider.class, Preferences.class, "myPreferences");
        if (!updater.compareAndSet(this, null, preferences))
        {
            throw new IllegalStateException("Cannot set preferences more than once.");
        }

        myPreferences.addPreferenceChangeListener(myPrefsKey, myPreferencesListener);

        String preferredUnits = preferences.getString(myPrefsKey, null);
        if (preferredUnits != null)
        {
            setPreferredUnits(preferredUnits);
        }
    }

    @Override
    public void setPreferredUnits(Class<? extends T> units)
    {
        if (!Utilities.sameInstance(myPreferredUnits.getAndSet(units), units))
        {
            if (myPreferences != null)
            {
                myPreferences.putString(myPrefsKey, getSelectionLabel(units), this);
            }
            notifyChanges(units);
        }
    }

    /**
     * Internal method to get direct access to the available units,
     * unsynchronized. The caller must perform synchronization using
     * {@link #getLock()}.
     *
     * @return The available units.
     */
    protected List<Class<? extends T>> getAvailableUnitsUnsync()
    {
        return myAvailableUnits;
    }

    /**
     * Get the lock used to synchronize changes to the available units.
     *
     * @return The lock.
     */
    protected final Lock getLock()
    {
        return myLock;
    }

    /**
     * Get the type of the values accepted by the units' constructors.
     *
     * @return The value type.
     */
    protected abstract Class<? extends Number> getValueType();

    /**
     * Notify listeners of changes.
     *
     * @param preferredType The new preferred units.
     */
    protected void notifyChanges(final Class<? extends T> preferredType)
    {
        myChangeSupport.notifyListeners(listener -> listener.preferredUnitsChanged(preferredType), null);
    }
    
    /**
     * Notify listeners of changes.
     *
     * @param preferredType The new preferred units.
     */
    protected void notifyPrevChanges(final Class<? extends T> preferredType)
    {
        myChangeSupport.notifyListeners(listener -> listener.prevpreferredUnitsChanged(preferredType), null);
    }
    
    /**
     * Notify listeners of changes.
     *
     * @param newUnits The new types.
     */
    protected void notifyChanges(final Collection<Class<? extends T>> newUnits)
    {
        myChangeSupport.notifyListeners(listener -> listener.availableUnitsChanged(getSuperType(), newUnits), null);
    }

    /**
     * Test that a type can be instantiated.
     *
     * @param type The units type.
     * @throws InvalidUnitsException If the type cannot be instantiated.
     */
    protected abstract void testUnits(Class<? extends T> type) throws InvalidUnitsException;

    /**
     * Set the preferred units using the selection label.
     *
     * @param selectionLabel The long label.
     */
    private void setPreferredUnits(String selectionLabel)
    {
        Class<? extends T> units = getUnitsWithSelectionLabel(selectionLabel);
        if (units != null)
        {
            setPreferredUnits(units);
        }
    }

    /**
     * Set the previous preferred units using the selection label.
     *
     * @param selectionLabel The long label.
     */
    public void setPrevPreferredUnits(Class<? extends T> units)
    {
        if (!Utilities.sameInstance(myPrevPreferredUnits.getAndSet(units), units))
        {
            if (myPreferences != null)
            {
                myPreferences.putString(myPrefsKey, getSelectionLabel(units), this);
            }
        }
        notifyPrevChanges(units);
    }
}
