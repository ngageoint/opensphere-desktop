package io.opensphere.server.state;

import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.toolbox.LayerConfiguration;

/**
 * A set of configuration items used to describe a unique WFS Server type within
 * the OpenSphere data model.
 */
public class DefaultWFSLayerConfiguration implements LayerConfiguration
{
    /**
     * The configuration name of the server.
     */
    private final String myName;

    /**
     * The XPath of the configuration type's node within the OpenSphere
     * SavedState XML data structure.
     */
    private final String myStateXPath;

    /**
     * The server customization implementation associated with this
     * configuration instance.
     */
    private final Class<? extends ServerCustomization> myCustomizationImplementation;

    /**
     * A flag used to indicate that the server type supports changing the time
     * column.
     */
    private final boolean myTimeColumnChangeable;

    /**
     * Creates a new server configuration, using the supplied parameters. The
     * XPath will be generated using the supplied name, and the standard
     * location within the OpenSphere XML saved state document structure. To
     * deviate from the standard XML structure, use the alternate constructor.
     *
     * @param name the configuration name of the server.
     * @param customizationImplementation the server customization
     *            implementation associated with this configuration instance.
     */
    public DefaultWFSLayerConfiguration(String name, Class<? extends ServerCustomization> customizationImplementation)
    {
        this(name, StateConstants.XPATH_PREFIX + name + StateConstants.XPATH_SUFFIX, customizationImplementation);
    }

    /**
     * Creates a new server configuration, using the supplied parameters. The
     * XPath will be generated using the supplied name, and the standard
     * location within the OpenSphere XML saved state document structure. To
     * deviate from the standard XML structure, use the alternate constructor.
     *
     * @param name the configuration name of the server.
     * @param customizationImplementation the server customization
     *            implementation associated with this configuration instance.
     * @param timeColumnChangeable A flag used to indicate that the server type
     *            supports changing the time column.
     */
    public DefaultWFSLayerConfiguration(String name, Class<? extends ServerCustomization> customizationImplementation,
            boolean timeColumnChangeable)
    {
        this(name, StateConstants.XPATH_PREFIX + name + StateConstants.XPATH_SUFFIX, customizationImplementation,
                timeColumnChangeable);
    }

    /**
     * Creates a new server configuration, using the supplied parameters. The
     * XPath will be generated using the supplied parameters.
     *
     * @param name the configuration name of the server.
     * @param stateXPath The XPath of the configuration type's node within the
     *            OpenSphere SavedState XML data structure.
     * @param customizationImplementation the server customization
     *            implementation associated with this configuration instance.
     */
    public DefaultWFSLayerConfiguration(String name, String stateXPath,
            Class<? extends ServerCustomization> customizationImplementation)
    {
        this(name, stateXPath, customizationImplementation, false);
    }

    /**
     * Creates a new server configuration, using the supplied parameters. The
     * XPath will be generated using the supplied parameters.
     *
     * @param name the configuration name of the server.
     * @param stateXPath The XPath of the configuration type's node within the
     *            OpenSphere SavedState XML data structure.
     * @param customizationImplementation the server customization
     *            implementation associated with this configuration instance.
     * @param timeColumnChangeable A flag used to indicate that the server type
     *            supports changing the time column.
     */
    public DefaultWFSLayerConfiguration(String name, String stateXPath,
            Class<? extends ServerCustomization> customizationImplementation, boolean timeColumnChangeable)
    {
        myName = name;
        myStateXPath = stateXPath;
        myCustomizationImplementation = customizationImplementation;
        myTimeColumnChangeable = timeColumnChangeable;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.LayerConfiguration#getName()
     */
    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.LayerConfiguration#getStateXPath()
     */
    @Override
    public String getStateXPath()
    {
        return myStateXPath;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.LayerConfiguration#getCustomizationImplementation()
     */
    @Override
    public Class<? extends ServerCustomization> getCustomizationImplementation()
    {
        return myCustomizationImplementation;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.LayerConfiguration#isTimeColumnChangeable()
     */
    @Override
    public boolean isTimeColumnChangeable()
    {
        return myTimeColumnChangeable;
    }
}
