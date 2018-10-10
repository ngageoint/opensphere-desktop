package io.opensphere.core.hud.awt;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.Timer;

import io.opensphere.core.util.Utilities;

/** An internal frame whose content pane's aspect ratio is maintained. */
public class FixedAspectInternalFrame extends AbstractInternalFrame
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Whether to completely allow a size change. */
    private boolean myAllowChange;

    /** The desired aspect ratio (width/height). */
    private double myAspectRatio;

    /** The last allowed size. */
    private Dimension myLastAllowedSize;

    /** The size correction timer. */
    private Timer myTimer;

    /**
     * Calculates the allowed size.
     *
     * @param fromSize the from size
     * @param toSize the to size
     * @param aspectRatio the aspect ratio (width/height)
     * @return the allowed size
     */
    private static Dimension getAllowedSize(Dimension fromSize, Dimension toSize, double aspectRatio)
    {
        int width;
        int height;

        boolean widthChanged = fromSize == null || toSize.width != fromSize.width;
        boolean heightChanged = fromSize == null || toSize.height != fromSize.height;
        if (heightChanged && !widthChanged)
        {
            height = toSize.height;
            width = (int)Math.round(height * aspectRatio);
        }
        else
        {
            width = toSize.width;
            height = (int)Math.round(width / aspectRatio);
        }

        return new Dimension(width, height);
    }

    /**
     * Creates a non-resizable, non-closable, non-maximizable, non-iconifiable
     * <code>JInternalFrame</code> with no title.
     */
    public FixedAspectInternalFrame()
    {
        super();
    }

    /**
     * Creates a non-maximizable, non-iconifiable <code>JInternalFrame</code>
     * with the specified title, resizability, and closability.
     *
     * @param aTitle the <code>String</code> to display in the title bar
     * @param aResizable if <code>true</code>, the internal frame can be resized
     * @param aClosable if <code>true</code>, the internal frame can be closed
     */
    public FixedAspectInternalFrame(String aTitle, boolean aResizable, boolean aClosable)
    {
        super(aTitle, aResizable, aClosable);
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
    public FixedAspectInternalFrame(String aTitle, boolean aResizable, boolean aClosable, boolean aMaximizable)
    {
        super(aTitle, aResizable, aClosable, aMaximizable);
    }

    /**
     * Sets the aspect ratio and size.
     *
     * @param aspectRatio the aspect ratio (width/height)
     */
    public void setAspect(double aspectRatio)
    {
        myAspectRatio = aspectRatio;
        Dimension newSize = getAllowedSize(myLastAllowedSize, getContentPane().getSize(), myAspectRatio);
        setTrustedSize(newSize);
    }

    /**
     * Sets aspect ratio and size with a size.
     *
     * @param width the width
     * @param height the height
     */
    public void setAspect(int width, int height)
    {
        myAspectRatio = (double)width / height;
        setTrustedSize(new Dimension(width, height));
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        Boolean rolledProp = (Boolean)((JComponent)this).getClientProperty(AbstractInternalFrame.ROLL_PROPERTY);
        if (myAllowChange || Utilities.booleanValue(rolledProp))
        {
            stopTimer();
        }
        else
        {
            startTimer();
        }
        super.setBounds(x, y, width, height);
    }

    /**
     * Sets the size to a trusted size.
     *
     * @param size the size
     */
    private void setTrustedSize(Dimension size)
    {
        myLastAllowedSize = (Dimension)size.clone();
        myAllowChange = true;
        getContentPane().setPreferredSize(size);
        getContentPane().setSize(size);
        pack();
        myAllowChange = false;
    }

    /**
     * Starts the size correction timer.
     */
    private void startTimer()
    {
        if (myTimer == null)
        {
            myTimer = new Timer(1000, e ->
            {
                if (myLastAllowedSize != null)
                {
                    Dimension newSize = getAllowedSize(myLastAllowedSize, getContentPane().getSize(), myAspectRatio);
                    setTrustedSize(newSize);
                }
            });
        }
        myTimer.start();
    }

    /**
     * Stops the size correction timer.
     */
    private void stopTimer()
    {
        if (myTimer != null)
        {
            myTimer.stop();
        }
    }
}
