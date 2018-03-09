package io.opensphere.server.control;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.common.util.UrlUtil;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;
import io.opensphere.mantle.datasources.UrlSource;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServerSourceEditor;
import io.opensphere.server.toolbox.ServerSourceController;

/**
 * Abstract implementation of {@link ServerSourceController} that provides an implementation of the change support.
 */
public abstract class AbstractServerSourceController implements ServerSourceController
{
    /** The Constant ourEventExecutor. */
    protected static final ThreadPoolExecutor ourEventExecutor = new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("OGCServerDataSourceController:Dispatch"));

    /** The Change support. */
    private final WeakChangeSupport<ConfigChangeListener> myChangeSupport;

    /** Listener for changes to the proxy settings. */
    private final NetworkConfigurationManager.NetworkConfigurationChangeListener myNetworkConfigChangeListener =
            this::reloadBadSources;

    /** The Map of sources to reload listeners. */
    private final Map<IDataSource, Runnable> myFinishReloadMap =
            Collections.synchronizedMap(new HashMap<IDataSource, Runnable>());

    /** Object that holds and manages the Server source configs. */
    private IDataSourceConfig myConfig;

    /** Lock for the config to avoid thread contention. */
    private final ReentrantReadWriteLock myConfigLock = new ReentrantReadWriteLock();

    /** The current server type name. */
    private String myCurrentTypeName;

    /** The server type to editor map. */
    private final Map<String, SingleCallable<ServerSourceEditor>> myEditorMap = New.map();

    /** The class to use when storing or retrieving preferences. */
    private Class<?> myPrefsTopic;

    /** The server customizations. */
    private final Collection<ServerCustomization> myServerCustomizations = New.list(4);

    /** The core toolbox. */
    private Toolbox myToolbox;

    static
    {
        ourEventExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Constructor.
     */
    public AbstractServerSourceController()
    {
        myChangeSupport = new WeakChangeSupport<>();
    }

    @Override
    public boolean accept()
    {
        ServerSourceEditor editor = getCurrentSourceEditor();
        boolean isValid = editor.accept();
        if (isValid)
        {
            IDataSource source = editor.getChangedSource();
            if (editor.isNewSource())
            {
                addSource(source);

                if (source.isActive())
                {
                    activateSource(source);
                }
            }
            else
            {
                if (!source.isActive())
                {
                    updateSource(source);
                }
            }
        }
        return isValid;
    }

    @Override
    public void addConfigChangeListener(ConfigChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void addSource(IDataSource source)
    {
        myConfigLock.writeLock().lock();
        try
        {
            myConfig.addSource(source);
            saveConfigState();
        }
        finally
        {
            myConfigLock.writeLock().unlock();
        }
    }

    @Override
    public ServerCustomization getCurrentServerCustomization()
    {
        return getServerCustomization(myCurrentTypeName);
    }

    @Override
    public ServerSourceEditor getCurrentSourceEditor()
    {
        ServerSourceEditor editor = null;
        SingleCallable<ServerSourceEditor> callable = myEditorMap.get(myCurrentTypeName);
        if (callable != null)
        {
            editor = callable.call();
        }
        return editor;
    }

    @Override
    public ServerCustomization getServerCustomization(String typeName)
    {
        ServerCustomization match = null;
        for (ServerCustomization serverCustomization : myServerCustomizations)
        {
            if (typeName.equalsIgnoreCase(serverCustomization.getServerType()))
            {
                match = serverCustomization;
                break;
            }
        }
        return match;
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        myConfigLock.readLock().lock();
        try
        {
            return myConfig == null ? Collections.<IDataSource>emptyList() : myConfig.getSourceList();
        }
        finally
        {
            myConfigLock.readLock().unlock();
        }
    }

    @Override
    public Collection<String> getTypeNames()
    {
        Collection<String> typeNames = New.list(myServerCustomizations.size());
        for (ServerCustomization serverCustomization : myServerCustomizations)
        {
            typeNames.add(serverCustomization.getServerType());
        }
        return typeNames;
    }

    @Override
    public void open(Toolbox toolbox, Class<?> prefsTopic)
    {
        myToolbox = toolbox;
        myPrefsTopic = prefsTopic;

        myToolbox.getSystemToolbox().getNetworkConfigurationManager().addChangeListener(myNetworkConfigChangeListener);
    }

    @Override
    public boolean overridesController(ServerSourceController controller)
    {
        return false;
    }

    @Override
    public void removeConfigChangeListener(ConfigChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public boolean removeSource(IDataSource source)
    {
        boolean removed = false;
        myConfigLock.writeLock().lock();
        try
        {
            removed = myConfig.removeSource(source);
            if (removed)
            {
                saveConfigState();
            }
        }
        finally
        {
            myConfigLock.writeLock().unlock();
        }
        return removed;
    }

    @Override
    public void saveConfigState()
    {
        myConfigLock.readLock().lock();
        try
        {
            myToolbox.getPreferencesRegistry().getPreferences(myPrefsTopic).putJAXBObject(getPrefsKey(), myConfig, false, this);
        }
        finally
        {
            myConfigLock.readLock().unlock();
        }
        fireChanged();
    }

    @Override
    public void setCurrentTypeName(String currentTypeName)
    {
        myCurrentTypeName = currentTypeName;
    }

    /**
     * Adds a server type.
     *
     * @param serverCustomization the server customization
     * @param editor the editor
     */
    protected void addServerType(ServerCustomization serverCustomization, SingleCallable<ServerSourceEditor> editor)
    {
        myServerCustomizations.add(serverCustomization);
        myEditorMap.put(serverCustomization.getServerType(), editor);
    }

    /**
     * Notify registered listeners when something changes.
     */
    protected void fireChanged()
    {
        myChangeSupport.notifyListeners(new Callback<ConfigChangeListener>()
        {
            @Override
            public void notify(ConfigChangeListener listener)
            {
                listener.configChanged();
            }
        }, ourEventExecutor);
    }

    /**
     * Gets the preferences keyword for this controller's source type.
     *
     * @return the preferences key
     */
    protected String getPrefsKey()
    {
        return "serverConfig";
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    protected Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Load saved sources from configuration and activate them as appropriate.
     */
    protected void initialize()
    {
        myConfigLock.readLock().lock();
        try
        {
            for (IDataSource source : myConfig.getSourceList())
            {
                if (source.isActive() || source.loadError())
                {
                    activateSource(source);
                }
            }
        }
        finally
        {
            myConfigLock.readLock().unlock();
        }
    }

    /**
     * Sets the config.
     *
     * @param config the new config
     */
    protected void setConfig(IDataSourceConfig config)
    {
        myConfig = config;
    }

    /**
     * Notifies the config that a source has been updated and needs to be saved/persisted.
     *
     * @param source the source
     */
    protected void updateSource(IDataSource source)
    {
        myConfigLock.writeLock().lock();
        try
        {
            myConfig.updateSource(source);
            saveConfigState();
        }
        finally
        {
            myConfigLock.writeLock().unlock();
            synchronized (myFinishReloadMap)
            {
                Runnable finishReload;
                if ((finishReload = myFinishReloadMap.get(source)) != null)
                {
                    myFinishReloadMap.remove(source);
                    finishReload.run();
                }
            }
        }
    }

    /** Reload sources which are marked as active, but are not actually activated. */
    protected void reloadBadSources()
    {
        Set<String> activeSourceURLs = getActiveSourceURLs();
        getSourceList().stream().forEach(source ->
        {
            if (source instanceof UrlSource)
            {
                if (source.isActive() && !activeSourceURLs.contains(((UrlSource)source).getURL()))
                {
                    myFinishReloadMap.put(source, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            activateSource(source);
                        }
                    });
                    deactivateSource(source);
                }
            }
        });
    }

    /**
     * Gets the set of active sources.
     *
     * @return the set of active sources
     */
    private Set<String> getActiveSourceURLs()
    {
        Set<String> activeURLs = new HashSet<String>();
        DataGroupController dgc = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController();
        dgc.getActiveMembers(false).stream().forEach(dti ->
        {
            if (UrlUtil.isValidAbsoluteUrl(dti.getUrl()))
            {
                activeURLs.add(dti.getUrl());
            }
        });
        return activeURLs;
    }

    /**
     * Callable really only calls call once.
     *
     * @param <V> the result type of method <tt>call</tt>
     */
    public abstract static class SingleCallable<V> implements Callable<V>
    {
        /** The result. */
        private V myResult;

        @Override
        public V call()
        {
            if (myResult == null)
            {
                myResult = callOnce();
            }
            return myResult;
        }

        /**
         * The call method to perform once.
         *
         * @return the result
         */
        protected abstract V callOnce();
    }
}
