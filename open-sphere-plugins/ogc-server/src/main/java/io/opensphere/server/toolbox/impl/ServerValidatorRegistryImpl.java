package io.opensphere.server.toolbox.impl;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.opensphere.core.util.collections.New;
import io.opensphere.server.services.OGCServiceValidator;
import io.opensphere.server.toolbox.ServerValidatorRegistry;

/**
 * Default implementation of {@link ServerValidatorRegistry}.
 */
public class ServerValidatorRegistryImpl implements ServerValidatorRegistry
{
    /** The validator map. */
    private final Map<String, OGCServiceValidator> myValidatorMap = New.map();

    /** Change lock that controls access to the Validator Map. */
    private final ReadWriteLock myChangeLock = new ReentrantReadWriteLock();

    @Override
    public void register(OGCServiceValidator validator)
    {
        try
        {
            myChangeLock.writeLock().lock();
            myValidatorMap.put(validator.getService(), validator);
        }
        finally
        {
            myChangeLock.writeLock().unlock();
        }
    }

    @Override
    public OGCServiceValidator retrieve(String service)
    {
        try
        {
            myChangeLock.readLock().lock();
            return myValidatorMap.get(service);
        }
        finally
        {
            myChangeLock.readLock().unlock();
        }
    }
}
