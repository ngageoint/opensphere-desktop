package io.opensphere.core.appl.versions.model;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

import io.opensphere.core.preferences.BooleanPreferenceBinding;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.StringPreferenceBinding;
import io.opensphere.core.util.CompositeService;
import io.opensphere.core.util.concurrent.InlineExecutor;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;

/** The model of an auto-update session. */
public class AutoUpdatePreferences extends CompositeService
{
    /** Default protocol for auto-update interactions. */
    public static final String DEFAULT_PROTOCOL = "https";

    /** The default name for the launch configuration file. */
    private static final String DEFAULT_LAUNCH_CONFIGURATION_FILENAME = "config.properties";

    /** The preferences. */
    private final Preferences myPreferences;

    /** The auto-update preference binding. */
    private final BooleanPreferenceBinding myAutoUpdate;

    /** The update-without-prompt preference binding. */
    private final BooleanPreferenceBinding myUpdateWithoutPrompt;

    /** The auto-update hostname preference binding. */
    private final StringPreferenceBinding myAutoUpdateHostname;

    /** The latest version URL preference binding. */
    private final StringPreferenceBinding myLatestVersionUrl;

    /** The update URL preference binding. */
    private final StringPreferenceBinding myUpdateUrl;

    /** The directory where the application versions are installed. */
    private final File myInstallDirectory = new File(System.getProperty("user.dir")).getParentFile();

    /**
     * A non-preference-based field containing the newest installed version
     * (note that this may be newer than the version actually running).
     */
    private String myNewestLocalVersion;

    /**
     * A non-preference-based field containing the newest remote version
     * available on the server.
     */
    private String myNewestRemoteVersion;

    /**
     * Creates a new auto-update model for preferences.
     *
     * @param preferences the set of preferences for the auto-update model.
     */
    public AutoUpdatePreferences(Preferences preferences)
    {
        super(5);

        myPreferences = preferences;

        myAutoUpdate = addBooleanProperty("Enable Auto-Update", AutoUpdatePreferenceKeys.AUTO_UPDATE_ENABLED_KEY, true);
        myUpdateWithoutPrompt = addBooleanProperty("Update Without Prompt",
                AutoUpdatePreferenceKeys.UPDATE_WITHOUT_PROMPT_ENABLED_KEY, false);
        myAutoUpdateHostname = addStringProperty("Auto-Update Hostname", AutoUpdatePreferenceKeys.AUTO_UPDATE_HOSTNAME_KEY,
                myPreferences.getString(AutoUpdatePreferenceKeys.AUTO_UPDATE_HOSTNAME_KEY, null));
        myLatestVersionUrl = addStringProperty("Latest Version URL", AutoUpdatePreferenceKeys.AUTO_UPDATE_LATEST_VERSION_URL_KEY,
                myPreferences.getString(AutoUpdatePreferenceKeys.AUTO_UPDATE_LATEST_VERSION_URL_KEY, null));
        myUpdateUrl = addStringProperty("Version Download URL", AutoUpdatePreferenceKeys.AUTO_UPDATE_UPDATE_URL_KEY,
                myPreferences.getString(AutoUpdatePreferenceKeys.AUTO_UPDATE_UPDATE_URL_KEY, null));
    }

    /**
     * Adds a boolean property binding service.
     *
     * @param text the text
     * @param propertyName the property name
     * @param defaultValue the default value
     * @return The property binding
     */
    public BooleanPreferenceBinding addBooleanProperty(String text, String propertyName, boolean defaultValue)
    {
        return addService(new BooleanPreferenceBinding(new ConcurrentBooleanProperty(this, text), propertyName, defaultValue,
                myPreferences, new InlineExecutor()));
    }

    /**
     * Adds a String property binding service.
     *
     * @param text the text
     * @param propertyName the property name
     * @param defaultValue the default value
     * @return The property binding
     */
    public StringPreferenceBinding addStringProperty(String text, String propertyName, String defaultValue)
    {
        return addService(new StringPreferenceBinding(new ConcurrentStringProperty(this, text), propertyName, defaultValue,
                myPreferences, new InlineExecutor()));
    }

    /**
     * Gets the value of the {@link #myPreferences} field.
     *
     * @return the value stored in the {@link #myPreferences} field.
     */
    public Preferences getPreferences()
    {
        return myPreferences;
    }

    /**
     * Gets the value of the {@link #myAutoUpdate} field.
     *
     * @return the value stored in the {@link #myAutoUpdate} field.
     */
    public boolean isAutoUpdateEnabled()
    {
        return myAutoUpdate.getProperty().get();
    }

    /**
     * Gets the {@link #myAutoUpdate} property.
     *
     * @return the {@link #myAutoUpdate} property.
     */
    public BooleanProperty autoUpdateProperty()
    {
        return myAutoUpdate.getProperty();
    }

