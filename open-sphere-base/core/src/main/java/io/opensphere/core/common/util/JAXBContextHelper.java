package io.opensphere.core.common.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBContextHelper
{

    private static ReentrantLock createContextLock = new ReentrantLock();

    private static HashMap<String, JAXBContext> contextMap = new HashMap<>();

    /**
     * Creates a new {@link JAXBContext} in a thread safe way.
     *
     * @param target the class for which to make a context
     * @return the {@link JAXBContext}
     * @throws JAXBException
     */
    private static JAXBContext createContext(Class<?> target) throws JAXBException
    {
        JAXBContext ctx = null;
        createContextLock.lock();
        try
        {
            ctx = JAXBContext.newInstance(target);
        }
        finally
        {
            createContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Creates a new {@link JAXBContext} in a thread safe way.
     *
     * @param target the class for which to make a context
     * @return the {@link JAXBContext}
     * @throws JAXBException
     */
    private static JAXBContext createContext(String target) throws JAXBException
    {
        JAXBContext ctx = null;
        createContextLock.lock();
        try
        {
            ctx = JAXBContext.newInstance(target);
        }
        finally
        {
            createContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Creates a multi class context, does not play in the context cache but is
     * created within the context creation lock so there will be no threading
     * issues
     *
     * Changed: 6/16/2011: have added code to put these multiContext items in
     * the cache. Then methodology here is to sort array of classes to be bound,
     * and then use the sorted array to generate and comma separated
     * {@link String} to be used as the key to the {@link HashMap}.
     *
     * @param classesToBeBound
     * @return the {@link JAXBContext}
     * @throws JAXBException
     */
    public static JAXBContext createMultiContext(Class<?>... classesToBeBound) throws JAXBException
    {
        JAXBContext ctx = null;
        createContextLock.lock();
        try
        {
            String[] classNames = new String[classesToBeBound.length];
            StringBuffer sb = new StringBuffer();

            ctx = JAXBContext.newInstance(classesToBeBound);

            for (int i = 0; i < classesToBeBound.length; i++)
            {
                classNames[i] = classesToBeBound[i].getName();
            }

            Arrays.sort(classNames);
            sb.append(classNames[0]);
            for (int i = 1; i < classNames.length; i++)
            {
                sb.append(",");
                sb.append(classNames[i]);
            }
            contextMap.put(sb.toString(), ctx);

        }
        finally
        {
            createContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Gets or creates a JAXBContext and caches it so it does not have to be
     * created twice but creates/retrieves all contexts in a thread safe way
     *
     * @param boundClasses
     * @return the {@link JAXBContext}
     * @throws JAXBException
     */
    public static JAXBContext getMultiCachedContext(final Class<?>... boundClasses) throws JAXBException
    {
        JAXBContext ctx;
        createContextLock.lock();

        String[] classNames = new String[boundClasses.length];

        for (int i = 0; i < boundClasses.length; i++)
        {
            classNames[i] = boundClasses[i].getName();
        }

        Arrays.sort(classNames);
        StringBuffer sb = new StringBuffer();

        sb.append(classNames[0]);
        for (int i = 1; i < classNames.length; i++)
        {
            sb.append(",");
            sb.append(classNames[i]);
        }

        try
        {
            ctx = contextMap.get(sb.toString());
            if (ctx == null)
            {
                ctx = createMultiContext(boundClasses);
            }
        }
        finally
        {
            createContextLock.unlock();
        }
        return ctx;
    }

    /**
     * Gets or creates a JAXBContext and caches it so it does not have to be
     * created twice but creates/retrieves all contexts in a thread safe way
     *
     * @param target - the target class for which to create a context.
     * @return the {@link JAXBContext}
     * @throws JAXBException
     */
    public static JAXBContext getCachedContext(Class<?> target) throws JAXBException
    {
        JAXBContext ctx = null;
        createContextLock.lock();
        try
        {
            ctx = contextMap.get(target.getName());

            if (ctx == null)
            {
                ctx = createContext(target);
                contextMap.put(target.getName(), ctx);
            }
        }
        finally
        {
            createContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Gets or creates a JAXBContext and caches it so it does not have to be
     * created twice but creates/retrieves all contexts in a thread safe way
     *
     * @param target - the target class for which to create a context.
     * @return the {@link JAXBContext}
     * @throws JAXBException
     */
    public static JAXBContext getCachedContext(String target) throws JAXBException
    {
        JAXBContext ctx = null;
        createContextLock.lock();
        try
        {
            ctx = contextMap.get(target);

            if (ctx == null)
            {
                ctx = createContext(target);
                contextMap.put(target, ctx);
            }
        }
        finally
        {
            createContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Removes a cached context from the internal cache
     *
     * @param target - the target class to remove
     */
    public static void removeCachedContext(Class<?> target)
    {
        createContextLock.lock();
        try
        {
            contextMap.remove(target.getName());
        }
        finally
        {
            createContextLock.unlock();
        }
    }

    /**
     * Clears all cached contexts
     */
    public static void clearCachedContexts()
    {
        createContextLock.lock();
        try
        {
            contextMap.clear();
        }
        finally
        {
            createContextLock.unlock();
        }
    }
}
