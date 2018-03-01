package io.opensphere.controlpanels.toolbox;

import java.awt.Component;
import java.util.Properties;
import java.util.function.Function;

import io.opensphere.controlpanels.ControlPanelToolbox;
import io.opensphere.controlpanels.DetailPanelProvider;
import io.opensphere.controlpanels.SimpleRegistry;
import io.opensphere.controlpanels.GenericThingProvider;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * The default implementation of the control panel toolbox.
 */
public class ControlPanelToolboxImpl implements ControlPanelToolbox
{
    /**
     * The detail panel provider registry in which providers are registered.
     */
    private final SimpleRegistry<DetailPanelProvider> myDetailPanelRegistry;

    private final SimpleRegistry<Function<DataGroupInfo, Component>> myLayerControlRegistry;

    /**
     * The default panel provider to use when none of the entries in the
     * registry can handle the target data.
     */
    // private final GenericThingProvider myDefaultProvider;

    /**
     * The parent toolbox.
     */
    private final Toolbox myParentToolbox;

    /**
     * The time span of the visible portion of the timeline
     */
    private ObservableValue<io.opensphere.core.model.time.TimeSpan> myUISpan;

    /**
     * Creates a new control panel toolbox.
     *
     * @param toolbox the toolbox through which system interactions occur.
     * @param pluginProperties the properties with which to configure the
     *            toolbox.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ControlPanelToolboxImpl(Toolbox toolbox, Properties pluginProperties)
    {
        myParentToolbox = toolbox;
        myDetailPanelRegistry = new SimpleRegistryImpl<>();
        myDetailPanelRegistry.initialize(toolbox, pluginProperties);

        myLayerControlRegistry = new SimpleRegistryImpl<>();
        myLayerControlRegistry.initialize(toolbox, pluginProperties);
        // myDefaultProvider = new
        // DefaultDetailPanelProvider<Object>(myParentToolbox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.PluginToolbox#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "A toolbox extension for control panels";

    }

    @Override
    public DetailPanelProvider getDefaultProvider()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObservableValue<TimeSpan> getUISpan()
    {
        // TODO Auto-generated method stub
        return myUISpan;
    }

    @Override
    public void setUISpan(ObservableValue<TimeSpan> uiSpan)
    {
        this.myUISpan = uiSpan;

    }

    @Override
    public SimpleRegistry<Function<DataGroupInfo, Component>> getLayerControlProviderRegistry()
    {
        // TODO Auto-generated method stub
        return myLayerControlRegistry;
    }

    @Override
    public SimpleRegistry<DetailPanelProvider> getDetailPanelProviderRegistry()
    {
        // TODO Auto-generated method stub
        return myDetailPanelRegistry;
    }
}
