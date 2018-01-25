package io.opensphere.mantle.datasources.impl;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.DataSourceChangeListener;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * The Class AbstractDataSource.
 */
@XmlRootElement(name = "AbstractDataSource")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractDataSource implements IDataSource
{
    /** The Constant ourEventExecutor. */
    protected static final ThreadPoolExecutor ourEventExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("AbstractFileDataSource:Worker"));

    /** The my data source change listeners. */
    @XmlTransient
    private final Set<DataSourceChangeListener> myDataSourceChangeListeners;

    /** The my frozen. */
    @XmlTransient
    private final AtomicBoolean myFrozen;

    /** The my is busy. */
    @XmlTransient
    private final AtomicBoolean myIsBusy;

    /** The my is loaded. */
    @XmlTransient
    private final AtomicBoolean myIsLoaded;

    /** The my is locked. */
    @XmlTransient
    private final AtomicBoolean myIsLocked;

    /** If true, do not save this source to a config. */
    @XmlTransient
    private final AtomicBoolean myIsTransient;

    static
    {
        ourEventExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Basic CTOR.
     */
    public AbstractDataSource()
    {
        myDataSourceChangeListeners = new HashSet<>();
        myIsBusy = new AtomicBoolean(false);
        myFrozen = new AtomicBoolean(false);
        myIsLocked = new AtomicBoolean(false);
        myIsLoaded = new AtomicBoolean(false);
        myIsTransient = new AtomicBoolean(false);
    }

    @Override
    public boolean addAndRemoveOnly()
    {
        return false;
    }

    @Override
    public void addDataSourceChangeListener(DataSourceChangeListener lstr)
    {
        synchronized (myDataSourceChangeListeners)
        {
            myDataSourceChangeListeners.add(lstr);
        }
    }

    @Override
    public boolean exportsAsBundle()
    {
        return false;
    }

    @Override
    public void exportToFile(File selectedFile, Component parent, ActionListener callback)
    {
    }

    /**
     * Fires a {@link DataSourceChangeEvent} to all
     * {@link DataSourceChangeListener} of this source.
     *
     * @param evt - the event to dispatch
     */
    public void fireDataSourceChanged(final DataSourceChangeEvent evt)
    {
        synchronized (myDataSourceChangeListeners)
        {
            for (DataSourceChangeListener lstr : myDataSourceChangeListeners)
            {
                final DataSourceChangeListener chngLstr = lstr;
                ourEventExecutor.execute(() -> chngLstr.dataSourceChanged(evt));
            }
        }
    }

    @Override
    public boolean isBusy()
    {
        return myIsBusy.get();
    }

    @Override
    public boolean isFrozen()
    {
        return myFrozen.get();
    }

    @Override
    public boolean isLoaded()
    {
        return myIsLoaded.get();
    }

    @Override
    public boolean isLocked()
    {
        return myIsLocked.get();
    }

    @Override
    public boolean isTransient()
    {
        return myIsTransient.get();
    }

    @Override
    public void removeDataSourceChangeListener(DataSourceChangeListener lstr)
    {
        synchronized (myDataSourceChangeListeners)
        {
            myDataSourceChangeListeners.remove(lstr);
        }
    }

    @Override
    public void setBusy(boolean isBusy, Object source)
    {
        myIsBusy.set(isBusy);
        fireDataSourceChanged(new DataSourceChangeEvent(this, SOURCE_BUSY_CHANGED, source));
    }

    @Override
    public void setFrozen(boolean isBusy, Object source)
    {
        myFrozen.set(isBusy);
        fireDataSourceChanged(new DataSourceChangeEvent(this, SOURCE_FROZEN_CHANGED, source));
    }

    @Override
    public void setIsLoaded(boolean isLoaded)
    {
        myIsLoaded.set(isLoaded);
    }

    @Override
    public void setLocked(boolean isLocked, Object source)
    {
        myIsLocked.set(isLocked);
        fireDataSourceChanged(new DataSourceChangeEvent(this, SOURCE_LOCKED_CHANGED, source));
    }

    @Override
    public void setTransient(boolean isLoaded)
    {
        myIsTransient.set(isLoaded);
    }

    @Override
    public boolean supportsFileExport()
    {
        return false;
    }

    @Override
    public void updateDataLocations(File destDataDir)
    {
    }
}
