package io.opensphere.mantle.data.dynmeta.impl;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnStringController.
 */
public class DynamicMetadataStringController extends AbstractDynamicMetadataController<String>
{
    /**
     * Instantiates a new dynamic column string controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataStringController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public void appendValue(List<Long> cacheIds, Object valueToAppend, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        String stringValueToAppend = validateValueIsAcceptable(valueToAppend);
        if (!cacheIds.isEmpty() && stringValueToAppend != null)
        {
            for (Long id : cacheIds)
            {
                String oldStringValue = getIdToValueMap().get(id.longValue());
                if (oldStringValue == null)
                {
                    getIdToValueMap().put(id.longValue(), stringValueToAppend);
                }
                else
                {
                    getIdToValueMap().put(id.longValue(), oldStringValue + stringValueToAppend);
                }
            }
            fireChangeEvent(cacheIds, source);
        }
    }

    @Override
    public void appendValue(long elementId, Object valueToAppend, Object source)
    {
        String stringValueToAppend = validateValueIsAcceptable(valueToAppend);
        String oldStringValue = getIdToValueMap().get(elementId);
        if (oldStringValue != null)
        {
            getIdToValueMap().put(elementId, oldStringValue + stringValueToAppend);
            fireChangeEvent(source, elementId);
        }
        else
        {
            if (stringValueToAppend != null)
            {
                getIdToValueMap().put(elementId, stringValueToAppend);
                fireChangeEvent(source, elementId);
            }
        }
    }

    @Override
    public Class<?> getColumnClass()
    {
        return String.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return true;
    }
}
