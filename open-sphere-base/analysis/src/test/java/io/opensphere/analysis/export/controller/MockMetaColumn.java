package io.opensphere.analysis.export.controller;

import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.mantle.data.element.DataElement;

/**
 * a Mock {@link MetaColumn} used for testing.
 */
public class MockMetaColumn extends MetaColumn<Object>
{
    /**
     * Constructs a new mock {@link MetaColumn}.
     *
     * @param columnIdentifier Used to identify the column.
     */
    public MockMetaColumn(String columnIdentifier)
    {
        super(columnIdentifier, Object.class, true);
    }

    @Override
    public Object getValue(int rowIndex, DataElement dataElement)
    {
        return null;
    }
}
