package io.opensphere.controlpanels.timeline;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;

import io.opensphere.core.control.ContextMenuSelectionAdapter;
import io.opensphere.core.control.action.ActionContext;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.collections.New;

/** Controller for the main timeline panel. */
public class TimelineController
{
    /** Indicates if currently dragging. */
    private boolean myDragging;

    /** The master timeline layer. */
    private final CompositeLayer myMasterLayer;

    /** The context action manager used for menu generation. */
    private final ContextActionManager myContextActionManager;

    /** The timeline panel. */
    private final TimelinePanel myTimelinePanel;

    /** The timeline UI model. */
    private final TimelineUIModel myUIModel;

    /**
     * Menu provider for the timespan context.
     */
    private final ContextMenuProvider<TimespanContextKey> myMenuProvider = new ContextMenuProvider<TimespanContextKey>()
    {
        @Override
        public int getPriority()
        {
            return 0;
        }

        @Override
        public List<? extends Component> getMenuItems(String contextId, TimespanContextKey key)
        {
            return myMasterLayer.getMenuItems(contextId, key);
        }
    };

    /**
     * Constructor.
     *
     * @param uiModel The timeline UI model.
     * @param panel The timeline panel.
     * @param masterLayer The master timeline layer.
     * @param contextActionManager The action context used for menu generation.
     */
    public TimelineController(TimelineUIModel uiModel, TimelinePanel panel, CompositeLayer masterLayer,
            ContextActionManager contextActionManager)
    {
        myUIModel = uiModel;
        myTimelinePanel = panel;
        myMasterLayer = masterLayer;
        myContextActionManager = contextActionManager;

        addListeners();

        contextActionManager.registerContextMenuItemProvider(ContextIdentifiers.TIMESPAN_CONTEXT, TimespanContextKey.class,
                myMenuProvider);
    }

    /**
     * Adds a layer.
     *
     * @param layer the layer
     */
    public void addLayer(TimelineLayer layer)
    {
        layer.setUIModel(myUIModel);
        myMasterLayer.getLayers().add(layer);
    }

    /**
     * Get the menu items from the master layer for a mouse event.
     *
     * @param e The event.
     * @return The menu items.
     */
    public List<JMenuItem> getMenuItems(MouseEvent e)
    {
        List<JMenuItem> items = New.list();
        myMasterLayer.getMenuItems(e.getPoint(), items);
        return items;
    }

