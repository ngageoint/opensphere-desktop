package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * A glass pane to put over a frame which will intercept events to prevent
 * interaction with the frame.
 */
public class DisabledGlassPane extends JComponent
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Use glass pane to prevent interaction with the parent frame.
     *
     * @param child the component whose parent frame should be disabled.
     * @return The original glass pane of the parent frame.
     */
    public static Component disableParentFrame(Component child)
    {
        Component currentGlass = null;
        RootPaneContainer parentFrame = (RootPaneContainer)SwingUtilities.getAncestorOfClass(RootPaneContainer.class, child);
        if (parentFrame != null)
        {
            currentGlass = parentFrame.getGlassPane();
            DisabledGlassPane glass = new DisabledGlassPane();
            parentFrame.setGlassPane(glass);
            glass.activate();
        }
        return currentGlass;
    }

    /**
     * Restore interaction with the parent frame.
     *
     * @param child the component whose parent frame should be enabled.
     * @param glass This should typically be the original glass pane of the
     *            parent frame.
     */
    public static void enableParentFrame(Component child, Component glass)
    {
        RootPaneContainer parentFrame = (RootPaneContainer)SwingUtilities.getAncestorOfClass(RootPaneContainer.class, child);
        if (parentFrame != null)
        {
            parentFrame.setGlassPane(glass);
        }
    }

    /** Constructor. */
    public DisabledGlassPane()
    {
        setOpaque(false);
        Color background = new Color(0, 0, 0, 0);
        setBackground(background);
        setLayout(new GridBagLayout());

        addMouseListener(new MouseAdapter()
        {
        });
        addMouseMotionListener(new MouseMotionAdapter()
        {
        });

        addKeyListener(new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                e.consume();
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                e.consume();
            }

            @Override
            public void keyTyped(KeyEvent e)
            {
                e.consume();
            }
        });
    }

    /** When activated, events are intercepted. */
    public void activate()
    {
        setVisible(true);
    }

    /** When deactivated, events are not intercepted. */
    public void deactivate()
    {
        // when a component is not visible, swing events are not delivered.
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
    }
}
