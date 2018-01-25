package io.opensphere.mantle.data.cache.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.impl.encoder.DiskEncodeHelper;
import io.opensphere.mantle.data.impl.encoder.EncodeType;

/**
 * The Class DiskEncodeHelper.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public final class DiskCacheEncodeHelper
{
    /**
     * Encode loaded element data.
     *
     * @param oos the oos
     * @param led the led
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeLoadedElementData(ObjectOutputStream oos, LoadedElementData led) throws IOException
    {
        DiskEncodeHelper.encodeOriginId(oos, led.getOriginId());
        DiskEncodeHelper.encodeMetaDataList(oos, led.getMetaData());
        DiskEncodeHelper.encodeMapGeometrySupport(oos, led.getMapGeometrySupport());
    }

    /**
     * Encode meta data list as a stand-alone item.
     *
     * @param oos the oos
     * @param aList the a list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeMetaDataList(ObjectOutputStream oos, List<Object> aList) throws IOException
    {
        if (aList == null)
        {
            DiskEncodeHelper.encodeNull(oos);
        }
        else
        {
            if (aList instanceof DynamicMetaDataList)
            {
                encodeDynamicMetaDataList(oos, (DynamicMetaDataList)aList);
            }
            else
            {
                DiskEncodeHelper.encodeMetaDataListInternal(oos, aList);
            }
        }
    }

    /**
     * Encode dynamic meta data list.
     *
     * @param oos the oos
     * @param dmdl the dmdl
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeDynamicMetaDataList(ObjectOutputStream oos, DynamicMetaDataList dmdl) throws IOException
    {
        EncodeType.DYNAMIC_METADATALIST.encode(oos);
        oos.writeInt(dmdl.getTypeHashCode());
        dmdl.encode(oos);
    }

    /**
     * Instantiates a new disk encode helper.
     */
    private DiskCacheEncodeHelper()
    {
    }
}
