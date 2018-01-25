package io.opensphere.osh.aerialimagery.transformer.modelproviders;

import io.opensphere.core.cache.matcher.NumberPropertyMatcher;
import io.opensphere.core.cache.matcher.NumberPropertyMatcher.OperatorType;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;
import io.opensphere.osh.aerialimagery.util.Constants;

/**
 * Provides a platforms location and orientation at a given time.
 */
public class PlatformMetadataProvider implements ModelProvider
{
    /**
     * The data registry.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Constructs a new metadata provider.
     *
     * @param dataRegistry The data registry.
     */
    public PlatformMetadataProvider(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    @Override
    public PlatformMetadata getModel(DataTypeInfo dataType, DataTypeInfo videoLayer, long time, PlatformMetadata previousMetadata)
    {
        DataModelCategory category = new DataModelCategory(dataType.getUrl(), Constants.PLATFORM_METADATA_FAMILY,
                dataType.getTypeKey());
        PropertyMatcher<Long> lte = new NumberPropertyMatcher<Long>(Constants.METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR,
                OperatorType.LTE, Long.valueOf(time));
        PropertyMatcher<Long> gte = new NumberPropertyMatcher<Long>(Constants.METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR,
                OperatorType.GTE, Long.valueOf(time - 1000));
        SimpleQuery<PlatformMetadata> query = new SimpleQuery<>(category, Constants.PLATFORM_METADATA_DESCRIPTOR,
                New.list(lte, gte));
        myDataRegistry.performQuery(query);

        PlatformMetadata metadata = null;
        if (query.getResults() != null && !query.getResults().isEmpty())
        {
            for (PlatformMetadata aMetadata : query.getResults())
            {
                if (aMetadata.getTime().getTime() <= time
                        && (metadata == null || metadata.getTime().getTime() < aMetadata.getTime().getTime()))
                {
                    metadata = aMetadata;
                }
            }
        }

        return metadata;
    }
}
