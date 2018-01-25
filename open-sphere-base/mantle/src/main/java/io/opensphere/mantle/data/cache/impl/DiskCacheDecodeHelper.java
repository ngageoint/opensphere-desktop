package io.opensphere.mantle.data.cache.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.mantle.data.element.DynamicMetaDataList;
import io.opensphere.mantle.data.impl.encoder.DiskDecodeHelper;
import io.opensphere.mantle.data.impl.encoder.EncodeType;

/**
 * The Class DiskDecodeHelper.
 */
public final class DiskCacheDecodeHelper
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DiskCacheDecodeHelper.class);

    /**
     * Decode dynamic meta data list.
     *
     * @param ois the ois
     * @return the dynamic meta data list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static DynamicMetaDataList decodeDynamicMetaDataList(ObjectInputStream ois) throws IOException
    {
        DynamicMetaDataList dmdl = null;
        int typeHashCode = ois.readInt();
        Class<DynamicMetaDataList> cl = DynamicMetaDataClassRegistry.getInstance().getDynamicClassForHashCode(typeHashCode);
        try
        {
            dmdl = cl.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            LOGGER.error("Failed to create DynamicMetaDataList", e);
        }
        if (dmdl != null)
        {
            dmdl.decode(ois);
        }
        return dmdl;
    }

    /**
     * Decode loaded element data.
     *
     * @param oos the oos
     * @return the loaded element data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static LoadedElementData decodeLoadedElementData(ObjectInputStream oos) throws IOException
    {
        LoadedElementData result = new LoadedElementData();
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            result.setOriginId(Long.valueOf(oos.readLong()));
        }

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            if (et == EncodeType.DYNAMIC_METADATALIST)
            {
                result.setMetaData(decodeDynamicMetaDataList(oos));
            }
            else
            {
                result.setMetaData(DiskDecodeHelper.decodeMetaDataListInternal(oos));
            }
        }

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            result.setMapGeometrySupport(DiskDecodeHelper.decodeMapGeometrySupportInternal(oos));
        }
        return result;
    }

    /**
     * Decode meta data list as a stand-alone object from the input stream.
     * (i.e. reads off header encode type byte)
     *
     * @param oos the oos
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static List<Object> decodeMetaDataList(ObjectInputStream oos) throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            if (et == EncodeType.LIST)
            {
                return DiskDecodeHelper.decodeMetaDataListInternal(oos);
            }
            else if (et == EncodeType.DYNAMIC_METADATALIST)
            {
                return decodeDynamicMetaDataList(oos);
            }
        }
        return null;
    }

    /**
     * Instantiates a new disk encode helper.
     */
    private DiskCacheDecodeHelper()
    {
    }
}
