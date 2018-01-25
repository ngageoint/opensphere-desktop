package io.opensphere.imagery;

/**
 * The Class ImageryLayerKey.
 */
public class ImageryLayerKey
{
    /** The LAYER_SEPARATOR. */
    public static final String LAYERNAME_SEPARATOR = "!!";

    /** My server name. */
    private final String myGroupName;

    /** My layer name. */
    private final String myLayerName;

    /**
     * Creates a key from the server name and the layer name.
     *
     * @param serverName the server name
     * @param layerName the layer name
     * @return the string
     */
    public static String createKey(String serverName, String layerName)
    {
        StringBuilder builder = new StringBuilder();
        if (serverName != null && !serverName.isEmpty())
        {
            builder.append(serverName).append(LAYERNAME_SEPARATOR);
        }
        builder.append(layerName);
        return builder.toString();
    }

    /**
     * Instantiates a new WMSLayerKey from an existing layerKey string.
     *
     * @param layerKey the layer key
     */
    public ImageryLayerKey(String layerKey)
    {
        String[] components = layerKey.split(LAYERNAME_SEPARATOR);
        if (components.length == 2)
        {
            myGroupName = components[0];
        }
        else
        {
            myGroupName = null;
        }
        myLayerName = layerKey;
    }

    /**
     * Instantiates a new ImageryLayerKey from a group and layer name.
     *
     * @param groupName the server name
     * @param layerName the layer name
     */
    public ImageryLayerKey(String groupName, String layerName)
    {
        myGroupName = groupName;
        myLayerName = layerName;
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    public String getGroupName()
    {
        return myGroupName;
    }

    /**
     * Gets the key that uniquely identifies a layer.
     *
     * @return the layer key
     */
    public String getLayerKey()
    {
        return createKey(myGroupName, myLayerName);
    }

    /**
     * Gets the layer name.
     *
     * @return the layer name
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    @Override
    public String toString()
    {
        return createKey(myGroupName, myLayerName);
    }
}
