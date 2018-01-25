package io.opensphere.core.hud.framework.layout;

import io.opensphere.core.hud.framework.LayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;

/**
 * Grid bounds within the layout. The grid cells occupied are inclusive. For
 * example, if the bounds are (0, 0) to (0, 0), the component will be place so
 * that fully occupies the cell (0, 0).
 */
public class GridLayoutConstraints implements LayoutConstraints
{
    /**
     * Box which defines which cells the associated component will occupy within
     * the layout. Boundaries are inclusive.
     */
    private final ScreenBoundingBox myGridBox;

    /**
     * Construct me.
     *
     * @param gridBox grid cells to occupy.
     */
    public GridLayoutConstraints(ScreenBoundingBox gridBox)
    {
        myGridBox = gridBox;
    }

    /**
     * Get the gridBox.
     *
     * @return the gridBox
     */
    public ScreenBoundingBox getGridBox()
    {
        return myGridBox;
    }
}
