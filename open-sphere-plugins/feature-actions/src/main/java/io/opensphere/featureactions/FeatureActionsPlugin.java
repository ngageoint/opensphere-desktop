package io.opensphere.featureactions;

import java.util.Collection;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractServicePlugin;
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
    
    private FeatureActionStateController myStateController;

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

        return New.list(toolbox.getPluginToolboxRegistry().getRegistrationService(pluginToolbox), controller);
    }
}
