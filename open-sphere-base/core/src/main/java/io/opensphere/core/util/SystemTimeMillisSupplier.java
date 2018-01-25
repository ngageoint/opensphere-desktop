package io.opensphere.core.util;

/**
 * Supplier for the current system time as returned by
 * {@link System#currentTimeMillis}.
 */
public class SystemTimeMillisSupplier implements LongSupplier
{
    /** Singleton. */
    public static final SystemTimeMillisSupplier INSTANCE = new SystemTimeMillisSupplier();

    @Override
    public long get()
    {
        return System.currentTimeMillis();
    }
}
