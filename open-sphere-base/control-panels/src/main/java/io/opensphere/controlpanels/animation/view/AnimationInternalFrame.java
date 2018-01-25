package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Component;
import java.util.Collections;

import javax.swing.JFrame;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.hud.awt.HUDJInternalFrame;

/**
 * The main animation (timeline) internal frame.
 */
public class AnimationInternalFrame extends AbstractInternalFrame
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The internal frame border height. */
    private static final int FRAME_BORDER_HEIGHT = 30;

    /** The UI registry. */
    private final UIRegistry myUIRegistry;

    /** The component in the frame. */
    private Component myComponent;

    /**
     * Constructor.
     *
     * @param uiRegistry The UI registry
     */
    public AnimationInternalFrame(UIRegistry uiRegistry)
    {
        super("Timeline", true, true, false);
        myUIRegistry = uiRegistry;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));

        HUDJInternalFrame hudFrame = new HUDJInternalFrame(new HUDJInternalFrame.Builder().setInternalFrame(this));
        myUIRegistry.getComponentRegistry().addObjectsForSource(this, Collections.singleton(hudFrame));
    }

    /**
     * Sizes and positions the frame to the default location.
     */
    public void resizeAndPositionToDefault()
    {
        setSize(getParent().getWidth() - 6, myComponent.getPreferredSize().height + FRAME_BORDER_HEIGHT);
        setLocation(3, getParent().getHeight() - getHeight());
        validate();
    }

    /**
     * Sets the component within the frame.
     *
     * @param comp the component
     */
    public final void setComponent(Component comp)
    {
        // Set the new component
        if (myComponent != null)
        {
            remove(myComponent);
        }
        myComponent = comp;
        add(comp);

        resizeAndPositionToDefault();
    }
}
