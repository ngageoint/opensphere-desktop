package io.opensphere.wms.state.save.controllers;

import java.util.List;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.wms.layer.WMSLayer;

/**
 * Provides a set of all WMS layers currently in the system.
 */
public class WMSLayerProvider
{
    /**
     * The data registry containing many of the different WMS layers.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Constructs a new WMS layer provider.
     *
     * @param registry The data registry containing wms layers.
     */
    public WMSLayerProvider(DataRegistry registry)
    {
        myDataRegistry = registry;
    }

    /**
     * Gets all WMS layers within the system.
     *
     * @return The WMS layers.
     */
    public List<WMSLayer> getLayers()
    {
        DataModelCategory category = new DataModelCategory(null, WMSLayer.class.getName(), null);
        PropertyDescriptor<WMSLayer> property = new PropertyDescriptor<>("value", WMSLayer.class);

        SimpleQuery<WMSLayer> query = new SimpleQuery<>(category, property);

        myDataRegistry.performLocalQuery(query);

        return query.getResults();
    }
}
