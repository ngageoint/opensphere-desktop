package io.opensphere.wfs.state;

import io.opensphere.core.Toolbox;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;
import io.opensphere.wfs.state.activate.WFSDataTypeBuilder;
import io.opensphere.wfs.state.activate.WFSStateActivator;
import io.opensphere.wfs.state.controllers.WFSNodeReader;
import io.opensphere.wfs.state.deactivate.WFSStateDeactivator;
import io.opensphere.wfs.state.save.WFSStateSaver;

/**
 * Base class for WFS state controller.
 */
public abstract class BaseWFSStateController extends AbstractModuleStateController
{
    /** The State saver. */
    private final WFSStateSaver myStateSaver;

    /** The Activator. */
    private final WFSStateActivator myActivator;

    /** The Deactivator. */
    private final WFSStateDeactivator myDeactivator;

    /**
     * The toolbox through which application state is accessed.
     */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new base wfs state controller.
     *
     * @param toolbox the toolbox
     */
    public BaseWFSStateController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myStateSaver = new WFSStateSaver(toolbox);

        WFSLayerConfigurationManager configurationManager = toolbox.getPluginToolboxRegistry()
                .getPluginToolbox(ServerToolbox.class).getLayerConfigurationManager();
        myActivator = new WFSStateActivator(toolbox, new WFSDataTypeBuilder(toolbox), new WFSNodeReader(configurationManager));
        myDeactivator = new WFSStateDeactivator(toolbox);
    }

    /**
     * Gets the value of the {@link #myToolbox} field.
     *
     * @return the value stored in the {@link #myToolbox} field.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Gets the activator.
     *
     * @return the activator
     */
    public WFSStateActivator getActivator()
    {
        return myActivator;
    }

    /**
     * Gets the deactivator.
     *
     * @return the deactivator
     */
    public WFSStateDeactivator getDeactivator()
    {
        return myDeactivator;
    }

    /**
     * Gets the layer state saver.
     *
     * @return The state saver.
     */
    public WFSStateSaver getSaver()
    {
        return myStateSaver;
    }
}
