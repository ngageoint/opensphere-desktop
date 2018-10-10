package io.opensphere.core.appl;

import java.util.function.Predicate;

import javax.swing.JFrame;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.ControlRegistryImpl;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.control.ui.impl.UIRegistryImpl;
import io.opensphere.core.data.DataRegistryImpl;
import io.opensphere.core.datafilter.impl.DataFilterRegistryImpl;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.GeometryRegistryImpl;
import io.opensphere.core.hud.awt.HUDFrame;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.impl.ImporterRegistryImpl;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.metrics.impl.MetricsRegistryImpl;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.OrderManagerRegistryImpl;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.preferences.PreferencesRegistryImpl;
import io.opensphere.core.search.SearchRegistryImpl;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.server.impl.ServerProviderRegistryImpl;
import io.opensphere.core.util.registry.GenericRegistry;

/**
 * Manager for the code system registries.
 */
public class RegistryManagerImpl
{
    /** The control registry. */
    private final ControlRegistry myControlRegistry;

    /** The Data filter registry. */
    private final DataFilterRegistryImpl myDataFilterRegistry;

    /** The data registry. */
    private final DataRegistryImpl myDataRegistry;

    /** The envoy registry. */
    private final GenericRegistry<Envoy> myEnvoyRegistry;

    /** The geometry registry. */
    private final GeometryRegistry myGeometryRegistry;

    /** The importer registry. */
    private final ImporterRegistryImpl myImporterRegistry;

    /** The Metrics registry. */
    private final MetricsRegistryImpl myMetricsRegistry;

    /** The order manager registry. */
    private final OrderManagerRegistry myOrderManagerRegistry;

    /** The plugin toolbox Registry. */
    private final PluginToolboxRegistry myPluginToolboxRegistry;

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /** The search provider registry. */
    private final SearchRegistryImpl mySearchRegistry;

    /**
     * The server provider registry.
     */
    private final ServerProviderRegistry myServerProviderRegistry;

    /** The transformer registry. */
    private final GenericRegistry<Transformer> myTransformerRegistry;

    /** The UI registry. */
    private final UIRegistry myUIRegistry;

    /**
     * Subscriber that adds internal frames that implement the importer
     * interface to the importer registry.
     */
    private final GenericSubscriber<HUDFrame> myComponentRegistryToImporterBinding = (source, adds, removes) ->
    {
        Predicate<HUDFrame> importerPredicate = f1 -> f1 instanceof HUDJInternalFrame
                && ((HUDJInternalFrame)f1).getInternalFrame() instanceof FileOrURLImporter;
        adds.stream().filter(importerPredicate)
                .forEach(f2 -> myImporterRegistry.addImporter((FileOrURLImporter)((HUDJInternalFrame)f2).getInternalFrame()));
        removes.stream().filter(importerPredicate).forEach(
            f3 -> myImporterRegistry.removeImporter((FileOrURLImporter)((HUDJInternalFrame)f3).getInternalFrame()));
    };

    /** The units registry. */
    private final UnitsRegistry myUnitsRegistry;

    /**
     * Constructor.
     *
     * @param executorManager The executor manager.
     * @param cache A cache implementation, or <code>null</code> if there is
     *            none.
     * @param mainFrame The top level frame for the application.
     */
    public RegistryManagerImpl(ExecutorManager executorManager, Cache cache, JFrame mainFrame)
    {
        myPreferencesRegistry = new PreferencesRegistryImpl(executorManager.getPreferencesEventExecutor(),
                executorManager.getPreferencesPersistExecutor());
        myControlRegistry = new ControlRegistryImpl();
        myDataRegistry = cache == null ? null : new DataRegistryImpl(executorManager.createDataRegistryExecutor(), cache);
        myEnvoyRegistry = new GenericRegistry<>();
        myGeometryRegistry = new GeometryRegistryImpl(executorManager.getGeometryDataRetrieverExecutor());
        myTransformerRegistry = new GenericRegistry<>();
        myUIRegistry = new UIRegistryImpl(myControlRegistry, mainFrame, myPreferencesRegistry);
        myUnitsRegistry = new UnitsRegistryImpl(myPreferencesRegistry);
        myPluginToolboxRegistry = new PluginToolboxRegistryImpl();
        myMetricsRegistry = new MetricsRegistryImpl();
        myDataFilterRegistry = new DataFilterRegistryImpl(myPreferencesRegistry);
        mySearchRegistry = new SearchRegistryImpl();
        myImporterRegistry = new ImporterRegistryImpl();
        myOrderManagerRegistry = new OrderManagerRegistryImpl(myPreferencesRegistry);
        myServerProviderRegistry = new ServerProviderRegistryImpl();
    }

