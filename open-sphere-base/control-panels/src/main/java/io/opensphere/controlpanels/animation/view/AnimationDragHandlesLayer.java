package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.timeline.DragHandle;
import io.opensphere.controlpanels.timeline.DragHandlesLayer;
import io.opensphere.controlpanels.timeline.ObservableTimeSpan;
import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.controlpanels.timeline.TimelineLayer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.NoEffectPredicate;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;

/**
 * Animation version of DragHandlesLayer.
 */
class AnimationDragHandlesLayer extends DragHandlesLayer
{
    /** Constraint on where the time can be set. */
    private final Predicate<? super TimeInstant> myConstraint;

    /** The snap function for the left handle. */
    private final SnapFunction myLeftSnapFunction;

    /** The name of the layer. */
    private final String myName;

    /** The play state listener. */
    private final ChangeListener<PlayState> myPlayStateListener;

    /** The snap function for the right handle. */
    private final SnapFunction myRightSnapFunction;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     * 
     * @param toolbox The toolbox through which application state is accessed.
     * @param observableTimeSpan the observable time span
     * @param constraint constraint on where the time span can be dragged
     * @param leftSnapFunction the snap function for the left drag handle
     * @param rightSnapFunction the snap function for the right drag handle
     * @param playState the play state
     * @param name the name of the layer
     * @param color the color
     * @param hoverColor the hover color
     */
    public AnimationDragHandlesLayer(Toolbox toolbox, ObservableTimeSpan observableTimeSpan,
            final Function<? super TimeInstant, ? extends TimeInstant> constraint, SnapFunction leftSnapFunction,
            SnapFunction rightSnapFunction, final ObservableValue<PlayState> playState, String name, Color color,
            Color hoverColor)
    {
        super(observableTimeSpan, name, constraint, leftSnapFunction, rightSnapFunction, color, hoverColor);
        myToolbox = toolbox;
        myName = name;
        myConstraint = new NoEffectPredicate<TimeInstant>(constraint);
        myLeftSnapFunction = leftSnapFunction;
        myRightSnapFunction = rightSnapFunction;

        myPlayStateListener = new ChangeListener<PlayState>()
        {
            @Override
            public void changed(ObservableValue<? extends PlayState> observable, PlayState oldValue, PlayState newValue)
            {
                setFlagVisible(!playState.get().isPlaying());
                getUIModel().repaint();
            }
        };
        playState.addListener(myPlayStateListener);
    }

    /**
     * Add a supplier for menu items for the drag handles.
     *
     * @param menuItemSupplier The menu item supplier.
     */
    public void addDragHandleMenuItemSupplier(Supplier<? extends Collection<? extends JMenuItem>> menuItemSupplier)
    {
        for (TimelineLayer timelineLayer : getLayers())
        {
            if (timelineLayer instanceof DragHandle)
            {
                ((DragHandle)timelineLayer).addMenuItemSupplier(menuItemSupplier);
            }
        }
    }

