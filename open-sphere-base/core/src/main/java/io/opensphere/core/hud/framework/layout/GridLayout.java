package io.opensphere.core.hud.framework.layout;

import java.util.HashMap;
import java.util.Map;

import io.opensphere.core.hud.framework.AbstractLayout;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Panel;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * Handle component placement.
 */
public class GridLayout extends AbstractLayout<GridLayoutConstraints>
{
    /** Components that are arranged within this layout. */
    private final Map<Component, GridLayoutConstraints> myComponents = new HashMap<>();

    /** Grid height. */
    private final int myGridHeight;

    /** Grid width. */
    private final int myGridWidth;

    /**
     * Create a HUDLayout.
     *
     * @param gridWidth grid width.
     * @param gridHeight grid height.
     * @param panel The panel for which I am the layout manager.
     */
    public GridLayout(int gridWidth, int gridHeight, Panel<GridLayoutConstraints, GridLayout> panel)
    {
        super(panel);
        myGridWidth = gridWidth;
        myGridHeight = gridHeight;
    }

    @Override
    public void add(Component subComp, GridLayoutConstraints constraint)
    {
        myComponents.put(subComp, constraint);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.AbstractLayout#getConstraints(io.opensphere.core.hud.framework.Component)
     */
    @Override
    public GridLayoutConstraints getConstraints(Component subComp)
    {
        return myComponents.get(subComp);
    }

    @Override
    public void complete()
    {
        for (Map.Entry<Component, GridLayoutConstraints> entry : myComponents.entrySet())
        {
            ScreenBoundingBox location = getLocation(entry.getValue());
            Component subComp = entry.getKey();
            subComp.setFrameLocation(location);
            subComp.init();
        }
    }

    /**
     * Get the height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return myGridHeight;
    }

    @Override
    public ScreenBoundingBox getLocation(GridLayoutConstraints constraints)
    {
        ScreenBoundingBox gridBox = constraints.getGridBox();

        ScreenBoundingBox frameBox = getPanel().getDrawBounds();
        ScreenPosition upperLeftFrame = frameBox.getUpperLeft();

        double usableWidth = frameBox.getWidth() - getPanel().getBorder().getLeftInset() - getPanel().getBorder().getRightInset();
        double usableHeight = frameBox.getHeight() - getPanel().getBorder().getTopInset()
                - getPanel().getBorder().getBottomInset();

        double upperLeftStartX = upperLeftFrame.getX() + getPanel().getBorder().getLeftInset();
        double upperLeftStartY = upperLeftFrame.getY() + getPanel().getBorder().getTopInset();

        ScreenPosition upperLeftGrid = gridBox.getUpperLeft();
        double upperLeftX = upperLeftStartX + upperLeftGrid.getX() * (usableWidth / myGridWidth);
        double upperLeftY = upperLeftStartY + upperLeftGrid.getY() * (usableHeight / myGridHeight);

        ScreenPosition lowerRightGrid = gridBox.getLowerRight();
        double lowerRightX = upperLeftStartX + (lowerRightGrid.getX() + 1) * (usableWidth / myGridWidth);
        double lowerRightY = upperLeftStartY + (lowerRightGrid.getY() + 1) * (usableHeight / myGridHeight);

        ScreenPosition frameUpperLeft = new ScreenPosition((int)upperLeftX, (int)upperLeftY);
        ScreenPosition frameLowerRight = new ScreenPosition((int)lowerRightX, (int)lowerRightY);

        return new ScreenBoundingBox(frameUpperLeft, frameLowerRight);
    }

    /**
     * Get the width.
     *
     * @return the width
     */
    public int getWidth()
    {
        return myGridWidth;
    }

    @Override
    public void remove(Component subComp)
    {
        myComponents.remove(subComp);
    }
}
