package io.opensphere.controlpanels.animation.view;

import static io.opensphere.controlpanels.timeline.DragHandle.TRIANGLE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.timeline.CompositeLayer;
import io.opensphere.controlpanels.timeline.ContextLabel;
import io.opensphere.controlpanels.timeline.DragHandle;
import io.opensphere.controlpanels.timeline.DraggableTimeWindowLayer;
import io.opensphere.controlpanels.timeline.ObservableTimeSpan;
import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.units.duration.Years;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.CalendarUtilities;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;

/**
 * Active time window layer.
 */
class ActiveWindowLayer extends CompositeLayer
{
    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The direction of the active window. */
    private Direction myDirection = Direction.FORWARD;

    /** The drag handles layer. */
    private final AnimationDragHandlesLayer myDragHandles;

    /** Layer that draws the fade. */
    private final FadeLayer myFadeLayer;

    /** Optimization for painting triangles. */
    private int mySide = SwingUtilities.RIGHT;

    /** The snap function. */
    private final SnapFunction mySnapFunction;

    /** The time manager. */
    private final TimeManager myTimeManager;

    /** The time model. */
    private final ObservableValue<TimeSpan> myTimeModel;

    /** Supplier for the end of the active time span. */
    private final Supplier<TimeInstant> myTimeSpanEndSupplier = new Supplier<TimeInstant>()
    {
        @Override
        public TimeInstant get()
        {
            return myTimeModel.get().getEndInstant();
        }
    };

    /** Supplier for the start of the active time span. */
    private final Supplier<TimeInstant> myTimeSpanStartSupplier = new Supplier<TimeInstant>()
    {
        @Override
        public TimeInstant get()
        {
            return myTimeModel.get().getStartInstant();
        }
    };

    /** The time window layer. */
    private final DraggableTimeWindowLayer myTimeWindow;

    /**
     * The "find active window" shape that happens to be a triangle.
     */
    private Polygon myTriangle;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     *
     * @param toolbox The toolbox through which application state is accessed.
     * @param timeModel the time model
     * @param animationModel the animation model
     * @param millisPerPixel the resolution of the timeline display
     * @param timeManager the time manager
     */
    public ActiveWindowLayer(Toolbox toolbox, ObservableValue<TimeSpan> timeModel, AnimationModel animationModel,
            Supplier<Double> millisPerPixel, TimeManager timeManager)
    {
        super();
        myToolbox = toolbox;
        myTimeModel = timeModel;
        myAnimationModel = animationModel;
        myTimeManager = timeManager;

        mySnapFunction = new ActiveSpanEndSnapFunction(animationModel, millisPerPixel);

        Function<TimeInstant, TimeInstant> constraint = new Function<TimeInstant, TimeInstant>()
        {
            @Override
            public TimeInstant apply(TimeInstant t)
            {
                if (myAnimationModel.getLoopSpanLocked().get().booleanValue())
                {
                    return myAnimationModel.getLoopSpan().get().clamp(t);
                }
                else
                {
                    return t;
                }
            }
        };
        myTimeWindow = new DraggableTimeWindowLayer(timeModel, AnimationConstants.ACTIVE_HANDLE_COLOR,
                ColorUtilities.opacitizeColor(AnimationConstants.ACTIVE_HANDLE_COLOR, 64), constraint,
                animationModel.getActiveSpanDuration(), mySnapFunction, null);
        myTimeWindow.setToolTipText("The active span");
        getLayers().add(myTimeWindow);

        ObservableTimeSpan observableTimeSpan = new ObservableTimeSpan(timeModel);

        SnapFunction leftSnapFunction = new ActiveDurationEndSnapFunction(myTimeSpanEndSupplier, myTimeSpanStartSupplier,
                animationModel, millisPerPixel);
        SnapFunction rightSnapFunction = new ActiveDurationEndSnapFunction(myTimeSpanStartSupplier, myTimeSpanEndSupplier,
                animationModel, millisPerPixel);

        myDragHandles = new AnimationDragHandlesLayer(toolbox, observableTimeSpan, constraint, leftSnapFunction,
                rightSnapFunction, animationModel.playStateProperty(), "active", AnimationConstants.ACTIVE_HANDLE_COLOR,
                AnimationConstants.ACTIVE_HANDLE_HOVER_COLOR);
        getLayers().add(myDragHandles);

        myFadeLayer = new FadeLayer(timeModel, animationModel, myTimeWindow.getBGColor());
        getLayers().add(myFadeLayer);
    }

    @Override
    public boolean canDrag(Point p)
    {
        return !myAnimationModel.getPlayState().isPlaying() && super.canDrag(p);
    }

