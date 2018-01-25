package io.opensphere.geopackage.mantle;

import java.io.Serializable;
import java.util.Map;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;

/**
 * Populates a {@link MetaDataProvider} with data from a geopackage feature row.
 */
public class MetaDataProviderPopulator
{
    /**
     * Populates a {@link MetaDataProvider} with data from the specified
     * geopackage feature row.
     *
     * @param row The row with the data.
     * @param metadataInfo The {@link MetaDataInfo} for the layer.
     * @return A newly created and populated {@link MetaDataProvider}.
     */
    public MetaDataProvider populateProvider(Map<String, Serializable> row, MetaDataInfo metadataInfo)
    {
        MDILinkedMetaDataProvider provider = new MDILinkedMetaDataProvider(metadataInfo);

        for (String columnName : metadataInfo.getKeyNames())
        {
            provider.setValue(columnName, row.get(columnName));
        }

        return provider;
    }
}
