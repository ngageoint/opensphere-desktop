package io.opensphere.overlay;

/**
 * The Class OverlayToolboxImpl.
 */
public class OverlayToolboxImpl implements OverlayToolbox
{
    /** The Selection mode controller. */
    private final SelectionModeController mySelectionModeController;

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
}
