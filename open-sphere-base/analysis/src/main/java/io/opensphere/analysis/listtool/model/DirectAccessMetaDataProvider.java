package io.opensphere.analysis.listtool.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * A fake MetaDataProvider that delegates supported operations to the
 * DirectAccessRetriever for performance.
 */
class DirectAccessMetaDataProvider implements MetaDataProvider
{
    /** The cache id. */
    private final long myCacheId;

    /** The direct access retriever. */
    private final DirectAccessRetriever myDirectAccessRetriever;

    /**
     * Constructor.
     *
     * @param cacheId The cache id
     * @param directAccessRetriever The direct access retriever
     */
    public DirectAccessMetaDataProvider(long cacheId, DirectAccessRetriever directAccessRetriever)
    {
        myCacheId = cacheId;
        myDirectAccessRetriever = directAccessRetriever;
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    public DirectAccessMetaDataProvider(DirectAccessMetaDataProvider source)
    {
        myCacheId = source.myCacheId;
        myDirectAccessRetriever = source.myDirectAccessRetriever;
    }

    @Override
    public List<String> getKeys()
    {
        throw new UnsupportedOperationException("getKeys() is not supported for DirectAccessMetaDataProvider");
    }

    @Override
    public Object getValue(String key)
    {
        Object value = null;
        List<Object> values = getValues();
        if (CollectionUtilities.hasContent(values))
        {
            int keyIndex = myDirectAccessRetriever.getDataType().getMetaDataInfo().getKeyIndex(key);
            if (keyIndex != -1)
            {
                value = values.get(keyIndex);
            }
        }
        return value;
    }

    @Override
    public List<Object> getValues()
    {
        return myDirectAccessRetriever.getMetaData(myCacheId);
    }

    @Override
    public boolean hasKey(String key)
    {
        throw new UnsupportedOperationException("hasKey() is not supported for DirectAccessMetaDataProvider");
    }

    @Override
    public boolean keysMutable()
    {
        throw new UnsupportedOperationException("keysMutable() is not supported for DirectAccessMetaDataProvider");
    }

    @Override
    public void removeKey(String key)
    {
        throw new UnsupportedOperationException("removeKey() is not supported for DirectAccessMetaDataProvider");
    }

    @Override
    public boolean setValue(String key, Serializable value)
    {
        throw new UnsupportedOperationException("setValue() is not supported for DirectAccessMetaDataProvider");
    }

    @Override
    public boolean valuesMutable()
    {
        throw new UnsupportedOperationException("valuesMutable() is not supported for DirectAccessMetaDataProvider");
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.MetaDataProvider#createCopy(DataTypeInfo)
     */
    @Override
    public MetaDataProvider createCopy(DataTypeInfo newDataType)
    {
        return new DirectAccessMetaDataProvider(this);
    }
}
