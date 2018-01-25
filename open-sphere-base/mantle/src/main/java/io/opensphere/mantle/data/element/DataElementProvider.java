package io.opensphere.mantle.data.element;

import java.util.Iterator;
import java.util.List;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Interface DataElementProvider.
 */
public interface DataElementProvider extends Iterator<DataElement>
{
    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    DataTypeInfo getDataTypeInfo();

    /**
     * Gets the error messages if hadError returns true or null if no error
     * messages.
     *
     * These messages will be displayed to the user message dialog and so should
     * not be trace or debug type messages.
     *
     * @return the error messages
     */
    List<String> getErrorMessages();

    /**
     * Gets the error messages if hadWarning returns true or null if no warning
     * messages.
     *
     * These messages will be displayed to the user message dialog and so should
     * not be trace or debug type messages.
     *
     * @return the error messages
     */
    List<String> getWarningMessages();

    /**
     * Had error.
     *
     * @return true if an error has been encountered while providing the
     *         elements.
     */
    boolean hadError();

    /**
     * Had warning.
     *
     * @return true if a warning was issued while providing the elements.
     */
    boolean hadWarning();
}
