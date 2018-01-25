package io.opensphere.core.hud.widget.buttons;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.hud.framework.AbstractLayout;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.LayoutConstraints;
import io.opensphere.core.hud.framework.Panel;

/**
 * Toggle button.
 *
 * @param <S> Layout constraint type.
 * @param <T> Layout type.
 */
public abstract class ToggleButton<S extends LayoutConstraints, T extends AbstractLayout<S>> extends Panel<S, T>
{
    /** true when this button is active. */
    private boolean myActive;

    /** Item to display so that the user knows what is being selected. */
    private Component myActiveComponent;

    /** background for the selection area. */
    private Component myBackground;

    /**
     * If I am a member of a button group defer control of selection to the
     * group.
     */
    private ButtonGroup myButtonGroup;

    /** Item to display so that the user knows what is being selected. */
    private Component myInactiveComponent;

    /** Support for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /**
     * Construct a ToggleButton.
     *
     * @param parent parent component.
     */
    public ToggleButton(Component parent)
    {
        super(parent);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    /**
     * Construct a ToggleButton.
     *
     * @param parent parent component.
     * @param active Initial active state for the button.
     */
    public ToggleButton(Component parent, boolean active)
    {
        super(parent);
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
        myActive = true;
    }

    /** Do whatever is necessary when the button becomes active. */
    public void activate()
    {
        if (myActive)
        {
            return;
        }
        myActive = true;
        updateGeometries(getActiveComponent().getGeometries(), getInactiveComponent().getGeometries());
    }

    /** Do whatever is necessary when the button becomes inactive. */
    public void deactivate()
    {
        if (!myActive)
        {
            return;
        }
        myActive = false;
        updateGeometries(getInactiveComponent().getGeometries(), getActiveComponent().getGeometries());
    }

    /**
     * Get the activeComponent.
     *
     * @return the activeComponent
     */
    public Component getActiveComponent()
    {
        if (myActiveComponent == null)
        {
            myActiveComponent = createActiveComponent();
        }
        return myActiveComponent;
    }

    /**
     * Get the background.
     *
     * @return the background
     */
    public Component getBackground()
    {
        if (myBackground == null)
        {
            myBackground = createBackgroundComponent();
        }
        return myBackground;
    }

    /**
     * Get the buttonGroup.
     *
     * @return the buttonGroup
     */
    public ButtonGroup getButtonGroup()
    {
        return myButtonGroup;
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        Set<Geometry> geoms = new HashSet<>();

        geoms.addAll(getBorder().getGeometries());
        geoms.addAll(myBackground.getGeometries());

        if (myActive)
        {
            if (myActiveComponent != null)
            {
                geoms.addAll(myActiveComponent.getGeometries());
            }
        }
        else
        {
            if (myInactiveComponent != null)
            {
                geoms.addAll(myInactiveComponent.getGeometries());
            }
        }
        return geoms;
    }

    /**
     * Get the inactiveComponent.
     *
     * @return the inactiveComponent
     */
    public Component getInactiveComponent()
    {
        if (myInactiveComponent == null)
        {
            myInactiveComponent = createInactiveComponent();
        }
        return myInactiveComponent;
    }

    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    @Override
    public void mouseClicked(Geometry geom, MouseEvent evt)
    {
        if (myButtonGroup != null)
        {
            myButtonGroup.componentClicked(this);
        }
        else
        {
            if (myActive)
            {
                deactivate();
            }
            else
            {
                activate();
            }
        }
    }

    /**
     * Set the activeComponent.
     *
     * @param activeComponent the activeComponent to set
     */
    public void setActiveComponent(Component activeComponent)
    {
        myActiveComponent = activeComponent;
    }

    /**
     * Set the background.
     *
     * @param background the background to set
     */
    public void setBackground(Component background)
    {
        myBackground = background;
    }

    /**
     * Set the buttonGroup.
     *
     * @param buttonGroup the buttonGroup to set
     */
    public void setButtonGroup(ButtonGroup buttonGroup)
    {
        myButtonGroup = buttonGroup;
    }

    /**
     * Set the inactiveComponent.
     *
     * @param inactiveComponent the inactiveComponent to set
     */
    public void setInactiveComponent(Component inactiveComponent)
    {
        myInactiveComponent = inactiveComponent;
    }

    /**
     * Create the default component to display when active. This should be
     * non-null. If you do not want anything displayed, use
     * <code>EmptyRenderable</code>.
     *
     * @return the component to display when active.
     */
    protected abstract Component createActiveComponent();

    /**
     * Create the default component to display for the background. The
     * background is also required to register a geometry for mouse events.
     *
     * @return the component to display when active.
     */
    protected abstract Component createBackgroundComponent();

    /**
     * Create the default component to draw when inactive. This should be
     * non-null. If you do not want anything displayed, use
     * <code>EmptyRenderable</code>.
     *
     * @return the component to display when active.
     */
    protected abstract Component createInactiveComponent();
}
