package io.opensphere.controlpanels.state;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.SplitButton;

/**
 * Plugin that provides the controls for saving and restoring state.
 */
public class StatePlugin extends PluginAdapter
{
    /** The toolbox. */
    private Toolbox myToolbox;

    /** The menu provider for the disable button. */
    private DisableStatesMenuProvider myDisableStatesMenuProvider;

    /** The import controller. */
    private StateImportController myImportController;

    /**
     * The Life cycle event listener. Make sure all plugins are initialized
     * before we add the state button.
     */
    private final transient EventListener<ApplicationLifecycleEvent> myLifeCycleEventListener = new EventListener<ApplicationLifecycleEvent>()
    {
        @Override
        public void notify(ApplicationLifecycleEvent event)
        {
            if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
            {
                EventQueueUtilities.runOnEDT(() -> initializeAfterPlugins());
            }
        }
    };

    /** The State view. */
    private StateView myStateView;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;

        myDisableStatesMenuProvider = new DisableStatesMenuProvider(toolbox.getModuleStateManager());
        toolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT,
                Void.class, myDisableStatesMenuProvider);

        myImportController = new StateImportController(toolbox.getUIRegistry().getMainFrameProvider(),
                toolbox.getModuleStateManager(), toolbox);
        toolbox.getImporterRegistry().addImporter(myImportController);

        toolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifeCycleEventListener);
    }

    @Override
    public void close()
    {
        myImportController.getStateController().deactivateAllStates();

        myToolbox.getUIRegistry().getToolbarComponentRegistry().deregisterToolbarComponent(ToolbarLocation.NORTH, "State");
        myToolbox.getUIRegistry().getContextActionManager().deregisterContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT,
                Void.class, myDisableStatesMenuProvider);
        myToolbox.getImporterRegistry().removeImporter(myImportController);
    }

    /**
     * Performs additional initialization after all the plugins have
     * initialized.
     */
    private void initializeAfterPlugins()
    {
        StateController controller = new StateControllerImpl(myToolbox);
        myStateView = new StateView(controller, myImportController, myToolbox);
        myImportController.setStateController(controller);

        SplitButton stateControlButton = myStateView.getStateControlButton();
        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH,
                stateControlButton.getText(), stateControlButton, 470, SeparatorLocation.LEFT);
        myToolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(stateControlButton.getIcon(), "States",
                "Shows state controls for activating/deactivating a state, importing a state (via file or url), saving a state, "
                        + "disabling states, and deleting states. Different parts of the current state can be saved including "
                        + "Filters, Time, Current View, Animation, Map Layers and Layers, Query Regions, and Styles. ");
    }
}
