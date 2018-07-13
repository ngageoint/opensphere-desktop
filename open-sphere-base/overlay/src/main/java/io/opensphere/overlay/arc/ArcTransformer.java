package io.opensphere.overlay.arc;

import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeographicPositionsContextKey;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.IconButton;

/** Transformer Arc length tool. */
public final class ArcTransformer extends DefaultTransformer
{
    /**
     * The manager for actions associated with controlling which provider will
     * manage completed arcs.
     */
    private final ContextActionManager myActionManager;

    /**
     * When this button is selected, I will be the action provider for completed
     * arcs.
     */
    private IconButton myActivationButton;

    /** The handler for drawing new arcs. */
    private final ArcGenerator myArcGenerator;

    /** The manager for actions against completed arcs. */
    private final CompletedArcManager myCompletedArcManager;

    /** The action provider for completed arcs. */
    private final ContextSingleActionProvider<GeographicPositionsContextKey> myCompProvider = new ContextSingleActionProvider<GeographicPositionsContextKey>()
    {
        @Override
        public void doAction(String contextId, GeographicPositionsContextKey key, int x, int y)
        {
            if (ContextIdentifiers.ARC_CONTEXT.equals(contextId) && key.getPositions().size() > 1)
            {
                myToolbox.getUIRegistry().getContextActionManager()
                        .clearContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class);
                myCompletedArcManager.addCompletedArc(key.getPositions());
            }
        }

        @Override
        public void invalidated()
        {
            myActivationButton.setSelected(false);
            myArcGenerator.clearCurrentArc();
        }
    };

    /**
     * The context for using the unmodified mouse actions for drawing on the
     * canvas.
     */
    private final ContextSingleActionProvider<MouseEvent> myDrawProvider = new ContextSingleActionProvider<MouseEvent>()
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
            myActivationButton.setSelected(false);
            myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.ARC_CONTEXT,
                    GeographicPositionsContextKey.class, myCompProvider);
            myArcGenerator.clearCurrentArc();
        }
    };

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     */
    public ArcTransformer(Toolbox toolbox)
    {
        super(null);
        Utilities.checkNull(toolbox, "toolbox");

        myToolbox = toolbox;
        if (myToolbox.getUIRegistry() != null)
        {
            myActionManager = myToolbox.getUIRegistry().getContextActionManager();
        }
        else
        {
            myActionManager = null;
        }
        myCompletedArcManager = new CompletedArcManager(this, toolbox);
        myArcGenerator = new ArcGenerator(this, toolbox);

        registerToolbarButton();
        registerKeyMapping();
    }

    @Override
    public void close()
    {
        super.close();
        myCompletedArcManager.close();
        myArcGenerator.close();
        if (myToolbox.getUIRegistry() != null)
        {
            myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.ARC_CONTEXT,
                    GeographicPositionsContextKey.class, myCompProvider);
            myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                    myDrawProvider);
        }
    }

    /**
     * Get the arcGenerator.
     *
     * @return the arcGenerator
     */
    public ArcGenerator getArcGenerator()
    {
        return myArcGenerator;
    }

    /**
     * Get the completedArcManager.
     *
     * @return the completedArcManager
     */
    public CompletedArcManager getCompletedArcManager()
    {
        return myCompletedArcManager;
    }

    /**
     * Creates the button which can activate this feature and registers it with the
     * toolbar.
     */
    private void registerToolbarButton()
    {
        myActivationButton = new IconButton("Ruler");
        IconUtil.setIcons(myActivationButton, "/images/measure.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                IconUtil.ICON_SELECTION_FOREGROUND);
        myActivationButton.setToolTipText("Measure distance and bearing");
        myActivationButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.overlay.ruler.button.use-ruler");
                    toggleMyActivationButton();
                }
            }
        });

        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "ArcLength",
                myActivationButton, 370, SeparatorLocation.NONE, new Insets(0, 2, 0, 2));

        ImageIcon arcLengthIcon = IconUtil.getNormalIcon("/images/measure.png");
        myToolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(arcLengthIcon, "ArcLength Tool",
                "To measure, click the map. As the cursor moves, a segment will be created that displays the current length "
                        + "and heading. Click a second time to end the first segment and start another. To finish measuring, "
                        + "press the Enter or Spacebar key or double click");
    }

    /**
     * Creates the key mapping which can activate this feature and sets up the listener.
     */
    private void registerKeyMapping()
    {
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)
                .addListener(new DiscreteEventAdapter("Drawing", "ArcLength Tool", "Measure distance and bearing")
                {
                    @Override
                    public void eventOccurred(InputEvent event)
                    {
                        toggleMyActivationButton();
                    }
                }, new DefaultKeyPressedBinding(KeyEvent.VK_M));
    }

    /**
     * Allows the user to activate/deactivate the ArcLength tool and button.
     */
    private void toggleMyActivationButton()
    {
        myActivationButton.setSelected(!myActivationButton.isSelected());
        if (myActivationButton.isSelected())
        {
            myActionManager.registerContextSingleActionProvider(ContextIdentifiers.ARC_CONTEXT,
                    GeographicPositionsContextKey.class, myCompProvider);
            myActionManager.registerContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                    myDrawProvider);
        }
        else
        {
            myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.ARC_CONTEXT,
                    GeographicPositionsContextKey.class, myCompProvider);
            myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                    myDrawProvider);
            myArcGenerator.clearCurrentArc();
        }
    }
}
