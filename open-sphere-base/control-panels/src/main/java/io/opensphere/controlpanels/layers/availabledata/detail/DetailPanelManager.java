package io.opensphere.controlpanels.layers.availabledata.detail;

import java.util.Collection;

import io.opensphere.controlpanels.ControlPanelToolbox;
import io.opensphere.controlpanels.DetailPane;
import io.opensphere.controlpanels.GenericThing;
import io.opensphere.controlpanels.GenericThingProvider;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The manager responsible for locating and instantiating Detail Panel
 * implementations.
 */
public class DetailPanelManager
{
    /**
     * The toolbox from which detail panel providers are located.
     */
    private final Toolbox myToolbox;

    /**
     * The default detail pane to return if no providers are found.
     */
    private DetailPane myDefaultPane;

    /**
     * Creates a new panel manager, populated with the supplied toolbox.
     *
     * @param pToolbox The toolbox from which detail panel providers are
     *            located.
     */
    public DetailPanelManager(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
    }

    /**
     * Gets the detail panel for the supplied data type.
     *
     * @param dataGroupInfo the datatype for which to get the detail panel.
     * @return a panel on which details may be rendered.
     */
    public DetailPane getDetailPanel(DataGroupInfo dataGroupInfo)
    {
        if (dataGroupInfo != null)
        {
            ControlPanelToolbox controlPanelToolbox = myToolbox.getPluginToolboxRegistry()
                    .getPluginToolbox(ControlPanelToolbox.class);

            Collection<GenericThingProvider> providers = controlPanelToolbox.getDetailPanelProviderRegistry().getProviders();

            if (providers != null)
            {
                for (GenericThingProvider provider : providers)
                {
                    if (provider.supports(dataGroupInfo))
                    {
                        return provider.getDetailPanel(dataGroupInfo);
                    }
                }
            }

            // if execution makes it here, then none of the providers can handle
            // the supplied data group, so use the default:
            GenericThingProvider defaultProvider = controlPanelToolbox.getDefaultDetailPanelProvider();
            return defaultProvider.getDetailPanel(dataGroupInfo);
        }

        if (myDefaultPane == null)
        {
            myDefaultPane = new BlankPane(myToolbox);
        }
        return myDefaultPane;
    }
}
