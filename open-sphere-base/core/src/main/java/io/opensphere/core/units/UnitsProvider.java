package io.opensphere.core.units;

import java.util.Collection;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.units.angle.Coordinates;
import io.opensphere.core.units.length.Length;

/**
 * Interface for facilities that provide units.
 *
 * @param <T> The supertype of the unit types in this provider.
 */
public interface UnitsProvider<T>
{
    /**
     * Add a listener for changes to the available units. Only a weak reference
     * is held to the listener.
     *
     * @param listener The listener.
     */
    void addListener(UnitsChangeListener<T> listener);

    /**
     * Add units to this unit provider. Notify listeners.
     *
     * @param type The type of the new units.
     * @throws InvalidUnitsException If the new type is invalid.
     */
    void addUnits(Class<? extends T> type);

    /**
     * Convert from one type to another. If the input object is already in the
     * desired units, the same object may be returned.
     *
     * @param <S> The desired type.
     * @param desiredUnits The type to return.
     * @param from The source object.
     * @return An instance of the desired type.
     * @throws InvalidUnitsException If the desired type is invalid.
     */
    <S extends T> S convert(Class<S> desiredUnits, T from);

    /**
     * Get an instance from a long label representation.
     *
     * @param label The label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     */
    T fromLongLabelString(String label) throws UnitsParseException;

    /**
     * Get an instance from a magnitude and a long label.
     *
     * @param magnitude The magnitude.
     * @param longLabel The long label.
     * @return The object.
     * @throws InvalidUnitsException If the units are invalid.
     * @throws UnitsParseException If no units could be found to match the
     *             label.
     */
    T fromMagnitudeAndLongLabel(Number magnitude, String longLabel);

    /**
     * Get an instance from a magnitude and a selection label.
     *
     * @param magnitude The magnitude.
     * @param selectionLabel The selection label.
     * @return The object.
     * @throws InvalidUnitsException If the units are invalid.
     * @throws UnitsParseException If no units could be found to match the
     *             label.
     */
    T fromMagnitudeAndSelectionLabel(Number magnitude, String selectionLabel);

    /**
     * Get an instance from a magnitude and a short label.
     *
     * @param magnitude The magnitude.
     * @param shortLabel The short label.
     * @return The object.
     * @throws InvalidUnitsException If the units are invalid.
     * @throws UnitsParseException If no units could be found to match the
     *             label.
     */
    T fromMagnitudeAndShortLabel(Number magnitude, String shortLabel);

    /**
     * Get an instance from a short label representation.
     *
     * @param label The label.
     * @return The object.
     * @throws UnitsParseException If the label cannot be parsed.
     */
    T fromShortLabelString(String label) throws UnitsParseException;

    /**
     * Get an instance from a type and a magnitude.
     *
     * @param <S> The type to create.
     * @param type The type to create.
     * @param magnitude The magnitude, in native units.
     * @return The new instance.
     * @throws InvalidUnitsException If the type is invalid.
     */
    <S extends T> S fromUnitsAndMagnitude(Class<S> type, Number magnitude);

    /**
     * Get the unit types currently available from this unit provider.
     *
     * @param allowAutoscale Indicates if auto-scale units should be returned.
     *
     * @return The types.
     */
    Collection<Class<? extends T>> getAvailableUnits(boolean allowAutoscale);

    /**
     * Convenience method to get the selection labels of all the available units
     * registered with this provider.
     *
     * @param allowAutoscale Indicates if auto-scale units are desired.
     *
     * @return The selection labels.
     */
    String[] getAvailableUnitsSelectionLabels(boolean allowAutoscale);

    /**
     * Get the class that will be used to display a value. This may be different
     * from the type of the provided value if the value is an auto-scaling unit.
     *
     * @param value The value.
     * @return The display class.
     */
    Class<? extends T> getDisplayClass(T value);

    /**
     * Get the fixed-scale units appropriate for a particular value. If the
     * input {@code type} is not an auto-scale type, it is simply returned
     * regardless of the input {@code value}.
     *
     * @param type The input type.
     * @param value The value.
     * @return The preferred units.
     */
    Class<? extends T> getFixedScaleUnits(Class<? extends T> type, T value);

