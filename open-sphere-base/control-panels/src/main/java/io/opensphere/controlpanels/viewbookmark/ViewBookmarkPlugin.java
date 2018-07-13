package io.opensphere.controlpanels.viewbookmark;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.JMenuItem;

import io.opensphere.controlpanels.viewbookmark.controller.ViewBookmarkController;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.SplitButton;

/**
 * The Class ViewBookmarkPlugin.
 */
public class ViewBookmarkPlugin extends PluginAdapter
{
    /** The Activation button. */
    private SplitButton myBookmarksActivationButton;

    /** The Controller. */
    private ViewBookmarkController myController;

    /** The Frame. */
    private transient HUDJInternalFrame myFrame;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** The View bookmark manager. */
    private ViewBookmarkManager myViewBookmarkManager;

    /** The View bookmark manager. */
    private ViewBookmarkMenuManager myViewBookmarkMenuManager;

    /**
     * Gets the controller.
     *
     * @return the controller
     */
    public ViewBookmarkController getController()
    {
        return myController;
    }

    /**
     * Creates the toolbar activation button.
     *
     * @return the j button
     */
    public SplitButton getSplitButton()
    {
        if (myBookmarksActivationButton == null)
        {
            myBookmarksActivationButton = new SplitButton("Look Angle", null);
            IconUtil.setIcons(myBookmarksActivationButton, "/images/earth-boresight.png");
            myBookmarksActivationButton.setToolTipText("Save the current look angle");
            myBookmarksActivationButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.control-panels.look-angle.button.save-look-angle");
                    myController.saveViewerPosition();
                }
            });
        }
        return myBookmarksActivationButton;
    }

    /**
     * Gets the view bookmark manager.
     *
     * @return the view bookmark manager
     */
    public ViewBookmarkMenuManager getViewBookmarkManager()
    {
        return myViewBookmarkMenuManager;
    }

    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        myController = new ViewBookmarkController(toolbox);
        JMenuItem addMenuItem = new JMenuItem("Save the current look angle...");
        addMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.control-panels.look-angle.selection.save-look-angle");
                getController().saveViewerPosition();
            }
        });
        JMenuItem manageMenuItem = new JMenuItem("Delete saved look angles...");
        manageMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.control-panels.look-angle.selection.delete-look-angle");
                getViewBookmarkManagerFrame().setVisible(true);
            }
        });
        myViewBookmarkMenuManager = new ViewBookmarkMenuManager(myController, getSplitButton(), addMenuItem, manageMenuItem);
        myToolbox = toolbox;
        toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "BookMarks",
                getSplitButton(), 460, SeparatorLocation.NONE);

        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(IconUtil.getNormalIcon("/images/earth-boresight.png"),
                "Look Angle", "Press the button to save the current look angle. Use the dropdown to manage look angles.");
    }

    /**
     * Creates the internal frame.
     *
     * @return the view bookmark manager
     */
    private HUDJInternalFrame getViewBookmarkManagerFrame()
    {
        if (myViewBookmarkManager == null)
        {
            myViewBookmarkManager = new ViewBookmarkManager(myController);
            Dimension mainFrameDim = myToolbox.getUIRegistry().getMainFrameProvider().get().getSize();
            Dimension frameDim = myViewBookmarkManager.getSize();
            myViewBookmarkManager.setLocation((int)(mainFrameDim.getWidth() - 20 - frameDim.getWidth()), 5);

            HUDJInternalFrame.Builder builder = new HUDJInternalFrame.Builder();
            builder.setInternalFrame(myViewBookmarkManager);
            myFrame = new HUDJInternalFrame(builder);
            myController.getToolbox().getUIRegistry().getComponentRegistry().addObjectsForSource(this,
                    Collections.singleton(myFrame));
        }
        return myFrame;
    }
}
