package io.opensphere.core.units.length;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.opensphere.core.units.UnitsParseException;

/**
 * Produce an instance of the correct unit type from a string which contains
 * both the value and the units.
 */
public final class ImbeddedUnitsLengthUtilities
{
    /**
     * A regular expression pattern to match floating point numbers. Using a
     * regular expression is slower than the method used by the units provider,
     * but allows us to be more permissive when looking for a numeric value
     * imbedded within the string.
     */
    private static final Pattern FloatingPointPattern = Pattern.compile("[+-]?[0-9]*\\.*[0-9]*[eE]?[0-9]*");

    /**
     * Determine the length contained in the string. If no recognized units are
     * given in the string, meters will be assumed.
     *
     * @param length The string which represents the measurement.
     * @return The length or 0 meters if no length can be determined.
     */
    public static Length getLength(String length)
    {
        Matcher mat = FloatingPointPattern.matcher(length);
        if (mat.matches())
        {
            return new Meters(Double.parseDouble(length));
        }
        else
        {
            try
            {
                // This should produce the correct length type when the short
                // label matches something that we know about.
                return new LengthUnitsProvider().fromShortLabelString(length);
            }
            catch (UnitsParseException e)
            {
                // As a last resort, look for anything matching a floating point
                // value and assume meters for the units.
                mat.reset();
                if (mat.find() && mat.end() > 0)
                {
                    double value = Double.parseDouble(length.substring(mat.start(), mat.end()));
                    return new Meters(value);
                }
            }
        }
        return new Meters(0);
    }

    /** Disallow instantiation. */
    private ImbeddedUnitsLengthUtilities()
    {
    }
}
