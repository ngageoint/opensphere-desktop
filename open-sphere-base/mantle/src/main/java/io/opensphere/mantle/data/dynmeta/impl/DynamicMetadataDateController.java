package io.opensphere.mantle.data.dynmeta.impl;

import java.util.Date;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnDateController.
 */
public class DynamicMetadataDateController extends AbstractDynamicMetadataController<Date>
{
    /**
     * Instantiates a new dynamic column date controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataDateController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public Class<?> getColumnClass()
    {
        return Date.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return false;
    }
}
