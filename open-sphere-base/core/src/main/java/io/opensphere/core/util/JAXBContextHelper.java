package io.opensphere.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import io.opensphere.core.util.ref.SoftReference;

/**
 * Helper class that creates and caches JAXB contexts.
 */
public final class JAXBContextHelper
{
    /** Map of cached contexts. */
    private static Map<String, SoftReference<JAXBContext>> ourContextMap = new HashMap<>();

    /** Lock for creating contexts. */
    private static ReentrantLock ourCreateContextLock = new ReentrantLock();

    /**
     * Clears all cached contexts.
     */
    public static void clearCachedContexts()
    {
        ourCreateContextLock.lock();
        try
        {
            ourContextMap.clear();
        }
        finally
        {
            ourCreateContextLock.unlock();
        }
    }

    /**
     * Gets or creates a JAXBContext and caches it so it does not have to be
     * created twice but creates/retrieves all contexts in a thread safe way.
     *
     * @param classes The classes for which to make a context.
     * @return the {@link JAXBContext}
     * @throws JAXBException If an error occurred obtaining the JAXBContext.
     */
    public static JAXBContext getCachedContext(Class<?>... classes) throws JAXBException
    {
        JAXBContext ctx;
        ourCreateContextLock.lock();
        try
        {
            String key;
            if (classes.length == 1)
            {
                key = classes[0].getName();
            }
            else if (classes.length == 0)
            {
                key = "";
            }
            else
            {
                String[] names = new String[classes.length];
                for (int i = 0; i < classes.length; ++i)
                {
                    names[i] = classes[i].getName();
                }
                Arrays.sort(names);
                StringBuilder sb = new StringBuilder(32);
                for (String name : names)
                {
                    sb.append(name).append(',');
                }
                sb.setLength(sb.length() - 1);
                key = sb.toString();
            }

            SoftReference<JAXBContext> ref = ourContextMap.get(key);

            ctx = ref == null ? null : ref.get();
            if (ctx == null)
            {
                ctx = createContext(classes);
                ref = new SoftReference<>(ctx);
                ourContextMap.put(key, ref);
            }
        }
        finally
        {
            ourCreateContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Gets or creates a JAXBContext and caches it so it does not have to be
     * created twice but creates/retrieves all contexts in a thread safe way.
     *
     * @param classes The classes for which to make a context.
     * @return the {@link JAXBContext}
     * @throws JAXBException If an error occurred obtaining the JAXBContext.
     */
    public static JAXBContext getCachedContext(Collection<? extends Class<?>> classes) throws JAXBException
    {
        return getCachedContext(classes.toArray(new Class<?>[classes.size()]));
    }

    /**
     * Gets or creates a JAXBContext from one or more packages. The packages
     * must contain "jaxb.index" files and/or object factories.
     *
     * @param packages The packages.
     * @return The context.
     * @throws JAXBException If an error occurred obtaining the JAXBContext.
     * @see JAXBContext#newInstance(String, ClassLoader)
     */
    public static JAXBContext getCachedContext(Package... packages) throws JAXBException
    {
        String[] packageNames = new String[packages.length];
        for (int i = 0; i < packages.length;)
        {
            packageNames[i] = packages[i++].getName();
        }

        Arrays.sort(packageNames);

        StringBuilder sb = new StringBuilder(32);
        for (String packageName : packageNames)
        {
            sb.append(packageName).append(':');
        }
        sb.setLength(sb.length() - 1);

        return getCachedContext(sb.toString());
    }

    /**
     * Gets or creates a JAXBContext and caches it so it does not have to be
     * created twice but creates/retrieves all contexts in a thread safe way.
     *
     * @param contextPath Colon-separated list of package names.
     * @return the {@link JAXBContext}
     * @throws JAXBException If an error occurred obtaining the JAXBContext.
     * @see JAXBContext#newInstance(String, ClassLoader)
     */
    public static JAXBContext getCachedContext(String contextPath) throws JAXBException
    {
        JAXBContext ctx;
        ourCreateContextLock.lock();
        try
        {
            SoftReference<JAXBContext> ref;
            ref = ourContextMap.get(contextPath);

            ctx = ref == null ? null : ref.get();
            if (ctx == null)
            {
                ctx = createContext(contextPath);
                ref = new SoftReference<>(ctx);
                ourContextMap.put(contextPath, ref);
            }
        }
        finally
        {
            ourCreateContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Removes a cached context from the internal cache.
     *
     * @param target - the target class to remove
     */
    public static void removeCachedContext(Class<?> target)
    {
        ourCreateContextLock.lock();
        try
        {
            ourContextMap.remove(target.getName());
        }
        finally
        {
            ourCreateContextLock.unlock();
        }
    }

    /**
     * Creates a new {@link JAXBContext} in a thread safe way.
     *
     * @param classes The classes for which to make a context.
     * @return the {@link JAXBContext}
     * @throws JAXBException If an error occurred obtaining the JAXBContext.
     */
    private static JAXBContext createContext(Class<?>[] classes) throws JAXBException
    {
        JAXBContext ctx;
        ourCreateContextLock.lock();
        try
        {
            ctx = JAXBContext.newInstance(classes);
        }
        finally
        {
            ourCreateContextLock.unlock();
        }

        return ctx;
    }

    /**
     * Creates a new {@link JAXBContext} in a thread safe way.
     *
     * @param contextPath Colon-separated list of package names.
     * @return the {@link JAXBContext}
     * @throws JAXBException If an error occurred obtaining the JAXBContext.
     * @see JAXBContext#newInstance(String, ClassLoader)
     */
    private static JAXBContext createContext(String contextPath) throws JAXBException
    {
        JAXBContext ctx;
        ourCreateContextLock.lock();
        try
        {
            ctx = JAXBContext.newInstance(contextPath);
        }
        finally
        {
            ourCreateContextLock.unlock();
        }

        return ctx;
    }

    /** Disallow instantiation. */
    private JAXBContextHelper()
    {
    }
}
