package io.opensphere.server.toolbox;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.services.OGCServiceValidator;

/**
 * The Class ServerToolboxUtils.
 */
public final class ServerToolboxUtils
{
    /**
     * Builds a label from the passed-in server type.
     *
     * @param type the type of server instantiation
     * @return a string that identifies the type to a user
     */
    public static String buildLabelFromType(String type)
    {
        if (ServerCustomization.DEFAULT_TYPE.equals(type))
        {
            return "Generic OGC Server";
        }
        else if (type.trim().toLowerCase().endsWith("server"))
        {
            return type;
        }
        return StringUtilities.concat(type, " Server");
    }

    /**
     * Reformat a layer name for comparison
     * <ul>
     * <li>Strips the namespace off the layerName
     * <li>Converts non-alphanumeric characters to "_" in layer names (Some
     * server implementations replace non-alphanums in type names with "_").
     * </ul>
     *
     * @param layerName the layer name
     * @return reformatted name
     */
    public static String formatNameForComparison(String layerName)
    {
        if (layerName == null || layerName.isEmpty())
        {
            return "";
        }

        // Removed this because there are layers whose names have colons in
        // them. VORTEX-3757
//        int nsIndex = layerName.lastIndexOf(':') > 0 ? layerName.lastIndexOf(':') + 1 : 0;

        return layerName.replaceAll("[(\\W)]", "_");
    }

    /**
     * Gets the {@link ServerListManager} with a convenience method from the
     * {@link ServerToolbox}.
     *
     * @param tb the {@link Toolbox}
     * @return the {@link ServerListManager} from the {@link ServerToolbox}
     */
    public static ServerListManager getServerLayerListManager(Toolbox tb)
    {
        ServerToolbox serverTb = getServerToolbox(tb);
        if (serverTb != null)
        {
            return getServerToolbox(tb).getServerLayerListManager();
        }
        return null;
    }

    /**
     * Gets the {@link ServerRefreshController} with a convenience method from
     * the {@link ServerToolbox}.
     *
     * @param tb the {@link Toolbox}
     * @return the {@link ServerRefreshController} from the
     *         {@link ServerToolbox}
     */
    public static ServerRefreshController getServerRefreshController(Toolbox tb)
    {
        ServerToolbox serverTb = getServerToolbox(tb);
        if (serverTb != null)
        {
            return getServerToolbox(tb).getServerRefreshController();
        }
        return null;
    }

    /**
     * Gets the {@link ServerSourceControllerManager} with a convenience method
     * from the {@link ServerToolbox}.
     *
     * @param tb the {@link Toolbox}
     * @return the {@link ServerSourceControllerManager} from the
     *         {@link ServerToolbox}
     */
    public static ServerSourceControllerManager getServerSourceControllerManager(Toolbox tb)
    {
        ServerToolbox serverTb = getServerToolbox(tb);
        if (serverTb != null)
        {
            return serverTb.getServerSourceControllerManager();
        }
        return null;
    }

    /**
     * Gets the {@link ServerToolbox}.
     *
     * @param tb the core {@link Toolbox}
     * @return the {@link ServerToolbox}
     */
    public static ServerToolbox getServerToolbox(Toolbox tb)
    {
        if (tb != null && tb.getPluginToolboxRegistry() != null)
        {
            return tb.getPluginToolboxRegistry().getPluginToolbox(ServerToolbox.class);
        }
        return null;
    }

    /**
     * Gets the {@link OGCServiceValidator} with a convenience method from the
     * {@link ServerToolbox}.
     *
     * @param tb the {@link Toolbox}
     * @return the {@link OGCServiceValidator} from the {@link ServerToolbox}
     */
    public static ServerValidatorRegistry getServerValidatorRegistry(Toolbox tb)
    {
        ServerToolbox serverTb = getServerToolbox(tb);
        if (serverTb != null)
        {
            return getServerToolbox(tb).getServerValidatorRegistry();
        }
        return null;
    }

    /** Disallow Instantiation. */
    private ServerToolboxUtils()
    {
    }
}
