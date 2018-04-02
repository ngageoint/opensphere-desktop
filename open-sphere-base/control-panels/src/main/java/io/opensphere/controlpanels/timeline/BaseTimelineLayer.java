package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.Timer;

import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;

/**
 * The base timeline layer.
 */
class BaseTimelineLayer extends AbstractTimelineLayer
{
    /** The selection background color. */
    private static final Color SELECTION_BG_COLOR = ColorUtilities.opacitizeColor(Color.DARK_GRAY, 64);

    /** The time span when dragging started. */
    private TimeSpan myDragStartValue;

    /** Format for time now. */
    private final SimpleDateFormat myFormat = new SimpleDateFormat("HH:mm:ss");

    /** A rectangle that can be used for painting. */
    private final Rectangle myRectangle = new Rectangle();

    /** Label for the current time. */
    private final ContextLabel myTimenowLabel = new ContextLabel();

    /** The swing timer for painting the future. */
    private Timer myTimer;

    /** Listener for changes to the UI span. */
    private final ChangeListener<TimeSpan> myUISpanChangeListener = new ChangeListener<TimeSpan>()
    {
        @Override
        public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
        {
            if (newValue.getEnd() > System.currentTimeMillis())
            {
                if (myTimer == null)
                {
                    myTimer = new Timer((int)(1000 - System.currentTimeMillis() % 1000), new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            // If we've gotten more than 100 ms behind, reset
                            // the timer.
                            if (System.currentTimeMillis() % 1000 > 100)
                            {
                                myTimer.setInitialDelay((int)(1000 - System.currentTimeMillis() % 1000));
                                myTimer.restart();
                            }
                            // Repaint the future
                            getUIModel().repaint();
                        }
                    });
                    myTimer.setDelay(1000);
                    myTimer.start();
                }
            }
            else if (myTimer != null)
            {
                myTimer.stop();
                myTimer = null;
            }
        }
    };

    /**
     * Constructor.
     *
     * @param uiModel timeline UI model
     */
    public BaseTimelineLayer(TimelineUIModel uiModel)
    {
        setUIModel(uiModel);

        uiModel.getUISpan().addListener(myUISpanChangeListener);
        TimeSpan value = uiModel.getUISpan().get();
        myUISpanChangeListener.changed(uiModel.getUISpan(), value, value);
    }

    @Override
    public boolean canDrag(Point p)
    {
        return true;
    }

    @Override
    public Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime)
    {
        if (beginning)
        {
            myDragStartValue = getUIModel().getUISpan().get();
        }
        getUIModel().getUISpan().set(myDragStartValue.minus(new Milliseconds(dragTime)));
        return this;
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, TimespanContextKey key)
    {
        JMenuItem item = new JMenuItem("Zoom", new GenericFontIcon(AwesomeIcon.ICON_CROP, Color.WHITE));
        item.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getUIModel().getUISpan().set(key.getTimeSpan());
            }
        });

        return Collections.singletonList(item);
    }

    @Override
    public boolean hasDragObject(Object dragObject)
    {
        return Utilities.sameInstance(this, dragObject);
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);
        paintFuture(g2d);
        addDragSelectionLayer();
    }

    /**
     * Adds a temporary layer for the user's drag selection.
     */
    private void addDragSelectionLayer()
    {
        if (getUIModel().getFirstMousePoint() != null && getUIModel().getLastMousePoint() != null)
        {
            WeakObservableValue<TimeSpan> timeSpan = new WeakObservableValue<>();
            timeSpan.set(getUIModel().getDragSelectionSpan());
            TimeWindowLayer layer = new TimeWindowLayer(timeSpan, Color.DARK_GRAY);
            layer.setStartLabelVisible(true);
            layer.setEndLabelVisible(true);
            layer.setDurationLabelVisible(true);
            getTemporaryLayers().add(layer);
        }
    }

    /**
     * Paints the future time.
     *
     * @param g2d the graphics
     */
    private void paintFuture(Graphics2D g2d)
    {
        long now = System.currentTimeMillis() / 1000 * 1000;
        if (getUIModel().getUISpan().get().getEnd() > now)
        {
            int nowX = getUIModel().timeToX(now);
            int width = MathUtil.subtractSafe(AWTUtilities.getMaxX(getUIModel().getTimelinePanelBounds()), nowX);
            g2d.setColor(SELECTION_BG_COLOR);
            myRectangle.setBounds(nowX, getUIModel().getTimelinePanelBounds().y, width,
                    getUIModel().getTimelinePanelBounds().height);
            g2d.fill(myRectangle);
            g2d.setColor(getUIModel().getComponent().getForeground());

            String label = myFormat.format(new Date(now));
            int labelOffset = (int)(AWTUtilities.getTextWidth(label, g2d) / 2);
            myTimenowLabel.update(label, MathUtil.subtractSafe(nowX, labelOffset),
                    AWTUtilities.getMaxY(getUIModel().getTopDragPanelBounds()) - 2);
            getTemporaryLayers().add(myTimenowLabel);
        }
    }
}
