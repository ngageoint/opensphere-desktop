package io.opensphere.wfs.filter;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wfs.util.WFSConstants;

/**
 * Gets the time fields for a given data type.
 */
public final class WFSTimeFieldGetter
{
    /**
     * Gets the start and end time fields for the specified data type.
     *
     * @param dataType The data type to get time fields for.
     * @return The start and end time field names.
     */
    public static Pair<String, String> getTimeFieldNames(DataTypeInfo dataType)
    {
        String startTimeName = null;
        String endTimeName = dataType.getMetaDataInfo().getKeyForSpecialType(EndTimeKey.DEFAULT);
        if (StringUtils.isNotEmpty(endTimeName))
        {
            startTimeName = dataType.getMetaDataInfo().getTimeKey();
        }
        else
        {
            final String name = ((WFSMetaDataInfo)dataType.getMetaDataInfo()).isDynamicTime()
                    || dataType.getMetaDataInfo().getTimeKey() == null ? WFSConstants.DEFAULT_TIME_QUERY_KEY
                            : dataType.getMetaDataInfo().getTimeKey();
            startTimeName = name;
            endTimeName = name;
        }

        return new Pair<String, String>(startTimeName, endTimeName);
    }

    /**
     * Get a transform that will return the proper time field name when given a
     * field associated with the time key in the given type.
     *
     * @param dataTypeInfo The type.
     * @return The transform.
     */
    public static Function<String, String> getTimeFieldTransform(DataTypeInfo dataTypeInfo)
    {
        return field -> dataTypeInfo.getMetaDataInfo().getSpecialTypeForKey(field) instanceof TimeKey
                ? getTimeFieldNames(dataTypeInfo).getFirstObject() : field;
    }

    /** Disallow instantiation. */
    private WFSTimeFieldGetter()
    {
    }
}
