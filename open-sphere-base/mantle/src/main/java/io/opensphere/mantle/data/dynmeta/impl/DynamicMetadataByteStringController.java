package io.opensphere.mantle.data.dynmeta.impl;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ByteString;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnByteStringController.
 */
public class DynamicMetadataByteStringController extends AbstractDynamicMetadataController<ByteString>
{
    /**
     * Instantiates a new dynamic column byte string controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataByteStringController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public void appendValue(List<Long> cacheIds, Object valueToAppend, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        ByteString tValueToAppend = validateValueIsAcceptable(valueToAppend);
        if (!cacheIds.isEmpty() && tValueToAppend != null)
        {
            for (Long id : cacheIds)
            {
                ByteString oldValue = getIdToValueMap().get(id.longValue());
                if (oldValue == null)
                {
                    getIdToValueMap().put(id.longValue(), tValueToAppend);
                }
                else
                {
                    getIdToValueMap().put(id.longValue(), new ByteString(oldValue.toString() + tValueToAppend.toString()));
                }
            }
            fireChangeEvent(cacheIds, source);
        }
    }

    @Override
    public void appendValue(long elementId, Object valueToAppend, Object source)
    {
        ByteString tValueToAppend = validateValueIsAcceptable(valueToAppend);
        ByteString oldValue = getIdToValueMap().get(elementId);
        if (oldValue != null)
        {
            getIdToValueMap().put(elementId, new ByteString(oldValue.toString() + tValueToAppend.toString()));
            fireChangeEvent(source, elementId);
        }
        else
        {
            if (tValueToAppend != null)
            {
                getIdToValueMap().put(elementId, tValueToAppend);
                fireChangeEvent(source, elementId);
            }
        }
    }

    @Override
    public Class<?> getColumnClass()
    {
        return ByteString.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return true;
    }
}
