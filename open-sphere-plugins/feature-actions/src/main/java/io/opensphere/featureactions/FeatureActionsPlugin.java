package io.opensphere.featureactions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.controller.FeatureActionsController;
import io.opensphere.featureactions.controller.FeatureActionStateController;
import io.opensphere.featureactions.editor.ui.ActionEditorDisplayer;
import io.opensphere.featureactions.editor.ui.ActionEditorDisplayerImpl;
import io.opensphere.featureactions.editor.ui.FeatureActionsMenuProvider;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.featureactions.toolbox.FeatureActionsToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Feature Actions plugin. */
public class FeatureActionsPlugin extends AbstractServicePlugin
{
    /**
     * Provides a menu item for the user in the layers window to edit a layer's
     * feature actions.
     */
    private FeatureActionsMenuProvider myEditorMenuProvider;

    /** The state controller to manage feature actions. */
    private FeatureActionStateController myStateController;

    /** The menu item for removing all feature actions. */
    private ContextMenuProvider<Void> myClearFeaturesMenuProvider;

    @Override
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
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

        return New.list(toolbox.getPluginToolboxRegistry().getRegistrationService(pluginToolbox), controller);
    }
}
