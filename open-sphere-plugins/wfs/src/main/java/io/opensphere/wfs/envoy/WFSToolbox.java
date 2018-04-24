package io.opensphere.wfs.envoy;

import io.opensphere.core.PluginToolbox;
import io.opensphere.core.modulestate.ModuleStateController;

/** A toolbox implementation for the WFS Plugin. */
public class WFSToolbox implements PluginToolbox
{
    /**
     * The factory used to create new envoys.
     */
    private WFSEnvoyFactory myEnvoyFactory;

    /**
     * The helper class used by envoys during execution.
     */
    private WFSEnvoyHelper myEnvoyHelper;

    /** The state controller. */
    private ModuleStateController myStateController;

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.PluginToolbox#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "WFS Toolbox";
    }

    /**
     * Sets the value of the {@link #myEnvoyFactory} field.
     *
     * @param envoyFactory the value to store in the {@link #myEnvoyFactory}
     *            field.
     */
    public void setEnvoyFactory(WFSEnvoyFactory envoyFactory)
    {
        myEnvoyFactory = envoyFactory;
    }

    /**
     * Gets the value of the {@link #myEnvoyFactory} field.
     *
     * @return the value stored in the {@link #myEnvoyFactory} field.
     */
    public WFSEnvoyFactory getEnvoyFactory()
    {
        return myEnvoyFactory;
    }

    /**
     * Sets the value of the {@link #myEnvoyHelper} field.
     *
     * @param envoyHelper the value to store in the {@link #myEnvoyHelper}
     *            field.
     */
    public void setEnvoyHelper(WFSEnvoyHelper envoyHelper)
    {
        myEnvoyHelper = envoyHelper;
    }

    /**
     * Gets the value of the {@link #myEnvoyHelper} field.
     *
     * @return the value stored in the {@link #myEnvoyHelper} field.
     */
    public WFSEnvoyHelper getEnvoyHelper()
    {
        return myEnvoyHelper;
    }

    /**
     * Gets the stateController.
     *
     * @return the stateController
     */
    public ModuleStateController getStateController()
    {
        return myStateController;
    }

    /**
     * Sets the stateController.
     *
     * @param stateController the stateController
     */
    public void setStateController(ModuleStateController stateController)
    {
        myStateController = stateController;
    }
}