    /**
     * Get the long label for a type.
     *
     * @param type The type.
     * @param plural If the label should be in its plural form.
     * @return The label.
     * @throws InvalidUnitsException If the type is invalid.
     */
    String getLongLabel(Class<? extends T> type, boolean plural);

    /**
     * Get the preferred units from the unit provider for a particular value.
     * The preferred units may change based on the magnitude of the value if the
     * units auto-scale.
     *
     * @param value The value.
     * @return The preferred units.
     */
    Class<? extends T> getPreferredFixedScaleUnits(T value);

    /**
     * Get the preferred units from this unit provider.
     *
     * @return The preferred units.
     */
    Class<? extends T> getPreferredUnits();
    
    /**
     * Get the preferred units from this unit provider.
     *
     * @return The preferred units.
     */
    Class<? extends T> getPrevPreferredUnits();

    /**
     * Get a selection label for a type. This is a label that could be used to
     * select desired units (e.g., from a menu.)
     *
     * @param type The type.
     * @return The label.
     * @throws InvalidUnitsException If the type is invalid.
     */
    String getSelectionLabel(Class<? extends T> type);

    /**
     * Get the short label for a type.
     *
     * @param type The type.
     * @param plural If the label should be in its plural form.
     * @return The label.
     * @throws InvalidUnitsException If the type is invalid.
     */
    String getShortLabel(Class<? extends T> type, boolean plural);

    /**
     * Get the supertype of the units in this provider.
     *
     * @return The supertype.
     */
    Class<T> getSuperType();

    /**
     * Get the units that have a particular long label.
     *
     * @param label The label.
     * @return The type, or {@code null} if one was not found.
     */
    Class<? extends T> getUnitsWithLongLabel(String label);

    /**
     * Get the units that have a particular selection label.
     *
     * @param label The label.
     * @return The type, or {@code null} if one was not found.
     */
    Class<? extends T> getUnitsWithSelectionLabel(String label);

    /**
     * Get the units that have a particular short label.
     *
     * @param label The label.
     * @return The type, or {@code null} if one was not found.
     */
    Class<? extends T> getUnitsWithShortLabel(String label);

    /**
     * Get if a type automatically scales.
     *
     * @param type The type.
     * @return {@code true} if the type automatically scales.
     */
    boolean isAutoscale(Class<? extends T> type);

    /**
     * Remove a listener for changes to the available units.
     *
     * @param listener The listener.
     */
    void removeListener(UnitsChangeListener<T> listener);

    /**
     * Remove a type of units from this unit provider. Notify listeners.
     *
     * @param type The new unit type.
     */
    void removeUnits(Class<? extends T> type);

    /**
     * Set the preferences for the units provider.
     *
     * @param preferences The preferences.
     */
    void setPreferences(Preferences preferences);

    /**
     * Set the preferred units for this unit provider.
     *
     * @param units The preferred units.
     */
    void setPreferredUnits(Class<? extends T> units);

    /**
     * Get the short label string for an instance.
     *
     * @param obj The instance.
     * @return The long label string.
     */
    String toShortLabelString(T obj);

    /**
     * A listener for changes to the available units.
     *
     * @param <T> The supertype of the unit types that this listener handles.
     */
    interface UnitsChangeListener<T>
    {
        /**
         * Method called when the available units change in this unit provider.
         *
         * @param superType The supertype of the unit types that changed.
         * @param newTypes The types after the change.
         */
        default void availableUnitsChanged(Class<T> superType, Collection<Class<? extends T>> newTypes)
        {
            
        };

        /**
         * Method called when the preferred units change in this unit provider.
         *
         * @param type The new preferred unit type.
         */
        void preferredUnitsChanged(Class<? extends T> type);

        /**
         * Method called when the preferred units change in this unit provider.
         *
         * @param Coordinates The new preferred unit type.
         */
        default void prevpreferredUnitsChanged(Class<? extends T> preferredType) {
            
        };

    }
    
    /**
     * Method called when the preferred units change in this unit provider.
     *
     * @param type The previous preferred unit type.
     */
    void setPrevPreferredUnits(Class<? extends T> units);
}
