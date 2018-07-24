package io.opensphere.overlay;

import io.opensphere.core.Toolbox;

/**
 * The Class OverlayToolboxImpl.
 */
public class OverlayToolboxImpl implements OverlayToolbox
{
    /** The Selection mode controller. */
    private final SelectionModeController mySelectionModeController;

    private final ControlsLayoutManager myControlsLayoutManager;

    /**
     * Instantiates a new overlay toolbox impl.
     *
     * @param smc the smc
     */
    public OverlayToolboxImpl(Toolbox toolbox, SelectionModeController smc)
    {
        mySelectionModeController = smc;
        myControlsLayoutManager = new DefaultControlsLayoutManager(toolbox);
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
     * @see io.opensphere.overlay.OverlayToolbox#getControlsLayoutManager()
     */
    @Override
    public ControlsLayoutManager getControlsLayoutManager()
    {
        return myControlsLayoutManager;
    }
}
