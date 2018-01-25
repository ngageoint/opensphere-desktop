package io.opensphere.mantle.data.accessor;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.accessor.PropertyArrayAccessor;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;

/**
 * The Class MapDataElementPropertyListAccessor.
 */
public class DataElementPropertyListAccessor extends PropertyArrayAccessor<DataElement>
{
    /** The key names. */
    private final List<String> myKeyNames;

    /**
     * Instantiates a new map data element property list accessor.
     *
     * @param dti the DataTypeInfo
     */
    public DataElementPropertyListAccessor(DataTypeInfo dti)
    {
        this(dti.getMetaDataInfo());
    }

    /**
     * Instantiates a new map data element property list accessor.
     *
     * @param metaDataInfo the metaDataInfo
     */
    public DataElementPropertyListAccessor(MetaDataInfo metaDataInfo)
    {
        super(metaDataInfo == null || metaDataInfo.getPropertyArrayDescriptor() == null
                ? MetaDataInfo.EMPTY_PROPERTY_ARRAY_DESCRIPTOR : metaDataInfo.getPropertyArrayDescriptor());
        myKeyNames = metaDataInfo == null ? Collections.<String>emptyList() : metaDataInfo.getKeyNames();
    }

    @Override
    public Object[] access(DataElement input)
    {
        Object[] propValues = new Object[myKeyNames.size()];
        if (input.getMetaData() != null)
        {
            for (int index = 0; index < myKeyNames.size(); ++index)
            {
                propValues[index] = input.getMetaData().getValue(myKeyNames.get(index));
            }
        }
        return propValues;
    }
}
