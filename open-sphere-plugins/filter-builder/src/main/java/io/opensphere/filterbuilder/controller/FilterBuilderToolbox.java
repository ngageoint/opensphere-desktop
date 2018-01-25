package io.opensphere.filterbuilder.controller;

import java.util.List;
import java.util.Map;

import io.opensphere.core.PluginToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.filterbuilder.config.FilterBuilderConfigurationManager;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

// GCD:  Outside the filter builder source, this interface is used only by
// external tools in class FilterCountByToolActivities.  There, it is only used
// to obtain access to the main Toolbox (which is used to get the reference to
// the FilterBuilderToolbox in the first place) and the MantleToolbox, which is
// readily obtainable from the main Toolbox.  I.e., there is currently no
// legitimate use for this interface.
/**
 * FilterBuilderPluginToolbox.
 */
public interface FilterBuilderToolbox extends PluginToolbox
{
    /**
     * Get the data sources that are available to the system at the current
     * time. The key to the {@link Map} should be the source of the data (i.e.
     * server or file). The values of the {@link Map} should be a {@link List}
     * of the data types from the server.
     *
     * @return available data types
     */
    Map<DataGroupInfo, List<DataTypeInfo>> getAvailbleDataTypes();

    /**
     * Get the metadata column keys for a the specified {@link DataTypeInfo}
     * provided. If the {@link DataTypeInfo} is null, then the returned list
     * will be empty.
     *
     * @param dataType a {@link DataTypeInfo} object
     * @return {@link List} of column keys or an empty {@link List}
     */
    List<String> getColumnsForDataType(DataTypeInfo dataType);

    /**
     * Get the configuration for the plugin.
     *
     * @return the configuration for this plugin
     */
    FilterBuilderConfigurationManager getConfiguration();

    /**
     * Gets the controller for the filterBuilder.
     *
     * @return the controller
     */
    FilterBuilderController getController();

    /**
     * Convenience method to get the {@link DataFilterRegistry} from the main.
     *
     * @return filter registry {@link Toolbox}.
     */
    DataFilterRegistry getDataFilterRegistry();

    /**
     * Search the filterable data types as returned by
     * {@link #getFilterableDataTypes()} for a {@link DataTypeInfo} with the
     * same <code>pKey</code> value. If that value is not found in any of the
     * {@link DataTypeInfo}s, then <code>null</code> is returned.
     *
     * @param pKey the key
     * @return the associated data type, or null
     */
    DataTypeInfo getDateTypeInfoForKey(String pKey);

    /**
     * Get a list of filterable data types in the form of {@link DataTypeInfo}s.
     *
     * @return filterable data types
     */
    List<DataTypeInfo> getFilterableDataTypes();

    /**
     * Get the main program toolbox that was provided at plugin initialization.
     *
     * @return toolbox
     */
    Toolbox getMainToolBox();

    /**
     * Gets the mantle toolBox.
     *
     * @return the mantle toolbox.
     */
    MantleToolbox getMantleToolBox();

    /**
     * Get the reference to the public service interface for the filter builder.
     *
     * @return as suggested above
     */
    FilterBuilderService getFbService();
}
