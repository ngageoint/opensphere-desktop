package io.opensphere.core.units;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.core.util.lang.Pair;

/**
 * Common utilities for units.
 */
public final class UnitsUtilities
{
    /** An object pool. */
    private static final SharedObjectPool<Object> POOL = new SharedObjectPool<>();

    /**
     * Helper method for creating new units object.
     *
     * @param <S> The type.
     * @param type The length type.
     * @param argTypes The constructor argument types.
     * @param args The constructor arguments.
     * @return The instance.
     * @throws InvalidUnitsException If the type is invalid.
     * @throws InconvertibleUnits If the unit types are not compatible.
     */
    public static <S> S create(Class<S> type, Class<?>[] argTypes, Object[] args) throws InvalidUnitsException, InconvertibleUnits
    {
        try
        {
            @SuppressWarnings("unchecked")
            S result = (S)POOL.get(type.getConstructor(argTypes).newInstance(args));
            return result;
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException e)
        {
            throw new InvalidUnitsException(type, e);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof RuntimeException)
            {
                throw (RuntimeException)e.getCause();
            }
            else if (e.getCause() instanceof Error)
            {
                throw (Error)e.getCause();
            }
            else
            {
                throw new InvalidUnitsException(type, e);
            }
        }
    }

    /**
     * Helper method for creating new units object.
     *
     * @param <S> The type to be created.
     * @param <T> The argument type.
     * @param type The type to be created.
     * @param argType The constructor argument type.
     * @param arg The constructor argument.
     * @return The instance.
     * @throws InvalidUnitsException If the type is invalid.
     * @throws InconvertibleUnits If the unit types are not compatible.
     */
    public static <S, T> S create(Class<S> type, Class<T> argType, T arg) throws InvalidUnitsException, InconvertibleUnits
    {
        return create(type, new Class<?>[] { argType }, new Object[] { arg });
    }

    /**
     * Return a units object from a units provider that matches a long label
     * string.
     *
     * @param <S> The supertype of the units.
     * @param unitsProvider The units provider.
     * @param valueType The type of the values in the units.
     * @param label The label string.
     * @return The instance.
     * @throws UnitsParseException If the label cannot be parsed or the units
     *             cannot be found.
     */
    public static <S> S createFromLongLabelString(UnitsProvider<S> unitsProvider, Class<? extends Number> valueType, String label)
        throws UnitsParseException
    {
        Pair<String, String> splitLabel = splitLabel(label);

        Class<? extends S> unitType = unitsProvider.getUnitsWithLongLabel(splitLabel.getSecondObject());
        if (unitType != null)
        {
            return createFromStrings(splitLabel.getFirstObject(), unitType, valueType);
        }
        else
        {
            throw new UnitsParseException("Failed to find appropriate units for string: " + label);
        }
    }

    /**
     * Return a units object from a units provider that matches a short label
     * string.
     *
     * @param <S> The supertype of the units.
     * @param unitsProvider The units provider.
     * @param valueType The type of the values in the units.
     * @param label The label string.
     * @return The instance.
     * @throws UnitsParseException If the label cannot be parsed or the units
     *             cannot be found.
     */
    public static <S> S createFromShortLabelString(UnitsProvider<S> unitsProvider, Class<? extends Number> valueType,
            String label)
        throws UnitsParseException
    {
        Pair<String, String> splitLabel = splitLabel(label);
        Class<? extends S> unitType = unitsProvider.getUnitsWithShortLabel(splitLabel.getSecondObject());
        if (unitType != null)
        {
            return createFromStrings(splitLabel.getFirstObject(), unitType, valueType);
        }
        else
        {
            throw new UnitsParseException("Failed to find appropriate units for string: " + label);
        }
    }

    /**
     * Determine if a unit type auto-scales.
     *
     * @param type The type.
     * @return {@code true} if the type auto-scales.
     */
    public static boolean isAutoscale(Class<?> type)
    {
        return type.isAnnotationPresent(AutoscaleUnit.class);
    }

    /**
     * Return a units object from a units provider that matches a short label
     * string.
     *
     * @param <S> The supertype of the units.
     * @param magnitudeString A string representation of the magnitude.
     * @param valueType The type of the values in the units.
     * @param type The type of the units.
     * @return The instance.
     * @throws UnitsParseException If the label cannot be parsed or the units
     *             cannot be found.
     * @throws InvalidUnitsException If the type is invalid.
     */
    private static <S> S createFromStrings(String magnitudeString, Class<? extends S> type, Class<? extends Number> valueType)
        throws UnitsParseException, InvalidUnitsException
    {
        Object mag;
        try
        {
            if (valueType.equals(BigDecimal.class))
            {
                mag = new BigDecimal(magnitudeString);
            }
            else if (valueType.equals(Double.TYPE))
            {
                mag = Double.valueOf(magnitudeString);
            }
            else
            {
                throw new IllegalArgumentException("Value type " + valueType + " is not handled.");
            }
        }
        catch (NumberFormatException e)
        {
            throw new UnitsParseException("Failed to parse magnitude string: " + magnitudeString, e);
        }
        return create(type, new Class<?>[] { valueType }, new Object[] { mag });
    }

    /**
     * Split the label on the first character which cannot be part of the
     * number.
     *
     * @param label The label.
     * @return The pair of strings.
     * @throws UnitsParseException If the label cannot be parsed.
     */
    private static Pair<String, String> splitLabel(String label) throws UnitsParseException
    {
        // Skip leading spaces.
        int index = 0;
        while (index < label.length() && Character.isWhitespace(label.charAt(index)))
        {
            ++index;
        }

        // Find a non-numeric character.
        while (index < label.length())
        {
            if (Character.isDigit(label.charAt(index)) || label.charAt(index) == '.' || label.charAt(index) == 'e'
                    || label.charAt(index) == 'E' || label.charAt(index) == '+' || label.charAt(index) == '-')
            {
                ++index;
            }
            else
            {
                break;
            }
        }

        Pair<String, String> strings;
        if (index < label.length())
        {
            String suffix = label.substring(index).trim();
            String magnitudeString = label.substring(0, index);
            strings = new Pair<>(magnitudeString, suffix);
        }
        else
        {
            throw new UnitsParseException("Failed to parse numeric portion of units string: " + label);
        }
        return strings;
    }

    /** Disallow instantiation. */
    private UnitsUtilities()
    {
    }
}
