package io.opensphere.core.appl;

import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsParseException;
import io.opensphere.core.units.UnitsProvider;

/**
 * An implementation for {@link UnitsRegistry}.
 */
final class UnitsRegistryImpl implements UnitsRegistry
{
    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /** The map of units types to providers. */
    private final Map<Class<?>, UnitsProvider<?>> myProviderMap = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public UnitsRegistryImpl(PreferencesRegistry preferencesRegistry)
    {
        myPreferencesRegistry = preferencesRegistry;
        Preferences preferences = myPreferencesRegistry.getPreferences(UnitsRegistry.class);
        for (UnitsProvider<?> provider : ServiceLoader.load(UnitsProvider.class))
        {
            provider.setPreferences(preferences);
            addUnitsProvider(provider);
        }
    }

    @Override
    public void addUnitsProvider(UnitsProvider<?> provider)
    {
        myProviderMap.put(provider.getSuperType(), provider);
    }

    @Override
    public <S, T extends S> T convert(Class<S> unitsSupertype, Class<T> desiredUnits, S from)
        throws InvalidUnitsException, UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).convert(desiredUnits, from);
    }

    @Override
    public <T> T convertUsingLongLabel(Class<T> unitsSupertype, T magnitude, String longLabel)
        throws UnitsParseException, UnitsProviderNotFoundException, InvalidUnitsException
    {
        Class<? extends T> type = getUnitsProvider(unitsSupertype).getUnitsWithLongLabel(longLabel);
        if (type == null)
        {
            throw new UnitsParseException("Failed to find appropriate units for string: " + longLabel);
        }
        return getUnitsProvider(unitsSupertype).convert(type, magnitude);
    }

    @Override
    public <T> T convertUsingShortLabel(Class<T> unitsSupertype, T magnitude, String shortLabel)
        throws UnitsParseException, UnitsProviderNotFoundException, InvalidUnitsException
    {
        UnitsProvider<T> unitsProvider = getUnitsProvider(unitsSupertype);
        return unitsProvider.convert(unitsProvider.getUnitsWithShortLabel(shortLabel), magnitude);
    }

    @Override
    public <T> T fromLongLabelString(Class<T> unitsSupertype, String label)
        throws UnitsParseException, UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).fromLongLabelString(label);
    }

    @Override
    public <T> T fromMagnitudeAndLongLabel(Class<T> unitsSupertype, Number magnitude, String longLabel)
        throws UnitsParseException, InvalidUnitsException, UnitsProviderNotFoundException
    {
        UnitsProvider<T> unitsProvider = getUnitsProvider(unitsSupertype);
        return unitsProvider.fromMagnitudeAndLongLabel(magnitude, longLabel);
    }

    @Override
    public <T> T fromMagnitudeAndShortLabel(Class<T> unitsSupertype, Number magnitude, String shortLabel)
        throws UnitsParseException, UnitsProviderNotFoundException, InvalidUnitsException
    {
        UnitsProvider<T> unitsProvider = getUnitsProvider(unitsSupertype);
        return unitsProvider.fromMagnitudeAndShortLabel(magnitude, shortLabel);
    }

    @Override
    public <T> T fromShortLabelString(Class<T> unitsSupertype, String label)
        throws UnitsParseException, UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).fromShortLabelString(label);
    }

    @Override
    public <T> T fromUnitsAndMagnitude(Class<? super T> unitsSupertype, Class<T> type, Number magnitude)
        throws InvalidUnitsException, UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).fromUnitsAndMagnitude(type, magnitude);
    }

    @Override
    public <T> Collection<Class<? extends T>> getAvailableUnits(Class<T> unitsSupertype, boolean allowAutoscale)
        throws UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).getAvailableUnits(allowAutoscale);
    }

    @Override
    public <T> String[] getAvailableUnitsSelectionLabels(Class<T> unitsSupertype, boolean allowAutoscale)
        throws UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).getAvailableUnitsSelectionLabels(allowAutoscale);
    }

    @Override
    public <T> Class<? extends T> getPreferredFixedScaleUnits(Class<T> unitsSupertype, T value)
        throws UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).getPreferredFixedScaleUnits(value);
    }

    @Override
    public <T> Class<? extends T> getPreferredUnits(Class<T> unitsSupertype) throws UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).getPreferredUnits();
    }

    @Override
    public <T> Class<? extends T> getPrevPreferredUnits(Class<T> unitsSupertype) throws UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).getPrevPreferredUnits();
    }

    @Override
    public <T> UnitsProvider<T> getUnitsProvider(Class<T> unitsSupertype) throws UnitsProviderNotFoundException
    {
        @SuppressWarnings("unchecked")
        UnitsProvider<T> unitsProvider = (UnitsProvider<T>)myProviderMap.get(unitsSupertype);
        if (unitsProvider == null)
        {
            throw new UnitsProviderNotFoundException(unitsSupertype);
        }
        return unitsProvider;
    }

    @Override
    public Collection<? extends UnitsProvider<?>> getUnitsProviders()
    {
        return myProviderMap.values();
    }

    @Override
    public <T> Class<? extends T> getUnitsWithSelectionLabel(Class<T> unitsSupertype, String label)
        throws UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).getUnitsWithSelectionLabel(label);
    }

    @Override
    public void removeUnitsProvider(UnitsProvider<?> provider)
    {
        myProviderMap.remove(provider.getSuperType());
    }

    @Override
    public void resetAllPreferredUnits(Object source)
    {
        myPreferencesRegistry.resetPreferences(UnitsRegistry.class, source);
    }

    @Override
    public <T> String toShortLabelString(Class<? super T> unitsSupertype, T obj) throws UnitsProviderNotFoundException
    {
        return getUnitsProvider(unitsSupertype).toShortLabelString(obj);
    }
}
