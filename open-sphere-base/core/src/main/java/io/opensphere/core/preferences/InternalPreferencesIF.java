package io.opensphere.core.preferences;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.util.security.CipherFactory;

/**
 * Internal interface that defines additional methods not for public
 * consumption.
 */
public interface InternalPreferencesIF extends Preferences
{
    /**
     * Merge another set of preferences into me. None of my existing values will
     * be changed; only new preferences will be added.
     *
     * @param other The other set of preferences.
     */
    void merge(Preferences other);

    /**
     * Replace my preferences with the input, generating events for my
     * subscribers.
     *
     * @param prefs The new preferences.
     * @param persist Indicates if the preferences should be persisted.
     * @param source The originator of this call.
     */
    void replacePreferences(InternalPreferencesIF prefs, boolean persist, Object source);

    /**
     * Set the factory for creating ciphers to encrypt the preferences.
     *
     * @param cipherFactory The cipher factory.
     */
    void setCipherFactory(CipherFactory cipherFactory);

    /**
     * Set if the preferences should be compressed on-disk.
     *
     * @param flag {@code true} if the preferences should be compressed.
     */
    void setCompressed(boolean flag);

    /**
     * Set the executor for sending events.
     *
     * @param eventDispatchExecutor The executor.
     * @throws IllegalArgumentException If the executor is {@code null}.
     */
    void setEventDispatchExecutor(Executor eventDispatchExecutor);

    /**
     * Set the preferences persistence manager.
     *
     * @param persistenceManager The persistence manager.
     */
    void setPersistenceManager(PreferencesPersistenceManager persistenceManager);

    /**
     * Set the executor for persisting preferences.
     *
     * @param persistExecutor The executor.
     * @throws IllegalArgumentException If the executor is {@code null}.
     */
    void setPersistExecutor(ScheduledExecutorService persistExecutor);

    /**
     * Access the preferences values.
     *
     * @return The preferences values.
     */
    Collection<? extends Preference<?>> values();
}
