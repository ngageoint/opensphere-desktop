package io.opensphere.core.util;

/**
 * A general purpose interface for a service.
 * <p>
 * Worthwhile goals for a service might be:
 * <ul>
 * <li>The {@link #close()} method should undo everything done in the
 * {@link #open()} method, thus putting the service back in the state it was in
 * immediately after construction.</li>
 * <li>Idempotence for the {@link #open()} and {@link #close()} methods.</li>
 * <li>Thread-safety if necessary.</li>
 * </ul>
 */
public interface Service extends AutoCloseable
{
    /**
     * Opens the service.
     */
    void open();

    /**
     * Closes the service, relinquishing any underlying resources.
     */
    @Override
    void close();
}
