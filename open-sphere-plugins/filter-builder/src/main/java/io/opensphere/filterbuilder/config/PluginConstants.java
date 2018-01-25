package io.opensphere.filterbuilder.config;

import java.awt.Dimension;

/**
 * PluginConstants.
 */
public final class PluginConstants
{
    // property keys
    /** The property key form the runtime directory property. */
    public static final String RUNTIME_DIR_KEY = "runtimeDir";

    /** The Constant DEFUALT_DIRECTORY_KEY. */
    public static final String DEFUALT_DIRECTORY_KEY = "defaultDirectory";

    // default values
    /** The default directory for saving filters. */
    public static final String DEFAULT_DIRECTORY = "filterBuilder";

    /** The default file name for saving filters. */
    public static final String DEFAULT_FILE_NAME = "filters.xml";

    /** The Constant DEFAULT_WINDOW_SIZE. */
    public static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(1024, 768);

    /**
     * Disallow instantiation.
     */
    private PluginConstants()
    {
    }
}
