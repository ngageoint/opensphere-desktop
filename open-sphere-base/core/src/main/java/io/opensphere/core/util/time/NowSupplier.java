package io.opensphere.core.util.time;

import java.util.function.Supplier;

import io.opensphere.core.model.time.TimeInstant;

/**
 * Supplier of now.
 */
public class NowSupplier implements Supplier<TimeInstant>
{
    /** Reusable instance of NowSupplier. */
    public static final Supplier<TimeInstant> INSTANCE = new NowSupplier();

    @Override
    public TimeInstant get()
    {
        return TimeInstant.get();
    }
}
