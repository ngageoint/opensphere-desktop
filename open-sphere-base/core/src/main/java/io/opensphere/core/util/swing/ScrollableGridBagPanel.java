package io.opensphere.core.util.swing;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Scrollable;

/**
 * An extension to the standard {@link GridBagPanel}, in which the
 * {@link Scrollable} interface is implemented.
 */
public class ScrollableGridBagPanel extends GridBagPanel implements Scrollable
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = 1159482239365622414L;

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
     */
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        return super.getPreferredSize();
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle,
     *      int, int)
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle pVisibleRect, int pOrientation, int pDirection)
    {
        return 10;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle,
     *      int, int)
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle pVisibleRect, int pOrientation, int pDirection)
    {
        return 10;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
     */
    @Override
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
     */
    @Override
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getScrollableTracksViewportHeight()
    {
        return true;
    }
}
