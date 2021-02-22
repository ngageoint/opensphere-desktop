package io.opensphere.featureactions;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.AlertNotificationButton;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.featureactions.controller.FeatureActionsController;
import io.opensphere.featureactions.controller.FeatureActionStateController;
import io.opensphere.featureactions.editor.ui.ActionEditorDisplayer;
import io.opensphere.featureactions.editor.ui.ActionEditorDisplayerImpl;
import io.opensphere.featureactions.editor.ui.FeatureActionPanel;
import io.opensphere.featureactions.editor.ui.FeatureActionsMenuProvider;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.featureactions.toolbox.FeatureActionsToolbox;
import io.opensphere.mantle.crust.DataTypeChecker;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Feature Actions plugin.
 */
public class FeatureActionsPlugin extends AbstractServicePlugin
{
    /** The listener for data group activation changes. */
    private final transient Runnable myActivationListener = this::handleDataGroupActivationChange;

    /** The menu item for removing all feature actions. */
    private ContextMenuProvider<Void> myClearFeaturesMenuProvider;

    /**
     * Provides a menu item for the user in the layers window to edit a layer's
     * feature actions.
     */
    private FeatureActionsMenuProvider myEditorMenuProvider;

    /** The button for opening the feature action manager. */
    private FeatureActionAlertButton myFeatureActionManagerButton;

    /** The state controller to manage feature actions. */
    private FeatureActionStateController myStateController;

    /** The toolbox. */
    private Toolbox myToolbox;

    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;
        FeatureActionsRegistry registry = new FeatureActionsRegistry(toolbox.getPreferencesRegistry());
        FeatureActionsToolbox pluginToolbox = new FeatureActionsToolbox(registry);
        FeatureActionsController controller = new FeatureActionsController(toolbox, registry);
        myStateController = new FeatureActionStateController(registry, MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController());

        ActionEditorDisplayer displayer = new ActionEditorDisplayerImpl(toolbox, registry);
        UIRegistry uiRegistry = toolbox.getUIRegistry();
        myEditorMenuProvider = new FeatureActionsMenuProvider(displayer, uiRegistry);
        uiRegistry.getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupContextKey.class, myEditorMenuProvider);
        toolbox.getModuleStateManager().registerModuleStateController("Feature Action", myStateController);

        myClearFeaturesMenuProvider = new ContextMenuProvider<>()
        {
            @Override
            public List<JMenuItem> getMenuItems(String contextId, Void key)
            {
                JMenuItem clearFeatures = new JMenuItem("Clear Feature Actions");
                clearFeatures.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        registry.clearAll();
                    }
                });
                return Collections.singletonList(clearFeatures);
            }

            @Override
            public int getPriority()
            {
                return 9;
            }
        };

        toolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT,
                Void.class, myClearFeaturesMenuProvider);

        myFeatureActionManagerButton = new FeatureActionAlertButton(null);
        myFeatureActionManagerButton.addActionListener(e -> showFeatureActionManager(toolbox, registry));
        EventQueueUtilities.invokeLater(() -> toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(
                ToolbarLocation.SOUTH, "FeatureActionManager", myFeatureActionManagerButton, 202, SeparatorLocation.NONE));
        myFeatureActionManagerButton.setIcons();
        MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController().addActivationListener(myActivationListener);

        return New.list(toolbox.getPluginToolboxRegistry().getRegistrationService(pluginToolbox), controller);
    }

    /**
     * Checks how many current active layers that use feature actions are enabled to
     * determine the enabled status of the feature action manager button.
     * Called whenever a data group is (de)activated.
     */
    private void handleDataGroupActivationChange()
    {
        long activeLayers = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController().getActiveMembers(false)
                .stream().filter(e -> DataTypeChecker.isFeatureType(e)).count();
        if (myFeatureActionManagerButton.isEnabled() && activeLayers == 0)
        {
            myFeatureActionManagerButton.disableButton();
        }
        else if (!myFeatureActionManagerButton.isEnabled() && activeLayers != 0)
        {
            myFeatureActionManagerButton.enableButton();
        }
    }

    /**
     * Construct and show the feature action manager.
     *
     * @param toolbox the toolbox
     * @param registry the feature action registry
     */
    private void showFeatureActionManager(Toolbox toolbox, FeatureActionsRegistry registry)
    {
        Window owner = toolbox.getUIRegistry().getMainFrameProvider().get();
        JFXDialog dialog = new JFXDialog(owner, "Feature Actions", false);
        dialog.setFxNode(new FeatureActionPanel(toolbox, registry, dialog));
        dialog.setSize(new Dimension(900, 600));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(owner);
        dialog.setModalityType(ModalityType.MODELESS);
        dialog.setVisible(true);
    }

    /**
     * The custom alert button class for the feature action manager button.
     */
    private static class FeatureActionAlertButton extends AlertNotificationButton
    {
        /** The serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The enabled status of the button. */
        private boolean myEnabled;

        /**
         * Instantiates a new feature action alert button.
         *
         * @param icon the icon
         */
        public FeatureActionAlertButton(ImageIcon icon)
        {
            super(icon);
            setAlertColor(ColorUtilities.convertFromHexString("0000CCFF", 0, 1, 2, 3));
            setToolTipText("Manage Feature Actions");
            myEnabled = true;
        }

        /**
         * Disables the button. Updates visuals to reflect the change.
         */
        public void disableButton()
        {
            setEnabled(false);
            setToolTipText("No active layers use Feature Actions");
            setIcon(new GenericFontIcon(AwesomeIconSolid.MAGIC, Color.GRAY));
            myEnabled = false;
        }

        /**
         * Enables the button. Updates visuals to reflect the change.
         */
        public void enableButton()
        {
            setEnabled(true);
            setToolTipText("Manage Feature Actions");
            setIcon(new GenericFontIcon(AwesomeIconSolid.MAGIC, IconUtil.DEFAULT_ICON_FOREGROUND));
            myEnabled = true;
        }

        /**
         * Gets whether the button is enabled or not.
         *
         * @return true if the button is enabled, false otherwise
         */
        public boolean isEnabled()
        {
            return myEnabled;
        }

        @Override
        public void setAlertCount(int count)
        {
            setCount(count);
            setAlertCounterText(Integer.toString(count));
            repaint();
        }

        /**
         * Sets up the appropriate colors for the feature action manager button.
         */
        public void setIcons()
        {
            setIcon(new GenericFontIcon(AwesomeIconSolid.MAGIC, IconUtil.DEFAULT_ICON_FOREGROUND));
            setRolloverIcon(new GenericFontIcon(AwesomeIconSolid.MAGIC, IconUtil.DEFAULT_ICON_ROLLOVER));
            setPressedIcon(new GenericFontIcon(AwesomeIconSolid.MAGIC,
                    ColorUtilities.darken(IconUtil.DEFAULT_ICON_FOREGROUND, 0.5)));
        }
    }
}
