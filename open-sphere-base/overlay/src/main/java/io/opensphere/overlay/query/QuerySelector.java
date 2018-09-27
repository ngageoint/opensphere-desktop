package io.opensphere.overlay.query;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.event.DataRemovalEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.GenericFontIcon;
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

    /** The button through which query actions are performed. */
    private final QuerySelectorSplitButton mySplitButton;

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

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** Listener for changes to the selection mode. */
    private final transient SelectionModeChangeListener mySelectionModeListener;

    /** Hold a reference to my listeners. */
    private final List<BoundEventListener> myControlEventListeners = new ArrayList<>();

    /** A reference to the subscriber for data removal events. */
    private EventListener<? super DataRemovalEvent> mySubscriber;

    /**
     * Instantiates a new query selector.
     *
     * @param toolbox the toolbox
     */
    public QuerySelector(Toolbox toolbox)
    {
        super(false);
        myToolbox = toolbox;

        mySelectionModeListener = this::selectionModeChanged;
        mySelectionModeController = OverlayToolboxUtils.getOverlayToolbox(toolbox).getSelectionModeController();
        mySelectionModeController.addSelectionModeChangeListener(mySelectionModeListener);

        setOpaque(false);
        setBorder(null);
        setLayout(new GridBagLayout());

        mySplitButton = new QuerySelectorSplitButton(OverlayToolboxUtils.getOverlayToolbox(toolbox));
        mySplitButton.toggledProperty().set(false);
        mySplitButton.currentSelectionModeProperty().set(mySelectionModeController.getDefaultSelectionMode());
        mySplitButton.currentSelectionModeProperty().addListener((obs, oldMode, newMode) ->
        {
            Quantify.collectConditionalMetric("mist3d.query." + newMode.toString(), mySplitButton.isSelected());
            mySelectionModeController.setSelectionMode(newMode);
        });

        addEventListeners();
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

    /**
     * Configures event listeners on the button.
     */
    private void addEventListeners()
    {
        mySplitButton.addActionListener(e ->
        {
            if (mySplitButton.toggledProperty().get())
            {
                mySelectionModeController.setSelectionMode(SelectionMode.NONE);
            }
            else
            {
                mySelectionModeController.setSelectionMode(mySplitButton.currentSelectionModeProperty().get());
            }
        });

        DiscreteEventAdapter escapeListener = new DiscreteEventAdapter("Query", "Cancel Query",
                "Move the camera so it faces directly at the surface with north up.")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                mySelectionModeController.setSelectionMode(SelectionMode.NONE);
            }
        };
        myControlEventListeners.add(escapeListener);
        ControlContext context = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        context.addListener(escapeListener, new DefaultKeyPressedBinding(KeyEvent.VK_ESCAPE));

        mySubscriber = e -> mySelectionModeController.setSelectionMode(SelectionMode.NONE);
        myToolbox.getEventManager().subscribe(DataRemovalEvent.class, mySubscriber);
    }

    /**
     * An event handler method used to react to selection mode changes.
     *
     * @param mode the new selection mode of the application.
     */
    private void selectionModeChanged(SelectionMode mode)
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
            mySplitButton.getMenu().setVisible(false);
        }
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
        Icon boxIcon = new GenericFontIcon(AwesomeIconRegular.SQUARE, IconUtil.DEFAULT_ICON_FOREGROUND);
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(boxIcon, "Query Selector(Rectangle)",
                "Press the Shift button and click the map and drag to create a rectangular area on the map for querying, zooming, purging, etc.");

        Icon circleIcon = new GenericFontIcon(AwesomeIconRegular.CIRCLE, IconUtil.DEFAULT_ICON_FOREGROUND);
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(circleIcon, "Query Selector(Circle)",
                "Press the Shift button and click the map and drag to create a circular area on the map for querying, zooming, purging, etc.");

        Icon polyIcon = new GenericFontIcon(AwesomeIconRegular.STAR, IconUtil.DEFAULT_ICON_FOREGROUND);
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(polyIcon, "Query Selector(Polygon)",
                "Press the Shift button and click the map. Move the cursor and each subsequent click will add an edge to the polygon. "
                        + "While holding the Shift button, double left clicking the mouse will end the polygon, "
                        + "and right clicking will remove the most recent edge of the polygon. "
                        + "The polygon can be used for querying, zooming, purging, etc.");
        Icon lineIcon = new GenericFontIcon(AwesomeIconSolid.LONG_ARROW_ALT_RIGHT, IconUtil.DEFAULT_ICON_FOREGROUND);
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(lineIcon, "Query Selector(Line)",
                "Press the Shift button and click the map. Move the cursor and each subsequent click will add an edge to the line. "
                        + "While holding the Shift button, double left clicking the mouse will end the line, "
                        + "and right clicking will remove the most recent edge of the line. "
                        + "The line can be used for querying, zooming, purging, etc. Only points along the line's edges are affected.");
        Icon clearIcon = IconUtil.getColorizedIcon(IconType.CLOSE, Color.RED);
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(clearIcon, "Clear",
                "Cancels, disables, or cleans up various parts of the application the user may have used. "
                        + "These include queries, search results, spatial filters, goto points, and states. "
                        + "Use the dropdown menu to select one target to clear. "
                        + "Additionally, in the active layers window, this button will disable the currently selected layer(s).");
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
}
