package io.opensphere.core.appl.versions.model;

/**
 * A set of constants in which the keys used for auto-update preferences are
 * defined.
 */
public final class AutoUpdatePreferenceKeys
{
    /** The key for the auto-update download URL. */
    public static final String AUTO_UPDATE_UPDATE_URL_KEY = "DownloadURL";

    /** The key for the auto-update latest version URL. */
    public static final String AUTO_UPDATE_LATEST_VERSION_URL_KEY = "LatestVersionURL";

    /** The key for the auto-update hostname. */
    public static final String AUTO_UPDATE_HOSTNAME_KEY = "AutoUpdateHost";

    /** Key for the auto-update protocol value. */
    public static final String AUTO_UPDATE_PROTOCOL_KEY = "AutoUpdateProtocol";

    /** Key for the launch configuration filename value. */
    public static final String LAUNCH_CONFIGURATION_FILENAME_KEY = "LaunchConfigurationFilename";

    /** Key for the auto-update enabled state. */
    public static final String UPDATE_WITHOUT_PROMPT_ENABLED_KEY = "update-without-prompt.enabled";

    /** Key for the auto-update enabled state. */
    public static final String AUTO_UPDATE_ENABLED_KEY = "auto-update.enabled";

    /** The preferred version property key. */
    public static final String PREFERRED_VERSION_KEY = "preferred.version";

    /**
     * Default constructor, hidden from use.
     */
    private AutoUpdatePreferenceKeys()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }
}
