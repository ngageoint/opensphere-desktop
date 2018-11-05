package io.opensphere.mantle.iconproject.panels.transform;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.util.StringConverter;

/**
 * A converter used to hide all decimal values from a spinner.
 */
final class NoDecimalStringConverter extends StringConverter<Double>
{
    /** A formatter used to alter the presentation of a numeric value. */
    private static final DecimalFormat FORMAT = new DecimalFormat("#");

    @Override
    public String toString(Double value)
    {
        if (value == null)
        {
            return "";
        }
        return FORMAT.format(value);
    }

    @Override
    public Double fromString(String value)
    {
        try
        {
            // If the specified value is null or zero-length, return null
            if (value == null)
            {
                return null;
            }

            String localValue = value.trim();
            if (localValue.length() < 1)
            {
                return null;
            }

            // Perform the requested parsing
            return FORMAT.parse(localValue).doubleValue();
        }
        catch (ParseException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}