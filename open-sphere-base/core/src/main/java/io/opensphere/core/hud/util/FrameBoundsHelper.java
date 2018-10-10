package io.opensphere.core.hud.util;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.util.function.Supplier;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/** A helper for validating and adjusting frame positions when changes occur. */
@SuppressWarnings("PMD.GodClass")
public class FrameBoundsHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FrameBoundsHelper.class);

    /** The desktop pane in which the frame lives. */
    private JDesktopPane myDesktopPane;

    /** The frame for which this helper is applicable. */
    private final PositionBoundedFrame myFrame;

    /** The provider for the desktop pane in which the frame lives. */
    private final Supplier<? extends JFrame> myFrameProvider;

    /** The current horizontal glue. */
    private HorizontalGlue myHorizontalGlue;

    /** The pixel offset distance from the edges to maintain. */
    private int myInset;

    /** Listener for changes to the frame inset preference. */
    private final PreferenceChangeListener myInsetChangeListener;

    /** When true, the windows should stick to the edges. */
    private boolean mySticky;

    /** Listener for changes to the stick to edge preference. */
    private final PreferenceChangeListener myStickyChangeListener = evt ->
    {
        synchronized (FrameBoundsHelper.this)
        {
            mySticky = evt.getValueAsBoolean(FrameOptionsProvider.DEFAULT_STICKY);
            resetGlue();
        }
    };

    /** The current vertical glue. */
    private VerticalGlue myVerticalGlue;

    /**
     * Constructor.
     *
     * @param prefsRegistry The system preferences registry.
     * @param frameProvider The provider for the parent frame that the frames
     *            are drawn in.
     * @param frame The frame for which this helper is applicable.
     */
    public FrameBoundsHelper(PreferencesRegistry prefsRegistry, Supplier<? extends JFrame> frameProvider,
            PositionBoundedFrame frame)
    {
        myFrame = frame;
        myInsetChangeListener = evt ->
        {
            synchronized (FrameBoundsHelper.this)
            {
                myInset = evt.getValueAsInt(FrameOptionsProvider.DEFAULT_INSET);
                myFrame.repositionForInsets();
            }
        };

        mySticky = prefsRegistry.getPreferences(FrameOptionsProvider.class).getBoolean(FrameOptionsProvider.STICKY_PREFERENCE_KEY,
                FrameOptionsProvider.DEFAULT_STICKY);
        myInset = prefsRegistry.getPreferences(FrameOptionsProvider.class).getInt(FrameOptionsProvider.INSET_PREFERENCE_KEY,
                FrameOptionsProvider.DEFAULT_INSET);

        prefsRegistry.getPreferences(FrameOptionsProvider.class)
                .addPreferenceChangeListener(FrameOptionsProvider.INSET_PREFERENCE_KEY, myInsetChangeListener);
        prefsRegistry.getPreferences(FrameOptionsProvider.class)
                .addPreferenceChangeListener(FrameOptionsProvider.STICKY_PREFERENCE_KEY, myStickyChangeListener);

        myFrameProvider = frameProvider;
    }

    /**
     * Get the adjusted frame position when the internal frame has been moved or
     * resized. If no adjustment is required, the returned rectangle will be the
     * same as the given one.
     *
     * @param bounds The bounds before adjustment.
     * @param checkGlue After adjusting the bounds, determine which types of
     *            glue to apply.
     * @return The bounds after adjustment.
     */
    public synchronized Rectangle getAdjustedFrameBoundsForInternalFrameChange(Rectangle bounds, boolean checkGlue)
    {
        if (!EventQueue.isDispatchThread())
        {
            LOGGER.error("Refusing to get the adjusted frame bounds while not on the EDT.");
            return myFrame.getBounds();
        }

        if (bounds == null || bounds.equals(myFrame.getBounds()))
        {
            return myFrame.getBounds();
        }

        return getBoundsOnScreen(bounds, checkGlue);
    }

    /**
     * Get the adjusted frame position when the main frame has been resized. If
     * no adjustment is required, the returned rectangle will be the same as the
     * given one.
     *
     * @param bounds The bounds before adjustment.
     * @return The bounds after adjustment.
     */
    public synchronized Rectangle getAdjustedFrameBoundsForMainFrameChange(Rectangle bounds)
    {
        if (bounds == null || !EventQueue.isDispatchThread())
        {
            return myFrame.getBounds();
        }

        if (getDesktopPane() == null)
        {
            LOGGER.error("Attempting to adjust frame position for frame with no parent.");
            return bounds;
        }

        Rectangle adjustedBounds;
        if (mySticky)
        {
            int xLoc = bounds.x;
            int width = bounds.width;
            int rightEdge = getDesktopPane().getWidth() - bounds.width;
            if (myHorizontalGlue == HorizontalGlue.BOTH)
            {
                xLoc = myInset;
                width += rightEdge - myInset;
            }
            else if (myHorizontalGlue == HorizontalGlue.LEFT)
            {
                xLoc = myInset;
            }
            else if (myHorizontalGlue == HorizontalGlue.RIGHT)
            {
                xLoc = rightEdge - myInset;
            }
            myHorizontalGlue = determineHorizontalGlue(xLoc, rightEdge).or(myHorizontalGlue);

            int yLoc = bounds.y;
            int bottomEdge = getDesktopPane().getHeight() - bounds.height;
            if (myVerticalGlue == VerticalGlue.TOP)
            {
                yLoc = myInset;
            }
            else if (myVerticalGlue == VerticalGlue.BOTTOM)
            {
                yLoc = bottomEdge - myInset;
            }
            myVerticalGlue = determineVerticalGlue(yLoc, bottomEdge);

            adjustedBounds = new Rectangle(xLoc, yLoc, width, bounds.height);
        }
        else
        {
            adjustedBounds = bounds;
        }

        // make sure it is on screen, but don't adjust the glue as it should
        // already be set.
        return getBoundsOnScreen(adjustedBounds, false);
    }

    /**
     * Determine whether the frame should be glued or unglued.
     *
     * @param xLocation The x-coordinate of the left edge of the frame.
     * @param rightEdge The width adjusted x-coordinate of the right edge of the
     *            frame.
     * @return The glue type.
     */
    private HorizontalGlue determineHorizontalGlue(int xLocation, int rightEdge)
    {
        if (xLocation <= myInset && xLocation >= rightEdge - myInset)
        {
            return HorizontalGlue.BOTH;
        }
        else if (xLocation <= myInset)
        {
            return HorizontalGlue.LEFT;
        }
        else if (xLocation >= rightEdge - myInset)
        {
            return HorizontalGlue.RIGHT;
        }
        else
        {
            return HorizontalGlue.NONE;
        }
    }

    /**
     * Determine whether the frame should be glued or unglued.
     *
     * @param yLocation The y-coordinate of the top edge of the frame.
     * @param bottomEdge The height adjusted y-coordinate of the bottom edge of
     *            the frame.
     * @return The glue type.
     */
    private VerticalGlue determineVerticalGlue(int yLocation, int bottomEdge)
    {
        if (yLocation <= myInset)
        {
            return VerticalGlue.TOP;
        }
        else if (yLocation >= bottomEdge - myInset)
        {
            return VerticalGlue.BOTTOM;
        }
        else
        {
            return VerticalGlue.NONE;
        }
    }

    /**
     * Get a frame location adjusted to be in the parent window. If the frame is
     * already contained, the adjusted bounds will be the same as the given
     * bounds.
     *
     * @param bounds The bounds to adjust.
     * @param checkGlue After adjusting the bounds, determine which types of
     *            glue to apply.
     * @return The adjusted bounds.
     */
    private Rectangle getBoundsOnScreen(Rectangle bounds, boolean checkGlue)
    {
        if (bounds == null)
        {
            return myFrame.getBounds();
        }

        JDesktopPane desktopPane = getDesktopPane();
        if (desktopPane == null)
        {
            LOGGER.error("Attempting to adjust frame position for frame with no parent.");
            return bounds;
        }

        int xLoc = bounds.x;
        int yLoc = bounds.y;

        int rightEdge = desktopPane.getWidth() - bounds.width;
        int bottomEdge = desktopPane.getHeight() - bounds.height;

        if (xLoc < myInset || yLoc < myInset || xLoc > rightEdge - myInset || yLoc > bottomEdge - myInset)
        {
            xLoc = Math.max(myInset, Math.min(xLoc, rightEdge - myInset));
            yLoc = Math.max(myInset, Math.min(yLoc, bottomEdge - myInset));
        }

        if (checkGlue && mySticky)
        {
            myVerticalGlue = determineVerticalGlue(yLoc, bottomEdge);
            myHorizontalGlue = determineHorizontalGlue(xLoc, rightEdge);
        }

        return new Rectangle(xLoc, yLoc, bounds.width, bounds.height);
    }

    /**
     * Get the desktop pane.
     *
     * @return The desktop pane.
     */
    private JDesktopPane getDesktopPane()
    {
        if (myDesktopPane == null)
        {
            JRootPane rootPane = myFrameProvider.get().getRootPane();
            for (Component comp : rootPane.getLayeredPane().getComponents())
            {
                if (comp instanceof JDesktopPane)
                {
                    myDesktopPane = (JDesktopPane)comp;
                    break;
                }
            }
        }
        return myDesktopPane;
    }

    /** Reset the current values for the glue. */
    private synchronized void resetGlue()
    {
        if (mySticky)
        {
            Rectangle bounds = myFrame.getBounds();
            int xLoc = bounds.x;
            int yLoc = bounds.y;
            int rightEdge = getDesktopPane().getWidth() - bounds.width;
            int bottomEdge = getDesktopPane().getHeight() - bounds.height;

            myHorizontalGlue = determineHorizontalGlue(xLoc, rightEdge);
            myVerticalGlue = determineVerticalGlue(yLoc, bottomEdge);
        }
        else
        {
            myVerticalGlue = VerticalGlue.NONE;
            myHorizontalGlue = HorizontalGlue.NONE;
        }
    }

    /** The available types of horizontal glue. */
    private enum HorizontalGlue
    {
        /** Stick to the left and right. */
        BOTH,

        /** Stick to the left. */
        LEFT,

        /** Do not stick. */
        NONE,

        /** Stick to the right. */
        RIGHT;

        /**
         * OR operation with another glue.
         *
         * @param other The other glue.
         * @return The result.
         */
        @SuppressWarnings("PMD.ShortMethodName")
        public HorizontalGlue or(HorizontalGlue other)
        {
            switch (this)
            {
                case BOTH:
                    return this;
                case LEFT:
                    return other == RIGHT || other == BOTH ? BOTH : this;
                case NONE:
                    return other == null ? this : other;
                case RIGHT:
                    return other == LEFT || other == BOTH ? BOTH : this;
                default:
                    throw new UnexpectedEnumException(this);
            }
        }
    }

    /** The available types of vertical glue. */
    private enum VerticalGlue
    {
        /** Stick to the bottom. */
        BOTTOM,

        /** Do not stick. */
        NONE,

        /** Stick to the top. */
        TOP,

        ;
    }
}
