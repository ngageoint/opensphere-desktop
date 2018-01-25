package io.opensphere.mantle.datasources;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import io.opensphere.core.Toolbox;

/**
 * The Class DataSourceController.
 */
public interface DataSourceController extends Comparable<DataSourceController>
{
    /**
     * Activates a data source.
     *
     * @param source the source to activate
     */
    void activateSource(IDataSource source);

    /**
     * Adds a IDataSource to the config.
     *
     * @param source the source
     */
    void addSource(IDataSource source);

    /**
     * Creates a source for this type, gives it to the caller does not add it to
     * this controller.
     *
     * @param parent the parent
     * @param type the type
     * @param chosenFiles the chosen files
     * @param sourcesInUse the sources in use
     * @param caller the caller
     */
    void createSource(Container parent, DataSourceType type, List<File> chosenFiles, Set<IDataSource> sourcesInUse,
            IDataSourceCreator caller);

    /**
     * De-activeate a data source.
     *
     * @param source the source to de-activate
     */
    void deactivateSource(IDataSource source);

    /**
     * Returns a list of the classes of the sources that are handled by this
     * controller.
     *
     * @return the source classes
     */
    List<Class<? extends IDataSource>> getSourceClasses();

    /**
     * Returns the list of all {@link IDataSource} from the config.
     *
     * @return the {@link List} of {@link IDataSource}
     */
    List<IDataSource> getSourceList();

    /**
     * Gets the source type.
     *
     * @return the source type
     */
    DataSourceType getSourceType();

    /**
     * Gets the {@link Toolbox}.
     *
     * @return the toolbox
     */
    Toolbox getToolbox();

    /**
     * Returns the set of file filter extensions for this type.
     *
     * @return the type extensions
     */
    String[] getTypeExtensions();

    /**
     * Gets the name of the type for this file source like "CSV".
     *
     * @return the type name
     */
    String getTypeName();

    /**
     * Initial load/initialization of sources from configuration.
     */
    void initialize();

    /**
     * Removes an {@link IDataSource} from the config.
     *
     * @param source the source to remove
     * @param cleanup - if there are things that need to be cleaned up in a
     *            final remove do so when this is true
     * @param parent - the parent component
     * @return true if removed, false if not
     */
    boolean removeSource(IDataSource source, boolean cleanup, Component parent);

    /**
     * Sets the executor service.
     *
     * @param execService the new executor service
     */
    void setExecutorService(ExecutorService execService);

    /**
     * Notifies the config that a source has been updated and needs to be
     * saved/persisted.
     *
     * @param source the source
     */
    void updateSource(IDataSource source);

    /**
     * The DataSourceType.
     */
    enum DataSourceType
    {
        /** The CHOICE. */
        CHOICE("CHOICE"),

        /** The EXT_PROC. */
        EXT_PROC("External Process"),

        /** The FILE. */
        FILE("File"),

        /** The OTHER. */
        OTHER("OTHER"),

        /** The SERVER. */
        SERVER("Server"),

        /** The URL. */
        URL("URL");

        /** The source type str. */
        private final String mySourceTypeStr;

        /**
         * Instantiates a new data source type.
         *
         * @param pName the name
         */
        DataSourceType(final String pName)
        {
            mySourceTypeStr = pName;
        }

        @Override
        public String toString()
        {
            return mySourceTypeStr;
        }
    }
}
