package io.opensphere.core.modulestate;

import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.util.Service;

/**
 * Manager for module states. This is a facility for software modules to
 * persist, load, and manage their state.
 */
public interface ModuleStateManager
{
    /**
     * Deactivate all active states.
     */
    void deactivateAllStates();

    /**
     * Detect the modules that have saved state in the given DOM node.
     *
     * @param node The DOM node.
     * @return The detected modules.
     */
    Collection<String> detectModules(Node node);

    /**
     * Detect the modules that have saved state in the given state object.
     *
     * @param state The state object.
     * @return The detected modules.
     */
    Collection<String> detectModules(StateType state);

    /**
     * Get the active state ids.
     *
     * @return The ids.
     */
    Collection<? extends String> getActiveStateIds();

    /**
     * Get the module names currently registered with this manager.
     *
     * @return The names.
     */
    Collection<? extends String> getModuleNames();

    /**
     * Get the modules that currently have state that can be saved.
     *
     * @return The module names.
     */
    Collection<? extends String> getModulesThatCanSaveState();

    /**
     * Get the modules that should save state by default.
     *
     * @return The module names.
     */
    Collection<? extends String> getModulesThatSaveStateByDefault();

    /**
     * Get the state ids that have been registered with the manager.
     *
     * @return The state ids.
     */
    Collection<? extends String> getRegisteredStateIds();

    /**
     * Gets the state dependencies for a set of modules.
     *
     * @param modules the modules
     * @return the state dependencies for modules
     */
    Map<String, Collection<? extends String>> getStateDependenciesForModules(Collection<? extends String> modules);

    /**
     * Get the description for a state.
     *
     * @param state The id of the state.
     * @return The description.
     */
    String getStateDescription(String state);

    /**
     * Get the tags for a state.
     *
     * @param state The id of the state.
     * @return The tags.
     */
    Collection<? extends String> getStateTags(String state);

    /**
     * Get if a state is active.
     *
     * @param state The id of the state.
     * @return {@code true} if the state is active.
     */
    boolean isStateActive(String state);

    /**
     * <p>
     * Register the state controller for a module. If any states are currently
     * active, this will cause the currently active states to be immediately
     * activated in the given controller.
     * </p>
     * <p>
     * More than one controller may be registered with a single module name.
     * When states are activated/deactivated, all controllers associated with
     * the state's modules will be called in the order that they were
     * registered.
     * </p>
     *
     * @param moduleName The display name for the module.
     * @param controller The controller. Only a weak reference is kept.
     */
    void registerModuleStateController(String moduleName, ModuleStateController controller);

    /**
     * Register a saved state with the manager. This does not activate the
     * state.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags for the state.
     * @param modules The modules associated with the state.
     * @param element The DOM element containing the state.
     */
    void registerState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            Element element);

    /**
     * Register a saved state with the manager. This does not activate the
     * state.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags for the state.
     * @param modules The modules associated with the state.
     * @param state The state object containing the state.
     */
    void registerState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            StateType state);

    /**
     * Save the states of the specified modules to the given DOM.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags for the state.
     * @param modules The display names of the modules to be saved.
     * @param parentNode The parent node to add the state to.
     */
    void saveState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            Node parentNode);

    /**
     * Save the states of the specified modules to the given state object.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags for the state.
     * @param modules The display names of the modules to be saved.
     * @param state The state object to add the state to.
     */
    void saveState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            StateType state);

    /**
     * Toggle a state active/inactive. The state must be registered first.
     *
     * @param id The id of the state.
     */
    void toggleState(String id);

    /**
     * Unregister the state controller for a module. This does not deactivate
     * the state.
     *
     * @param moduleName The display name for the module.
     * @param controller The controller.
     */
    void unregisterModuleStateController(String moduleName, ModuleStateController controller);

    /**
     * Remove a saved state from the manager.
     *
     * @param id The id of the state.
     */
    void unregisterState(String id);

    /**
     * Creates a service that can be used to add/remove the given module state
     * controller.
     *
     * @param moduleName The display name for the module.
     * @param controller The controller.
     * @return the service
     */
    default Service getModuleStateControllerService(final String moduleName, final ModuleStateController controller)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                registerModuleStateController(moduleName, controller);
            }

            @Override
            public void close()
            {
                unregisterModuleStateController(moduleName, controller);
            }
        };
    }
}
