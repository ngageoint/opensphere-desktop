package io.opensphere.controlpanels.toolbox;

import java.awt.Component;
import java.util.Properties;
import java.util.function.Function;

import io.opensphere.controlpanels.ControlPanelToolbox;
import io.opensphere.controlpanels.DetailPanelProvider;
import io.opensphere.controlpanels.SimpleRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The default implementation of the control panel toolbox.
 */
public class ControlPanelToolboxImpl implements ControlPanelToolbox
{
    /**
     * The detail panel provider registry in which providers are registered.
     */
    private final SimpleRegistry<DetailPanelProvider> myDetailPanelRegistry;

    /**
     * The layer control provider registry in which providers are registered.
     */
    private final SimpleRegistry<Function<DataTypeInfo, Component>> myLayerControlRegistry;

    /**
     * The default panel provider to use when none of the entries in the
     * registry can handle the target data.
     */
    private final DetailPanelProvider myDefaultDetailPanelProvider;

    /**
     * The parent toolbox.
     */
    private final Toolbox myParentToolbox;

    /**
     * The time span of the visible portion of the timeline
     */
    private ObservableValue<TimeSpan> myUISpan;

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
        myDefaultDetailPanelProvider = new DefaultDetailPanelProvider(myParentToolbox);
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
    public DetailPanelProvider getDefaultDetailPanelProvider()
    {
        return myDefaultDetailPanelProvider;
    }

    @Override
    public ObservableValue<TimeSpan> getUISpan()
    {
        return myUISpan;
    }

    @Override
    public void setUISpan(ObservableValue<TimeSpan> uiSpan)
    {
        this.myUISpan = uiSpan;
    }

    @Override
    public SimpleRegistry<Function<DataTypeInfo, Component>> getLayerControlProviderRegistry()
    {
        return myLayerControlRegistry;
    }

    @Override
    public SimpleRegistry<DetailPanelProvider> getDetailPanelProviderRegistry()
    {
        return myDetailPanelRegistry;
    }
}
