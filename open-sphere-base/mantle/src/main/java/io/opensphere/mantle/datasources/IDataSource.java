package io.opensphere.mantle.datasources;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * A data source represents a provider of information. Data providers may be local to the user's machine, or remotely accessible.
 * Implementations of this interface are responsible for persisting information between sessions. The activated state of the data
 * source is also persisted between sessions, such that when a user closes the application, any active data sources are restored
 * to active state when the tool is restarted.
 */
public interface IDataSource
{
    /** The Constant EXPORT_FAILED. */
    String EXPORT_FAILED = "EXPORT_FAILED";

    /** The Constant EXPORT_SUCCESS. */
    String EXPORT_SUCCESS = "EXPORT_SUCCESS";

    /** The Constant SOURCE_BUSY_CHANGED. */
    String SOURCE_BUSY_CHANGED = "SOURCE_BUSY_CHANGED";

    /** The Constant SOURCE_FROZEN_CHANGED. */
    String SOURCE_FROZEN_CHANGED = "SOURCE_FROZEN_CHANGED";

    /** The Constant SOURCE_LOAD_ERROR_CHANGED. */
    String SOURCE_LOAD_ERROR_CHANGED = "SOURCE_LOAD_ERROR_CHANGED";

    /** The Constant SOURCE_LOCKED_CHANGED. */
    String SOURCE_LOCKED_CHANGED = "SOURCE_LOCKED_CHANGED";

    /**
     * If this function returns true it means that this source does not support
     * regular activation/deactivation where the configuration is persisted
     * locally. The source may only be added and removed from the manager ( and
     * external ) rather than activated and deactivated.
     *
     * @return true, if successful
     */
    boolean addAndRemoveOnly();

    /**
     * Adds a {@link DataSourceChangeListener} to this Sources listener set.
     *
     * @param lstr - the listener to add
     */
    void addDataSourceChangeListener(DataSourceChangeListener lstr);

    /**
     * Exports as bundle.
     *
     * @return true, if successful
     */
    boolean exportsAsBundle();

    /**
     * Export to file.
     *
     * @param selectedFile the selected file
     * @param parent the parent
     * @param callback the callback
     */
    void exportToFile(File selectedFile, Component parent, ActionListener callback);

    /**
     * Returns the name ( not the location ) of the file source.
     *
     * @return the Name
     */
    String getName();

    /**
     * Gets whether or not this source is active.
     *
     * @return true if active, false if not
     */
    boolean isActive();

    /**
     * Get whether or not this source is busy being loaded/changed/unloaded.
     *
     * @return true if busy
     */
    boolean isBusy();

    /**
     * Get whether or not this source is busy being loaded/changed/unloaded.
     *
     * @return true if busy
     */
    boolean isFrozen();

    /**
     * Gets the checks if is loaded.
     *
     * @return the checks if is loaded
     */
    boolean isLoaded();

    /**
     * Get whether or not this source is locked.
     *
     * @return true if locked
     */
    boolean isLocked();

    /**
     * Checks to see if this data source should be saved to a config file.
     *
     * @return true, do not save this source to a config
     */
    boolean isTransient();

    /**
     * Returns true if loading this file results in an error.
     *
     * @return true if error, false if not
     */
    boolean loadError();

    /**
     * Removes a {@link DataSourceChangeListener} from this sources listener
     * set.
     *
     * @param lstr - the listener to remove
     */
    void removeDataSourceChangeListener(DataSourceChangeListener lstr);

    /**
     * Sets whether or not this source is active.
     *
     * @param active the new active
     */
    void setActive(boolean active);

    /**
     * Sets if this source is busy being updated/loaded/unloaded Will send out
     * an event based on this change.
     *
     * @param isBusy - true if busy, false if not
     * @param source - the object setting us to busy for event dispatch purposes
     */
    void setBusy(boolean isBusy, Object source);

    /**
     * Sets if this source is busy being updated/loaded/unloaded Will send out
     * an event based on this change.
     *
     * @param isBusy - true if busy, false if not
     * @param source - the object setting us to busy for event dispatch purposes
     */
    void setFrozen(boolean isBusy, Object source);

    /**
     * Sets the checks if is loaded.
     *
     * @param isLoaded the new checks if is loaded
     */
    void setIsLoaded(boolean isLoaded);

    /**
     * Sets if this source has encountered a load error.
     *
     * @param error the error
     * @param source the source
     */
    void setLoadError(boolean error, Object source);

    /**
     * Sets if this source is locked Will send out an event based on this
     * change.
     *
     * @param isLocked - true if locked, false if not
     * @param source - the object setting us to locked for event dispatch
     *            purposes
     */
    void setLocked(boolean isLocked, Object source);

    /**
     * Sets the name of the source.
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Sets the source transient.
     *
     * @param isTransient the new transient
     */
    void setTransient(boolean isTransient);

    /**
     * Supports file export.
     *
     * @return true, if successful
     */
    boolean supportsFileExport();

    /**
     * Update data locations. //TODO: Add more documentation about this method.
     *
     * @param destDataDir the dest data dir
     */
    void updateDataLocations(File destDataDir);
}
