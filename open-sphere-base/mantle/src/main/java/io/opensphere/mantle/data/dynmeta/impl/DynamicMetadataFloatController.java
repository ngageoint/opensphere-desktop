package io.opensphere.mantle.data.dynmeta.impl;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnStringController.
 */
public class DynamicMetadataFloatController extends AbstractDynamicMetadataController<Float>
{
    /**
     * Instantiates a new dynamic column string controller.
     *
     * @param tb the tb
     * @param columnIndex the column index
     * @param columnName the column name
     * @param dti the dti
     */
    public DynamicMetadataFloatController(Toolbox tb, int columnIndex, String columnName, DataTypeInfo dti)
    {
        super(tb, columnIndex, columnName, dti);
    }

    @Override
    public void appendValue(List<Long> cacheIds, Object valueToAppend, Object source)
    {
        Utilities.checkNull(cacheIds, "cacheIds");
        Float fValueToAppend = validateValueIsAcceptable(valueToAppend);
        if (!cacheIds.isEmpty() && fValueToAppend != null)
        {
            for (Long id : cacheIds)
            {
                Float oldFloatValue = getIdToValueMap().get(id);
                if (oldFloatValue == null)
                {
                    getIdToValueMap().put(id, fValueToAppend);
                }
                else
                {
                    getIdToValueMap().put(id, oldFloatValue + fValueToAppend);
                }
            }
            fireChangeEvent(cacheIds, source);
        }
    }

    @Override
    public void appendValue(long elementId, Object valueToAppend, Object source)
    {
        Float fValueToAppend = validateValueIsAcceptable(valueToAppend);
        Float oldFloatValue = getIdToValueMap().get(elementId);
        if (oldFloatValue != null)
        {
            getIdToValueMap().put(elementId, oldFloatValue + fValueToAppend);
            fireChangeEvent(source, elementId);
        }
        else
        {
            if (fValueToAppend != null)
            {
                getIdToValueMap().put(elementId, fValueToAppend);
                fireChangeEvent(source, elementId);
            }
        }
    }

    @Override
    public Class<?> getColumnClass()
    {
        return Float.class;
    }

    @Override
    public boolean supportsAppend()
    {
        return true;
    }
}
