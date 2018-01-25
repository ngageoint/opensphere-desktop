package io.opensphere.core.preferences;

import java.util.Set;

import io.opensphere.core.util.security.CipherFactory;

/**
 * Registry for application preferences.
 */
public interface PreferencesRegistry
{
    /**
     * Gets the {@link Preferences} set for the given class, if the set does not
     * exist it will be created. Note that the topic is the fully qualified
     * class name.
     *
     * @param aClass The class to use for a topic.
     * @return The {@link Preferences}.
     */
    Preferences getPreferences(Class<?> aClass);

    /**
     * Gets the {@link Preferences} set for the given topic, if the set does not
     * exist it will be created.
     *
     * @param topic The preferences topic.
     * @return The {@link Preferences}.
     */
    Preferences getPreferences(String topic);

    /**
     * Returns the current set of all topics.
     *
     * @return {@link Set} of {@link String} topic names.
     */
    Set<String> getTopics();

    /**
     * Returns true if the specified class topic exists, so creation can be
     * avoided if that is not the intent. Note that the topic is the fully
     * qualified class name.
     *
     * @param aClass The class to use for a topic.
     * @return {@code true} if the topic exists, {@code false} if not.
     */
    boolean hasTopic(Class<?> aClass);

    /**
     * Returns true if the specified topic exists, so creation can be avoided if
     * that is not the intent.
     *
     * @param topic - the topic to check for existence
     * @return true if the topic exists, false if not
     */
    boolean hasTopic(String topic);

    /**
     * Reset the preferences for the given topic to the defaults configured in
     * the installation. Note that the topic is the fully qualified class name.
     *
     * @param aClass The class to use for a topic.
     * @param source The source of the reset, to be passed to listeners.
     */
    void resetPreferences(Class<?> aClass, Object source);

    /**
     * Reset the preferences for the given topic to the defaults configured in
     * the installation.
     *
     * @param topic The preferences topic.
     * @param source The source of the reset, to be passed to listeners.
     */
    void resetPreferences(String topic, Object source);

    /**
     * Set the cipher factory for a preferences topic.
     *
     * @param aClass The class to use for a topic.
     * @param cipherFactory The cipher factory.
     */
    void setPreferencesCipherFactory(Class<?> aClass, CipherFactory cipherFactory);

    /**
     * Set the cipher factory for a preferences topic.
     *
     * @param topic The preferences topic.
     * @param cipherFactory The cipher factory.
     */
    void setPreferencesCipherFactory(String topic, CipherFactory cipherFactory);

    /**
     * Set if the preferences for a topic should be compressed on-disk.
     *
     * @param aClass The class to use for a topic.
     * @param flag {@code true} if the preferences should be compressed.
     */
    void setPreferencesCompression(Class<?> aClass, boolean flag);

    /**
     * Set if the preferences for a topic should be compressed on-disk.
     *
     * @param topic The preferences topic.
     * @param flag {@code true} if the preferences should be compressed.
     */
    void setPreferencesCompression(String topic, boolean flag);
}
