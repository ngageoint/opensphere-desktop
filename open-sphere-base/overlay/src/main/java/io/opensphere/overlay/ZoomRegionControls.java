package io.opensphere.overlay;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.MapManager;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/**
 * Responsible for handling the drawing of zoom regions.
 */
public class ZoomRegionControls extends AbstractRegionControls
{
    /** The color for the box. */
    private static final Color COLOR = new Color(170, 0, 0, 255);

    /** The coordinates of the box. */
    private List<LatLonAlt> myPositions;

    /**
     * Construct the Selection Box Controls.
     *
     * @param mapManager The system map manager.
     * @param unitsRegistry The system units registry.
     * @param transformer The transformer used to generate the box geometry.
     */
    public ZoomRegionControls(MapManager mapManager, UnitsRegistry unitsRegistry, SelectionRegionTransformer transformer)
    {
        super(mapManager, unitsRegistry, transformer);
    }

    /**
     * Register with the control registry.
     *
     * @param controlRegistry The control registry.
     */
    public void register(ControlRegistry controlRegistry)
    {
        //@formatter:off
        ControlContext controlContext = controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        ZoomMouseListener listener = new ZoomMouseListener();
        controlContext.addListener(listener, new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK
                | InputEvent.CTRL_DOWN_MASK), new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED, InputEvent.BUTTON1_DOWN_MASK
                    | InputEvent.CTRL_DOWN_MASK), new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK
                        | InputEvent.CTRL_DOWN_MASK));
        addControlContextListener(listener);
        //@formatter:on
    }

    @Override
    protected void finishRegion()
    {
        if (myPositions != null)
        {
            zoomToBox(myPositions);
            myPositions = null;
        }
        super.finishRegion();
    }

    @Override
    protected Color getColor()
    {
        return COLOR;
    }

    @Override
    protected void setBoundingBoxRegion(LatLonAlt begin, LatLonAlt end)
    {
        super.setBoundingBoxRegion(begin, end);
        myPositions = Arrays.asList(begin, end);
    }

    /**
     * Zoom to the selected box.
     *
     * @param llas The coordinates of the box.
     */
    protected void zoomToBox(List<? extends LatLonAlt> llas)
    {
        Collection<GeographicPosition> positions = New.collection(llas.size());
        for (LatLonAlt lla : llas)
        {
            GeographicPosition pos = new GeographicPosition(lla);
            positions.add(pos);
        }
        ViewerAnimator animator = new ViewerAnimator(getStandardViewer(), positions, true);
        animator.start(100);
        animator.addListener(new ViewerAnimator.Listener()
        {
            @Override
            public void animationDone()
            {
                getTransformer().clearRegion();
            }
        });
    }

    /** Listener for mouse events which cause me to draw a zoom box. */
    private class ZoomMouseListener extends DiscreteEventAdapter
    {
        /** Constructor. */
        public ZoomMouseListener()
        {
            super("Zoom Boxes", "Draw Zoom Bounding Box", "Draws a bounding box that will be<br/>zoomed to.");
        }

        @Override
        public void eventOccurred(final InputEvent event)
        {
            /* If the event is a release, the modifiers will be ignored when
             * determining the binding. This means that we may receive this
             * event when a zoom region is not being drawn, so do not consume
             * the event. */
            if (event.getID() != MouseEvent.MOUSE_RELEASED)
            {
                event.consume();
            }
            getQueryRegionExecutor().execute(new Runnable()
            {
                @Override
                public void run()
                {
                    MouseEvent mouseEvent = (MouseEvent)event;
                    if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED)
                    {
                        addPosition(mouseEvent.getPoint());
                    }
                    else if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED)
                    {
                        finishRegion();
                    }
                    else if (mouseEvent.getID() == MouseEvent.MOUSE_DRAGGED)
                    {
                        setSelectionBox(getPositions(), mouseEvent);
                    }
                }
            });
        }
    }
}
