package io.opensphere.core.hud.widget.buttons;

import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.hud.widget.EmptyRenderable;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.MathUtil;

/**
 * <code>RadioButton</code> is a toggle button which is part of a
 * <code>ButtonGroup</code> and which has a label to tell the user what they are
 * selecting. The alignment for this component may be used to specify the
 * location of the button with respect to the label. For example use "RIGHT" and
 * "TOP" will put the button above and to the right of the label. "CENTER" and
 * "CENTER" may be used together, but be aware that the label will render across
 * the location of the button.
 */
public class RadioButton extends ToggleButton<GridLayoutConstraints, GridLayout>
{
    /** Label displayed to identify this button. */
    private final Component myLabel;

    /**
     * Construct a ToggleButton.
     *
     * @param parent parent component.
     * @param label component which is displayed as the label for the button.
     * @param group the group to which this button belongs.
     */
    public RadioButton(Component parent, Component label, ButtonGroup group)
    {
        super(parent);
        group.add(this);
        myLabel = label;
        myLabel.setParent(this);
        setHorizontalAlignment(0f);
        setVerticalAlignment(.5f);
    }

    /**
     * Construct a ToggleButton.
     *
     * @param parent parent component.
     * @param label component which is displayed as the label for the button.
     * @param group the group to which this button belongs.
     * @param active Initial active state for the button. This will cause the
     *            button to request being made active of the group.
     */
    public RadioButton(Component parent, Component label, ButtonGroup group, boolean active)
    {
        this(parent, label, group);
        group.componentClicked(this);
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        Set<Geometry> geoms = super.getGeometries();
        geoms.addAll(myLabel.getGeometries());

        return geoms;
    }

    @Override
    public void init()
    {
        initBorder();
        ScreenBoundingBox bbox = getDrawBounds();

        // Get the number of grids base on the size of the button. Use double
        // the number to allow half widths to be used.
        int hGrids = (int)(2d * bbox.getWidth() / getBackground().getDrawWidth());
        int vGrids = (int)(2d * bbox.getHeight() / getBackground().getDrawHeight());

        setLayout(new GridLayout(hGrids, vGrids, this));

        int buttonMinX = 0;
        int buttonMaxX = hGrids - 1;
        int labelMinX = 0;
        int labelMaxX = hGrids - 1;
        // button is located based on the component alignment
        if (MathUtil.isZero(getHorizontalAlignment()))
        {
            // take the two farthest to the left for the button
            buttonMaxX = 1;
            labelMinX = 3;
        }
        else if (MathUtil.isZero(1f - getHorizontalAlignment()))
        {
            // take the two farthest to the right for the button
            buttonMinX = hGrids - 2;
            labelMaxX = hGrids - 4;
        }

        int buttonMinY = 0;
        int buttonMaxY = vGrids - 1;
        int labelMinY = 0;
        int labelMaxY = vGrids - 1;
        if (MathUtil.isZero(1f - getHorizontalAlignment()))
        {
            buttonMaxY = 1;
            labelMinY = 3;
        }
        else if (MathUtil.isZero(getHorizontalAlignment()))
        {
            buttonMinY = vGrids - 2;
            labelMaxY = vGrids - 4;
        }

        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(buttonMinX, buttonMinY), new ScreenPosition(buttonMaxX, buttonMaxY)));

        // create the background
        Component bg = getBackground();
        add(bg, constr);

        // create the active component
        Component active = getActiveComponent();
        add(active, constr);

        // create the inactive component
        Component inactive = getInactiveComponent();
        add(inactive, constr);

        // place the label
        constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(labelMinX, labelMinY), new ScreenPosition(labelMaxX, labelMaxY)));
        add(myLabel, constr);

        getLayout().complete();
    }

    @Override
    protected Component createActiveComponent()
    {
        return new RadioSelectionMark(this);
    }

    @Override
    protected Component createBackgroundComponent()
    {
        return new RadioToggleBackground(this);
    }

    @Override
    protected Component createInactiveComponent()
    {
        return new EmptyRenderable(this);
    }
}
