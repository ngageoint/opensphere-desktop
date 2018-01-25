package io.opensphere.core.options;

/**
 * The Interface OptionsProviderUserObject.
 */
public class OptionsProviderUserObject
{
    /** The Options provider. */
    private final OptionsProvider myOptionsProvider;

    /** The Node name. */
    private final String myNodeName;

    /**
     * Instantiates a new options provider user object.
     *
     * @param nodeName the node name
     * @param provider the provider
     */
    public OptionsProviderUserObject(String nodeName, OptionsProvider provider)
    {
        myOptionsProvider = provider;
        myNodeName = nodeName;
    }

    /**
     * Gets the node name.
     *
     * @return the node name
     */
    public String getNodeName()
    {
        return myNodeName;
    }

    /**
     * Gets the options provider.
     *
     * @return the options provider
     */
    public OptionsProvider getOptionsProvider()
    {
        return myOptionsProvider;
    }

    @Override
    public String toString()
    {
        return myNodeName;
    }
}
