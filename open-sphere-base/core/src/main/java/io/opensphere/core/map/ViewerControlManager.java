package io.opensphere.core.map;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.CompoundEventMouseAdapter;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DefaultMouseWheelBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewer.control.EarthDragListener;
import io.opensphere.core.viewer.control.FastZoomListener;
import io.opensphere.core.viewer.control.ViewPitchDragListener;
import io.opensphere.core.viewer.control.ZoomToMouseZoomer;
import io.opensphere.core.viewer.impl.AbstractViewer;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ViewControlTranslator;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/**
 * Manager for viewer controllers.
 */
@SuppressWarnings("PMD.GodClass")
abstract class ViewerControlManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ViewerControlManager.class);

    /** Map of viewer types to control translators. */
    private static final Map<Class<? extends AbstractViewer>, ViewControlTranslator> ourViewerTypeToControlTranslator;

    /** Map of viewer types to control translator types. */
    private static final Map<Class<? extends AbstractViewer>, Class<? extends ViewControlTranslator>> ourViewerTypeToControlTranslatorType;

    /** Hold a reference to my listeners. */
    private final List<BoundEventListener> myControlEventListeners = new ArrayList<>();

    /** A reference to the current view control translator. */
    private final VolatileReference<ViewControlTranslator> myCurrentViewControlTranslator = new VolatileReference<>();

    /** The map context. */
    private final MapContext<DynamicViewer> myMapContext;

    /** The executor for viewer and projection updates. */
    private Executor myUpdateExecutor;

    static
    {
        ourViewerTypeToControlTranslator = new LinkedHashMap<>();
        ourViewerTypeToControlTranslatorType = new LinkedHashMap<>();
    }

    /**
     * Add a viewer type to control translator type mapping.
     *
     * @param viewerType The viewer type.
     * @param translatorType The translator type.
     */
    public static void addViewerTypeToControlTranslatorType(Class<? extends AbstractViewer> viewerType,
            Class<? extends ViewControlTranslator> translatorType)
    {
        ourViewerTypeToControlTranslatorType.put(viewerType, translatorType);
    }

    /**
     * Construct the viewer control manager.
     *
     * @param currentViewer A volatile reference to the current viewer.
     * @param mapContext The map context.
     */
    public ViewerControlManager(VolatileReference<DynamicViewer> currentViewer, MapContext<DynamicViewer> mapContext)
    {
        for (Entry<Class<? extends AbstractViewer>, Class<? extends ViewControlTranslator>> entry : ourViewerTypeToControlTranslatorType
                .entrySet())
        {
            Constructor<? extends ViewControlTranslator> constructor;
            Exception ex;
            try
            {
                constructor = entry.getValue().getConstructor(VolatileReference.class);
                ourViewerTypeToControlTranslator.put(entry.getKey(), constructor.newInstance(currentViewer));
                ex = null;
            }
            catch (SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException
                    | IllegalAccessException | InvocationTargetException e)
            {
                ex = e;
            }
            if (ex != null)
            {
                LOGGER.error("Failed to instantiate class " + entry.getValue() + ": " + ex, ex);
            }
        }

        myMapContext = mapContext;
    }

    /**
     * Add listeners for events sent by a control context.
     *
     * @param context The control context.
     */
    public void addControlListeners(ControlContext context)
    {
        addViewKeyListeners(context);
        addMouseListeners(context);

        final String category = "Fly-to";
        DiscreteEventAdapter flyToDenver = new DiscreteEventAdapter(category, "That shack outside La Grange",
                "You know what I'm talkin about")
        {
            private ViewerAnimator myViewerAnimator;

            @Override
            public synchronized void eventOccurred(InputEvent event)
            {
                if (myViewerAnimator != null)
                {
                    myViewerAnimator.stop();
                }

                // Just let me know if you want to go to that home out on the
                // range.
                if (event instanceof KeyEvent && ((KeyEvent)event).getKeyCode() == KeyEvent.VK_D)
                {
                    final double lat = 29.91339;
                    final double lon = -96.83475;
                    final double alt = 16000;
                    Projection proj = myMapContext.getProjection();
                    myViewerAnimator = new ViewerAnimator(myMapContext.getStandardViewer(),
                            proj.convertToModel(
                                    new GeographicPosition(
                                            LatLonAlt.createFromDegreesMeters(lat, lon, alt, Altitude.ReferenceLevel.ELLIPSOID)),
                                    Vector3d.ORIGIN));
                    myViewerAnimator.start();
                }
            }
        };
        flyToDenver.setReassignable(false);
        myControlEventListeners.add(flyToDenver);
        context.addListener(flyToDenver, new DefaultKeyPressedBinding(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
                new DefaultKeyPressedBinding(KeyEvent.VK_ESCAPE));
    }

    /**
     * Clean up.
     */
    public void close()
    {
        myControlEventListeners.clear();
    }

    /**
     * Get the control translators for all projections, used for translating a
     * control event to a command to the viewer.
     *
     * @return The translators.
     */
    public Collection<ViewControlTranslator> getAllControlTranslators()
    {
        return ourViewerTypeToControlTranslator.values();
    }

    /**
     * Get the current control translator, used for translating a control event
     * to a command to the current viewer.
     *
     * @return The translator.
     */
    public synchronized ViewControlTranslator getCurrentControlTranslator()
    {
        return myCurrentViewControlTranslator.get();
    }

    /**
     * Set the current viewer type.
     *
     * @param viewerType The viewer type.
     */
    public void setCurrentViewerType(Class<? extends DynamicViewer> viewerType)
    {
        myCurrentViewControlTranslator.set(ourViewerTypeToControlTranslator.get(viewerType));
    }

    /**
     * Add the controls for left, right, up and down movement.
     *
     * @param context The control context.
     * @param category The control category.
     */
    protected void addLeftRightUpDownControls(ControlContext context, final String category)
    {
        // Camera Right
        DiscreteEventAdapter cameraRightListener = new DiscreteEventAdapter(category, "Camera Right",
                "Moves the camera to the right")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewRight(event, false);
            }
        };
        myControlEventListeners.add(cameraRightListener);
        context.addListener(cameraRightListener, new DefaultKeyPressedBinding(KeyEvent.VK_RIGHT));

        // Camera Left
        DiscreteEventAdapter cameraLeftListener = new DiscreteEventAdapter(category, "Camera Left",
                "Moves the camera to the left")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewLeft(event, false);
            }
        };
        myControlEventListeners.add(cameraLeftListener);
        context.addListener(cameraLeftListener, new DefaultKeyPressedBinding(KeyEvent.VK_LEFT));

        // Micro Camera Right
        DiscreteEventAdapter microRightListener = new DiscreteEventAdapter(category, "Micro Camera Right",
                "Moves the camera to the right")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewRight(event, true);
            }
        };
        myControlEventListeners.add(microRightListener);
        context.addListener(microRightListener, new DefaultKeyPressedBinding(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));

        // Micro Camera Left
        DiscreteEventAdapter microLeftListener = new DiscreteEventAdapter(category, "Micro Camera Left",
                "Moves the camera to the left")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewLeft(event, true);
            }
        };
        myControlEventListeners.add(microLeftListener);
        context.addListener(microLeftListener, new DefaultKeyPressedBinding(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));

        // Camera Up
        DiscreteEventAdapter cameraUpListener = new DiscreteEventAdapter(category, "Camera Up", "Moves the camera up")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewUp(event, false);
            }
        };
        myControlEventListeners.add(cameraUpListener);
        context.addListener(cameraUpListener, new DefaultKeyPressedBinding(KeyEvent.VK_UP));

        // Camera Down
        DiscreteEventAdapter cameraDownListener = new DiscreteEventAdapter(category, "Camera Down", "Moves the camera down")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewDown(event, false);
            }
        };
        myControlEventListeners.add(cameraDownListener);
        context.addListener(cameraDownListener, new DefaultKeyPressedBinding(KeyEvent.VK_DOWN));

        // Micro Camera Up
        DiscreteEventAdapter microUpListener = new DiscreteEventAdapter(category, "Micro Camera Up", "Moves the camera up")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewUp(event, true);
            }
        };
        myControlEventListeners.add(microUpListener);
        context.addListener(microUpListener, new DefaultKeyPressedBinding(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK));

        // Micro Camera Down
        DiscreteEventAdapter microDownListener = new DiscreteEventAdapter(category, "Micro Camera Down", "Moves the camera down")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().viewDown(event, true);
            }
        };
        myControlEventListeners.add(microDownListener);
        context.addListener(microDownListener, new DefaultKeyPressedBinding(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK));
    }

    /**
     * Add the roll/pitch/yaw controls.
     *
     * @param context The control context.
     * @param category The control category.
     */
    protected void addRollPitchYawControls(ControlContext context, final String category)
    {
        // TODO pitch and yaw are not being bound until the viewer is fixed to
        // work with them.

//        // Pitch Up
//        DiscreteEventAdapter pitchUpListener = new DiscreteEventAdapter(category, "Pitch Up",
//                "Move the camera aim up from the current looking direction")
//        {
//            @Override
//            public void eventOccurred(InputEvent event)
//            {
//                getCurrentControlTranslator().pitchViewUp(event);
//            }
//        };
//        myControlEventListeners.add(pitchUpListener);
//        context.addListener(pitchUpListener, new DefaultKeyPressedBinding(KeyEvent.VK_A));
//
//        // Pitch Down
//        DiscreteEventAdapter pitchDownListener = new DiscreteEventAdapter(category, "Pitch down",
//                "Move the camera aim down from the current looking direction")
//        {
//            @Override
//            public void eventOccurred(InputEvent event)
//            {
//                getCurrentControlTranslator().pitchViewDown(event);
//            }
//        };
//        myControlEventListeners.add(pitchDownListener);
//        context.addListener(pitchDownListener, new DefaultKeyPressedBinding(KeyEvent.VK_Z));

        // Roll Left
        DiscreteEventAdapter rollLeftListener = new DiscreteEventAdapter(category, "Roll View Left",
                "Rotates the camera counter-clockwise along its current direction")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().rollViewLeft(event);
            }
        };
        myControlEventListeners.add(rollLeftListener);
        context.addListener(rollLeftListener, new DefaultKeyPressedBinding(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK));

        // Roll Right
        DiscreteEventAdapter rollRightListener = new DiscreteEventAdapter(category, "Roll View Right",
                "Rotates the camera clockwise along its current direction")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().rollViewRight(event);
            }
        };
        myControlEventListeners.add(rollRightListener);
        context.addListener(rollRightListener, new DefaultKeyPressedBinding(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK));

