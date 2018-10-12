package io.opensphere.core;

import java.util.Collection;

import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsParseException;
import io.opensphere.core.units.UnitsProvider;

/**
 * <p>
 * Interface for the units registry. This allows plug-ins to retrieve and modify
 * the current available and preferred units in the system.
 * </p>
 *
 * <pre>
 * Examples:
 * <code>
 *     UnitsRegistry registry;
 *     Length length;
 *
 *     // Get the available length units installed in the system.
 *     Collection&lt;Class&lt;? extends Length&gt;&gt; availableUnits;
 *     availableUnits = registry.getAvailableUnits(Length.class);
 *     // or
 *     UnitsProvider&lt;Length&gt; unitsProvider = registry.getUnitsProvider(Length.class);
 *     availableUnits = unitsProvider.getAvailableUnits();
 *
 *     // Get the short labels of the available length units in the system ("m", ft", etc.)
 *     String[] units;
 *     units = registry.getAvailableUnitsShortLabels(Length.class, true);
 *     // or
 *     units = unitsProvider.getAvailableUnitsShortLabels(true);
 *
 *     // Create a length in the preferred units, equivalent to 10 meters.
 *     length = Length.create(unitsProvider.getPreferredUnits(), new Meters(10.));
 *     // or
 *     length = Length.create(registry.getPreferredUnits(Length.class), new Meters(10.));
 *     // or
 *     length = registry.convert(Length.class, unitsProvider.getPreferredUnits(), new Meters(10.));
 *
 *     // Create a length in meters using a number and a short label.
 *     length = registry.fromMagnitudeAndShortLabel(Length.class, 5, "m");
 *
 *     // Create a length in nautical miles using a short label and another length.
 *     length = registry.convertUsingShortLabel(Length.class, new Meters(10.), "nm");
 * </code>
 * </pre>
 *
 */
public interface UnitsRegistry
{
    /**
     * Add a units provider to the registry.
     *
     * @param provider The provider.
     */
    void addUnitsProvider(UnitsProvider<?> provider);

    /**
     * Convert from one type to another.
     *
     * @param <S> The units supertype.
     * @param <T> The desired type.
     * @param unitsSupertype The supertype of units.
     * @param desiredType The type to return.
     * @param from The source object.
     * @return An instance of the desired type.
     * @throws InvalidUnitsException If the desired type is invalid.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <S, T extends S> T convert(Class<S> unitsSupertype, Class<T> desiredType, S from)
            throws InvalidUnitsException, UnitsProviderNotFoundException;

    /**
     * Convert from one type to another, determining the destination type from a
     * long label.
     *
     * @param <T> The supertype of units.
     * @param unitsSupertype The supertype of units.
     * @param magnitude The magnitude.
     * @param longLabel The long label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     * @throws InvalidUnitsException If the units that go with the label are
     *             invalid.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> T convertUsingLongLabel(Class<T> unitsSupertype, T magnitude, String longLabel)
            throws UnitsParseException, UnitsProviderNotFoundException, InvalidUnitsException;

    /**
     * Convert from one type to another, determining the destination type from a
     * short label.
     *
     * @param <T> The supertype of units.
     * @param unitsSupertype The supertype of units.
     * @param magnitude The magnitude.
     * @param shortLabel The short label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     * @throws InvalidUnitsException If the units that go with the label are
     *             invalid.
     */
    <T> T convertUsingShortLabel(Class<T> unitsSupertype, T magnitude, String shortLabel)
            throws UnitsParseException, UnitsProviderNotFoundException, InvalidUnitsException;

    /**
     * Get an instance from a long label representation (magnitude + long
     * label).
     *
     * @param <T> The supertype of units.
     * @param unitsSupertype The supertype of units.
     * @param label The label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> T fromLongLabelString(Class<T> unitsSupertype, String label) throws UnitsParseException, UnitsProviderNotFoundException;

    /**
     * Get an instance from a magnitude and a long label.
     *
     * @param <T> The supertype of units.
     * @param unitsSupertype The supertype of units.
     * @param magnitude The magnitude.
     * @param longLabel The long label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     * @throws InvalidUnitsException If the units that go with the label are
     *             invalid.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> T fromMagnitudeAndLongLabel(Class<T> unitsSupertype, Number magnitude, String longLabel)
            throws UnitsParseException, InvalidUnitsException, UnitsProviderNotFoundException;

    /**
     * Get an instance from a magnitude and a short label.
     *
     * @param <T> The supertype of units.
     * @param unitsSupertype The supertype of units.
     * @param magnitude The magnitude.
     * @param shortLabel The short label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     * @throws InvalidUnitsException If the units that go with the label are
     *             invalid.
     */
    <T> T fromMagnitudeAndShortLabel(Class<T> unitsSupertype, Number magnitude, String shortLabel)
            throws UnitsParseException, UnitsProviderNotFoundException, InvalidUnitsException;

