package io.opensphere.controlpanels.state;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/** Controller for the state plugin. */
public interface StateController
{
    /**
     * Deactivate all states.
     */
    void deactivateAllStates();

    /**
     * Get the names of the modules that are available for saving or loading
     * state.
     *
     * @return The module names.
     */
    Collection<? extends String> getAvailableModules();

    /**
     * Get the names of the states that can be activated.
     *
     * @return The state ids.
     */
    Collection<? extends String> getAvailableStates();

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
     * Remove some saved states.
     *
     * @param stateIds The ids of the states.
     */
    void removeStates(Collection<? extends String> stateIds);

    /**
     * Save a state.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags associated with the state.
     * @param modules The names of the modules to be saved.
     * @param saveToApplication If the state should be saved to the application.
     * @param outputStream The output stream the state should be exported to, or
     *            {@code null} if it should not be exported.
     */
    void saveState(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules,
            boolean saveToApplication, OutputStream outputStream);

    /**
     * Toggle a state.
     *
     * @param id The id of the state.
     */
    void toggleState(String id);
}
