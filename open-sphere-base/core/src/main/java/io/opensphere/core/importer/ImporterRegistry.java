package io.opensphere.core.importer;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import io.opensphere.core.util.Service;

/**
 * The Interface ImporterRegistry. A registry of {@link FileOrURLImporter}s.
 */
public interface ImporterRegistry
{
    /**
     * Adds the importer to the registry.
     *
     * @param importer the importer
     */
    void addImporter(FileOrURLImporter importer);

    /**
     * Adds the {@link ImporterRegistryListener} to the registry for change
     * notifications.
     *
     * @param listener the listener to add.
     */
    void addListener(ImporterRegistryListener listener);

    /**
     * Creates a service that can add and remove the importer to/from the
     * registry.
     *
     * @param importer the importer
     * @return the service
     */
    Service createImporterService(FileOrURLImporter importer);

    /**
     * Gets a list of the {@link FileOrURLImporter} that pass the provided
     * {@link Predicate} sorted by the provided {@link Comparator}.  Null
     * arguments are handled gracefully.  If <i>filter</i> is null, then all
     * importers are returned, and if <i>comparator</i> is null, then the
     * resulting list is not sorted.
     *
     * @param filter the {@link Predicate} to use to filter the
     *            {@link FileOrURLImporter}s
     * @param comparator the {@link Comparator} to use to order the result list
     *            ( ordered by name if null ).
     * @return the {@link FileOrURLImporter}s that pass the filter as a
     *         {@link List}.
     */
    List<FileOrURLImporter> getImporters(Predicate<FileOrURLImporter> filter,
            Comparator<FileOrURLImporter> comparator);

    /**
     * Return a list of file or file group importers ordered by name.
     * @return bla
     */
    default List<FileOrURLImporter> getFileImporters()
    {
        return getImporters(value -> value.getName() != null && (value.importsFiles() || value.importsFileGroups()),
                FileOrURLImporter.LEX_ORDER);
    }

    /**
     * Return a list of url importers ordered by precedence.
     * @return bla
     */
    default List<FileOrURLImporter> getUrlImporters()
    {
        return getImporters(value -> value.importsURLs(), FileOrURLImporter.PREC_ORDER);
    }

    /**
     * Removes the importer from the registry.
     *
     * @param importer the importer
     */
    void removeImporter(FileOrURLImporter importer);

    /**
     * Removes the {@link ImporterRegistryListener} from the registry.
     *
     * @param listener the listener to remove.
     */
    void removeListener(ImporterRegistryListener listener);

    /**
     * Creates a service that can be used to add/remove the given importer.
     *
     * @param importer the importer
     * @return the service
     */
    default Service getImporterService(final FileOrURLImporter importer)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addImporter(importer);
            }

            @Override
            public void close()
            {
                removeImporter(importer);
            }
        };
    }

    /**
     * Listener for importer registry changes.
     */
    @FunctionalInterface
    interface ImporterRegistryListener
    {
        /**
         * Importers changed notification.
         */
        void importersChanged();
    }
}
