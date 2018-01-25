package io.opensphere.core.importer.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.EventQueueExecutor;

/**
 * The Class ImporterRegistryImpl.
 */
public class ImporterRegistryImpl implements ImporterRegistry
{
    /** The change support. */
    private final WeakChangeSupport<ImporterRegistry.ImporterRegistryListener> myChangeSupport;

    /** The my content lock. */
    private final ReentrantLock myContentLock;

    /** The importers. */
    private final Set<FileOrURLImporter> myImporters;

    /**
     * Instantiates a new importer registry impl.
     */
    public ImporterRegistryImpl()
    {
        myChangeSupport = new WeakChangeSupport<>();
        myContentLock = new ReentrantLock();
        myImporters = New.set();
    }

    @Override
    public void addImporter(FileOrURLImporter importer)
    {
        boolean changed = false;
        myContentLock.lock();
        try
        {
            changed = myImporters.add(importer);
        }
        finally
        {
            myContentLock.unlock();
        }
        if (changed)
        {
            fireRegistryChanged();
        }
    }

    @Override
    public void addListener(ImporterRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public Service createImporterService(final FileOrURLImporter importer)
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

    @Override
    public List<FileOrURLImporter> getImporters(Predicate<FileOrURLImporter> filter, Comparator<FileOrURLImporter> comparator)
    {
        List<FileOrURLImporter> result = New.list();
        myContentLock.lock();
        try
        {
            for (FileOrURLImporter importer : myImporters)
            {
                if (filter == null || filter.test(importer))
                {
                    result.add(importer);
                }
            }
        }
        finally
        {
            myContentLock.unlock();
        }
        if (comparator != null)
        {
            Collections.sort(result, comparator);
        }
        return result;
    }

    @Override
    public void removeImporter(FileOrURLImporter importer)
    {
        boolean changed = false;
        myContentLock.lock();
        try
        {
            changed = myImporters.remove(importer);
        }
        finally
        {
            myContentLock.unlock();
        }
        if (changed)
        {
            fireRegistryChanged();
        }
    }

    @Override
    public void removeListener(ImporterRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Fire registry changed.
     */
    private void fireRegistryChanged()
    {
        myChangeSupport.notifyListeners(listener -> listener.importersChanged(), new EventQueueExecutor());
    }
}
