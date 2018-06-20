package io.opensphere.mantle.data.dynmeta.impl;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnStringController.
 */
public class DynamicMetadataIntegerController extends AbstractDynamicMetadataController<Integer>
{
    /**
     * Instantiates a new dynamic column string controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataIntegerController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public void appendValue(List<Long> cacheIds, Object valueToAppend, Object source)
    {
        Integer intValueToAppend = validateValueIsAcceptable(valueToAppend);
        Utilities.checkNull(cacheIds, "cacheIds");
        if (!cacheIds.isEmpty() && intValueToAppend != null)
        {
            for (Long id : cacheIds)
            {
                Integer oldIntValue = getIdToValueMap().get(id.longValue());
                if (oldIntValue == null)
                {
                    getIdToValueMap().put(id.longValue(), intValueToAppend);
                }
                else
                {
                    getIdToValueMap().put(id.longValue(), Integer.valueOf(oldIntValue.intValue() + intValueToAppend.intValue()));
                }
            }
            fireChangeEvent(cacheIds, source);
        }
    }

    @Override
    public void appendValue(long elementId, Object valueToAppend, Object source)
    {
        Integer intValueToAppend = validateValueIsAcceptable(valueToAppend);
        Integer oldIntValue = getIdToValueMap().get(elementId);
        if (oldIntValue != null)
        {
            getIdToValueMap().put(elementId, Integer.valueOf(oldIntValue.intValue() + intValueToAppend.intValue()));
            fireChangeEvent(source, elementId);
        }
        else
        {
            if (intValueToAppend != null)
            {
                getIdToValueMap().put(elementId, intValueToAppend);
                fireChangeEvent(source, elementId);
            }
        }
    }

    @Override
    public Class<?> getColumnClass()
    {
        return Integer.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return true;
    }
}