    @Override
    public void getMenuItems(final Point p, List<JMenuItem> menuItems)
    {
        super.getMenuItems(p, menuItems);

        if (!getObservableTimeSpan().getSpan().get().equals(getUIModel().getUISpan().get()))
        {
            JMenuItem zoomItem = new JMenuItem(StringUtilities.concat(myName, " span"));
            zoomItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    getUIModel().getUISpan().set(getObservableTimeSpan().getSpan().get());
                }
            });
            JMenu zoomMenu = new JMenu("Zoom to");
            zoomMenu.setIcon(new GenericFontIcon(AwesomeIconSolid.CROP, Color.WHITE));
            zoomMenu.add(zoomItem);
            deconflictMenus(menuItems, zoomMenu);
        }

        deconflictMenus(menuItems, getSetMenu(p));
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, TimespanContextKey key)
    {
        TimeInstant left = myLeftSnapFunction.getSnapDestination(key.getTimeSpan().getStartInstant(), RoundingMode.FLOOR);
        TimeInstant right = myRightSnapFunction.getSnapDestination(key.getTimeSpan().getEndInstant(), RoundingMode.CEILING);

        TimeSpan dragSelectionSpan = TimeSpan.get(left, right);

        JMenuItem item = new JMenuItem(StringUtilities.concat("Set ", myName, " span here"));
        item.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.advanced-animation-controls.set-"
                    + myName.toLowerCase().replaceAll(" ", "-") + "-span-here");
            if (timeSpanAllowed(dragSelectionSpan))
            {
                getObservableTimeSpan().getSpan().set(dragSelectionSpan);
            }
        });

        if (!timeSpanAllowed(dragSelectionSpan))
        {
            item.setEnabled(false);
            item.setToolTipText("Span must be within outer span.");
        }

        return Collections.singletonList(item);
    }

    /**
     * Get the menu for setting the span start/end times.
     *
     * @param p The mouse point.
     * @return The menu.
     */
    protected JMenu getSetMenu(final Point p)
    {
        JMenuItem startItem = new JMenuItem(myName + " span start here");
        startItem.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.advanced-animation-controls.set-"
                    + myName.toLowerCase().replaceAll(" ", "-") + "-span-start-here");
            TimeInstant time = myLeftSnapFunction.getSnapDestination(getUIModel().xToTime(p.x), RoundingMode.HALF_UP);
            if (startAllowed(time))
            {
                // First try keeping the other end constant.
                getObservableTimeSpan().getStart().set(time);

                // If it didn't work, try keeping the duration constant.
                if (!getObservableTimeSpan().getStart().get().equals(time))
                {
                    getObservableTimeSpan().getSpan()
                            .set(TimeSpan.get(time, getObservableTimeSpan().getSpan().get().getDuration()));
                }
            }
        });
        JMenuItem endItem = new JMenuItem(myName + " span end here");
        endItem.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.advanced-animation-controls.set-"
                    + myName.toLowerCase().replaceAll(" ", "-") + "-span-end-here");
            TimeInstant time = myRightSnapFunction.getSnapDestination(getUIModel().xToTime(p.x), RoundingMode.HALF_UP);
            if (endAllowed(time))
            {
                // First try keeping the other end constant.
                getObservableTimeSpan().getEnd().set(time);

                // If it didn't work, try keeping the duration constant.
                if (!getObservableTimeSpan().getEnd().get().equals(time))
                {
                    getObservableTimeSpan().getSpan()
                            .set(TimeSpan.get(getObservableTimeSpan().getSpan().get().getDuration(), time));
                }
            }
        });

        JMenu setMenuItem = new JMenu("Set");
        setMenuItem.add(startItem);
        setMenuItem.add(endItem);

        if (!startAllowed(myLeftSnapFunction.getSnapDestination(getUIModel().xToTime(p.x), RoundingMode.HALF_UP)))
        {
            startItem.setEnabled(false);
        }
        if (!endAllowed(myRightSnapFunction.getSnapDestination(getUIModel().xToTime(p.x), RoundingMode.HALF_UP)))
        {
            endItem.setEnabled(false);
        }
        return setMenuItem;
    }

    /**
     * Determines if the end time is allowed.
     *
     * @param time the end time
     * @return whether it's allowed
     */
    private boolean endAllowed(TimeInstant time)
    {
        return myConstraint.test(time) && time.compareTo(getObservableTimeSpan().getStart().get()) > 0;
    }

    /**
     * Determines if the start time is allowed.
     *
     * @param time the start time
     * @return whether it's allowed
     */
    private boolean startAllowed(TimeInstant time)
    {
        return myConstraint.test(time) && time.compareTo(getObservableTimeSpan().getEnd().get()) < 0;
    }

    /**
     * Determines if the time span is allowed.
     *
     * @param timeSpan the time span
     * @return whether it's allowed
     */
    private boolean timeSpanAllowed(TimeSpan timeSpan)
    {
        return myConstraint.test(timeSpan.getStartInstant()) && myConstraint.test(timeSpan.getEndInstant());
    }
}
