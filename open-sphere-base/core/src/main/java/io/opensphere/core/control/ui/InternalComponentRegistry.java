package io.opensphere.core.control.ui;

import java.awt.Container;
import java.awt.Rectangle;

import io.opensphere.core.hud.awt.HUDFrame;
import io.opensphere.core.util.registry.GenericRegistry;

/** Internal frame registry. */
public abstract class InternalComponentRegistry extends GenericRegistry<HUDFrame>
{
    /**
     * Determine a good place to put a frame based on its dimensions and the
     * dimensions of the other visible frames in the registry.
     *
     * @param bounds The bounds of the frame.
     * @return The suggested bounds of the frame.
     */
    public abstract Rectangle determineDefaultFramePosition(Rectangle bounds);

    /**
     * For swing based frames, this is the container to which the frames will be
     * added.
     *
     * @return Container that holds swing components.
     */
    public abstract Container getInternalContainer();

    /**
     * Tell whether the mouse is over a Swing based HUD.
     *
     * @return true when the mouse is over a HUD.
     */
    public abstract boolean isMouseOverHUD();

    /**
     * Set whether the mouse is over a Swing based HUD.
     *
     * @param mouseOverHUD this should be set to true when the mouse is over
     *            Swing based HUDs only.
     */
    public abstract void setMouseOverHUD(boolean mouseOverHUD);
}
