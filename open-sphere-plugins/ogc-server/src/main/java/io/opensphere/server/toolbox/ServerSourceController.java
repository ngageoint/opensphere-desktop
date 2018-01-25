package io.opensphere.server.toolbox;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServerSourceEditor;

/**
 * Interface for controllers that manage server sources/configurations.
 */
public interface ServerSourceController
{
    /**
     * Accept (apply) changes.
     *
     * @return whether to allow the accept to complete
     */
    boolean accept();

    /**
     * Activates a data source.
     *
     * @param source the source to activate
     */
    void activateSource(IDataSource source);

    /**
     * Adds a configuration change listener.
     *
     * @param listener the listener
     */
    void addConfigChangeListener(ConfigChangeListener listener);

    /**
     * Adds a Source to the configuration.
     *
     * @param source the source to add
     */
    void addSource(IDataSource source);

    /**
     * Creates a new data source for the given server type.
     *
     * @param typeName the server type name
     * @return the new data source
     */
    IDataSource createNewSource(String typeName);

    /**
     * De-activate a data source.
     *
     * @param source the source to de-activate
     */
    void deactivateSource(IDataSource source);

    /**
     * Gets the server customization for the current server type.
     *
     * @return the server customization
     */
    ServerCustomization getCurrentServerCustomization();

    /**
     * Gets the component editor used to build the server sources that this
     * controller supports.
     *
     * @return the current editor
     */
    ServerSourceEditor getCurrentSourceEditor();

    /**
     * Gets the order this controller's types should appear in the add server
     * window relative to other controllers.
     *
     * @return the ordinal value
     */
    int getOrdinal();

    /**
     * Gets the server customization for the given server type.
     *
     * @param typeName the server type
     * @return the server customizations
     */
    ServerCustomization getServerCustomization(String typeName);

    /**
     * Build a brief formatted description of a source. This should be a
     * description that is formatted in plain text or html and can be shown to a
     * user in something like a Swing ToolTip.
     *
     * @param source the source from which to build a description
     * @return a brief formatted source description
     */
    String getSourceDescription(IDataSource source);

    /**
     * Returns the list of all {@link IDataSource} from the config.
     *
     * @return the {@link List} of {@link IDataSource}s
     */
    List<IDataSource> getSourceList();

    /**
     * Gets the name of the type for the given data source.
     *
     * @param source the data source
     * @return the type name
     */
    String getTypeName(IDataSource source);

    /**
     * Gets the names of the types serviced by this controller.
     *
     * @return the type names
     */
    Collection<String> getTypeNames();

    /**
     * Open and initialize the controller.
     *
     * @param toolbox the Core Toolbox
     * @param prefsTopic the class to use when retrieving preferences
     */
    void open(Toolbox toolbox, Class<?> prefsTopic);

    /**
     * Method that indicates if this controller overrides all the functionality
     * in the passed in controller.
     *
     * @param controller The controller to check if this class overrides it.
     * @return True if this controller override all the functionality of the
     *         specified controller.
     */
    boolean overridesController(ServerSourceController controller);

    /**
     * Removes a configuration change listener.
     *
     * @param listener the listener
     */
    void removeConfigChangeListener(ConfigChangeListener listener);

    /**
     * Removes an {@link IDataSource} from the configuration.
     *
     * @param source the source to remove
     * @return true if removed, false if not
     */
    boolean removeSource(IDataSource source);

    /**
     * Save the current configuration.
     *
     * TODO: The visibility of this method should be changed to private, but the
     * OptionsProvider is manipulating the underlying sources directly and needs
     * a way to force those changes to be persisted.
     */
    void saveConfigState();

    /**
     * Sets the current server type name.
     *
     * @param typeName the current server type name
     */
    void setCurrentTypeName(String typeName);

    /**
     * ConfigChangeListener.
     */
    @FunctionalInterface
    public interface ConfigChangeListener
    {
        /**
         * Config changed.
         */
        void configChanged();
    }
}
