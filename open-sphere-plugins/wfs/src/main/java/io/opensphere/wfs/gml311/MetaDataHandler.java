package io.opensphere.wfs.gml311;

import org.apache.log4j.Logger;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;

/**
 * The Class MetaDataHandler.
 */
public class MetaDataHandler
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(MetaDataHandler.class);

    /** The {@link MetaDataInfo} from the DataTypeInfo. */
    private final MetaDataInfo myMetaDataInfo;

    /** Current MetaData Provider. */
    private MetaDataProvider myProvider;

    /**
     * Create a new instance of the meta data handler.
     *
     * @param mdi the {@link MetaDataInfo} from the DataTypeInfo
     */
    public MetaDataHandler(MetaDataInfo mdi)
    {
        myMetaDataInfo = mdi;
    }

    /**
     * Gets the meta data provider.
     *
     * @return the meta data provider
     */
    public MetaDataProvider getMetaDataProvider()
    {
        return myProvider;
    }

    /**
     * Handle a MetaData key/value pair.
     *
     * @param key the MetaData key
     * @param value the key's value
     */
    public void handleMetaData(String key, String value)
    {
        if (!myMetaDataInfo.hasKey(key))
        {
            throw new IllegalArgumentException("No key [" + key + "] in MetaData for type.");
        }
        try
        {
            Class<?> type = myMetaDataInfo.getKeyClassType(key);
            if (Integer.class.equals(type))
            {
                myProvider.setValue(key, Integer.valueOf(value));
            }
            else if (Double.class.equals(type))
            {
                myProvider.setValue(key, Double.valueOf(value));
            }
            else
            {
                myProvider.setValue(key, value);
            }
        }
        catch (NumberFormatException nfe)
        {
            LOGGER.warn("DescribeFeature said this value \'" + value + "\' was a number, however it is not parseable ", nfe);
        }
    }

    /**
     * Reset the current MetaDataProvider.
     */
    public void reset()
    {
        myProvider = new MDILinkedMetaDataProvider(myMetaDataInfo);
    }
}