    /**
     * Gets the value of the {@link #myUpdateWithoutPrompt} field.
     *
     * @return the value stored in the {@link #myUpdateWithoutPrompt} field.
     */
    public boolean isUpdateWithoutPromptEnabled()
    {
        return myUpdateWithoutPrompt.getProperty().get();
    }

    /**
     * Gets the {@link #myUpdateWithoutPrompt} property.
     *
     * @return the {@link #myUpdateWithoutPrompt} property.
     */
    public BooleanProperty updateWithoutPromptProperty()
    {
        return myUpdateWithoutPrompt.getProperty();
    }

    /**
     * Gets the protocol to use for auto update interactions. This value is not
     * user-configurable, but is stored in preferences, so it is not bound to a
     * preference.
     *
     * @return the protocol to use for auto update interactions.
     */
    public String getAutoUpdateProtocol()
    {
        return myPreferences.getString(AutoUpdatePreferenceKeys.AUTO_UPDATE_PROTOCOL_KEY, DEFAULT_PROTOCOL);
    }

    /**
     * Gets the name of the file in which the launch configuration is
     * maintained. This value is not user-configurable, but is stored in
     * preferences, so it is not bound to a preference.
     *
     * @return the name of the file in which the launch configuration is
     *         maintained.
     */
    public String getLaunchConfigurationFilename()
    {
        return myPreferences.getString(AutoUpdatePreferenceKeys.LAUNCH_CONFIGURATION_FILENAME_KEY,
                DEFAULT_LAUNCH_CONFIGURATION_FILENAME);
    }

    /**
     * Gets the value of the {@link #myAutoUpdateHostname} field.
     *
     * @return the value stored in the {@link #myAutoUpdateHostname} field.
     */
    public String getAutoUpdateHostname()
    {
        return myAutoUpdateHostname.getProperty().get();
    }

    /**
     * Gets the value of the {@link #myUpdateUrl} field.
     *
     * @return the value stored in the {@link #myUpdateUrl} field.
     */
    public String getUpdateUrl()
    {
        return myUpdateUrl.getProperty().get();
    }

    /**
     * Gets the value of the {@link #myLatestVersionUrl} field.
     *
     * @return the value stored in the {@link #myLatestVersionUrl} field.
     */
    public String getLatestVersionUrl()
    {
        return myLatestVersionUrl.getProperty().get();
    }

    /**
     * Gets the {@link #myAutoUpdateHostname} property.
     *
     * @return the {@link #myAutoUpdateHostname} property.
     */
    public StringProperty autoUpdateHostnameProperty()
    {
        return myAutoUpdateHostname.getProperty();
    }

    /**
     * Gets the {@link #myUpdateUrl} property.
     *
     * @return the {@link #myUpdateUrl} property.
     */
    public StringProperty updateUrlProperty()
    {
        return myUpdateUrl.getProperty();
    }

    /**
     * Gets the {@link #myLatestVersionUrl} property.
     *
     * @return the {@link #myLatestVersionUrl} property.
     */
    public StringProperty latestVersionUrlProperty()
    {
        return myLatestVersionUrl.getProperty();
    }

    /**
     * Restores the default values.
     */
    public void restoreDefaults()
    {
        myAutoUpdate.restoreDefault();
        myUpdateWithoutPrompt.restoreDefault();
        myAutoUpdateHostname.restoreDefault();
        myLatestVersionUrl.restoreDefault();
        myUpdateUrl.restoreDefault();
    }

    /**
     * Gets the value of the {@link #myInstallDirectory} field.
     *
     * @return the value stored in the {@link #myInstallDirectory} field.
     */
    public File getInstallDirectory()
    {
        return myInstallDirectory;
    }

    /**
     * Stores the supplied value in the myNewestLocalVersion field.
     *
     * @param newestLocalVersion the newestLocalVersion to store.
     */
    public void setNewestLocalVersion(String newestLocalVersion)
    {
        myNewestLocalVersion = newestLocalVersion;
    }

    /**
     * Gets the value of the myNewestLocalVersion field.
     *
     * @return the value of the newestLocalVersion field.
     */
    public String getNewestLocalVersion()
    {
        return myNewestLocalVersion;
    }

    /**
     * Stores the supplied value in the myNewestRemoteVersion field.
     *
     * @param newestRemoteVersion the newestRemoteVersion to store.
     */
    public void setNewestRemoteVersion(String newestRemoteVersion)
    {
        myNewestRemoteVersion = newestRemoteVersion;
    }

    /**
     * Gets the value of the myNewestRemoteVersion field.
     *
     * @return the value of the newestRemoteVersion field.
     */
    public String getNewestRemoteVersion()
    {
        return myNewestRemoteVersion;
    }
}
