package io.opensphere.core.util;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Composite Service.
 */
@ThreadSafe
public class CompositeService implements Service
{
    /** The list of services. */
    @GuardedBy("myServices")
    private final List<Service> myServices;

    /**
     * Constructor.
     */
    public CompositeService()
    {
        this(10);
    }

    /**
     * Constructor.
     *
     * @param size the expected number of services (for memory savings)
     */
    public CompositeService(int size)
    {
        myServices = new ArrayList<>(size);
    }

    @Override
    public void open()
    {
        synchronized (myServices)
        {
            for (Service service : myServices)
            {
                service.open();
            }
        }
    }

    @Override
    public void close()
    {
        synchronized (myServices)
        {
            for (int i = myServices.size() - 1; i >= 0; --i)
            {
                myServices.get(i).close();
            }
        }
    }

    /**
     * Adds a service.
     *
     * @param <T> the service class type
     * @param service the service
     * @return The service
     */
    public final <T extends Service> T addService(T service)
    {
        synchronized (myServices)
        {
            myServices.add(service);
        }
        return service;
    }

    /**
     * Removes a service.
     *
     * @param service the service
     */
    public final void removeService(Service service)
    {
        synchronized (myServices)
        {
            myServices.remove(service);
        }
    }
}
