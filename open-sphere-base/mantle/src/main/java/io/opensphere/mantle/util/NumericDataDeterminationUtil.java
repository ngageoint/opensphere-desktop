package io.opensphere.mantle.util;

import java.util.Collection;

import io.opensphere.core.util.Utilities;

/**
 * The Class NumericDataDeterminationUtil.
 */
public final class NumericDataDeterminationUtil
{
    /**
     * Returns true if a sample of values is full of "numeric" data i.e. it can
     * be converted into a Double via Double.parseDouble() if all of the
     * non-null values are convertible it is considered numeric so there may
     * still be null values in the array.
     *
     * @param <T> the generic type to be tested
     * @param sampleCollection the sample collection of data to be tested
     * @return {@link NumericDetermination}
     */
    public static <T> NumericDetermination isSampleNumeric(Collection<T> sampleCollection)
    {
        Utilities.checkNull(sampleCollection, "sampleCollection");
        if (sampleCollection.isEmpty())
        {
            return NumericDetermination.UNDETERMINED;
        }
        return isSampleNumeric(sampleCollection.toArray(sampleCollection.toArray()));
    }

    /**
     * Returns true if a sample of values is full of "numeric" data i.e. it can
     * be converted into a Double via Double.parseDouble() if all of the
     * non-null values are convertible it is considered numeric so there may
     * still be null values in the array.
     *
     * @param <T> the generic type to be tested
     * @param sampleArray the sample array of data to be tested
     * @return {@link NumericDetermination}
     */
    public static <T> NumericDetermination isSampleNumeric(T[] sampleArray)
    {
        Utilities.checkNull(sampleArray, "sampleArray");

        if (sampleArray.length == 0)
        {
            return NumericDetermination.UNDETERMINED;
        }

        // If the class is a sub-type of number then it is a number
        // and we can break out because we already know.
        if (derivesFromNumber(sampleArray))
        {
            return NumericDetermination.NUMERIC;
        }

        // Start checking values, stop if we find an example of a non-numeric
        // non-null/empty value.
        boolean allConvertToNumbers = true;
        boolean allAreNull = true;
        for (T sample : sampleArray)
        {
            if (sample != null)
            {
                allAreNull = false;
                String sampleStr = sample.toString();
                if (sampleStr.isEmpty() || "null".equalsIgnoreCase(sampleStr))
                {
                    continue;
                }

                try
                {
                    Double.parseDouble(sampleStr);
                }
                catch (NumberFormatException e)
                {
                    allConvertToNumbers = false;
                    break;
                }
            }
        }

        if (allAreNull)
        {
            return NumericDetermination.UNDETERMINED;
        }

        if (!allConvertToNumbers)
        {
            return NumericDetermination.NOT_NUMERIC;
        }

        return NumericDetermination.NUMERIC;
    }

    /**
     * Derives from number.
     *
     * @param <T> the generic type
     * @param sampleArray the sample array
     * @return true, if successful
     */
    private static <T> boolean derivesFromNumber(T[] sampleArray)
    {
        T nonNullSample = null;
        for (T sample : sampleArray)
        {
            if (sample != null)
            {
                nonNullSample = sample;
                break;
            }
        }
        return nonNullSample != null && Number.class.isAssignableFrom(nonNullSample.getClass());
    }

    /**
     * Instantiates a new numeric data determination util.
     */
    private NumericDataDeterminationUtil()
    {
        // Don't allow instantiation.
    }

    /**
     * The Enum NumericDetermination.
     */
    public enum NumericDetermination
    {
        /** NOT_NUMERIC. */
        NOT_NUMERIC,

        /** NUMERIC. */
        NUMERIC,

        /** UNDETERMINED. */
        UNDETERMINED
    }
}
