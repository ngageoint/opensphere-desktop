package io.opensphere.analysis.listtool.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.collections.CollectionUtilities;
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
    public java.util.stream.Stream<String> matchKey(java.util.regex.Pattern key)
    {
        throw new UnsupportedOperationException("matchKey() is not supported for DirectAccessMetaDataProvider");
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
}