//        // Yaw right
//        DiscreteEventAdapter yawRightListener = new DiscreteEventAdapter(category, "Yaw View Right",
//                "Rotates the camera to the right around its up/down axis")
//        {
//            @Override
//            public void eventOccurred(InputEvent event)
//            {
//                getCurrentControlTranslator().yawViewRight(event);
//            }
//        };
//        myControlEventListeners.add(yawRightListener);
//        context.addListener(yawRightListener, new DefaultKeyPressedBinding(KeyEvent.VK_S));
//
//        // Yaw left
//        DiscreteEventAdapter yawLeftListener = new DiscreteEventAdapter(category, "Yaw View Left",
//                "Rotates the camera left around its up/down axis")
//        {
//            @Override
//            public void eventOccurred(InputEvent event)
//            {
//                getCurrentControlTranslator().yawViewLeft(event);
//            }
//        };
//        myControlEventListeners.add(yawLeftListener);
//        context.addListener(yawLeftListener, new DefaultKeyPressedBinding(KeyEvent.VK_X));
    }

    /**
     * Add the controls for zoom and reset.
     *
     * @param context The control context.
     * @param category The control category.
     */
    protected void addZoomAndReset(ControlContext context, final String category)
    {
        // Reset the view
        DiscreteEventAdapter resetListener = new DiscreteEventAdapter(category, "Reset View",
                "Move the camera so it faces directly at the surface with north up.")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().resetView(event);
            }
        };
        myControlEventListeners.add(resetListener);
        context.addListener(resetListener, new DefaultKeyPressedBinding(KeyEvent.VK_R));

        // Zoom In
        DiscreteEventAdapter mouseZoomInListener = new DiscreteEventAdapter(category, "Mouse Zoom in", "Zooms in the camera")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                ZoomToMouseZoomer zoomer = new ZoomToMouseZoomer();
                zoomer.zoomInView(getCurrentControlTranslator(), myMapContext, event);
            }
        };
        myControlEventListeners.add(mouseZoomInListener);
        context.addListener(mouseZoomInListener, new DefaultMouseWheelBinding(DefaultMouseWheelBinding.WheelDirection.UP));

        DiscreteEventAdapter keyZoomInListener = new DiscreteEventAdapter(category, "Zoom in", "Zooms in the camera")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().zoomInView(event);
            }
        };
        myControlEventListeners.add(keyZoomInListener);
        context.addListener(keyZoomInListener, new DefaultKeyPressedBinding(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK));

        // Zoom Out
        DiscreteEventAdapter mouseZoomOutListener = new DiscreteEventAdapter(category, "Mouse Zoom out", "Zooms out the camera")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                ZoomToMouseZoomer zoomer = new ZoomToMouseZoomer();
                zoomer.zoomOutView(getCurrentControlTranslator(), myMapContext, event);
            }
        };
        myControlEventListeners.add(mouseZoomOutListener);
        context.addListener(mouseZoomOutListener, new DefaultMouseWheelBinding(DefaultMouseWheelBinding.WheelDirection.DOWN));

        DiscreteEventAdapter keyZoomOutListener = new DiscreteEventAdapter(category, "Zoom out", "Zooms out the camera")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                getCurrentControlTranslator().zoomOutView(event);
            }
        };
        myControlEventListeners.add(keyZoomOutListener);
        context.addListener(keyZoomOutListener, new DefaultKeyPressedBinding(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK));
    }

    /**
     * Get the map context.
     *
     * @return The map context.
     */
    protected MapContext<DynamicViewer> getMapContext()
    {
        return myMapContext;
    }

    /**
     * Get the executor for view and projection changes.
     *
     * @return The executor.
     */
    protected Executor getUpdateExecutor()
    {
        return myUpdateExecutor;
    }

    /**
     * Hook method for projection changes.
     */
    protected abstract void switchProjection();

    /**
     * Add mouse listeners.
     *
     * @param context The control context.
     */
    private void addMouseListeners(ControlContext context)
    {
        String string = "View";
        CompoundEventMouseAdapter smoothYawListener = new CompoundEventMouseAdapter(string, "Smooth Yaw View",
                "Rotates the camera around its up/down axis with mouse movement")
        {
            @Override
            public void eventEnded(InputEvent event)
            {
                getCurrentControlTranslator().compoundViewYawEnd(event);
            }

            @Override
            public void eventStarted(InputEvent event)
            {
                getCurrentControlTranslator().compoundViewYawStart(event);
            }

            @Override
            public int getTargetPriority()
            {
                return 1000;
            }

            @Override
            public boolean isTargeted()
            {
                return true;
            }

            @Override
            public void mouseDragged(MouseEvent event)
            {
                getCurrentControlTranslator().compoundViewYawDrag(event);
            }

            @Override
            public void mouseMoved(MouseEvent event)
            {
                getCurrentControlTranslator().compoundViewYawDrag(event);
            }

            @Override
            public boolean mustBeTargeted()
            {
                return true;
            }
        };
        myControlEventListeners.add(smoothYawListener);
        context.addListener(smoothYawListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON3_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        myControlEventListeners.add(smoothYawListener);

//        CompoundEventMouseAdapter changeViewAxisListener = new CompoundEventMouseAdapter(string, "Change View Axis",
//                "Changes the view axis")
//        {
//            @Override
//            public void eventEnded(InputEvent event)
//            {
//                getCurrentControlTranslator().compoundMoveAxisEnd(event);
//            }
//
//            @Override
//            public void eventStarted(InputEvent event)
//            {
//                getCurrentControlTranslator().compoundMoveAxisStart(event);
//            }
//
//            @Override
//            public void mouseDragged(MouseEvent event)
//            {
//                getCurrentControlTranslator().compoundMoveAxisDrag(event);
//            }
//
//            @Override
//            public void mouseMoved(MouseEvent event)
//            {
//                getCurrentControlTranslator().compoundMoveAxisDrag(event);
//            }
//        };
//        context.addListener(changeViewAxisListener, new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED,
//                InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));

        EarthDragListener earthDragListener = new EarthDragListener(myCurrentViewControlTranslator.getReadOnly());
        myControlEventListeners.add(earthDragListener);
        context.addListener(earthDragListener, new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK));

        FastZoomListener fastZoomListener = new FastZoomListener(myCurrentViewControlTranslator.getReadOnly(), myMapContext);
        myControlEventListeners.add(fastZoomListener);
        context.addListener(fastZoomListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON2_DOWN_MASK));

        ViewPitchDragListener viewPitchDragListener = new ViewPitchDragListener(myCurrentViewControlTranslator.getReadOnly());
        myControlEventListeners.add(viewPitchDragListener);
        context.addListener(viewPitchDragListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON3_DOWN_MASK));
    }

    /**
     * Add key listeners.
     *
     * @param context The control context.
     */
    private void addViewKeyListeners(ControlContext context)
    {
        final String category = "View";
        addZoomAndReset(context, category);
        addLeftRightUpDownControls(context, category);
        addRollPitchYawControls(context, category);
    }
}
