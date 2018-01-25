package io.opensphere.analysis.base.model;

import java.util.Date;

import io.opensphere.analysis.util.DataTypeUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/** Data type enum. */
public enum DataType
{
    /** Numeric data. */
    NUMBER,

    /** Textual or enum data. */
    STRING,

    /** Date/time data. */
    DATE;

    /**
     * Gets the data type for the given column/layer.
     *
     * @param column the column
     * @param layer the layer
     * @return the data type
     */
    public static DataType toDataType(String column, DataTypeInfo layer)
    {
        DataType dataType;
        Class<?> keyClass = layer.getMetaDataInfo().getKeyClassType(column);
        if (Date.class.isAssignableFrom(keyClass) || layer.getMetaDataInfo().getSpecialTypeForKey(column) == TimeKey.DEFAULT)
        {
            dataType = DataType.DATE;
        }
        else if (DataTypeUtilities.isNumeric(keyClass))
        {
            dataType = DataType.NUMBER;
        }
        else
        {
            dataType = DataType.STRING;
        }
        return dataType;
    }
}
