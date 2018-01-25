package io.opensphere.core.hud.awt;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JInternalFrame;

import io.opensphere.core.hud.util.FrameBoundsHelper;
import io.opensphere.core.hud.util.PositionBoundedFrame;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * A frame which can be displayed on the GL canvas. TODO The name of this class
 * should be changed since it is not abstract.
 */
public class AbstractInternalFrame extends JInternalFrame implements PositionBoundedFrame
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The property name for the component's user property telling whether the
     * component has been rolled up.
     */
    public static final String ROLL_PROPERTY = "frameRolledUp";

    /** Listener interested in changes to the bounds of this frame. */
    private HUDFrameBoundsHandler myFrameBoundsHandler;

    /** Helper for adjusting the frame bounds. */
    private FrameBoundsHelper myFrameBoundsHelper;

    /**
     * When true, this frame will ignore inset, parent bounds and glue
     * adjustments.
     */
    private boolean myIgnoresAdjustments;

    /** When true, the frame can be popped out onto the JIDE dock. */
    private boolean myPopable = true;

    /**
     * Creates a non-resizable, non-closable, non-maximizable, non-iconifiable
     * <code>JInternalFrame</code> with no title.
     */
    public AbstractInternalFrame()
    {
        super();
        setFrameIcon(null);
    }

    /**
     * Creates a non-maximizable, non-iconifiable <code>JInternalFrame</code>
     * with the specified title, resizability, and closability.
     *
     * @param aTitle the <code>String</code> to display in the title bar
     * @param aResizable if <code>true</code>, the internal frame can be resized
     * @param aClosable if <code>true</code>, the internal frame can be closed
     */
    public AbstractInternalFrame(String aTitle, boolean aResizable, boolean aClosable)
    {
        super(aTitle, aResizable, aClosable);
        setFrameIcon(null);
        putClientProperty(ROLL_PROPERTY, Boolean.FALSE);
    }

    /**
     * Creates a non-iconifiable <code>JInternalFrame</code> with the specified
     * title, resizability, closability, and maximizability.
     *
     * @param aTitle the <code>String</code> to display in the title bar
     * @param aResizable if <code>true</code>, the internal frame can be resized
     * @param aClosable if <code>true</code>, the internal frame can be closed
     * @param aMaximizable if <code>true</code>, the internal frame can be
     *            maximized
     */
    public AbstractInternalFrame(String aTitle, boolean aResizable, boolean aClosable, boolean aMaximizable)
    {
        super(aTitle, aResizable, aClosable, aMaximizable);
        setFrameIcon(null);
    }

    /**
     * Get the frameBoundsListener.
     *
     * @return the frameBoundsListener
     */
    public HUDFrameBoundsHandler getFrameBoundsListener()
    {
        return myFrameBoundsHandler;
    }

    /**
     * Get the ignoresAdjustments.
     *
     * @return the ignoresAdjustments
     */
    public boolean ignoresAdjustments()
    {
        return myIgnoresAdjustments;
    }

    /**
     * Get the popable.
     *
     * @return the popable
     */
    public boolean isPopable()
    {
        return myPopable;
    }

    @Override
    public void repositionForInsets()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                setBounds(getBounds());
            }
        });
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        Rectangle adjustedBounds = new Rectangle(x, y, width, height);
        if (myFrameBoundsHelper != null && !myIgnoresAdjustments)
        {
            adjustedBounds = myFrameBoundsHelper.getAdjustedFrameBoundsForInternalFrameChange(new Rectangle(x, y, width, height),
                    true);
        }
        setFrameBoundsOnly(adjustedBounds);
        if (myFrameBoundsHandler != null)
        {
            myFrameBoundsHandler.boundsSet(adjustedBounds.x, adjustedBounds.y, adjustedBounds.width, adjustedBounds.height);
        }
    }

    /**
     * Adjust the bounds for when the main frame has been resized.
     *
     * @param initialLocation The location of the window before adjustment.
     */
    public void setBoundsForMainFrameChange(Rectangle initialLocation)
    {
        if (myFrameBoundsHelper != null && !myIgnoresAdjustments)
        {
            Rectangle adjustedBounds = myFrameBoundsHelper.getAdjustedFrameBoundsForMainFrameChange(initialLocation);
            setFrameBoundsOnly(adjustedBounds);
            if (myFrameBoundsHandler != null)
            {
                myFrameBoundsHandler.boundsSet(adjustedBounds.x, adjustedBounds.y, adjustedBounds.width, adjustedBounds.height);
            }
        }
    }

    /**
     * Set the helper for manage bounds adjustments.
     *
     * @param frameBoundsHelper the helper for manage bounds adjustments.
     */
    public void setFrameBoundsHelper(FrameBoundsHelper frameBoundsHelper)
    {
        myFrameBoundsHelper = frameBoundsHelper;
    }

    /**
     * Set the frameBoundsListener.
     *
     * @param frameBoundsListener the frameBoundsListener to set
     */
    public void setFrameBoundsListener(HUDFrameBoundsHandler frameBoundsListener)
    {
        myFrameBoundsHandler = frameBoundsListener;
    }

    /**
     * Moves and resizes this frame, without triggering any changes to the
     * associated screen tile.
     *
     * @param bounds the new bounds for the frame
     */
    public void setFrameBoundsOnly(Rectangle bounds)
    {
        super.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Set the ignoresAdjustments.
     *
     * @param ignoresAdjustments the ignoresAdjustments to set
     */
    public void setIgnoresAdjustments(boolean ignoresAdjustments)
    {
        myIgnoresAdjustments = ignoresAdjustments;
    }

    @Override
    public void setLocation(int x, int y)
    {
        setBounds(x, y, getWidth(), getHeight());
    }

    @Override
    public void setLocation(Point p)
    {
        setLocation(p.x, p.y);
    }

    /**
     * Set the popable.
     *
     * @param popable the popable to set
     */
    public void setPopable(boolean popable)
    {
        myPopable = popable;
    }
}
