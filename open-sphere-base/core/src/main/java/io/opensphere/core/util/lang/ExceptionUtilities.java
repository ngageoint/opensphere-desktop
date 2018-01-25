package io.opensphere.core.util.lang;

/**
 * Utilities for {@link Exception}s and the like.
 */
public final class ExceptionUtilities
{
    /**
     * Get the root cause of an exception by traversing its cause tree.
     *
     * @param t The exception.
     * @return The root cause.
     */
    public static Throwable getRootCause(Throwable t)
    {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause)
        {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Determine if the provided exception has the given type or any of its
     * causes has the given type.
     *
     * @param t The exception.
     * @param type The type to search for.
     * @return {@code true} if the type is found.
     */
    public static boolean hasCause(Throwable t, Class<? extends Throwable> type)
    {
        Throwable cause = t;

        do
        {
            if (type.isInstance(cause))
            {
                return true;
            }
        }
        while (cause.getCause() != cause && (cause = cause.getCause()) != null);
        return false;
    }

    /** Disallow instantiation. */
    private ExceptionUtilities()
    {
    }
}