    /**
     * Bind the UI component registry to the importer registry such that when UI
     * components are added that can support importing, they will also be added
     * to the importer registry. This must be done after the UI component
     * registry is initialized.
     */
    public void bindComponentRegistryToImporterRegistry()
    {
        myUIRegistry.getComponentRegistry().addSubscriber(myComponentRegistryToImporterBinding);
    }

    /**
     * Close the registries.
     */
    public void close()
    {
        myDataRegistry.close();
    }

    /**
     * Get the control registry.
     *
     * @return The control registry.
     */
    public ControlRegistry getControlRegistry()
    {
        return myControlRegistry;
    }

    /**
     * Get the data filter registry.
     *
     * @return The data filter registry.
     */
    public DataFilterRegistryImpl getDataFilterRegistry()
    {
        return myDataFilterRegistry;
    }

    /**
     * Get the data registry.
     *
     * @return The data registry.
     */
    public DataRegistryImpl getDataRegistry()
    {
        return myDataRegistry;
    }

    /**
     * Get the envoy registry.
     *
     * @return The envoy registry.
     */
    public GenericRegistry<Envoy> getEnvoyRegistry()
    {
        return myEnvoyRegistry;
    }

    /**
     * Get the geometry registry.
     *
     * @return The geometry registry.
     */
    public GeometryRegistry getGeometryRegistry()
    {
        return myGeometryRegistry;
    }

    /**
     * Get the importer registry.
     *
     * @return The importer registry.
     */
    public ImporterRegistryImpl getImporterRegistry()
    {
        return myImporterRegistry;
    }

    /**
     * Get the metrics registry.
     *
     * @return The metrics registry.
     */
    public MetricsRegistryImpl getMetricsRegistry()
    {
        return myMetricsRegistry;
    }

    /**
     * Gets the registry for order managers.
     *
     * @return the order manager registry.
     */
    public OrderManagerRegistry getOrderManagerRegistry()
    {
        return myOrderManagerRegistry;
    }

    /**
     * Get the plugin toolbox registry.
     *
     * @return The plugin toolbox registry.
     */
    public PluginToolboxRegistry getPluginToolboxRegistry()
    {
        return myPluginToolboxRegistry;
    }

    /**
     * Get the preferences registry.
     *
     * @return The preferences registry.
     */
    public PreferencesRegistry getPreferencesRegistry()
    {
        return myPreferencesRegistry;
    }

    /**
     * Get the search registry.
     *
     * @return The search registry.
     */
    public SearchRegistryImpl getSearchRegistry()
    {
        return mySearchRegistry;
    }

    /**
     * Get the server provider registry.
     *
     * @return The server provider registry.
     */
    public ServerProviderRegistry getServerProviderRegistry()
    {
        return myServerProviderRegistry;
    }

    /**
     * Get the transformer registry.
     *
     * @return The transformer registry.
     */
    public GenericRegistry<Transformer> getTransformerRegistry()
    {
        return myTransformerRegistry;
    }

    /**
     * Get the UI registry.
     *
     * @return The UI registry.
     */
    public UIRegistry getUIRegistry()
    {
        return myUIRegistry;
    }

    /**
     * Get the units registry.
     *
     * @return The units registry.
     */
    public UnitsRegistry getUnitsRegistry()
    {
        return myUnitsRegistry;
    }
}
