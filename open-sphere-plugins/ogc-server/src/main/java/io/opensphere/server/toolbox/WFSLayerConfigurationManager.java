package io.opensphere.server.toolbox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;

import io.opensphere.core.util.collections.New;
import io.opensphere.server.customization.ServerCustomization;

/**
 * A manager instance used to maintain the extensible list of WFS layer
 * configuration types, used to define configuration items needed for
 * interacting with the OpenSphere SavedState XML data model.
 */
public class WFSLayerConfigurationManager
{
    /**
     * The dictionary in which the set of known server configurations are
     * stored. The name of the server is stored as the key (allowing fast
     * lookups by server name, the value used as the key will be equivalent to
     * {@link LayerConfiguration#getName()}), and the server configuration
     * instance is used as the value.
     */
    private final Map<String, LayerConfiguration> myNameConfigurationDictionary;

    /**
     * Creates a new instance of the WFSServerConfigurationManager, initializing
     * an empty {@link #myNameConfigurationDictionary}.
     */
    public WFSLayerConfigurationManager()
    {
        myNameConfigurationDictionary = New.map();
    }

    /**
     * Adds the supplied configuration to the manager. If a configuration is
     * already associated with the supplied instance's name, the previous
     * implementation will be replaced, and the replaced instance will be
     * returned. If no association is present with the supplied instance's name,
     * a null value is returned, and the configuration is simply stored in the
     * dictionary.
     *
     * @param configuration the configuration to add to the dictionary.
     * @return the previously stored configuration associated with the
     *         configuration's name.
     */
    public LayerConfiguration addServerConfiguration(LayerConfiguration configuration)
    {
        Preconditions.checkNotNull(configuration);
        return myNameConfigurationDictionary.put(configuration.getName(), configuration);
    }

    /**
     * Tests to determine if the manager contains a configuration associated
     * with the supplied server name.
     *
     * @param serverName the name of the server for which to search.
     * @return true if the manager contains a configuration associated with the
     *         supplied name, false otherwise.
     */
    public boolean isSupportedServer(String serverName)
    {
        return myNameConfigurationDictionary.containsKey(serverName);
    }

    /**
     * Gets the layer state configuration associated with the supplied name. If
     * no association is known, a null value is returned.
     *
     * @param name the name for which to search within the manager's
     *            configurations.
     * @return a layer state configuration instance, which is associated with
     *         the supplied name, or null if none are known.
     */
    public LayerConfiguration getConfigurationFromName(String name)
    {
        return myNameConfigurationDictionary.get(name);
    }

    /**
     * Gets the complete set of known configurations as an unmodifiable
     * {@link Collection}.
     *
     * @return the complete set of known configurations as an unmodifiable
     *         {@link Collection}.
     */
    public Collection<LayerConfiguration> getAllConfigurations()
    {
        return Collections.unmodifiableCollection(myNameConfigurationDictionary.values());
    }

    /**
     * Gets the configuration associated with the supplied customization.
     *
     * @param customization the customization for which to get the associated
     *            configuration.
     * @return the configuration to use with the supplied server customization,
     *         or null if none are known.
     */
    public LayerConfiguration getConfigurationFromCustomization(ServerCustomization customization)
    {
        Preconditions.checkNotNull(customization);
        Collection<LayerConfiguration> values = myNameConfigurationDictionary.values();
        for (LayerConfiguration configuration : values)
        {
            if (configuration.getCustomizationImplementation().isAssignableFrom(customization.getClass()))
            {
                return configuration;
            }
        }

        // no configurations match, return null:
        return null;
    }
}
