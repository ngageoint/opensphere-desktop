package io.opensphere.mantle.data.dynmeta.impl;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnStringController.
 */
public class DynamicMetadataLongController extends AbstractDynamicMetadataController<Long>
{
    /**
     * Instantiates a new dynamic column string controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataLongController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public void appendValue(List<Long> cacheIds, Object valueToAppend, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        Long longValToAppend = validateValueIsAcceptable(valueToAppend);
        if (!cacheIds.isEmpty() && longValToAppend != null)
        {
            for (Long id : cacheIds)
            {
                Long oldLongValue = getIdToValueMap().get(id);
                if (oldLongValue == null)
                {
                    getIdToValueMap().put(id, longValToAppend);
                }
                else
                {
                    getIdToValueMap().put(id, oldLongValue + longValToAppend);
                }
            }
            fireChangeEvent(cacheIds, source);
        }
    }

    @Override
    public void appendValue(long elementId, Object valueToAppend, Object source)
    {
        Long longValToAppend = validateValueIsAcceptable(valueToAppend);
        Long oldLongValue = getIdToValueMap().get(elementId);
        if (oldLongValue != null)
        {
            getIdToValueMap().put(elementId, oldLongValue + longValToAppend);
            fireChangeEvent(source, elementId);
        }
        else
        {
            if (longValToAppend != null)
            {
                getIdToValueMap().put(elementId, longValToAppend);
                fireChangeEvent(source, elementId);
            }
        }
    }

    @Override
    public Class<?> getColumnClass()
    {
        return Long.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return true;
    }
}
