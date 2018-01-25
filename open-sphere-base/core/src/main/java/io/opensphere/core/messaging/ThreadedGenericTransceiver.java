package io.opensphere.core.messaging;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;

/**
 * A publisher/subscriber that receives messages and then publishes them to
 * subscribers using an {@link Executor}.
 *
 * @param <E> The type of objects being transceived.
 */
public class ThreadedGenericTransceiver<E> implements GenericPublisher<E>, GenericSubscriber<E>
{
    /** Executor used to publish messages. */
    private final ExecutorService myExecutor;

    /** Publisher used to publish messages. */
    private final DefaultGenericPublisher<E> myPublisher = new DefaultGenericPublisher<>();

    /**
     * Create the transceiver with a fixed number of threads and a thread
     * factory. The factory is used to create the threads used for publishing
     * messages.
     *
     * @param nThreads The number of threads to be used for the publisher.
     * @param factory A thread factory.
     */
    public ThreadedGenericTransceiver(int nThreads, ThreadFactory factory)
    {
        myExecutor = new FixedThreadPoolExecutor(nThreads, factory);
    }

    /**
     * Create the transceiver with a thread factory. The factory is used to
     * create the threads used for publishing messages.
     *
     * @param factory A thread factory.
     */
    public ThreadedGenericTransceiver(ThreadFactory factory)
    {
        final long keepAliveTime = 60L;
        myExecutor = new ThreadPoolExecutor(2, Integer.MAX_VALUE, keepAliveTime, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), factory);
    }

    @Override
    public void addSubscriber(GenericSubscriber<E> subscriber)
    {
        myPublisher.addSubscriber(subscriber);
    }

    @Override
    public void receiveObjects(Object source, Collection<? extends E> adds, Collection<? extends E> removes)
    {
        myPublisher.sendObjects(source, adds, removes, myExecutor);
    }

    @Override
    public void removeSubscriber(GenericSubscriber<E> subscriber)
    {
        myPublisher.removeSubscriber(subscriber);
    }
}
