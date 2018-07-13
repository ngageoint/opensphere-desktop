package io.opensphere.myplaces.specific.points;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.input.DontShowDialog;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;

/**
 * This class will create the add point button and add it to the toolbar.
 */
public class PlaceMarkToolbarButton
{
    /** The Annotation points controller. */
    private final AnnotationEditController myAnnotationPointsController;

    /** The Create map point button. */
    private IconButton myCreateMapPointButton;

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
            myCreateMapPointButton.setSelected(false);
            myCreateMapPointButton.setToolTipText("Create map point");
            myAnnotationPointsController.setPickingMode(false);
        }
    };

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new map point creator.
     *
     * @param controller the controller
     */
    public PlaceMarkToolbarButton(AnnotationEditController controller)
    {
        myToolbox = controller.getToolbox();
        myAnnotationPointsController = controller;
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                registerToolbarButton();
            }
        });
    }

    /**
     * Adds the track tool toolbar button.
     */
    private void registerToolbarButton()
    {
        myCreateMapPointButton = new IconButton("Point");
        IconUtil.setIcons(myCreateMapPointButton, "/images/location.png", IconUtil.DEFAULT_ICON_FOREGROUND,
                IconUtil.ICON_SELECTION_FOREGROUND);
        myCreateMapPointButton.setToolTipText("Create map point");
        myCreateMapPointButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.my-places.button.create-map-point");
                    ContextActionManager manager = myToolbox.getUIRegistry().getContextActionManager();
                    myCreateMapPointButton.setSelected(!myCreateMapPointButton.isSelected());
                    if (myCreateMapPointButton.isSelected())
                    {
                        DontShowDialog.showMessageDialog(myToolbox.getPreferencesRegistry(),
                                myToolbox.getUIRegistry().getMainFrameProvider().get(),
                                "Left click on the map to create map point.", "Point Instructions", true);

                        manager.registerContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                                myDrawProvider);
                        myCreateMapPointButton.setToolTipText("Click the map to create a new point");
                        myAnnotationPointsController.setPickingMode(true);
                    }
                    else
                    {
                        manager.deregisterContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class,
                                myDrawProvider);
                        myCreateMapPointButton.setToolTipText("Create map point");
                        myAnnotationPointsController.setPickingMode(false);
                    }
                }
            }
        });

        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "Map Points",
                myCreateMapPointButton, 360, SeparatorLocation.LEFT, new Insets(0, 2, 0, 2));
    }
}
