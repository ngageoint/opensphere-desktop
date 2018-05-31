package io.opensphere.mantle.data.element.impl;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;

/**
 * A {@link MetaDataProvider} where the key set is provided by a
 * {@link MetaDataInfo}.
 */
public abstract class AbstractMDILinkedMetaDataProvider implements MetaDataProvider
{
    /** The linked MetaDataInfo. */
    private final MetaDataInfo myMetaDataInfo;

    /**
     * CTOR.
     *
     * @param mdi the MetaDataInfo to be linked to.
     */
    protected AbstractMDILinkedMetaDataProvider(MetaDataInfo mdi)
    {
        myMetaDataInfo = mdi;
    }

    @Override
    public final List<String> getKeys()
    {
        return Collections.unmodifiableList(myMetaDataInfo.getKeyNames());
    }

    /**
     * Get the {@link MetaDataInfo} to which this provider is linked.
     *
     * @return the {@link MetaDataInfo}
     */
    public final MetaDataInfo getMetaDataInfo()
    {
        return myMetaDataInfo;
    }

    @Override
    public final boolean hasKey(String key)
    {
        return myMetaDataInfo.hasKey(key);
    }

    @Override
    public Stream<String> matchKey(Pattern key)
    {
        return myMetaDataInfo.getKeyNames().stream().filter(k -> key.matcher(k).matches());
    }

    @Override
    public final boolean keysMutable()
    {
        return false;
    }

    @Override
    public final void removeKey(String key)
    {
        throw new UnsupportedOperationException("Cannot remove key from a MDILinkedMetaDataProvider, remove via MetaDataInfo");
    }
}
