package io.opensphere.controlpanels.toolbox;

import java.util.Properties;

import io.opensphere.controlpanels.ControlPanelToolbox;
import io.opensphere.controlpanels.DetailPanelProvider;
import io.opensphere.controlpanels.DetailPanelProviderRegistry;
import io.opensphere.controlpanels.GenericThing;
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
    private final GenericThing<Object> myDetailPanelProviderRegistry;

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
     * Creates a new provider registry.
     *
     * @param pToolbox the toolbox through which system interactions occur.
     * @param pluginProperties the properties with which to configure the
     *            registry.
     * @return the instantiated and initialized registry.
     */
    protected GenericThing<Object> createDetailPanelProviderRegistry(Toolbox pToolbox, Properties pluginProperties)
    {
        GenericThing<Object> returnValue = new GenericThingImpl<Object>();
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
    @Override
    public void setUISpan(ObservableValue<de.micromata.opengis.kml.v_2_2_0.TimeSpan> uiSpan)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public GenericThing<?> getDetailPanelProviderRegistry()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GenericThing<?> getDefaultDetailPanelProvider()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObservableValue<de.micromata.opengis.kml.v_2_2_0.TimeSpan> getUISpan()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
