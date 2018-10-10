package io.opensphere.core.util.lang;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;

/** Utilities for doing Java reflection. */
public final class ReflectionUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ReflectionUtilities.class);

    /**
     * Find a declared field in a class, searching super-classes if necessary.
     *
     * @param type The class.
     * @param fieldName The field name.
     * @return The field object;
     * @throws NoSuchFieldException If the field could not be found.
     */
    public static Field getDeclaredField(Class<?> type, String fieldName) throws NoSuchFieldException
    {
        Field field;
        Class<? extends Object> cl = type;
        do
        {
            try
            {
                field = cl.getDeclaredField(fieldName);
                break;
            }
            catch (NoSuchFieldException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Class '" + type.getName() + "' did not contain a field named '" + fieldName + "'", e);
                }
                cl = cl.getSuperclass();
                if (cl == Object.class)
                {
                    throw e;
                }
            }
        }
        while (true);
        return field;
    }

    /**
     * Get the value of a field from an object using reflection. The field will
     * be made accessible if necessary.
     *
     * @param <T> The type of the value.
     * @param object The object that owns the field.
     * @param fieldName The field name.
     * @param fieldClass The field class.
     * @return The value, or {@code null} if the value is the wrong type.
     * @throws SecurityException If the field cannot be accessed because of
     *             security.
     * @throws NoSuchFieldException If the field does not exist in the object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object object, String fieldName, Class<T> fieldClass)
            throws SecurityException, NoSuchFieldException
    {
        Utilities.checkNull(object, "object");
        Utilities.checkNull(fieldName, "fieldName");
        Utilities.checkNull(fieldClass, "fieldClass");

        Field field = getDeclaredField(object.getClass(), fieldName);
        try
        {
            Object value;
            try
            {
                value = field.get(object);
            }
            catch (IllegalAccessException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Unable to access field '" + fieldName + "' from instance of class '"
                            + object.getClass().getName() + "'", e);
                }
                field.setAccessible(true);
                try
                {
                    value = field.get(object);
                }
                finally
                {
                    field.setAccessible(false);
                }
            }
            if (fieldClass.isInstance(value))
            {
                return (T)value;
            }
            return null;
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new ImpossibleException(e);
        }
    }

    /**
     * Set the value of a field in an object using reflection. The field will be
     * made accessible if necessary.
     *
     * @param object The object that owns the field.
     * @param fieldName The field name.
     * @param value The new value.
     * @throws SecurityException If the field cannot be accessed because of
     *             security.
     * @throws NoSuchFieldException If the field does not exist in the object.
     * @throws IllegalArgumentException If the value is the wrong type.
     */
    public static void setFieldValue(Object object, String fieldName, Object value)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException
    {
        Utilities.checkNull(object, "object");
        Utilities.checkNull(fieldName, "fieldName");

        Field field = getDeclaredField(object.getClass(), fieldName);

        try
        {
            try
            {
                field.set(object, value);
            }
            catch (IllegalAccessException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Unable to access field '" + fieldName + "' on instance of class '" + object.getClass().getName()
                            + "' to set value.", e);
                }
                field.setAccessible(true);
                try
                {
                    field.set(object, value);
                }
                finally
                {
                    field.setAccessible(false);
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new ImpossibleException(e);
        }
    }

    /** Disallow instantiation. */
    private ReflectionUtilities()
    {
    }
}
