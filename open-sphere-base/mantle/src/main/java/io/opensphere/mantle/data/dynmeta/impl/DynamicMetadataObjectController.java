package io.opensphere.mantle.data.dynmeta.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnObjectController.
 */
public class DynamicMetadataObjectController extends AbstractDynamicMetadataController<Object>
{
    /**
     * Instantiates a new dynamic column object controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataObjectController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public Class<?> getColumnClass()
    {
        return Object.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return false;
    }
}
