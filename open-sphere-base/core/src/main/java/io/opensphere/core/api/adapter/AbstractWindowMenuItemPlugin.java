package io.opensphere.core.api.adapter;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.viewer.impl.DynamicViewer;

/**
 * Simple plug-in adapter that registers a toggle menu item that displays a
 * window when selected.
 */
public abstract class AbstractWindowMenuItemPlugin extends AbstractLocationSaveMenuItemPlugin
{
    /** The Layer manager frame. */
    private Window myFrame;

    /**
     * CTOR with remember visibility state flag.
     *
     * @param title The title of the frame.
     * @param rememberVisibilityState - true to remember the visibility state
     *            session-to-session.
     * @param rememberLocation the remember location
     */
    public AbstractWindowMenuItemPlugin(String title, boolean rememberVisibilityState, boolean rememberLocation)
    {
        super(title, rememberVisibilityState, rememberLocation);
    }

    /**
     * Add a component listener to the internal frame that selects or deselects
     * the menu item as appropriate.
     *
     * @param window The window.
     */
    protected void addComponentListener(Window window)
    {
        window.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentMoved(ComponentEvent e)
            {
                updateStoredLocationPreference();
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
                if (getMenuItem() != null)
                {
                    getMenuItem().setSelected(true);
                    updateVisibilityPreference();
                }
            }
        });

        window.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                if (getMenuItem() != null)
                {
                    getMenuItem().setSelected(false);
                    updateVisibilityPreference();
                }
            }
        });
    }

    @Override
    protected void buttonDeselected()
    {
        if (myFrame != null)
        {
            myFrame.setVisible(false);
        }
    }

    @Override
    protected void buttonSelected()
    {
        getWindow().setVisible(true);
        setPreferredLocation();
    }

    /**
     * Create the Window.
     *
     * @param toolbox The application toolbox.
     * @return The Window.
     */
    protected abstract Window createWindow(Toolbox toolbox);

    @Override
    protected Point getLocation()
    {
        return getWindow().getLocation();
    }

    /**
     * Accessor for the Window.
     *
     * @return The Window.
     */
    protected Window getWindow()
    {
        assert EventQueue.isDispatchThread();
        if (myFrame == null)
        {
            myFrame = createWindow(getToolbox());
            addComponentListener(myFrame);
        }
        return myFrame;
    }

    @Override
    protected void setLocation(int xLoc, int yLoc)
    {
        DynamicViewer viewer = getToolbox().getMapManager().getStandardViewer();
        Rectangle vpRect = new Rectangle(0, 0, viewer.getViewportWidth(), viewer.getViewportHeight());

        Rectangle frameRect = new Rectangle(xLoc, yLoc, getWindow().getWidth(), getWindow().getHeight());
        if (vpRect.contains(frameRect))
        {
            getWindow().setLocation(xLoc, yLoc);
        }
    }
}
