package io.opensphere.core.appl;

import java.util.function.Supplier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.FrameBufferCaptureManager;
import io.opensphere.core.MapManager;
import io.opensphere.core.MemoryManager.MemoryListener;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.SecurityManager;
import io.opensphere.core.StatisticsManager;
import io.opensphere.core.SystemToolbox;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.authentication.UserInteractionSSLSocketFactory;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.event.EventManagerImpl;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.map.MapManagerImpl;
import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.modulestate.ModuleStateManagerImpl;
import io.opensphere.core.net.NetworkConfigurationOptionsProvider;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.security.SecurityManagerImpl;
import io.opensphere.core.security.options.SecurityOptionsProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.registry.GenericRegistry;

/**
 * Implementation of {@link Toolbox}.
 */
abstract class ToolboxImpl implements Toolbox
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ToolboxImpl.class);

    /** The animation manager. */
    private final AnimationManagerImpl myAnimationManager;

    /** The state controller for the animation manager. */
    private final AnimationManagerStateController myAnimationManagerStateController;

    /** The event manager. */
    private final EventManager myEventManager;

    /** The frame buffer capture manager. */
    private final FrameBufferCaptureManagerImpl myFrameBufferCaptureManager;

    /** The map manager. */
    private final MapManagerImpl myMapManager;

    /** A listener for memory events. */
    private final MemoryListener myMemoryListener = (oldStatus, newStatus) ->
    {
        final double defaultCacheSize = 10.;
        double cacheSize;
        switch (newStatus)
        {
            case CRITICAL:
                cacheSize = Utilities.parseSystemProperty("opensphere.db.criticalCacheSizePercentage", defaultCacheSize);
                break;
            case NOMINAL:
                cacheSize = Utilities.parseSystemProperty("opensphere.db.nominalCacheSizePercentage", defaultCacheSize);
                break;
            case WARNING:
                cacheSize = Utilities.parseSystemProperty("opensphere.db.warningCacheSizePercentage", defaultCacheSize);
                break;
            default:
                cacheSize = 0.;
                break;
        }
        final long bytes = (long)(Runtime.getRuntime().maxMemory() * cacheSize / 100);
        myRegistryManager.getDataRegistry().setInMemoryCacheSizeBytes(bytes);
    };

    /** The module state manager. */
    private final ModuleStateManager myModuleStateManager;

    /** The manager for the registries. */
    private final RegistryManagerImpl myRegistryManager;

    /** The security manager. */
    private final SecurityManagerImpl mySecurityManager;

    /** The system toolbox. */
    private final SystemToolbox mySystemToolbox;

    /** The time manager. */
    private final TimeManagerImpl myTimeManager;

    /** The manager of statistics. */
    private final StatisticsManager myStatisticsManager;

    /** The state controller for the time manager. */
    private final TimeManagerStateController myTimeManagerStateController;

    /**
     * Construct the toolbox.
     *
     * @param executorManager The executor manager.
     * @param cache A cache implementation, or <code>null</code> if there is
     *            none.
     * @param mainFrame The top level frame for the application.
     */
    public ToolboxImpl(ExecutorManager executorManager, Cache cache, JFrame mainFrame)
    {
        final long start = System.nanoTime();

        myRegistryManager = new RegistryManagerImpl(executorManager, cache, mainFrame);

        final ThreadPoolConfigs config = myRegistryManager.getPreferencesRegistry().getPreferences(ExecutorManager.class)
                .getJAXBObject(ThreadPoolConfigs.class, "envoyConfigs", null);
        executorManager.setConfigs(config);

        mySystemToolbox = new SystemToolboxImpl(myRegistryManager.getPreferencesRegistry())
        {
            @Override
            public void requestRestart()
            {
                ToolboxImpl.this.requestRestart();
            }
        };
        executorManager.setMemoryManager(mySystemToolbox.getMemoryManager());
        mySystemToolbox.getMemoryManager().addMemoryListener(myMemoryListener);
        myStatisticsManager = new StatisticsManagerImpl();
        myEventManager = new EventManagerImpl();
        myTimeManager = new TimeManagerImpl();
        myModuleStateManager = new ModuleStateManagerImpl(myRegistryManager.getPreferencesRegistry());

        myAnimationManager = new AnimationManagerImpl(myTimeManager,
                myRegistryManager.getPreferencesRegistry().getPreferences(AnimationManager.class),
                executorManager.getAnimatorExecutor());

        mySecurityManager = new SecurityManagerImpl(myRegistryManager.getPreferencesRegistry(), mainFrame);
        installSSLSocketFactory(myRegistryManager.getUIRegistry().getMainFrameProvider());
        myMapManager = new MapManagerImpl(myRegistryManager.getUIRegistry(), myEventManager,
                myRegistryManager.getPreferencesRegistry(), myRegistryManager.getUnitsRegistry(),
                executorManager.getMapExecutor(),
                myRegistryManager.getOrderManagerRegistry().getOrderManager(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                        DefaultOrderCategory.EARTH_ELEVATION_CATEGORY),
                myModuleStateManager);
        myFrameBufferCaptureManager = new FrameBufferCaptureManagerImpl();

        final ControlContext globeControls = getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        myMapManager.addControlListeners(globeControls);

        final SecurityOptionsProvider securityOptionsProvider = new SecurityOptionsProvider(mySecurityManager,
                myRegistryManager.getPreferencesRegistry());
        myRegistryManager.getUIRegistry().getOptionsRegistry().addOptionsProvider(securityOptionsProvider);

        final NetworkConfigurationOptionsProvider networkOptionsProvider = new NetworkConfigurationOptionsProvider(
                getSystemToolbox().getNetworkConfigurationManager(), myRegistryManager.getPreferencesRegistry());
        myRegistryManager.getUIRegistry().getOptionsRegistry().addOptionsProvider(networkOptionsProvider);

        myTimeManagerStateController = new TimeManagerStateController(myTimeManager, myAnimationManager);
        myAnimationManagerStateController = new AnimationManagerStateController(myAnimationManager, myTimeManager);

        myModuleStateManager.registerModuleStateController("Animation", myAnimationManagerStateController);
        myModuleStateManager.registerModuleStateController("Time", myTimeManagerStateController);

        LOGGER.info(StringUtilities.formatTimingMessage("Initialized toolbox in ", System.nanoTime() - start));
    }

    /** Close the toolbox. */
    public void close()
    {
        myRegistryManager.close();
        myMapManager.removeControlListeners();
    }

    /**
     * Finish bindings that need to happen after all the registries are
     * initialized.
     */
    public void finishBinding()
    {
        myRegistryManager.bindComponentRegistryToImporterRegistry();
    }

    @Override
    public AnimationManager getAnimationManager()
    {
        return myAnimationManager;
    }

    @Override
    public ControlRegistry getControlRegistry()
    {
        return myRegistryManager.getControlRegistry();
    }

    @Override
    public DataFilterRegistry getDataFilterRegistry()
    {
        return myRegistryManager.getDataFilterRegistry();
    }

    @Override
    public DataRegistry getDataRegistry()
    {
        return myRegistryManager.getDataRegistry();
    }

    @Override
    public GenericRegistry<Envoy> getEnvoyRegistry()
    {
        return myRegistryManager.getEnvoyRegistry();
    }

    @Override
    public EventManager getEventManager()
    {
        return myEventManager;
    }

    @Override
    public FrameBufferCaptureManager getFrameBufferCaptureManager()
    {
        return myFrameBufferCaptureManager;
    }

    @Override
    public GeometryRegistry getGeometryRegistry()
    {
        return myRegistryManager.getGeometryRegistry();
    }

    @Override
    public ImporterRegistry getImporterRegistry()
    {
        return myRegistryManager.getImporterRegistry();
    }

    @Override
    public MapManager getMapManager()
    {
        return myMapManager;
    }

    @Override
    public MetricsRegistry getMetricsRegistry()
    {
        return myRegistryManager.getMetricsRegistry();
    }

    @Override
    public ModuleStateManager getModuleStateManager()
    {
        return myModuleStateManager;
    }

    @Override
    public OrderManagerRegistry getOrderManagerRegistry()
    {
        return myRegistryManager.getOrderManagerRegistry();
    }

    @Override
    public PluginToolboxRegistry getPluginToolboxRegistry()
    {
        return myRegistryManager.getPluginToolboxRegistry();
    }

    @Override
    public PreferencesRegistry getPreferencesRegistry()
    {
        return myRegistryManager.getPreferencesRegistry();
    }

    @Override
    public SearchRegistry getSearchRegistry()
    {
        return myRegistryManager.getSearchRegistry();
    }

    @Override
    public SecurityManager getSecurityManager()
    {
        return mySecurityManager;
    }

    @Override
    public ServerProviderRegistry getServerProviderRegistry()
    {
        return myRegistryManager.getServerProviderRegistry();
    }

    @Override
    public SystemToolbox getSystemToolbox()
    {
        return mySystemToolbox;
    }

    @Override
    public TimeManager getTimeManager()
    {
        return myTimeManager;
    }

    @Override
    public GenericRegistry<Transformer> getTransformerRegistry()
    {
        return myRegistryManager.getTransformerRegistry();
    }

    @Override
    public UIRegistry getUIRegistry()
    {
        return myRegistryManager.getUIRegistry();
    }

    @Override
    public UnitsRegistry getUnitsRegistry()
    {
        return myRegistryManager.getUnitsRegistry();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.Toolbox#getStatisticsManager()
     */
    @Override
    public StatisticsManager getStatisticsManager()
    {
        return myStatisticsManager;
    }

    /**
     * Remove all objects from my registries that belong to a source.
     *
     * @param source The source object.
     */
    void removeObjectsForSource(Object source)
    {
        getEnvoyRegistry().removeObjectsForSource(source);
        getTransformerRegistry().removeObjectsForSource(source);
    }

    /**
     * Request an application restart.
     */
    abstract void requestRestart();

    /**
     * Installs the {@link UserInteractionSSLSocketFactory} to the system.
     *
     * @param supplier The main frame supplier.
     */
    private void installSSLSocketFactory(Supplier<? extends JFrame> supplier)
    {
        final SSLSocketFactory defaultFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        final UserInteractionSSLSocketFactory factory = new UserInteractionSSLSocketFactory(defaultFactory,
                myRegistryManager.getPreferencesRegistry(), mySecurityManager, supplier);
        HttpsURLConnection.setDefaultSSLSocketFactory(factory);
    }
}
