package io.opensphere.core.util.lang;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for checking thread-safety.
 */
public final class ThreadValidator
{
    /** Map of context to thread name. */
    private static volatile Map<Object, String> ourContextToThreadNameMap;

    /**
     * Throws an exception if the given context has been called from multiple
     * threads.
     *
     * @param context The context (class name, method name, field name, etc.)
     * @return {@code true}
     * @throws AssertionError If the context has been called from multiple
     *             threads.
     */
    @SuppressWarnings("PMD.NonThreadSafeSingleton")
    public static boolean isSingleThread(Object context) throws AssertionError
    {
        if (ourContextToThreadNameMap == null)
        {
            synchronized (ThreadValidator.class)
            {
                if (ourContextToThreadNameMap == null)
                {
                    ConcurrentHashMap<Object, String> concurrentHashMap = new ConcurrentHashMap<>();
                    concurrentHashMap.put(context, Thread.currentThread().getName());
                    ourContextToThreadNameMap = concurrentHashMap;
                    return true;
                }
            }
        }

        if (ourContextToThreadNameMap.containsKey(context))
        {
            String previousThread = ourContextToThreadNameMap.get(context);
            boolean singleThread = Thread.currentThread().getName().equals(previousThread);
            if (!singleThread)
            {
                throw new AssertionError(context + " has been called from multiple threads: " + previousThread + ", "
                        + Thread.currentThread().getName());
            }
        }
        else
        {
            ourContextToThreadNameMap.put(context, Thread.currentThread().getName());
        }

        return true;
    }

    /** Disallow instantiation. */
    private ThreadValidator()
    {
    }
}
