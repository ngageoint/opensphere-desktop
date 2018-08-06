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

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.event.DataRemovalEvent;
import io.opensphere.core.quantify.Quantify;
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
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(QuerySelector.class);

    /** The title of the window. */
    public static final String TITLE = "Query Selector";

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private final QuerySelectorSplitButton mySplitButton;

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
                Quantify.collectConditionalMetric("mist3d.query." + button.getMode().toString(), selection);
                mySelectionModeController.setSelectionMode(mode);
            }
        }
    };

    /**
     * The context for using the unmodified mouse actions for drawing on the
     * canvas.
     */
    private final transient ContextSingleActionProvider<MouseEvent> myDrawProvider = new ContextSingleActionProvider<>()
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
                mySplitButton.toggledProperty().set(false);
            }
            else
            {
                manager.registerContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                        myDrawProvider);
                mySplitButton.toggledProperty().set(true);
                mySplitButton.currentSelectionModeProperty().set(mode);
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

        mySplitButton = new QuerySelectorSplitButton();
        mySplitButton.toggledProperty().set(false);
        mySplitButton.currentSelectionModeProperty().set(mySelectionModeController.getDefaultSelectionMode());
        mySplitButton.currentSelectionModeProperty().addListener((obs, oldMode, newMode) ->
        {
            Quantify.collectConditionalMetric("mist3d.query." + newMode.toString(), mySplitButton.isSelected());
            mySelectionModeController.setSelectionMode(newMode);
        });

        mySplitButton.addActionListener(e ->
        {
            LOG.info("Action performed on split button.");
            mySelectionModeController.setSelectionMode(mySplitButton.currentSelectionModeProperty().get());
        });

        setDefaultModeBorder();

        addLegendIcons(toolbox);

        createRemoveSplitButton();

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.insets = new Insets(0, 2, 0, 2);
        add(myRemoveButton, gbc);

        gbc.gridx++;
        add(mySplitButton, gbc);
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
                    Quantify.collectMetric("mist3d.overlay.query.clear-all");
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
        mySplitButton.setSelected(false);
    }

    /**
     * Set the border of the last selected mode button to indicate which button
     * was last selected.
     */
    private void setDefaultModeBorder()
    {
        SelectionMode defaultMode = mySelectionModeController.getDefaultSelectionMode();
        mySplitButton.currentSelectionModeProperty().set(defaultMode);
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
