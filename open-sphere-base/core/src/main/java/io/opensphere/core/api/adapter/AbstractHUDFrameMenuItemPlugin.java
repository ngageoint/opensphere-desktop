package io.opensphere.core.api.adapter;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.InternalComponentRegistry;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.viewer.impl.DynamicViewer;

/**
 * Simple plug-in adapter that registers a toggle menu item that displays an
 * internal frame when selected.
 */
public abstract class AbstractHUDFrameMenuItemPlugin extends AbstractLocationSaveMenuItemPlugin
{
    /** The Layer manager frame. */
    private HUDJInternalFrame myFrame;

    /**
     * CTOR with remember visibility state flag.
     *
     * @param title The title of the frame.
     * @param rememberVisibilityState - true to remember the visibility state
     *            session-to-session.
     * @param rememberLocation the remember location
     */
    public AbstractHUDFrameMenuItemPlugin(String title, boolean rememberVisibilityState, boolean rememberLocation)
    {
        super(title, rememberVisibilityState, rememberLocation);
    }

    /**
     * Add a component listener to the internal frame that selects or deselects
     * the menu item as appropriate.
     *
     * @param frame The HUD frame.
     */
    protected void addComponentListener(HUDJInternalFrame frame)
    {
        frame.getInternalFrame().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent e)
            {
                if (getMenuItem() != null)
                {
                    getMenuItem().setSelected(false);
                    updateVisibilityPreference();
                }
            }

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
        frame.getInternalFrame().addInternalFrameListener(new InternalFrameAdapter()
        {
            @Override
            public void internalFrameClosed(InternalFrameEvent e)
            {
                if (getMenuItem() != null)
                {
                    getMenuItem().setSelected(false);
                }
                myFrame = null;
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
        getHUDFrame().setVisible(true);
        setPreferredLocation();
    }

    /**
     * Create a HUD frame that wraps the internal frame returned by.
     *
     * @param toolbox The application toolbox.
     * @return The HUD frame. {@link #createInternalFrame(Toolbox)}.
     */
    protected HUDJInternalFrame createHUDFrame(Toolbox toolbox)
    {
        AbstractInternalFrame iframe = createInternalFrame(toolbox);
        HUDJInternalFrame.Builder builder = new HUDJInternalFrame.Builder();
        builder.setInternalFrame(iframe);
        return new HUDJInternalFrame(builder);
    }

    /**
     * Create the Internal Frame.
     *
     * @param toolbox The application toolbox.
     * @return The internal frame.
     */
    protected abstract AbstractInternalFrame createInternalFrame(Toolbox toolbox);

    /**
     * Accessor for the HUD frame.
     *
     * @return The HUD frame.
     */
    protected HUDJInternalFrame getHUDFrame()
    {
        assert EventQueue.isDispatchThread();
        if (myFrame == null)
        {
            myFrame = createHUDFrame(getToolbox());
            initHUDFrame(myFrame);
        }
        return myFrame;
    }

    @Override
    protected Point getLocation()
    {
        return getHUDFrame().getInternalFrame().getLocation();
    }

    /**
     * Initialize the HUD frame.
     *
     * @param frame The frame.
     */
    protected void initHUDFrame(HUDJInternalFrame frame)
    {
        addComponentListener(frame);
        installFrame(frame, getToolbox().getUIRegistry().getComponentRegistry());
    }

    /**
     * Install the frame in the component registry.
     *
     * @param frame The frame.
     * @param registry The component registry.
     */
    protected void installFrame(HUDJInternalFrame frame, InternalComponentRegistry registry)
    {
        registry.addObjectsForSource(this, Collections.singleton(frame));
    }

    @Override
    protected void setLocation(int xLoc, int yLoc)
    {
        DynamicViewer viewer = getToolbox().getMapManager().getStandardViewer();
        Rectangle vpRect = new Rectangle(0, 0, viewer.getViewportWidth(), viewer.getViewportHeight());

        AbstractInternalFrame iFrame = getHUDFrame().getInternalFrame();
        Rectangle frameRect = new Rectangle(xLoc, yLoc, iFrame.getWidth(), iFrame.getHeight());
        if (vpRect.contains(frameRect))
        {
            iFrame.setLocation(xLoc, yLoc);
        }
    }
}