    /**
     * Adds listeners.
     */
    private void addListeners()
    {
        myUIModel.getUISpan().addListener((observable, oldValue, newValue) -> myTimelinePanel.repaint());

        myTimelinePanel.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                myUIModel.updateBounds();
                myUIModel.calculateRatios();
            }
        });

        myTimelinePanel.addMouseListener(new TimelineMouseListener());

        myTimelinePanel.addMouseMotionListener(new MouseMotionListener()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                handleMouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                handleMouseMoved(e);
            }
        });

        myTimelinePanel.addMouseWheelListener(this::handleMouseWheelMoved);

        myTimelinePanel.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                handleKeyPressed(e);
            }
        });

        myTimelinePanel.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                myUIModel.setLockSelection(false);
                if (myUIModel.setFirstMousePoint(null))
                {
                    myUIModel.repaint();
                }
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                if (!e.isTemporary())
                {
                    myUIModel.setLockSelection(false);
                    if (myUIModel.setFirstMousePoint(null))
                    {
                        myUIModel.repaint();
                    }
                }
            }
        });
    }

    /**
     * Performs the drag for the mouse event.
     *
     * @param e the mouse event
     */
    private void drag(MouseEvent e)
    {
        double millisPerPixel = myUIModel.getMillisPerPixel().get().doubleValue();
        Duration dragTime = new Milliseconds(Math.round(millisPerPixel * (e.getX() - myUIModel.getLastMousePoint().x)));
        Object dragObject = myMasterLayer.drag(myUIModel.getDraggingObject(), myUIModel.getLastMousePoint(), e.getPoint(),
                !myDragging, dragTime);
        myDragging = true;
        myUIModel.setDraggingObject(dragObject);
        myTimelinePanel.repaint();
    }

    /**
     * Handles the key pressed event.
     *
     * @param e the event
     */
    private void handleKeyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)
        {
            Milliseconds moveAmount = new Milliseconds(myUIModel.getUISpan().get().getDurationMs() >> 4);
            if (e.getKeyCode() == KeyEvent.VK_LEFT)
            {
                AbstractTimelineLayer.minus(myUIModel.getUISpan(), moveAmount);
            }
            else
            {
                AbstractTimelineLayer.plus(myUIModel.getUISpan(), moveAmount);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP)
        {
            myUIModel.zoom(true, .5f);
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)
        {
            myUIModel.zoom(false, .5f);
        }
    }

    /**
     * Handles the mouse dragged event.
     *
     * @param e the event
     */
    private void handleMouseDragged(MouseEvent e)
    {
        Point point = e.getPoint();
        myUIModel.setCursorPosition(point);
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0)
        {
            if (e.isShiftDown())
            {
                if (myUIModel.getFirstMousePoint() == null)
                {
                    myUIModel.setFirstMousePoint(myUIModel.getLastMousePoint());
                }
                myUIModel.setLastMousePoint(point);
                myTimelinePanel.repaint();
            }
            else
            {
                drag(e);
            }
        }
    }

    /**
     * Handles the mouse moved event.
     *
     * @param e the event
     */
    private void handleMouseMoved(MouseEvent e)
    {
        Point point = e.getPoint();
        myUIModel.setCursorPosition(point);
        if (myUIModel.setLastMousePoint(point))
        {
            myTimelinePanel.repaint();
        }

        myTimelinePanel.setToolTipText(myMasterLayer.getToolTipText(e, null));

        // Reset the cursor if no layers can drag
        boolean canDrag = false;
        for (TimelineLayer layer : myMasterLayer.getLayers())
        {
            if (layer.canDrag(point) && !(layer instanceof BaseTimelineLayer))
            {
                canDrag = true;
                break;
            }
        }
        if (!canDrag)
        {
            myTimelinePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Handles the mouse wheel event.
     *
     * @param e the event
     */
    private void handleMouseWheelMoved(MouseWheelEvent e)
    {
        if (myUIModel.getTimelinePanelBounds() != null)
        {
            myUIModel.zoom(e.getWheelRotation() < 0, (float)e.getX() / myUIModel.getTimelinePanelBounds().width);
        }
    }

    /**
     * Mouse listener.
     */
    private final class TimelineMouseListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            myTimelinePanel.requestFocusInWindow();
            myMasterLayer.mouseEvent(e);
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            myDragging = false;
            if (myUIModel.setDraggingObject(null))
            {
                myTimelinePanel.repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            myUIModel.setLockSelection(false);
            myUIModel.setFirstMousePoint(null);
            myUIModel.setLastMousePoint(e.getPoint());
            myUIModel.repaint();
            if (e.getButton() == MouseEvent.BUTTON3)
            {
                myTimelinePanel.showPopup(e.getPoint(), getMenuItems(e));
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (myUIModel.getFirstMousePoint() != null && !myUIModel.getFirstMousePoint().equals(myUIModel.getLastMousePoint()))
            {
                myUIModel.setLockSelection(true);

                ActionContext<TimespanContextKey> timelineActionContext = myContextActionManager
                        .getActionContext(ContextIdentifiers.TIMESPAN_CONTEXT, TimespanContextKey.class);

                timelineActionContext.doAction(new TimespanContextKey(myUIModel.getDragSelectionSpan()), myTimelinePanel,
                        e.getX(), e.getY(), new ContextMenuSelectionAdapter()
                        {
                            @Override
                            public void popupMenuCanceled(PopupMenuEvent ignore)
                            {
                                myUIModel.setFirstMousePoint(null);
                                myUIModel.setLockSelection(false);
                                myUIModel.repaint();
                            }

                            @Override
                            public void actionPerformed(ActionEvent ignore)
                            {
                                myUIModel.setFirstMousePoint(null);
                                myUIModel.setLockSelection(false);
                                myUIModel.repaint();
                            }
                        });
            }
            if (myUIModel.setDraggingObject(null))
            {
                myTimelinePanel.repaint();
            }
            myDragging = false;
        }
    }
}
