package io.opensphere.core.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.util.ref.SoftReference;

/**
 * General utilities.
 */
@SuppressWarnings("PMD.GodClass")
public final class Utilities
{
    /** Byte array output stream cache. */
    private static final ThreadLocal<SoftReference<ByteArrayOutputStream>> BAOS_CACHE = new ThreadLocal<>();

    /** Size of an {@link EnumMap} not counting its value array. */
    private static final int BASE_ENUM_MAP_SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + 6 * Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES)
            + MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES, Constants.MEMORY_BLOCK_SIZE_BYTES);

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(Utilities.class);

    /**
     * Get the boolean value or return {@code false} if the value is
     * {@code null}.
     *
     * @param value The {@link Boolean} value.
     * @return The boolean value.
     */
    public static boolean booleanValue(@Nullable Boolean value)
    {
        return value != null && value.booleanValue();
    }

    /**
     * Ensure that a parameter is not null.
     *
     * @param <T> The type of the parameter.
     * @param parameter The parameter reference.
     * @param name The name of the parameter.
     * @return The parameter, if it is not {@code null}.
     * @throws IllegalArgumentException If the parameter is null.
     */
    public static <T> T checkNull(T parameter, String name) throws IllegalArgumentException
    {
        if (parameter == null)
        {
            throw new IllegalArgumentException("Parameter [" + name + "] cannot be null.");
        }
        return parameter;
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static boolean[] clone(boolean[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static byte[] clone(byte[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static char[] clone(char[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone a {@link Date}, returning {@code null} if the input date is
     * {@code null}.
     *
     * @param date The date to be cloned.
     * @return The clone.
     */
    public static Date clone(Date date)
    {
        return date == null ? null : (Date)date.clone();
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static double[] clone(double[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static float[] clone(float[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static int[] clone(int[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static long[] clone(long[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Clone an array, returning {@code null} if the input array is {@code null}
     * .
     *
     * @param obj The array to be cloned.
     * @return The clone.
     */
    public static Object[] clone(Object[] obj)
    {
        return obj == null ? null : obj.clone();
    }

    /**
     * Close some {@link AutoCloseable}s, logging any exceptions as warnings.
     *
     * @param closeables The resources.
     */
    public static void close(AutoCloseable... closeables)
    {
        close(true, closeables);
    }

    /**
     * Close some {@link AutoCloseable}, logging any exceptions as debug.
     *
     * @param closeables The resources.
     */
    public static void closeQuietly(AutoCloseable... closeables)
    {
        close(false, closeables);
    }

    /**
     * Gets whichever of the objects is non-null. If they neither is null, the
     * resolver is used to return a value. If both are null, null is returned.
     *
     * @param <T> The object type
     * @param o1 The first object
     * @param o2 The second object
     * @param resolver The resolver
     * @return The result object, or null
     */
    public static <T> T getNonNull(T o1, T o2, BinaryOperator<T> resolver)
    {
        T result = null;
        if (o1 != null && o2 != null)
        {
            result = resolver.apply(o1, o2);
        }
        else if (o1 != null)
        {
            result = o1;
        }
        else
        {
            result = o2;
        }
        return result;
    }

    /**
     * Concatenate two byte arrays.
     *
     * @param arr1 The first array.
     * @param arr2 The second array.
     * @return The combined array.
     */
    public static byte[] concatenate(byte[] arr1, byte[] arr2)
    {
        byte[] result = Arrays.copyOf(arr1, arr1.length + arr2.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    /**
     * Concatenate two int arrays.
     *
     * @param arr1 The first array.
     * @param arr2 The second array.
     * @return The combined array.
     */
    public static int[] concatenate(int[] arr1, int[] arr2)
    {
        int[] result = Arrays.copyOf(arr1, arr1.length + arr2.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    /**
     * Concatenate two long arrays.
     *
     * @param arr1 The first array.
     * @param arr2 The second array.
     * @return The combined array.
     */
    public static long[] concatenate(long[] arr1, long[] arr2)
    {
        long[] result = Arrays.copyOf(arr1, arr1.length + arr2.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    /**
     * Determine if two doubles are identical, or if they are both
     * {@link Double#NaN}.
     *
     * @param val1 The first value.
     * @param val2 The second value.
     * @return If the doubles are equal or both {@link Double#NaN}.
     */
    public static boolean equalsOrBothNaN(double val1, double val2)
    {
        return val1 == val2 || Double.isNaN(val1) && Double.isNaN(val2);
    }

    /**
     * Get a byte array output stream. If there is a cached instance for this
     * thread, reset it and return it. Otherwise create a new one.
     *
     * @return A byte array output stream.
     */
    public static ByteArrayOutputStream getByteArrayOutputStream()
    {
        SoftReference<ByteArrayOutputStream> ref = BAOS_CACHE.get();
        ByteArrayOutputStream baos;
        if (ref == null || (baos = ref.get()) == null)
        {
            baos = new ByteArrayOutputStream();
            ref = new SoftReference<>(baos);
            BAOS_CACHE.set(ref);
        }
        else
        {
            baos.reset();
        }
        return baos;
    }

    /**
     * Returns the value if it's not null, otherwise returns the default value.
     *
     * @param <T> The type of the value
     * @param value the value
     * @param defaultValue the default value
     * @return the value, or default value if the value is null
     */
    public static <T> T getValue(T value, T defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /**
     * If an object is not null, provide it to a consumer.
     *
     * @param <T> The type of the object.
     * @param obj The object.
     * @param consumer The consumer.
     */
    public static <T> void ifNotNull(T obj, Consumer<? super T> consumer)
    {
        if (obj != null)
        {
            consumer.accept(obj);
        }
    }

    /**
     * Find an element in an array.
     *
     * @param arr The array.
     * @param element The element to find.
     * @return The index of the first element in the array that matches the
     *         input, or -1 if it was not found.
     */
    public static int indexOf(char[] arr, char element)
    {
        for (int index = 0; index < arr.length; ++index)
        {
            if (arr[index] == element)
            {
                return index;
            }
        }
        return -1;
    }

    /**
     * Determine if the values in an array are sequential.
     *
     * @param ints The array.
     * @return If the values are sequential, {@code true}.
     */
    public static boolean isSequential(int[] ints)
    {
        if (ints.length > 1)
        {
            // Check that the array does not cross Integer.MAX_VALUE.
            int delta = ints[0] - ints[1];
            if (delta != 1 && delta != -1 || (delta > 0 ? ints[0] < ints[ints.length - 1] : ints[0] > ints[ints.length - 1]))
            {
                return false;
            }
            for (int index = 1; index < ints.length - 1;)
            {
                if (ints[index] - ints[++index] != delta)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine if the values in an array are sequential.
     *
     * @param longs The array.
     * @return If the values are sequential, {@code true}.
     */
    public static boolean isSequential(long[] longs)
    {
        if (longs.length > 1)
        {
            // Check that the array does not cross Long.MAX_VALUE.
            long delta = longs[0] - longs[1];
            if (delta != 1 && delta != -1
                    || (delta > 0 ? longs[0] < longs[longs.length - 1] : longs[0] > longs[longs.length - 1]))
            {
                return false;
            }
            for (int index = 1; index < longs.length - 1;)
            {
                if (longs[index] - longs[++index] != delta)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine if two references refer to different objects. This is intended
     * to make it explicit when a reference comparison is desired rather than an
     * {@link Object#equals(Object)} comparison.
     *
     * @param obj1 The first object reference.
     * @param obj2 The second object reference.
     * @return If the two references are not the same object.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public static boolean notSameInstance(Object obj1, Object obj2)
    {
        return obj1 != obj2;
    }

    /**
     * Parse the String as a double.
     *
     * @param value The String to parse.
     * @param defaultValue The value to return if parsing fails.
     * @return The parsed double value from the String.
     */
    public static double parseDouble(String value, double defaultValue)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Could not parse " + value + " as a double. Using " + defaultValue);
            }
            return defaultValue;
        }
    }

    /**
     * Parse the String as a float.
     *
     * @param value The String to parse.
     * @param defaultValue The value to return if parsing fails.
     * @return The parsed float value from the String.
     */
    public static float parseFloat(String value, float defaultValue)
    {
        try
        {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Could not parse " + value + " as a float. Using " + defaultValue);
            }
            return defaultValue;
        }
    }

    /**
     * Parse the String as an int.
     *
     * @param value The String to parse.
     * @param defaultValue The value to return if parsing fails.
     * @return The parsed int value from the String.
     */
    public static int parseInt(String value, int defaultValue)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Could not parse " + value + " as an int. Using " + defaultValue);
            }
            return defaultValue;
        }
    }

    /**
     * Parse the String as a long.
     *
     * @param value The String to parse.
     * @param defaultValue The value to return if parsing fails.
     * @return The parsed long value from the String.
     */
    public static long parseLong(String value, long defaultValue)
    {
        try
        {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Could not parse " + value + " as an long. Using " + defaultValue);
            }
            return defaultValue;
        }
    }

    /**
     * Parse a system property as a double.
     *
     * @param propName The property name.
     * @param defValue The value to return if the system property does not exist
     *            or cannot be parsed.
     * @return The value.
     */
    public static double parseSystemProperty(String propName, double defValue)
    {
        String stringValue = System.getProperty(propName);
        return stringValue == null ? defValue : parseDouble(stringValue, defValue);
    }

    /**
     * Parse a system property as a float.
     *
     * @param propName The property name.
     * @param defValue The value to return if the system property does not exist
     *            or cannot be parsed.
     * @return The value.
     */
    public static float parseSystemProperty(String propName, float defValue)
    {
        String stringValue = System.getProperty(propName);
        return stringValue == null ? defValue : parseFloat(stringValue, defValue);
    }

    /**
     * Parse a system property as an int.
     *
     * @param propName The property name.
     * @param defValue The value to return if the system property does not exist
     *            or cannot be parsed.
     * @return The value.
     */
    public static int parseSystemProperty(String propName, int defValue)
    {
        String stringValue = System.getProperty(propName);
        return stringValue == null ? defValue : parseInt(stringValue, defValue);
    }

    /**
     * Parse a system property as a long.
     *
     * @param propName The property name.
     * @param defValue The value to return if the system property does not exist
     *            or cannot be parsed.
     * @return The value.
     */
    public static long parseSystemProperty(String propName, long defValue)
    {
        String stringValue = System.getProperty(propName);
        return stringValue == null ? defValue : parseLong(stringValue, defValue);
    }

    /**
     * Get the primitive type for a wrapper class.
     *
     * @param wrapper The wrapper class.
     * @return The primitive type, or {@code null} if the input was not a
     *         wrapper class.
     */
    public static Class<?> primitiveTypeFor(Class<?> wrapper)
    {
        if (wrapper == Integer.class)
        {
            return Integer.TYPE;
        }
        else if (wrapper == Boolean.class)
        {
            return Boolean.TYPE;
        }
        else if (wrapper == Byte.class)
        {
            return Byte.TYPE;
        }
        else if (wrapper == Character.class)
        {
            return Character.TYPE;
        }
        else if (wrapper == Short.class)
        {
            return Short.TYPE;
        }
        else if (wrapper == Long.class)
        {
            return Long.TYPE;
        }
        else if (wrapper == Float.class)
        {
            return Float.TYPE;
        }
        else if (wrapper == Double.class)
        {
            return Double.TYPE;
        }
        else if (wrapper == Void.class)
        {
            return Void.TYPE;
        }
        else
        {
            return null;
        }
    }

    /**
     * Create a new array that is a copy of the input array minus an object. If
     * the array does not contain the object, it will be returned as-is.
     *
     * @param arr The array.
     * @param obj The object to be removed.
     *
     * @param <T> The type of the array.
     * @return The new array.
     */
    public static <T> T[] removeFromArray(T[] arr, Object obj)
    {
        int foundIndex = -1;
        for (int index = 0; index < arr.length; ++index)
        {
            if (arr[index] == null ? obj == null : arr[index].equals(obj))
            {
                foundIndex = index;
                break;
            }
        }
        T[] newArr;
        if (foundIndex >= 0)
        {
            newArr = Arrays.copyOf(arr, arr.length - 1);
            if (foundIndex < arr.length - 1)
            {
                System.arraycopy(arr, foundIndex + 1, newArr, foundIndex, arr.length - foundIndex - 1);
            }
        }
        else
        {
            newArr = arr;
        }
        return newArr;
    }

    /**
     * Determine if two references refer to the same object. This is intended to
     * make it explicit when a reference comparison is desired rather than an
     * {@link Object#equals(Object)} comparison.
     *
     * @param obj1 The first object reference.
     * @param obj2 The second object reference.
     * @return If the two references are equal.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public static boolean sameInstance(Object obj1, Object obj2)
    {
        return obj1 == obj2;
    }

    /**
     * Estimate the memory size of an {@link ArrayList}.
     *
     * @param arrayLength The length of the array embedded in the list.
     * @return The size in bytes.
     */
    public static int sizeOfArrayListBytes(int arrayLength)
    {
        int listObjSize = MathUtil.roundUpTo(
                Constants.OBJECT_SIZE_BYTES + 2 * Constants.INT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);
        int arrayObjSize = MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + arrayLength * Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);
        return listObjSize + arrayObjSize;
    }

    /**
     * Estimate the memory size of an {@link EnumMap} that uses the provided
     * {@link Enum}.
     *
     * @param <K> The enum type.
     * @param type The enum type.
     * @return The size in bytes.
     */
    public static <K extends Enum<K>> int sizeOfEnumMapBytes(Class<K> type)
    {
        int enumLength = EnumSet.allOf(type).size();
        int valueArraySize = MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES * enumLength,
                Constants.MEMORY_BLOCK_SIZE_BYTES);

        return BASE_ENUM_MAP_SIZE_BYTES + valueArraySize;
    }

    /**
     * Estimate the memory size of a {@link LinkedList}.
     *
     * @param size The size of the list.
     * @return The size in bytes.
     */
    public static int sizeOfLinkedListBytes(int size)
    {
        int listObjSize = MathUtil.roundUpTo(
                Constants.OBJECT_SIZE_BYTES + 2 * Constants.INT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);
        int entrySize = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + 3 * Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES);
        return listObjSize + (size + 1) * entrySize;
    }

    /**
     * Sum the numbers in a collection as doubles.
     *
     * @param collection The collection of numbers.
     * @return The total.
     */
    public static double sumDouble(Collection<? extends Number> collection)
    {
        double total = 0.;
        for (Number number : collection)
        {
            total += number.doubleValue();
        }
        return total;
    }

    /**
     * Sum the numbers in a collection as floats.
     *
     * @param collection The collection of numbers.
     * @return The total.
     */
    public static float sumFloat(Collection<? extends Number> collection)
    {
        float total = 0f;
        for (Number number : collection)
        {
            total += number.floatValue();
        }
        return total;
    }

    /**
     * Sum the numbers in a collection as ints.
     *
     * @param collection The collection of numbers.
     * @return The total.
     */
    public static int sumInt(Collection<? extends Number> collection)
    {
        int total = 0;
        for (Number number : collection)
        {
            total += number.intValue();
        }
        return total;
    }

    /**
     * Sum the numbers in a collection as longs.
     *
     * @param collection The collection of numbers.
     * @return The total.
     */
    public static long sumLong(Collection<? extends Number> collection)
    {
        long total = 0L;
        for (Number number : collection)
        {
            total += number.longValue();
        }
        return total;
    }

    /**
     * Determine if the sum of two longs is too large.
     *
     * @param a The first addend.
     * @param b The second addend.
     * @return If an overflow will occur, {@code true}.
     */
    public static boolean sumOverflow(long a, long b)
    {
        return b > Long.MAX_VALUE - a;
    }

    /**
     * Sum the numbers in a collection as ints, rounding along the way.
     *
     * @param collection The collection of numbers.
     * @return The total.
     */
    public static int sumRoundInt(Collection<? extends Number> collection)
    {
        int total = 0;
        for (Number number : collection)
        {
            total += Math.round(number.floatValue());
        }
        return total;
    }

    /**
     * Sum the numbers in a collection as ints, rounding along the way.
     *
     * @param collection The collection of numbers.
     * @return The total.
     */
    public static long sumRoundLong(Collection<? extends Number> collection)
    {
        long total = 0;
        for (Number number : collection)
        {
            total += Math.round(number.doubleValue());
        }
        return total;
    }

    /**
     * Run a runnable the given number of times.
     *
     * @param times How many times.
     * @param r The runnable.
     */
    public static void times(int times, Runnable r)
    {
        for (int index = 0; index < times; ++index)
        {
            r.run();
        }
    }

    /**
     * Get the unique values from an array. The array must already be sorted.
     *
     * @param arr The sorted input array.
     * @return The unique values.
     */
    public static int[] unique(int[] arr)
    {
        if (arr.length < 2)
        {
            return arr;
        }

        int lastDistinctIndex = 0;
        int lastId = arr[0];
        int[] distinctIds = new int[arr.length];
        distinctIds[0] = lastId;
        for (int index = 1; index < arr.length;)
        {
            int id = arr[index++];
            if (id != lastId)
            {
                lastId = id;
                distinctIds[++lastDistinctIndex] = id;
            }
        }

        if (lastDistinctIndex < distinctIds.length - 1)
        {
            distinctIds = Arrays.copyOf(distinctIds, lastDistinctIndex + 1);
        }

        return distinctIds;
    }

    /**
     * Get the unique values from an array. This version can handle an array
     * that is not in order.
     *
     * @param arr The unsorted input array.
     * @return The unique (sorted) values.
     */
    public static int[] uniqueUnsorted(int[] arr)
    {
        int[] sorted = arr.clone();
        Arrays.sort(sorted);
        return unique(sorted);
    }

    /**
     * Wait on the given object's monitor. This should always be called from
     * within a {@code synchronize} block on the given object, and within a loop
     * that checks the condition being waited upon.
     *
     * @param obj The object to wait on.
     * @return {@code true} if the wait was interrupted
     * @throws IllegalMonitorStateException If the current thread does not have
     *             a lock on the object's monitor.
     */
    @SuppressFBWarnings("WA_NOT_IN_LOOP")
    public static boolean wait(Object obj)
    {
        try
        {
            obj.wait();
            return false;
        }
        catch (InterruptedException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Interrupted on wait: " + e, e);
            }
            return true;
        }
    }

    /**
     * Close some {@link AutoCloseable}s, logging any exceptions as warnings.
     *
     * @param warn If any warning messages should be logged.
     * @param closeables The resources.
     */
    @SuppressWarnings({ "PMD.AvoidInstanceofChecksInCatchClause", "PMD.UnusedPrivateMethod" })
    private static void close(boolean warn, AutoCloseable... closeables)
    {
        Throwable t = null;
        AutoCloseable thrower = null;
        for (AutoCloseable c : closeables)
        {
            if (c != null)
            {
                try
                {
                    c.close();
                }
                catch (Exception | Error e)
                {
                    if (warn)
                    {
                        LOGGER.warn("Failed to close Closeable: " + e);
                    }
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Failed to close Closeable: " + e, e);
                    }

                    if (t == null)
                    {
                        t = e;
                        thrower = c;
                    }
                    // Give precedence to Errors and RuntimeExceptions.
                    else if (e instanceof Error && !(t instanceof Error)
                            || e instanceof RuntimeException && !(t instanceof Error || t instanceof RuntimeException))
                    {
                        e.addSuppressed(t);
                        t = e;
                        thrower = c;
                    }
                    else
                    {
                        t.addSuppressed(e);
                    }
                }
            }
        }

        if (t instanceof Error)
        {
            throw (Error)t;
        }

        // Suppress RuntimeExceptions for non-Closeables.
        else if (t instanceof RuntimeException && thrower instanceof Closeable)
        {
            throw (RuntimeException)t;
        }
    }

    /** Disallow instantiation, because. */
    private Utilities()
    {
    }
}
