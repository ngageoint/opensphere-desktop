package io.opensphere.core.util.lang;

import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;

import net.jcip.annotations.ThreadSafe;

/**
 * Extension to {@link TaskCanceller} that will also register wrapped tasks with
 * a Phaser.
 */
@ThreadSafe
public class PhasedTaskCanceller extends TaskCanceller
{
    /** The phaser. */
    private final Phaser myPhaser;

    /**
     * Constructor.
     *
     * @param parent The parent canceller.
     * @param phaser The phaser.
     */
    public PhasedTaskCanceller(TaskCanceller parent, Phaser phaser)
    {
        super(parent);
        myPhaser = phaser;
    }

    @Override
    public <T> Callable<T> wrap(Callable<T> c)
    {
        myPhaser.register();
        return super.wrap((Callable<T>)() ->
        {
            try
            {
                return c.call();
            }
            finally
            {
                myPhaser.arriveAndDeregister();
            }
        });
    }
}
