package io.opensphere.core.hud.framework;

import io.opensphere.core.model.ScreenBoundingBox;

/**
 * Handle component placement.
 *
 * @param <T> the layout constraint type associated with the layout.
 */
public abstract class AbstractLayout<T extends LayoutConstraints>
{
    /** The panel which owns me. */
    private final Panel<T, ?> myPanel;

    /**
     * Create a HUDLayout.
     *
     * @param panel The panel for which I am the layout manager.
     */
    public AbstractLayout(Panel<T, ?> panel)
    {
        myPanel = panel;
    }

    /**
     * Add the given component to the layout and position it based on the
     * constraint.
     *
     * @param subComp component to add.
     * @param constraint parameters describing the position of the component in
     *            the layout.
     */
    public abstract void add(Component subComp, T constraint);

    /**
     * Gets the constraints associated with the supplied component.
     * 
     * @param subComp the component for which to get the constraints.
     * @return the constraints associated with the supplied component, or null
     *         if none are known.
     */
    public abstract T getConstraints(Component subComp);

    /**
     * When no more components are to be added, set the positions and initialize
     * the sub components. Until this is called sub components may not be
     * initialized.
     */
    public abstract void complete();

    /**
     * Get the location in the frame (pixels) used by the grid box.
     *
     * @param constraints Constrains which detail placement within the layout.
     * @return frame location.
     */
    public abstract ScreenBoundingBox getLocation(T constraints);

    /**
     * Get the panel.
     *
     * @return the panel
     */
    public Panel<T, ?> getPanel()
    {
        return myPanel;
    }

    /**
     * Remove the given component from the layout.
     *
     * @param subComp component to remove.
     */
    public abstract void remove(Component subComp);
}
