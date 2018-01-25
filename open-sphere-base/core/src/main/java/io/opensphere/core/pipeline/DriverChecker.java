package io.opensphere.core.pipeline;

import java.awt.Component;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.DontShowDialog;

/** Checks the driver version and handles any problems. */
public class DriverChecker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DriverChecker.class);

    /** The component used to position my dialogs. */
    private final Component myComponent;

    /** The preferences. */
    private final Preferences myPrefs;

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /** The display lists preference key. */
    private final String myDisplayListPrefsKey;

    /**
     * Constructor.
     *
     * @param component The component used to place dialogs.
     * @param prefs The preferences.
     * @param preferencesRegistry The preferences registry
     * @param displayListPrefsKey The display lists preference key
     */
    public DriverChecker(Component component, Preferences prefs, PreferencesRegistry preferencesRegistry,
            String displayListPrefsKey)
    {
        myComponent = component;
        myPrefs = prefs;
        myPreferencesRegistry = preferencesRegistry;
        myDisplayListPrefsKey = displayListPrefsKey;
    }

    /**
     * Checks the graphics driver for compatibility.
     *
     * @param gl the GL object
     */
    public void checkGraphicsDriver(GL gl)
    {
        String glVendor = gl.glGetString(GL.GL_VENDOR);
        String driverVersion = getDriverVersion(gl);
        if (glVendor != null && driverVersion != null)
        {
            Map<String, String> configMap = myPrefs.getStringMap("unsupportedDrivers", Collections.emptyMap());
            List<String> actions = getActions(glVendor, driverVersion, configMap);
            if (CollectionUtilities.hasContent(actions))
            {
                for (String action : actions)
                {
                    switch (action)
                    {
                        case "warn":
                            warn(driverVersion);
                            break;
                        case "disableTooltips":
                            disableTooltips(driverVersion);
                            break;
                        case "disableDisplayLists":
                            disableDisplayLists(driverVersion);
                            break;
                        default:
                    }
                }
            }
        }
        else
        {
            LOGGER.info("Unable to determine GL vendor / driver version");
        }
    }

    /**
     * Warns the user that the driver version is bad.
     *
     * @param driverVersion the driver version
     */
    private void warn(String driverVersion)
    {
        String message = "<html>Graphics driver version " + driverVersion + " is known to have problems.<br>"
                + "Please contact your system administrator to install a newer version.</html>";
        DontShowDialog.showMessageDialog(myPreferencesRegistry, myComponent, message, "Unsupported Graphics Driver", false);
    }

    /**
     * Disables tooltips and warns the user that the driver version is bad.
     *
     * @param driverVersion the driver version
     */
    private void disableTooltips(String driverVersion)
    {
        ToolTipManager.sharedInstance().setEnabled(false);

        String message = "<html>Graphics driver version " + driverVersion + " is known to have problems.<br>"
                + "Tooltips have been disabled to prevent problems.<br>"
                + "Please contact your system administrator to install a newer version.</html>";
        DontShowDialog.showMessageDialog(myPreferencesRegistry, myComponent, message, "Unsupported Graphics Driver", false);
    }

    /**
     * Disables display lists and warns the user that the driver version is bad.
     *
     * @param driverVersion the driver version
     */
    private void disableDisplayLists(String driverVersion)
    {
        myPrefs.putBoolean(myDisplayListPrefsKey, false, this);

        String message = "<html>Graphics driver version " + driverVersion + " is known to have problems.<br>"
                + "Fast text rendering has been disabled to prevent problems.<br>"
                + "Please contact your system administrator to install a newer version.</html>";
        DontShowDialog.showMessageDialog(myPreferencesRegistry, myComponent, message, "Unsupported Graphics Driver", false);
    }

    /**
     * Gets the actions to perform for the given driver.
     *
     * @param glVendor the GL vendor
     * @param driverVersion the driver version
     * @param configMap the config map of unsupported driver versions
     * @return the actions to perform, or null
     */
    static List<String> getActions(String glVendor, String driverVersion, Map<String, String> configMap)
    {
        List<String> actions = null;
        for (Map.Entry<String, String> entry : configMap.entrySet())
        {
            String[] tokens = entry.getKey().split(":");
            String vendor = tokens[0];
            String versionFragment = tokens[1];
            if (glVendor.startsWith(vendor) && driverVersion.startsWith(versionFragment) && !versionFragment.isEmpty())
            {
                actions = New.list(entry.getValue().split(","));
            }
        }
        return actions;
    }

    /**
     * Gets the the machine's driver version.
     *
     * @param gl the GL object
     * @return the driver version, or null
     */
    private static String getDriverVersion(GL gl)
    {
        String glVersion = gl.glGetString(GL.GL_VERSION);
        Matcher matcher = Pattern.compile("([\\d\\.]+)$").matcher(glVersion);
        return matcher.find() ? matcher.group(1) : null;
    }
}
