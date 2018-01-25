package io.opensphere.core.util.ref;

/**
 * Wrapper for a {@link java.lang.ref.ReferenceQueue} that works with
 * {@link Reference}s.
 *
 * @param <T> The type of object in the queue.
 */
public class ReferenceQueue<T>
{
    /** The wrapped reference queue. */
    private final java.lang.ref.ReferenceQueue<T> myReferenceQueue = new java.lang.ref.ReferenceQueue<T>();

    /**
     * Polls this queue to see if a reference object is available. If one is
     * available without further delay then it is removed from the queue and
     * returned. Otherwise this method immediately returns <tt>null</tt>.
     *
     * @return A reference object, if one was immediately available, otherwise
     *         <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public Reference<? extends T> poll()
    {
        java.lang.ref.Reference<? extends T> ref = myReferenceQueue.poll();
        return ref == null ? null : ((ReferenceProvider<T>)ref).getReference();
    }

    /**
     * Removes the next reference object in this queue, blocking until one
     * becomes available.
     *
     * @return A reference object, blocking until one becomes available
     * @throws InterruptedException If the wait is interrupted
     */
    public Reference<? extends T> remove() throws InterruptedException
    {
        return remove(0);
    }

    /**
     * Removes the next reference object in this queue, blocking until either
     * one becomes available or the given timeout period expires.
     *
     * <p>
     * This method does not offer real-time guarantees: It schedules the timeout
     * as if by invoking the {@link Object#wait(long)} method.
     *
     * @param timeout If positive, block for up to <code>timeout</code>
     *            milliseconds while waiting for a reference to be added to this
     *            queue. If zero, block indefinitely.
     *
     * @return A reference object, if one was available within the specified
     *         timeout period, otherwise <code>null</code>
     *
     * @throws IllegalArgumentException If the value of the timeout argument is
     *             negative
     *
     * @throws InterruptedException If the timeout wait is interrupted
     */
    @SuppressWarnings("unchecked")
    public Reference<? extends T> remove(long timeout) throws IllegalArgumentException, InterruptedException
    {
        java.lang.ref.Reference<? extends T> removed = myReferenceQueue.remove(timeout);
        return removed == null ? null : ((ReferenceProvider<T>)removed).getReference();
    }

    /**
     * Accessor for the wrapped reference queue.
     *
     * @return The wrapped reference queue.
     */
    protected java.lang.ref.ReferenceQueue<T> getReferenceQueue()
    {
        return myReferenceQueue;
    }
}