    /**
     * Get an instance from a short label representation (magnitude + short
     * label).
     *
     * @param <T> The supertype of units.
     * @param unitsSupertype The supertype of units.
     * @param label The label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> T fromShortLabelString(Class<T> unitsSupertype, String label) throws UnitsParseException, UnitsProviderNotFoundException;

    /**
     * Get an instance from a type and a magnitude.
     *
     * @param <T> The desired type.
     * @param unitsSupertype The supertype of units.
     * @param type The type to create.
     * @param magnitude The magnitude, in native units.
     * @return The new instance.
     * @throws InvalidUnitsException If the type is invalid.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> T fromUnitsAndMagnitude(Class<? super T> unitsSupertype, Class<T> type, Number magnitude)
            throws InvalidUnitsException, UnitsProviderNotFoundException;

    /**
     * Get the unit types currently available from the unit provider for a given
     * type of units.
     *
     * @param <T> The type of units.
     * @param unitsSupertype The type of units.
     * @param allowAutoscale Indicates if auto-scale units should be returned.
     *
     * @return The types.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> Collection<Class<? extends T>> getAvailableUnits(Class<T> unitsSupertype, boolean allowAutoscale)
            throws UnitsProviderNotFoundException;

    /**
     * Convenience method to get the selection labels of all the available units
     * registered with the provider for a given type of units.
     *
     * @param <T> The type of units.
     * @param unitsSupertype The type of units.
     * @param allowAutoscale Indicates if auto-scale units should be returned.
     *
     * @return The selection labels.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> String[] getAvailableUnitsSelectionLabels(Class<T> unitsSupertype, boolean allowAutoscale)
            throws UnitsProviderNotFoundException;

    /**
     * Get the preferred units from the unit provider for a particular value.
     * The preferred units may change based on the magnitude of the value if the
     * units auto-scale.
     *
     * @param <T> The type of units.
     * @param value The value.
     * @param unitsSupertype The type of units.
     * @return The preferred units.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> Class<? extends T> getPreferredFixedScaleUnits(Class<T> unitsSupertype, T value) throws UnitsProviderNotFoundException;

    /**
     * Get the preferred units from this unit provider.
     *
     * @param <T> The type of units.
     * @param unitsSupertype The type of units.
     * @return The preferred units.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> Class<? extends T> getPreferredUnits(Class<T> unitsSupertype) throws UnitsProviderNotFoundException;

    /**
     * Get the units provider for a category of units.
     *
     * @param <T> The type of units.
     * @param unitsSupertype The type of units.
     * @return The units provider.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> UnitsProvider<T> getUnitsProvider(Class<T> unitsSupertype) throws UnitsProviderNotFoundException;

    /**
     * Get the units providers installed in the system.
     *
     * @return The units providers.
     */
    Collection<? extends UnitsProvider<?>> getUnitsProviders();

    /**
     * Get the units that have a particular selection label.
     *
     * @param <T> The type of units.
     * @param unitsSupertype The type of units.
     * @param label The label.
     * @return The type, or {@code null} if appropriate units could not be
     *         found.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> Class<? extends T> getUnitsWithSelectionLabel(Class<T> unitsSupertype, String label)
            throws UnitsProviderNotFoundException;

    /**
     * Remove a units provider from the registry.
     *
     * @param provider The provider.
     */
    void removeUnitsProvider(UnitsProvider<?> provider);

    /**
     * Reset the preferred units for all units providers to the default.
     *
     * @param source The source of the reset, to be passed to listeners.
     */
    void resetAllPreferredUnits(Object source);

    /**
     * Get the short label string for an instance.
     *
     * @param <T> The supertype of units.
     * @param unitsSupertype The supertype of units.
     * @param obj The instance.
     * @return The long label string.
     * @throws UnitsProviderNotFoundException If the units provider has not been
     *             installed.
     */
    <T> String toShortLabelString(Class<? super T> unitsSupertype, T obj) throws UnitsProviderNotFoundException;

    /**
     * Exception thrown if a units provider is not found.
     */
    class UnitsProviderNotFoundException extends RuntimeException
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param unitsSupertype The units supertype.
         */
        public UnitsProviderNotFoundException(Class<?> unitsSupertype)
        {
            super("Units provider not found for supertype [" + unitsSupertype + "]");
        }
    }
}
