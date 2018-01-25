package io.opensphere.server.util;

import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * The Class ServerConstants that defines common constants used in the OGC
 * Server Plugin.
 */
public final class ServerConstants
{
    // Constants that pertain to storing/retrieving preferences

    // Other Constants
    /** The Constant LAYER_SEPARATOR. */
    public static final String LAYERNAME_SEPARATOR = "!!";

    /** The Constant OGC_SERVER_OPTIONS_PROVIDER_MAIN_TOPIC. */
    public static final String OGC_SERVER_OPTIONS_PROVIDER_MAIN_TOPIC = "Servers";

    /** The DEFAULT_SERVER_READ_TIMEOUT. */
    public static final int DEFAULT_SERVER_READ_TIMEOUT = 180000;

    /** The DEFAULT_SERVER_CONNECT_TIMEOUT. */
    public static final int DEFAULT_SERVER_CONNECT_TIMEOUT = 180000;

    /** The DEFAULT_SERVER_ACTIVATE_TIMEOUT. */
    public static final int DEFAULT_SERVER_ACTIVATE_TIMEOUT = 180000;

    /** The Constant DEFAULT_SERVER_READ_TIMEOUT_PREFERENCE_KEY. */
    public static final String DEFAULT_SERVER_ACTIVATION_TIMEOUT_PREFERENCE_KEY = "server.activation.timeout.milliseconds";

    /** The Constant DEFAULT_SERVER_READ_TIMEOUT_PREFERENCE_KEY. */
    public static final String DEFAULT_SERVER_READ_TIMEOUT_PREFERENCE_KEY = "server.read.timeout.milliseconds";

    /** The Constant DEFAULT_SERVER_CONNECT_TIMEOUT_PREFERENCE_KEY. */
    public static final String DEFAULT_SERVER_CONNECT_TIMEOUT_PREFERENCE_KEY = "server.connect.timeout.milliseconds";

    /**
     * The default permalink url.
     */
    public static final String DEFAULT_PERMALINK_URL = "/file-store/v1";

    /**
     * Gets the default server activate timeout from preferences.
     *
     * @param prefsRegistry The system preferences registry.
     * @return the default server activate timeout from preferences
     */
    public static int getDefaultServerActivateTimeoutFromPrefs(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(ServerConstants.class).getInt(
                ServerConstants.DEFAULT_SERVER_ACTIVATION_TIMEOUT_PREFERENCE_KEY,
                ServerConstants.DEFAULT_SERVER_ACTIVATE_TIMEOUT);
    }

    /**
     * Gets the default server connect timeout from preferences.
     *
     * @param prefsRegistry The system preferences registry.
     * @return the default server connect timeout from preferences
     */
    public static int getDefaultServerConnectTimeoutFromPrefs(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(ServerConstants.class).getInt(
                ServerConstants.DEFAULT_SERVER_CONNECT_TIMEOUT_PREFERENCE_KEY, ServerConstants.DEFAULT_SERVER_CONNECT_TIMEOUT);
    }

    /**
     * Gets the default server read timeout from preferences.
     *
     * @param prefsRegistry The system preferences registry.
     * @return the default server read timeout from preferences
     */
    public static int getDefaultServerReadTimeoutFromPrefs(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(ServerConstants.class)
                .getInt(ServerConstants.DEFAULT_SERVER_READ_TIMEOUT_PREFERENCE_KEY, ServerConstants.DEFAULT_SERVER_READ_TIMEOUT);
    }

    /**
     * Sets the default server activate timeout to preferences.
     *
     * @param prefsRegistry The system preferences registry.
     * @param timeoutMS the timeout milliseconds.
     * @param source the source
     */
    public static void setDefaultServerActivateTimeoutToPrefs(PreferencesRegistry prefsRegistry, int timeoutMS, Object source)
    {
        prefsRegistry.getPreferences(ServerConstants.class)
                .putInt(ServerConstants.DEFAULT_SERVER_ACTIVATION_TIMEOUT_PREFERENCE_KEY, timeoutMS, source);
    }

    /**
     * Sets the default server connect timeout to preferences.
     *
     * @param prefsRegistry The system preferences registry.
     * @param timeoutMS the timeout milliseconds.
     * @param source the source
     */
    public static void setDefaultServerConnectTimeoutToPrefs(PreferencesRegistry prefsRegistry, int timeoutMS, Object source)
    {
        prefsRegistry.getPreferences(ServerConstants.class).putInt(ServerConstants.DEFAULT_SERVER_CONNECT_TIMEOUT_PREFERENCE_KEY,
                timeoutMS, source);
    }

    /**
     * Sets the default server read timeout to preferences.
     *
     * @param prefsRegistry The system preferences registry.
     * @param timeoutMS the timeout milliseconds.
     * @param source the source
     */
    public static void setDefaultServerReadTimeoutToPrefs(PreferencesRegistry prefsRegistry, int timeoutMS, Object source)
    {
        prefsRegistry.getPreferences(ServerConstants.class).putInt(ServerConstants.DEFAULT_SERVER_READ_TIMEOUT_PREFERENCE_KEY,
                timeoutMS, source);
    }

    /** Forbid public instantiation of utility class. */
    private ServerConstants()
    {
    }
}
