package io.opensphere.wms.util;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;

/**
 * Collection of utility methods for sending, receiving, and managing elements
 * in the DataRegistry.
 */
public final class WMSDataRegistryHelper
{
    /**
     * Removes models from the registry using the components of a
     * DataModelCategory.
     *
     * @param dataRegistry the data registry
     * @param source the DataModelCategory source
     * @param family the DataModelCategory family
     * @param category the DataModelCategory category
     */
    public static void removeModels(DataRegistry dataRegistry, String source, String family, String category)
    {
        dataRegistry.removeModels(new DataModelCategory(source, family, category), false);
    }

    /**
     * Forbid instantiation of utility class.
     */
    private WMSDataRegistryHelper()
    {
    }
}
