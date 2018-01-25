package io.opensphere.hud.dashboard;

import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.metrics.MetricsProvider;
import io.opensphere.core.metrics.MetricsRegistry.MetricsRegistryListener;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.concurrent.EventQueueExecutor;

/**
 * The Class DashboardController.
 */
public class DashboardController implements MetricsRegistryListener
{
    /** The Change support. */
    private final WeakChangeSupport<DashboardControllerListener> myChangeSupport;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new dashboard controller.
     *
     * @param tb the tb
     */
    public DashboardController(Toolbox tb)
    {
        myToolbox = tb;
        myToolbox.getMetricsRegistry().addMetricsRegistryListener(this);
        myChangeSupport = new WeakChangeSupport<>();
    }

    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    public void addListener(DashboardControllerListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Accessor for the toolbox.
     *
     * @return The toolbox.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public void metricsProviderAdded(final MetricsProvider provider)
    {
        myChangeSupport.notifyListeners(new Callback<DashboardController.DashboardControllerListener>()
        {
            @Override
            public void notify(DashboardControllerListener listener)
            {
                listener.providerAdded(provider);
            }
        }, new EventQueueExecutor());
    }

    @Override
    public void metricsProviderRemoved(final MetricsProvider provider)
    {
        myChangeSupport.notifyListeners(new Callback<DashboardController.DashboardControllerListener>()
        {
            @Override
            public void notify(DashboardControllerListener listener)
            {
                listener.providerRemoved(provider);
            }
        }, new EventQueueExecutor());
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    public void removeListener(DashboardControllerListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Request sync.
     */
    public final void requestSync()
    {
        final Set<MetricsProvider> providerSet = myToolbox.getMetricsRegistry().getProviders();
        myChangeSupport.notifyListeners(new Callback<DashboardController.DashboardControllerListener>()
        {
            @Override
            public void notify(DashboardControllerListener listener)
            {
                listener.providersSync(providerSet);
            }
        }, new EventQueueExecutor());
    }

    /**
     * DashboardControllerListener.
     */
    public interface DashboardControllerListener
    {
        /**
         * Provider added.
         *
         * @param provider the provider
         */
        void providerAdded(MetricsProvider provider);

        /**
         * Provider removed.
         *
         * @param provider the provider
         */
        void providerRemoved(MetricsProvider provider);

        /**
         * Providers sync.
         *
         * @param providers the providers
         */
        void providersSync(Set<MetricsProvider> providers);
    }
}
