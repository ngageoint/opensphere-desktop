package io.opensphere.core.hud.widget;

import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Panel;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Slide. */
public class Slider extends Panel<GridLayoutConstraints, GridLayout>
{
    /** Background for the slider. */
    private Component myBackground;

    /** Arrangement for the slider. */
    private final Orientation myOrientation;

    /** Puck which shows the position of the slider. */
    private Component myPuck;

    /**
     * The pick size is the height when vertical or the width when horizontal.
     */
    private final double myPuckSize;

    /**
     * The thickness is the width when vertical or the height when horizontal.
     */
    private final double myThickness;

    /**
     * Construct a RadioButton.
     *
     * @param parent parent component.
     * @param orient arrangement.
     * @param puckSize Size of the puck along the track.
     * @param puckThickness Size of the puck across the track.
     */
    public Slider(Component parent, Orientation orient, double puckSize, double puckThickness)
    {
        super(parent);
        myOrientation = orient;
        myPuckSize = puckSize;
        myThickness = puckThickness;
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
            myBackground = new SliderBackground(this);
        }
        return myBackground;
    }

    /**
     * Get the orientation.
     *
     * @return the orientation
     */
    public Orientation getOrientation()
    {
        return myOrientation;
    }

    /**
     * Get the puck.
     *
     * @return the puck
     */
    public Component getPuck()
    {
        if (myPuck == null)
        {
            myPuck = new SliderPuck(this);
        }
        return myPuck;
    }

    /**
     * Get the puckSize.
     *
     * @return the puckSize
     */
    public double getPuckSize()
    {
        return myPuckSize;
    }

    /**
     * Get the thickness.
     *
     * @return the thickness
     */
    public double getThickness()
    {
        return myThickness;
    }

    @Override
    public void init()
    {
        initBorder();
        setLayout(new GridLayout(1, 1, this));

        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));

        Component puck = getPuck();
        add(puck, constr);

        Component bg = getBackground();
        add(bg, constr);

        getLayout().complete();
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
     * Set the puck.
     *
     * @param puck the puck to set
     */
    public void setPuck(Component puck)
    {
        myPuck = puck;
    }

    /** Arrangement for the slider. */
    public enum Orientation
    {
        /** Horizontal orientation. */
        HORIZONTAL,

        /** Vertical orientation. */
        VERTICAL;
    }
}
