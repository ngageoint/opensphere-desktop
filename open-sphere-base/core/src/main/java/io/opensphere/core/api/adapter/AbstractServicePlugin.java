package io.opensphere.core.api.adapter;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.util.Service;

/**
 * Base class for plugins that want to use
 * {@link io.opensphere.core.util.Service}s.
 */
public class AbstractServicePlugin extends PluginAdapter
{
    /** The service manager. */
    private volatile EventListenerService myServiceManager;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myServiceManager = new EventListenerService(toolbox.getEventManager());
        for (Service service : getServices(plugindata, toolbox))
        {
            myServiceManager.addService(service);
        }
        startServices();
    }

    @Override
    public void close()
    {
        System.out.println(this.getClass().getName());
        myServiceManager.close();
    }

    /**
     * Gets the services provided by this plugin. This is intended to be
     * overridden.
     *
     * @param plugindata the plugin data
     * @param toolbox the toolbox
     * @return the services
     */
    protected Collection<Service> getServices(PluginLoaderData plugindata, Toolbox toolbox)
    {
        return Collections.emptyList();
    }

    /**
     * Adds a service.
     *
     * @param <T> the service class type
     * @param service the service
     * @return The service
     */
    protected <T extends Service> T addService(T service)
    {
        return myServiceManager.addService(service);
    }

    /**
     * Starts the services.
     */
    protected void startServices()
    {
        myServiceManager.open();
    }
}
