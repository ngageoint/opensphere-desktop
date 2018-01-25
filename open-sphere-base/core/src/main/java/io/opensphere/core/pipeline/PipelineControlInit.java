package io.opensphere.core.pipeline;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import org.apache.log4j.Logger;

import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.ui.InternalComponentRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.pipeline.processor.AbstractProcessor;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractTileRenderer;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.util.Utilities;

/** Pipeline controls initializer. */
public final class PipelineControlInit
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PipelineControlInit.class);

    /** The registry for HUD windows. */
    private static InternalComponentRegistry ourComponentRegistry;

    /**
     * Events which need to be forwarded to the canvas for the control context
     * to be functional.
     */
    private static long ourEventMask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
            | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK;

    /** Controls for handling globe movements. */
    private static ControlContext ourGlobeControls;

    /**
     * Initialize the controls.
     *
     * @param controlRegistry The control registry.
     * @param component The canvas.
     * @param uiRegistry The system registry for UI related facilities.
     */
    public static void initialize(ControlRegistry controlRegistry, final Component component, final UIRegistry uiRegistry)
    {
        ourGlobeControls = controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);

        final AWTEventListener controlListener = new AWTEventListener()
        {
            /** When true, the mouse is over the canvas. */
            private boolean myOverCanvas;

            @Override
            public synchronized void eventDispatched(AWTEvent event)
            {
                if (event instanceof MouseEvent)
                {
                    if (event.getID() == MouseEvent.MOUSE_EXITED && exitedComponent(component, (MouseEvent)event))
                    {
                        myOverCanvas = false;
                    }
                    /* When a drag is started over the canvas and continues when
                     * the mouse is not over the canvas, the drag events will
                     * continue to be delivered with the canvas as the source
                     * component. In this case we are not over the canvas, but
                     * for other events we are over the canvas if the canvas is
                     * the source of a mouse event. */
                    else if (event.getSource() == component && event.getID() != MouseEvent.MOUSE_DRAGGED
                            && event.getID() != MouseEvent.MOUSE_RELEASED)
                    {
                        myOverCanvas = true;
                    }
                }

                if (myOverCanvas && !ourComponentRegistry.isMouseOverHUD() && !hasDialogAncestor((Component)event.getSource()))
                {
                    handleEvent(event);
                }
            }

            /**
             * Determine whether the exit event applies to the component. The
             * event applies to the component if the source is the component or
             * one of its' ancestors.
             *
             * @param comp The component for which the exited event might apply.
             * @param exitedEvent The event.
             * @return true when the event applies to the component.
             */
            private boolean exitedComponent(Component comp, MouseEvent exitedEvent)
            {
                if (comp == null)
                {
                    return false;
                }

                if (Utilities.sameInstance(comp, exitedEvent.getSource()))
                {
                    return true;
                }

                return exitedComponent(comp.getParent(), exitedEvent);
            }

            /**
             * Check to see if this component or any parent is a Dialog.
             *
             * @param comp The component whose ancestry should be checked.
             * @return true when there is a Dialog ancestor.
             */
            private boolean hasDialogAncestor(Component comp)
            {
                if (comp instanceof Dialog)
                {
                    return true;
                }
                if (comp.getParent() != null)
                {
                    return hasDialogAncestor(comp.getParent());
                }
                return false;
            }
        };

        // The component registry will not yet be initialized, so the tread will
        // block until initialization is complete.
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ourComponentRegistry = uiRegistry.getComponentRegistry();
                Toolkit.getDefaultToolkit().addAWTEventListener(controlListener, ourEventMask);
            }
        }).start();
    }

    /**
     * Handle an event in which we are interested and forward the event as
     * necessary.
     *
     * @param event The event to handle.
     */
    private static void handleEvent(AWTEvent event)
    {
        switch (event.getID())
        {
            case MouseEvent.MOUSE_CLICKED:
                ourGlobeControls.mouseClicked((MouseEvent)event);
                break;
            case MouseEvent.MOUSE_DRAGGED:
                ourGlobeControls.mouseDragged((MouseEvent)event);
                break;
            case MouseEvent.MOUSE_ENTERED:
                ourGlobeControls.mouseEntered((MouseEvent)event);
                break;
            case MouseEvent.MOUSE_EXITED:
                ourGlobeControls.mouseExited((MouseEvent)event);
                break;
            case MouseEvent.MOUSE_MOVED:
                ourGlobeControls.mouseMoved((MouseEvent)event);
                break;
            case MouseEvent.MOUSE_PRESSED:
                ourGlobeControls.mousePressed((MouseEvent)event);
                break;
            case MouseEvent.MOUSE_RELEASED:
                ourGlobeControls.mouseReleased((MouseEvent)event);
                break;
            case MouseEvent.MOUSE_WHEEL:
                ourGlobeControls.mouseWheelMoved((MouseWheelEvent)event);
                break;
            case KeyEvent.KEY_PRESSED:
                ourGlobeControls.keyPressed((KeyEvent)event);
                break;
            case KeyEvent.KEY_RELEASED:
                ourGlobeControls.keyReleased((KeyEvent)event);
                break;
            case KeyEvent.KEY_TYPED:
                if (!GLUtilities.isProduction())
                {
                    if (((KeyEvent)event).getKeyChar() == '`')
                    {
                        AbstractProcessor.toggleRendererUpdatesPaused();
                    }
                    else if (((KeyEvent)event).getKeyChar() == '\\')
                    {
                        AbstractProcessor.toggleSplitJoinPaused();
                    }
                    else if (((KeyEvent)event).getKeyChar() == 't')
                    {
                        AbstractRenderer.toggleFeature(AbstractTileRenderer.DEBUG_TILE_BORDERS);
                    }
                    else if (((KeyEvent)event).getKeyChar() == 'l')
                    {
                        AbstractRenderer.toggleFeature(AbstractTileRenderer.DEBUG_TESSELLATION_LINES);
                    }
                }
                ourGlobeControls.keyTyped((KeyEvent)event);
                break;
            default:
                LOGGER.warn("Received and unexpected mouse event type : " + event.getID());
                break;
        }
    }

    /** Disallow instantiation. */
    private PipelineControlInit()
    {
    }
}
