package io.opensphere.csvcommon.format;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

/**
 * Formats a cell string value to the appropriate value.
 *
 */
public interface CellFormatter
{
    /**
     * Formats the cell value to its appropriate object value.
     *
     * @param cellValue The cell value from the csv file.
     * @param format The format the cell value is in.
     * @return The converted value.
     * @throws ParseException Thrown if the cell value could not be formatted
     *             with the specified format.
     */
    Object formatCell(String cellValue, String format) throws ParseException;

    /**
     * Formats the object value to the string value.
     *
     * @param value The value to format to a string.
     * @param format The format to format the string value to.
     * @return The string representation.
     */
    String fromObjectValue(Object value, String format);

    /**
     * Gets the best possible format for the list of values, or null if one
     * could not be found.
     *
     * @param values The values to get the format for.
     * @return The format that matches the values or null if one could not be
     *         found.
     */
    String getFormat(List<String> values);

    /**
     * Gets all the currently known possible formats for this cell formatter.
     *
     * @return The collection of known formats.
     */
    Collection<String> getKnownPossibleFormats();

    /**
     * Gets the current format for this value from the system, or null if there
     * isn't one.
     *
     * @return The system format.
     */
    String getSystemFormat();
}