    @Override
    public Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime)
    {
        // Set the play state to stop so that the fade doesn't keep getting
        // painted on the wrong side. In general it probably makes sense to
        // consider it stopped once the active window is dragged anyway.
        myAnimationModel.setPlayState(PlayState.STOP);

        return super.drag(dragObject, from, to, beginning, dragTime);
    }

    @Override
    public void getMenuItems(Point p, List<JMenuItem> menuItems)
    {
        super.getMenuItems(p, menuItems);

        if (canDrag(p))
        {
            final JMenu setDurationMenu = new JMenu("Set active duration to");
            for (Duration dur : Arrays.asList(Days.ONE, Weeks.ONE, Months.ONE))
            {
                setDurationMenu.add(new JMenuItem(new SetDurationAction(myTimeModel, myAnimationModel, getUIModel(), dur)));
            }
            menuItems.add(setDurationMenu);

            if (!myAnimationModel.getSnapToDataBoundaries().get().booleanValue())
            {
                JMenuItem skipMenuItem = new JMenuItem(
                        new IntervalAction("Skip active span", myAnimationModel.getSkippedIntervals(), myTimeModel));
                skipMenuItem.setIcon(new GenericFontIcon(AwesomeIconSolid.BAN, Color.RED));
                menuItems.add(skipMenuItem);

                JMenuItem holdMenuItem = new JMenuItem(
                        new IntervalAction("Hold active span", myAnimationModel.getHeldIntervals(), myTimeModel));
                holdMenuItem.setIcon(new GenericFontIcon(AwesomeIconSolid.HAND_ROCK, Color.WHITE));
                menuItems.add(holdMenuItem);
            }
        }

        JMenu zoomMenu = new JMenu("Zoom to");
        zoomMenu.setIcon(new GenericFontIcon(AwesomeIconSolid.CROP, Color.WHITE));
        zoomMenu.add(new JSeparator());

        Calendar cal = Calendar.getInstance();
        CalendarUtilities.clearFields(cal, CalendarUtilities.HOUR_INDEX, CalendarUtilities.MILLISECOND_INDEX);
        zoomMenu.add(addSetUISpanMenuItem("today", TimeSpan.get(cal.getTime(), Days.ONE)));
        zoomMenu.add(addSetUISpanMenuItem("yesterday", TimeSpan.get(Days.ONE, cal.getTime())));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        zoomMenu.add(addSetUISpanMenuItem("this week", TimeSpan.get(cal.getTime(), Weeks.ONE)));
        zoomMenu.add(addSetUISpanMenuItem("last week", TimeSpan.get(Weeks.ONE, cal.getTime())));

        cal.setTimeInMillis(System.currentTimeMillis());
        CalendarUtilities.clearFields(cal, CalendarUtilities.DAY_INDEX, CalendarUtilities.MILLISECOND_INDEX);
        zoomMenu.add(addSetUISpanMenuItem("this month", TimeSpan.get(cal.getTime(), Months.ONE)));

        cal.setTimeInMillis(System.currentTimeMillis());
        CalendarUtilities.clearFields(cal, CalendarUtilities.HOUR_INDEX, CalendarUtilities.MILLISECOND_INDEX);
        zoomMenu.add(addSetUISpanMenuItem("last 30 days", TimeSpan.get(new Days(30), cal.getTime())));
        zoomMenu.add(addSetUISpanMenuItem("last 60 days", TimeSpan.get(new Days(60), cal.getTime())));
        zoomMenu.add(addSetUISpanMenuItem("last 90 days", TimeSpan.get(new Days(90), cal.getTime())));

        cal.set(Calendar.DAY_OF_YEAR, 0);
        zoomMenu.add(addSetUISpanMenuItem("this year", TimeSpan.get(cal.getTime(), Years.ONE)));

        deconflictMenus(menuItems, zoomMenu);
    }

    @Override
    public void mouseEvent(MouseEvent e)
    {
        super.mouseEvent(e);
        if (e.getID() == MouseEvent.MOUSE_CLICKED)
        {
            TimeSpan uiSpan = getUIModel().getUISpan().get();
            if (e.getClickCount() == 2)
            {
                TimeInstant time = mySnapFunction.getSnapDestination(getUIModel().xToTime(e.getX()), RoundingMode.CEILING);
                if (uiSpan.overlaps(time))
                {
                    Duration dur = myTimeModel.get().getDuration();
                    TimeSpan newSpan = TimeSpan.get(dur, time);

                    TimeSpan loopSpan = myAnimationModel.getLoopSpan().get();
                    if (newSpan.overlaps(loopSpan.getStartInstant()))
                    {
                        newSpan = TimeSpan.get(loopSpan.getStartInstant(), dur);
                    }
                    else if (newSpan.overlaps(loopSpan.getEndInstant()))
                    {
                        newSpan = TimeSpan.get(dur, loopSpan.getEndInstant());
                    }
                    else if (myAnimationModel.getLoopSpanLocked().get().booleanValue()
                            && !myAnimationModel.getLoopSpan().get().contains(newSpan))
                    {
                        getUIModel().getTemporaryMessage().set("Loop span is locked");
                        return;
                    }

                    myTimeModel.set(newSpan);
                }
            }
            else if (myTriangle != null && myTriangle.contains(e.getPoint()))
            {
                // Center the view on the active window
                long delta = myTimeModel.get().getMidpoint() - uiSpan.getMidpoint();
                plus(getUIModel().getUISpan(), new Milliseconds(delta));
            }
        }
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        Point p = getUIModel().getLastMousePoint();
        Object o = getUIModel().getDraggingObject();

        // Determine direction
        myDirection = myTimeManager.getActiveTimeSpans().getDirection() >= 0 ? Direction.FORWARD : Direction.BACKWARD;
        myTimeWindow.setDirection(myDirection);
        myFadeLayer.setDirection(myDirection);

        // Set label visibility
        myTimeWindow.setEndLabelVisible(myAnimationModel.getPlayState() == PlayState.FORWARD && myDirection == Direction.FORWARD);
        myTimeWindow.setDurationLabelVisible(isDraggingOrCanDrag());
        myDragHandles.setForceShowContextLabel(
                myTimeWindow.hasDragObject(o) || o == null && myTimeWindow.canDrag(p) && !myDragHandles.canDrag(p));

        super.paint(g2d);
        paintTriangle(g2d);
    }

    /**
     * Add a menu item that sets the UI span to a specific span.
     *
     * @param label The label for the menu item.
     * @param timeSpan The span.
     * @return The menu item.
     */
    private JMenuItem addSetUISpanMenuItem(String label, final TimeSpan timeSpan)
    {
        JMenuItem zoomItem = new JMenuItem(label);
        zoomItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getUIModel().getUISpan().set(timeSpan);
            }
        });
        TimeSpan constraint = getUIModel().getUISpanConstraint().get();
        zoomItem.setEnabled(constraint == null || constraint.contains(timeSpan));
        return zoomItem;
    }

    /**
     * Paint the "find active window" triangle.
     *
     * @param g2d the graphics context
     */
    private void paintTriangle(Graphics2D g2d)
    {
        if (myTimeModel.get().getEnd() < getUIModel().getUISpan().get().getStart())
        {
            Rectangle bounds = getUIModel().getTimelinePanelBounds();
            int rightX = bounds.x + TRIANGLE_SIZE;
            if (myTriangle == null || mySide == SwingConstants.RIGHT || myTriangle.xpoints[2] != rightX)
            {
                int centerY = bounds.y + (bounds.height >> 1);
                myTriangle = new Polygon(new int[] { bounds.x, rightX, rightX },
                        new int[] { centerY, centerY + TRIANGLE_SIZE, centerY - TRIANGLE_SIZE }, 3);
                mySide = SwingConstants.LEFT;
            }
        }
        else if (myTimeModel.get().getStart() > getUIModel().getUISpan().get().getEnd())
        {
            Rectangle bounds = getUIModel().getTimelinePanelBounds();
            int rightX = AWTUtilities.getMaxX(bounds);
            if (myTriangle == null || mySide == SwingConstants.LEFT || myTriangle.xpoints[2] != rightX)
            {
                int leftX = rightX - TRIANGLE_SIZE;
                int centerY = bounds.y + (bounds.height >> 1);
                myTriangle = new Polygon(new int[] { leftX, leftX, rightX },
                        new int[] { centerY - TRIANGLE_SIZE, centerY + TRIANGLE_SIZE, centerY }, 3);
                mySide = SwingConstants.RIGHT;
            }
        }
        else
        {
            myTriangle = null;
        }

        if (myTriangle != null)
        {
            Point mousePoint = getUIModel().getLastMousePoint();
            boolean isMouseOver = mousePoint != null && myTriangle.contains(mousePoint);

            g2d.setColor(isMouseOver ? AnimationConstants.ACTIVE_HANDLE_HOVER_COLOR : AnimationConstants.ACTIVE_HANDLE_COLOR);
            g2d.fill(myTriangle);

            if (isMouseOver && mousePoint != null)
            {
                String label = "Go to active window";
                Rectangle bounds = getUIModel().getTimelinePanelBounds();
                int labelPosition = mySide == SwingConstants.RIGHT ? SwingUtilities.LEFT : SwingConstants.RIGHT;
                int labelX = AWTUtilities.getTextXLocation(label,
                        labelPosition == SwingConstants.RIGHT ? bounds.x : AWTUtilities.getMaxX(bounds),
                        DragHandle.TRIANGLE_SIZE + 4, labelPosition, g2d);
                int labelY = bounds.y + (bounds.height >> 1) + 5;
                getTemporaryLayers().add(new ContextLabel(label, labelX, labelY));
            }
        }
    }
}
