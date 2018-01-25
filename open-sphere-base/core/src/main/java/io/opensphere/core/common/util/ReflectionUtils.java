package io.opensphere.core.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReflectionUtils
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Log LOGGER = LogFactory.getLog(ReflectionUtils.class);

    /**
     * Attempts to retrieve the value from the given object with the specified
     * <code>fieldName</code>.
     *
     * @param object the instance from which to fetch the value.
     * @param fieldName the field name for which to fetch the value.
     * @param parameters the parameters to pass if an accessor must be called to
     *            fetch the value.
     * @return the field's value or <code>null</code> if it could not be
     *         retrieved.
     */
    public static Object getValue(Object object, String fieldName, Object[] parameters)
    {
        Object value = null;
        boolean valueFetched = false;
        Class<? extends Object> clazz = object.getClass();

        // Attempt to get the field value directly.
        try
        {
            Field field = clazz.getField(fieldName);
            if (field != null)
            {
                value = field.get(object);
                valueFetched = true;
            }
        }
        catch (SecurityException e)
        {
            LOGGER.error(e);
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error(e);
        }
        catch (NoSuchFieldException e)
        {
            LOGGER.error(e);
        }
        catch (IllegalAccessException e)
        {
            LOGGER.error(e);
        }
        if (!valueFetched)
        {
            if (parameters == null)
            {
                parameters = new Object[0];
            }
            for (Method method : clazz.getMethods())
            {
                // If the method name matches the expected name, ensure that the
                // parameters match.
                if (method.getName().equals("get" + fieldName)
                        || method.getName().equals("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)))
                {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length != parameters.length)
                    {
                        continue;
                    }
                    boolean paramsMatch = true;
                    for (int index = 0; index < parameters.length; index++)
                    {
                        if (parameters[index] != null && !paramTypes[index].isAssignableFrom(parameters[index].getClass()))
                        {
                            paramsMatch = false;
                            break;
                        }
                    }
                    if (paramsMatch)
                    {
                        try
                        {
                            value = method.invoke(object, parameters);
                            break;
                        }
                        catch (IllegalArgumentException e)
                        {
                            LOGGER.error(e);
                        }
                        catch (IllegalAccessException e)
                        {
                            LOGGER.error(e);
                        }
                        catch (InvocationTargetException e)
                        {
                            LOGGER.error(e);
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * Creates a new instance of the specified class using the constructor
     * parameters with the {@link ReflectionUtils} class loader.
     *
     * @param <T> The expected type of the created class.
     *
     * @param className the fully qualified class name.
     * @param parameters the constructor arguments or <code>null</code> if none
     *            apply.
     * @return the new instance.
     * @see #newInstance(String, ClassLoader, Object...)
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, Object... parameters)
    {
        // No class loader was given, use mine
        return (T)newInstance(className, Thread.currentThread().getContextClassLoader(), parameters);
    }

    /**
     * Creates a new instance of the specified class using the constructor
     * parameters.
     *
     * @param <T> The expected type of the created class.
     *
     * @param className the fully qualified class name.
     * @param classLoader the {@link ClassLoader} to use when locating the
     *            className.
     * @param parameters the constructor arguments or <code>null</code> if none
     *            apply.
     * @return the new instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, ClassLoader classloader, Object... parameters)
    {
        Object instance = null;
        try
        {
            Class<?> clazz = Class.forName(className, Boolean.TRUE, classloader);
            instance = newInstance(clazz, parameters);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create a new instance of " + className, e);
        }
        return (T)instance;
    }

    /**
     * Creates a new instance of the specified class using the constructor
     * parameters.
     *
     * @param <T> The expected type of the created class.
     *
     * @param clazz the instance belongs to this Class
     * @param parameters the constructor arguments or <code>null</code> if none
     *            apply.
     * @return the new instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> clazz, Object... parameters)
    {
        Object instance = null;
        try
        {
            List<Class<?>> parameterTypes = new ArrayList<>();
            if (parameters != null && parameters.length > 0)
            {
                for (int index = 0; index < parameters.length; index++)
                {
                    Object parameter = parameters[index];
                    parameterTypes.add(parameter != null ? parameter.getClass() : null);
                }
            }

            instance = getConstructor(clazz, parameterTypes).newInstance(parameters);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create a new instance of " + clazz.getSimpleName(), e);
        }
        return (T)instance;
    }

    /**
     * Finds a matching constructor for the given class and argument types.
     *
     * @param clazz the instance belongs to this Class
     * @param parameters the constructor arguments or <code>null</code> if none
     *            apply. The constructor arguments can include a "null" in place
     *            of a class if the type couldn't be inferred from the value.
     * @return the new instance.
     */
    public static Constructor<?> getConstructor(Class<?> clazz, List<Class<?>> parameterTypes)
    {
        try
        {
            Constructor<?>[] constructors = clazz.getConstructors();
            Constructor<?> constructor = null;

            // Search for an exact match.
            for (Constructor<?> tmp : constructors)
            {
                if (tmp.getParameterTypes().length == parameterTypes.size())
                {
                    boolean match = true;
                    for (int index = 0; index < parameterTypes.size(); index++)
                    {
                        Class<?> tmpParamType = tmp.getParameterTypes()[index];
                        Class<?> paramType = parameterTypes.get(index);
                        // Check for: Null value assigned to primitive or a
                        // field that does not match the exact type of the
                        // inferred field.
                        if (paramType == null && tmpParamType.isPrimitive()
                                || paramType != null && !tmpParamType.equals(paramType))
                        {
                            match = false;
                            break;
                        }
                    }

                    if (match)
                    {
                        constructor = tmp;
                        break;
                    }
                }
            }

            // If an exact constructor match was not found, search for matches
            // via
            // inheritance.
            if (constructor == null)
            {
                for (Constructor<?> tmp : constructors)
                {
                    if (tmp.getParameterTypes().length == parameterTypes.size())
                    {
                        boolean match = true;
                        for (int index = 0; index < parameterTypes.size(); index++)
                        {
                            Class<?> tmpParamType = tmp.getParameterTypes()[index];
                            Class<?> paramType = parameterTypes.get(index);
                            if (paramType == null && tmpParamType.isPrimitive()
                                    || paramType != null && !tmpParamType.isAssignableFrom(paramType))
                            {
                                match = false;
                                break;
                            }
                        }

                        if (match)
                        {
                            constructor = tmp;
                            break;
                        }
                    }
                }
            }

            // Log an error and throw an exception.
            if (constructor == null)
            {
                String parameterNames = "";
                for (Class<?> paramType : parameterTypes)
                {
                    if (parameterNames.length() > 0)
                    {
                        parameterNames += ", ";
                    }
                    parameterNames += paramType == null ? "null" : paramType.getName();
                }
                throw new NoSuchMethodException(clazz.getName() + ".<init>(" + parameterNames + ")");
            }

            return constructor;
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "Failed to find " + clazz.getName() + " constructor with arguments: " + parameterTypes.toString() + ".", e);
        }
    }
}
