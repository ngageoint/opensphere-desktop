package io.opensphere.core.util.swing.table;

import java.util.List;

/**
 * A provider of row values.
 *
 * @param <T> the type of the data object
 */
@FunctionalInterface
public interface RowValuesProvider<T>
{
    /**
     * Gets the row values for the given row and data object.
     *
     * @param rowIndex the row index
     * @param dataObject the data object
     * @return the row values
     */
    List<?> getValues(int rowIndex, T dataObject);
}
