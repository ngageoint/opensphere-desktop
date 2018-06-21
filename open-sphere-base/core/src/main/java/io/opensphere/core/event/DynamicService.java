package io.opensphere.core.event;

import java.util.List;
import java.util.Map;

import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Service for managing services created dynamically (at run time).
 *
 * @param <K> the service key type
 * @param <S> the service type
 */
@ThreadSafe
public class DynamicService<K, S extends Service> extends EventListenerService
{
    /** The map of dynamic services. */
    @GuardedBy("myServiceMap")
    private final Map<K, S> myServiceMap = New.map();

    /**
     * Constructor.
     *
     * @param eventManager The event manager.
     */
    public DynamicService(EventManager eventManager)
    {
        super(eventManager);
    }

    /**
     * Constructor.
     *
     * @param eventManager The event manager.
     * @param size the expected number of static services (for memory savings)
     */
    public DynamicService(EventManager eventManager, int size)
    {
        super(eventManager, size);
    }

    @Override
    public void close()
    {
        clearDynamicServices();
        super.close();
    }

    /**
     * Gets a dynamic service.
     *
     * @param key the service key
     * @return the service
     */
    public S getDynamicService(K key)
    {
        synchronized (myServiceMap)
        {
            return myServiceMap.get(key);
        }
    }

    /**
     * Adds a dynamic service.
     *
     * @param key the service key
     * @param service the service
     */
    public void addDynamicService(K key, S service)
    {
        service.open();
        synchronized (myServiceMap)
        {
            myServiceMap.put(key, service);
        }
    }

    /**
     * Removes a dynamic service.
     *
     * @param key the service key
     * @return the service that was removed, or null
     */
    public S removeDynamicService(K key)
    {
        S service;
        synchronized (myServiceMap)
        {
            service = myServiceMap.remove(key);
        }
        if (service != null)
        {
            service.close();
        }
        return service;
    }

    /**
     * Removes all dynamic services.
     */
    public void clearDynamicServices()
    {
        List<S> services;
        synchronized (myServiceMap)
        {
            services = New.list(myServiceMap.values());
            myServiceMap.clear();
        }
        for (S service : services)
        {
            service.close();
        }
    }
}
