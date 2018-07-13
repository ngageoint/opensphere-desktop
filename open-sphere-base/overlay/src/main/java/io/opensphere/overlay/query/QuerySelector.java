package io.opensphere.overlay.query;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.event.DataRemovalEvent;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.misc.ControlMenuOptionContextSplitButton;
import io.opensphere.overlay.OverlayToolboxUtils;
import io.opensphere.overlay.SelectionModeChangeListener;
import io.opensphere.overlay.SelectionModeController;

/**
 * The query selector is the display which is used to determine the type of
 * selection regions to use (for example, box, circle, polygon, etc.).
 */
public final class QuerySelector extends JPanel
{
    /** The title of the window. */
    public static final String TITLE = "Query Selector";

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Box button. */
    private final SelectorToggleButton myBoxButton;

    /** The Button press listener. */
    private final transient MouseListener myButtonListener = new MouseAdapter()
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                SelectorToggleButton button = (SelectorToggleButton)e.getSource();
                boolean selection = !button.isSelected();
                SelectionMode mode = selection ? button.getMode() : SelectionMode.NONE;
                QuantifyToolboxUtils.collectConditionalMetric(myToolbox,
                        "mist3d.query.button." + button.getMode().toString(), selection);
                mySelectionModeController.setSelectionMode(mode);
            }
        }
    };

    /** The Circle button. */
    private final SelectorToggleButton myCircleButton;

    /** The border for the buttons is saved so that it can be restored. */
    private final Border myDefaultButtonBorder;

    /**
     * The context for using the unmodified mouse actions for drawing on the
     * canvas.
     */
    private final transient ContextSingleActionProvider<MouseEvent> myDrawProvider = new ContextSingleActionProvider<MouseEvent>()
    {
        @Override
        public void doAction(String contextId, MouseEvent key, int x, int y)
        {
            // TODO This is currently used for exclusive button grouping. The
            // action should be handled here when the controls are re-written.
        }

        @Override
        public void invalidated()
        {
            deselectAllButtons();
            setDefaultModeBorder();
            mySelectionModeController.setSelectionMode(SelectionMode.NONE);
        }
    };

    /** The Poly button. */
    private final SelectorToggleButton myPolyButton;

    /** The Remove button. */
    private DeleteSplitButton myRemoveButton;

    /** The Selection mode controller. */
    private final transient SelectionModeController mySelectionModeController;

    /** Listener for changes to the selection mode. */
    private final transient SelectionModeChangeListener mySelectionModeListener = new SelectionModeChangeListener()
    {
        @Override
        public void selectionModeChanged(SelectionMode mode)
        {
            ContextActionManager manager = myToolbox.getUIRegistry().getContextActionManager();
            deselectAllButtons();
            if (mode == SelectionMode.NONE)
            {
                setDefaultModeBorder();
                manager.deregisterContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                        myDrawProvider);
            }
            else
            {
                manager.registerContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                        myDrawProvider);
                if (mode == SelectionMode.BOUNDING_BOX)
                {
                    myBoxButton.setSelected(true);
                }
                else if (mode == SelectionMode.CIRCLE)
                {
                    myCircleButton.setSelected(true);
                }
                else if (mode == SelectionMode.POLYGON)
                {
                    myPolyButton.setSelected(true);
                }
            }
        }
    };

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /**
     * Instantiates a new query selector.
     *
     * @param toolbox the toolbox
     */
    public QuerySelector(Toolbox toolbox)
    {
        super(false);
        myToolbox = toolbox;
        mySelectionModeController = OverlayToolboxUtils.getOverlayToolbox(toolbox).getSelectionModeController();
        mySelectionModeController.addSelectionModeChangeListener(mySelectionModeListener);

        setOpaque(false);
        setBorder(null);
        setLayout(new GridBagLayout());

        myBoxButton = new SelectorToggleButton(SelectionMode.BOUNDING_BOX);
        IconUtil.setIcons(myBoxButton, "/images/rectangle.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                IconUtil.ICON_SELECTION_FOREGROUND);
        myBoxButton.setToolTipText("Draw a rectangle on the map for queries, zoom, or selection");
        myBoxButton.addMouseListener(myButtonListener);

        myCircleButton = new SelectorToggleButton(SelectionMode.CIRCLE);
        IconUtil.setIcons(myCircleButton, "/images/circle.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                IconUtil.ICON_SELECTION_FOREGROUND);
        myCircleButton.setToolTipText("Draw a circle on the map for queries, zoom, or selection");
        myCircleButton.addMouseListener(myButtonListener);

        myPolyButton = new SelectorToggleButton(SelectionMode.POLYGON);
        IconUtil.setIcons(myPolyButton, "/images/polygon2.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                IconUtil.ICON_SELECTION_FOREGROUND);
        myPolyButton.setToolTipText("Draw a polygon on the map for queries, zoom, or selection");
        myPolyButton.addMouseListener(myButtonListener);

        myDefaultButtonBorder = myPolyButton.getBorder();

        setDefaultModeBorder();

        addLegendIcons(toolbox);

        createRemoveSplitButton();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 2, 0, 2);
        add(myBoxButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 2, 0, 2);
        add(myCircleButton, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 2, 0, 2);
        add(myPolyButton, gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 2, 0, 2);
        add(myRemoveButton, gbc);
    }

    /** Gets the removes the button. */
    public void createRemoveSplitButton()
    {
        myRemoveButton = new DeleteSplitButton(myToolbox, "Clear", null);
        IconUtil.setIcons(myRemoveButton, IconType.CLOSE, Color.RED);
        myRemoveButton.setToolTipText("Discard all queried data");
        myRemoveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                if (myToolbox.getEventManager() != null)
                {
                    QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.overlay.query.button.clear-all");
                    int answer = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(myRemoveButton),
                            "Are you sure you want to clear all?", "Clear All", JOptionPane.OK_CANCEL_OPTION);
                    if (answer == JOptionPane.OK_OPTION)
                    {
                        myToolbox.getEventManager().publishEvent(new DataRemovalEvent(evt.getSource()));
                    }
                }
            }
        });
    }

    /**
     * Adds the legend icons.
     *
     * @param toolbox the toolbox
     */
    private void addLegendIcons(Toolbox toolbox)
    {
        Icon boxIcon = IconUtil.getNormalIcon("/images/rectangle.png");
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(boxIcon, "Query Selector(Rectangle)",
                "Press the Shift button and click the map and drag to create a rectangular area on the map for querying, zooming, purging, etc.");

        Icon circleIcon = IconUtil.getNormalIcon("/images/circle.png");
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(circleIcon, "Query Selector(Circle)",
                "Press the Shift button and click the map and drag to create a circular area on the map for querying, zooming, purging, etc.");

        Icon polyIcon = IconUtil.getNormalIcon("/images/polygon2.png");
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(polyIcon, "Query Selector(Polygon)",
                "Press the Shift button and click the map. Move the cursor and each subsequent click will add a side to the polygon. "
                        + "To end the polygon, while holding the Shift button, right click the mouse. "
                        + "The polygon can also be used for querying, zooming, purging, etc.");
    }

    /**
     * Deselect all of the buttons and reset the border to the default.
     */
    private void deselectAllButtons()
    {
        myBoxButton.setSelected(false);
        myBoxButton.setBorder(myDefaultButtonBorder);
        myCircleButton.setSelected(false);
        myCircleButton.setBorder(myDefaultButtonBorder);
        myPolyButton.setSelected(false);
        myPolyButton.setBorder(myDefaultButtonBorder);
    }

    /**
     * Set the border of the last selected mode button to indicate which button
     * was last selected.
     */
    private void setDefaultModeBorder()
    {
        SelectionMode defaultMode = mySelectionModeController.getDefaultSelectionMode();
        if (defaultMode == SelectionMode.BOUNDING_BOX)
        {
            myBoxButton.setBorder(new LineBorder(IconUtil.ICON_SELECTION_FOREGROUND.darker(), 1, false));
        }
        else if (defaultMode == SelectionMode.CIRCLE)
        {
            myCircleButton.setBorder(new LineBorder(IconUtil.ICON_SELECTION_FOREGROUND.darker(), 1, false));
        }
        else if (defaultMode == SelectionMode.POLYGON)
        {
            myPolyButton.setBorder(new LineBorder(IconUtil.ICON_SELECTION_FOREGROUND.darker(), 1, false));
        }
    }

    /**
     * The Class DeleteSplitButton.
     */
    private static class DeleteSplitButton extends ControlMenuOptionContextSplitButton<Void>
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new delete split button.
         *
         * @param tb the tb
         * @param text the text
         * @param icon the icon
         */
        public DeleteSplitButton(Toolbox tb, String text, Icon icon)
        {
            super(tb.getUIRegistry().getContextActionManager(), text, icon, ContextIdentifiers.DELETE_CONTEXT, Void.class);
        }
    }

    /**
     * The Class SelectorToggleButton.
     */
    private static class SelectorToggleButton extends IconButton
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Mode. */
        private final SelectionMode myMode;

        /**
         * Instantiates a new selector toggle button.
         *
         * @param mode the mode
         */
        public SelectorToggleButton(SelectionMode mode)
        {
            super();
            myMode = mode;
        }

        /**
         * Gets the mode.
         *
         * @return the mode
         */
        public SelectionMode getMode()
        {
            return myMode;
        }
    }
}
