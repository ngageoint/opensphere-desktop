package io.opensphere.overlay.controls;

import java.awt.Dimension;

/**
 * A spacer class used to pad between other control components.
 */
public class ControlSpacer implements ControlComponent
{
    /** The height of the spacer, expressed in pixels. */
    private final int myHeight;

    /** The width of the spacer, expressed in pixels. */
    private final int myWidth;

    /**
     * Creates a new spacer with the supplied dimensions.
     * 
     * @param width the width of the spacer, expressed in pixels.
     * @param height the height of the spacer, expressed in pixels.
     */
    public ControlSpacer(int width, int height)
    {
        myWidth = width;
        myHeight = height;
    }

    /**
     * Creates a new spacer with the supplied dimension.
     * <p>
     * WARNING: the dimension stores width and height in double fields, while
     * spacers store values in integer fields. Values are cast to integers,
     * which will lead to loss of decimal precision due to truncation.
     * </p>
     * 
     * @param dimension the dimensions of the spacer.
     */
    public ControlSpacer(Dimension dimension)
    {
        myWidth = (int)dimension.getWidth();
        myHeight = (int)dimension.getHeight();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.controls.ControlComponent#getWidth()
     */
    @Override
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.controls.ControlComponent#getHeight()
     */
    @Override
    public int getHeight()
    {
        return myHeight;
    }
}
