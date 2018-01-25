package io.opensphere.core.preferences;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import io.opensphere.core.util.security.CipherFactory;

/**
 * Helper to persist the preferences.
 */
public interface PreferencesPersistenceManager
{
    /**
     * Delete the preferences set. This is only possible if this persistence
     * manager supports saving.
     *
     * @param topic The preferences topic.
     *
     * @throws UnsupportedOperationException If {@link #supportsSave()} returns
     *             {@code false}.
     */
    void delete(String topic);

    /**
     * Retrieves a set of preferences from the store. If the store does not
     * exist or if the file cannot be accessed, return {@code null}.
     *
     * @param topic The preferences topic.
     * @param cipherFactory The optional cipher factory.
     * @param compressed {@code true} if the preferences are compressed.
     * @return The loaded preferences or {@code null}.
     */
    InternalPreferencesIF load(String topic, CipherFactory cipherFactory, boolean compressed);

    /**
     * Persist the preferences set.
     *
     * @param preferences The preferences to be persisted.
     * @param cipherFactory The optional cipher factory.
     * @param compressed {@code true} if the preferences should be compressed.
     *
     * @throws IOException If the preferences cannot be saved due to an IO
     *             error.
     * @throws JAXBException If there is a JAXB error.
     * @throws UnsupportedOperationException If {@link #supportsSave()} returns
     *             {@code false}.
     */
    void save(Preferences preferences, CipherFactory cipherFactory, boolean compressed) throws IOException, JAXBException;

    /**
     * Get if this persistence manager supports compression.
     *
     * @return {@code true} if compression is supported.
     */
    boolean supportsCompression();

    /**
     * Get if this persistence manager supports encryption.
     *
     * @return {@code true} if encryption is supported.
     */
    boolean supportsEncryption();

    /**
     * Get if this persistence manager supports saving preferences.
     *
     * @return {@code true} if save is supported.
     */
    boolean supportsSave();
}
