package io.opensphere.hud.glswing;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;

/** Helper class for managing AWTEvents. */
public class GLSwingEventListenerHelper
{
    /** Listener for ComponentEvent on the internal frame. */
    private final InternalFrameComponentListener myComponentListener;

    /** The GLSwing frame for which this helper is managing events. */
    private final GLSwingInternalFrame myGLSwingFrame;

    /**
     * Listen for hierarchy events to determine when the frame's render order
     * may have been changed.
     */
    private final HierarchyListener myHierarchyListener;

    /** Listener for InternalFrameEvent on the internal frame. */
    private final InternalFrameAdapter myInternalFrameListener;

    /**
     * Listener for when the button is pressed for popping the frame out of the
     * HUD.
     */
    private final PropertyChangeListener myPopListener;

    /** Listener for rolling the frame up or down. */
    private final PropertyChangeListener myRollupListener;

    /** Listener for view changes. */
    private final ViewChangeListener myViewChangeListener;

    /** Support for viewer changes. */
    private final ViewChangeSupport myViewChangeSupport;

    /**
     * Constructor.
     *
     * @param frame The GLSwing frame for which this helper is managing events.
     * @param viewChangeSupport Support for viewer changes.
     */
    public GLSwingEventListenerHelper(GLSwingInternalFrame frame, ViewChangeSupport viewChangeSupport)
    {
        myGLSwingFrame = frame;
        myViewChangeSupport = viewChangeSupport;

        myInternalFrameListener = new InternalFrameAdapter()
        {
            @Override
            public void internalFrameClosed(InternalFrameEvent e)
            {
                myGLSwingFrame.handleFrameClosed();
            }
        };
        myGLSwingFrame.getHUDFrame().getInternalFrame().addInternalFrameListener(myInternalFrameListener);

        myComponentListener = new InternalFrameComponentListener();
        myGLSwingFrame.getHUDFrame().getInternalFrame().addComponentListener(myComponentListener);

        myHierarchyListener = new HierarchyListener()
        {
            @Override
            public void hierarchyChanged(HierarchyEvent e)
            {
                GLSwingEventManager.getInstance().validateRenderOrders();
            }
        };
        myGLSwingFrame.getHUDFrame().getInternalFrame().addHierarchyListener(myHierarchyListener);

        myViewChangeListener = new ViewChangeListener()
        {
            @Override
            public void viewChanged(final Viewer viewer, final ViewChangeType type)
            {
                myGLSwingFrame.handleViewChanged(viewer, type);
            }
        };
        myViewChangeSupport.addViewChangeListener(myViewChangeListener);

        myPopListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                myGLSwingFrame.popFrame();
            }
        };
        myGLSwingFrame.getHUDFrame().getInternalFrame().addPropertyChangeListener("framePopped", myPopListener);

        myRollupListener = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                boolean rolled = ((Boolean)evt.getNewValue()).booleanValue();
                if (rolled)
                {
                    myGLSwingFrame.windowShadeUp();
                }
                else
                {
                    myGLSwingFrame.windowShadeDown();
                }
            }
        };
        myGLSwingFrame.getHUDFrame().getInternalFrame().addPropertyChangeListener("frameRolledUp", myRollupListener);
    }

    /** Cleanup listeners which where added to the internal frame. */
    protected void close()
    {
        myGLSwingFrame.getHUDFrame().getInternalFrame().removeComponentListener(myComponentListener);
        myGLSwingFrame.getHUDFrame().getInternalFrame().removeInternalFrameListener(myInternalFrameListener);
        myGLSwingFrame.getHUDFrame().getInternalFrame().removeHierarchyListener(myHierarchyListener);
        myGLSwingFrame.getHUDFrame().getInternalFrame().removePropertyChangeListener("framePopped", myPopListener);
        myGLSwingFrame.getHUDFrame().getInternalFrame().removePropertyChangeListener("frameRolledUp", myRollupListener);
        myViewChangeSupport.removeViewChangeListener(myViewChangeListener);
    }

    /**
     * Component listener for the JInternalFrame.
     */
    private final class InternalFrameComponentListener extends ComponentAdapter
    {
        @Override
        public void componentHidden(ComponentEvent e)
        {
            myGLSwingFrame.handleComponentHidden(e);
        }

        @Override
        public void componentShown(ComponentEvent e)
        {
            myGLSwingFrame.handleComponentShown(e);
        }
    }
}
