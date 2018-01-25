package io.opensphere.tracktool;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.callout.CalloutDragListener;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeographicPositionsContextKey;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.registry.TrackRegistry;

/**
 * The Class TrackToolTransformer.
 */
public final class TrackToolTransformer extends DefaultTransformer
{
    /**
     * The manager for actions associated with controlling which provider will
     * manage completed tracks.
     */
    private final ContextActionManager myActionManager;

    /**
     * When this button is selected, I will be the action provider for completed
     * arcs.
     */
    private IconButton myActivationButton;

    /** Manager for handling completed tracks. */
    private CompletedTrackManager myCompletedTrackManager;

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
                myTrackRegistry.createNewTrackFromPositions(key.getPositions(), myToolbox, myModel.getDataGroups(),
                        myEditListener);
            }
        }

        @Override
        public void invalidated()
        {
            myActivationButton.setSelected(false);
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
        }
    };

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The track registry. */
    private final TrackRegistry myTrackRegistry;

    /**
     * The my places model.
     */
    private final MyPlacesModel myModel;

    /**
     * The edit listener.
     */
    private final MyPlacesEditListener myEditListener;

    /**
     * The callout drag listener.
     */
    private final CalloutDragListener<Track> myDragListener;

    /**
     * Instantiates a new track tool transformer.
     *
     * @param toolbox the toolbox
     * @param registry the track registry
     * @param model The my places model.
     * @param editListener The edit listener.
     * @param dragListener The callout drag listener.
     */
    public TrackToolTransformer(Toolbox toolbox, TrackRegistry registry, MyPlacesModel model, MyPlacesEditListener editListener,
            CalloutDragListener<Track> dragListener)
    {
        super(null);
        Utilities.checkNull(toolbox, "toolbox");
        Utilities.checkNull(registry, "controller");

        myToolbox = toolbox;
        myTrackRegistry = registry;
        myModel = model;
        myEditListener = editListener;
        myDragListener = dragListener;

        if (myToolbox.getUIRegistry() != null)
        {
            myActionManager = myToolbox.getUIRegistry().getContextActionManager();
        }
        else
        {
            myActionManager = null;
        }

        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                registerToolbarButton(myToolbox);
            }
        });
    }

    @Override
    public void close()
    {
        super.close();
        myCompletedTrackManager.close();
        if (myToolbox.getUIRegistry() != null)
        {
            myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.ARC_CONTEXT,
                    GeographicPositionsContextKey.class, myCompProvider);
            myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                    myDrawProvider);
        }
    }

    @Override
    public void open()
    {
        super.open();
        myCompletedTrackManager = new CompletedTrackManager(myToolbox, this, myTrackRegistry, myDragListener);
    }

    /**
     * Adds the track tool toolbar button.
     *
     * @param toolbox the toolbox
     */
    private void registerToolbarButton(final Toolbox toolbox)
    {
        myActivationButton = new IconButton("Track");
        IconUtil.setIcons(myActivationButton, "/images/path-default.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                IconUtil.ICON_SELECTION_FOREGROUND);
        myActivationButton.setToolTipText("Create a track");
        myActivationButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    myActivationButton.setSelected(!myActivationButton.isSelected());
                    if (myActivationButton.isSelected())
                    {
                        myActionManager.registerContextSingleActionProvider(ContextIdentifiers.ARC_CONTEXT,
                                GeographicPositionsContextKey.class, myCompProvider);
                        myActionManager.registerContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT,
                                MouseEvent.class, myDrawProvider);
                    }
                    else
                    {
                        myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.ARC_CONTEXT,
                                GeographicPositionsContextKey.class, myCompProvider);
                        myActionManager.deregisterContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT,
                                MouseEvent.class, myDrawProvider);
                    }
                }
            }
        });
        toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "Track Tool",
                myActivationButton, 380, SeparatorLocation.NONE, new Insets(0, 2, 0, 2));
    }
}
