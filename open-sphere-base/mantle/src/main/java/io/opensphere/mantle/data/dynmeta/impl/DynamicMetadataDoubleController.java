package io.opensphere.mantle.data.dynmeta.impl;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnStringController.
 */
public class DynamicMetadataDoubleController extends AbstractDynamicMetadataController<Double>
{
    /**
     * Instantiates a new dynamic column string controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataDoubleController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public void appendValue(List<Long> cacheIds, Object valueToAppend, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        Double doubleValToAppend = validateValueIsAcceptable(valueToAppend);
        if (!cacheIds.isEmpty() && doubleValToAppend != null)
        {
            for (Long id : cacheIds)
            {
                Double oldDoubleValue = getIdToValueMap().get(id);
                if (oldDoubleValue == null)
                {
                    getIdToValueMap().put(id, doubleValToAppend);
                }
                else
                {
                    getIdToValueMap().put(id, oldDoubleValue + doubleValToAppend);
                }
            }
            fireChangeEvent(cacheIds, source);
        }
    }

    @Override
    public void appendValue(long elementId, Object valueToAppend, Object source)
    {
        Double doubleValToAppend = validateValueIsAcceptable(valueToAppend);
        Double oldDoubleValue = getIdToValueMap().get(elementId);
        if (oldDoubleValue != null)
        {
            getIdToValueMap().put(elementId, oldDoubleValue + doubleValToAppend);
            fireChangeEvent(source, elementId);
        }
        else
        {
            if (doubleValToAppend != null)
            {
                getIdToValueMap().put(elementId, doubleValToAppend);
                fireChangeEvent(source, elementId);
            }
        }
    }

    @Override
    public Class<?> getColumnClass()
    {
        return Double.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return true;
    }
}
