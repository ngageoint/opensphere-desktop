package io.opensphere.overlay;

import io.opensphere.overlay.query.QueryActionManager;

/**
 * The Class OverlayToolboxImpl.
 */
public class OverlayToolboxImpl implements OverlayToolbox
{
    /** The Selection mode controller. */
    private final SelectionModeController mySelectionModeController;

    /**
     * The QueryActionManager used to allow multiple types of actions to be
     * registered from different plugins.
     */
    private final QueryActionManager myQueryActionManager = new QueryActionManager();

    /**
     * Instantiates a new overlay toolbox impl.
     *
     * @param smc the smc
     */
    public OverlayToolboxImpl(SelectionModeController smc)
    {
        mySelectionModeController = smc;
    }

    @Override
    public String getDescription()
    {
        return "Toolbox for the Overlay Plugin";
    }

    @Override
    public SelectionModeController getSelectionModeController()
    {
        return mySelectionModeController;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.OverlayToolbox#getQueryActionManager()
     */
    @Override
    public QueryActionManager getQueryActionManager()
    {
        return myQueryActionManager;
    }
}
