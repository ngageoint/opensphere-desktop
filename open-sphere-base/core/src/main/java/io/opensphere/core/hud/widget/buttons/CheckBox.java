package io.opensphere.core.hud.widget.buttons;

import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.hud.widget.EmptyRenderable;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * A check box toggle button.
 * 
 * @deprecated use {@link javax.swing.JCheckBox} or
 *             {@link javafx.scene.control.CheckBox} instead.
 */
@Deprecated
public class CheckBox extends ToggleButton<GridLayoutConstraints, GridLayout>
{
    /**
     * Construct a ToggleButton.
     *
     * @param parent parent component.
     */
    public CheckBox(Component parent)
    {
        super(parent);
    }

    /**
     * Construct a ToggleButton.
     *
     * @param parent parent component.
     * @param active Initial active state for the button.
     */
    public CheckBox(Component parent, boolean active)
    {
        super(parent, active);
    }

    @Override
    public void init()
    {
        initBorder();
        setLayout(new GridLayout(1, 1, this));
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));

        // create the background
        Component bg = getBackground();
        add(bg, constr);

        // create the active component
        Component active = getActiveComponent();
        add(active, constr);

        // create the inactive component
        Component inactive = getInactiveComponent();
        add(inactive, constr);

        getLayout().complete();
    }

    @Override
    protected Component createActiveComponent()
    {
        return new CheckMark(this);
    }

    @Override
    protected Component createBackgroundComponent()
    {
        return new BoxToggleBackground(this);
    }

    @Override
    protected Component createInactiveComponent()
    {
        return new EmptyRenderable(this);
    }
}
