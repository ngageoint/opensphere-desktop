package io.opensphere.controlpanels.toolbox;

import java.util.Properties;

import io.opensphere.controlpanels.ControlPanelToolbox;
import io.opensphere.controlpanels.DetailPanelProvider;
import io.opensphere.controlpanels.DetailPanelProviderRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableValue;

/**
 * The default implementation of the control panel toolbox.
 */
public class ControlPanelToolboxImpl implements ControlPanelToolbox
{
    /**
     * The detail panel provider registry in which providers are registered.
     */
    private final DetailPanelProviderRegistry myDetailPanelProviderRegistry;

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
        myDetailPanelProviderRegistry = createDetailPanelProviderRegistry(myParentToolbox, pluginProperties);
        myDefaultDetailPanelProvider = new DefaultDetailPanelProvider(myParentToolbox);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.ControlPanelToolbox#getDefaultDetailPanelProvider()
     */
    @Override
    public DetailPanelProvider getDefaultDetailPanelProvider()
    {
        return myDefaultDetailPanelProvider;
    }

    /**
     * Creates a new provider registry.
     *
     * @param pToolbox the toolbox through which system interactions occur.
     * @param pluginProperties the properties with which to configure the
     *            registry.
     * @return the instantiated and initialized registry.
     */
    protected DetailPanelProviderRegistry createDetailPanelProviderRegistry(Toolbox pToolbox, Properties pluginProperties)
    {
        DetailPanelProviderRegistry returnValue = new DetailPanelProviderRegistryImpl();
        returnValue.initialize(pToolbox, pluginProperties);

        return returnValue;
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

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.ControlPanelToolbox#getDetailPanelProviderRegistry()
     */
    @Override
    public DetailPanelProviderRegistry getDetailPanelProviderRegistry()
    {
        return myDetailPanelProviderRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.ControlPanelToolbox#getUISpan()
     */
    @Override
    public ObservableValue<TimeSpan> getUISpan()
    {
        return myUISpan;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.ControlPanelToolbox#setUISpan(ObservableValue) ()
     */
    @Override
    public void setUISpan(ObservableValue<TimeSpan> uiSpan)
    {
        this.myUISpan = uiSpan;
    }
}
