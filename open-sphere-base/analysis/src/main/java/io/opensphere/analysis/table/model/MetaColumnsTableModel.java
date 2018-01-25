package io.opensphere.analysis.table.model;

import java.util.List;

import javax.swing.table.TableModel;

import io.opensphere.mantle.data.element.DataElement;

/**
 * Interface to a table model that contains {@link DataElement} and also
 * contains additional {@link MetaColumn} pertaining to the individual
 * {@link DataElement}.
 */
public interface MetaColumnsTableModel extends TableModel
{
    /**
     * Returns a column given its name. Implementation is naive so this should
     * be overridden if this method is to be called often. This method is not in
     * the <code>TableModel</code> interface and is not used by the
     * <code>JTable</code>.
     *
     * @param columnName string containing name of column to be located
     * @return the column with <code>columnName</code>, or -1 if not found
     */
    int findColumn(String columnName);

    /**
     * Gets the data object at the given row index.
     *
     * @param rowIndex the row index
     * @return the data object
     */
    DataElement getDataAt(int rowIndex);

    /**
     * Gets the metaColumns.
     *
     * @return the metaColumns
     */
    List<MetaColumn<?>> getMetaColumns();
}
