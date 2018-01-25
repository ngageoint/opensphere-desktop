package io.opensphere.mantle.util;

import java.awt.Color;
import java.io.File;
import java.net.URL;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * The Class Constants.
 */
public final class MantleConstants
{
    /** The Constant CONF_DIR_NAME. */
    public static final String CONF_DIR_NAME = "conf";

    /** The Constant SELECT_COLOR. */
    public static final Color SELECT_COLOR = new Color(88, 0, 0);

    /** The Constant SELECT_SIZE_ADDITION. */
    public static final float SELECT_SIZE_ADDITION = 5;

    /** The Constant SELECT_WIDTH_ADDITION. */
    public static final int SELECT_WIDTH_ADDITION = 2;

    /** The Constant Date Format Configuration File Preferences Key Name. */
    public static final String USER_DATE_FORMAT_CONFIG_FILE_KEY = "DateFormatConfig";

    /** The Constant Date Format Configuration File Preferences Topic Name. */
    public static final String USER_DATE_FORMAT_CONFIG_FILE_TOPIC = "io.opensphere.core.v1.DateFormatConfiguration";

    /** Preferences key for user lat/lon formats. */
    public static final String USER_LATLON_FORMAT_KEY = "LatLonFormat";

    /** Preferences topic for user lat/lon formats. */
    public static final String USER_LATLON_FORMAT_TOPIC = "UserLatLonFormat";

    /** The Constant SDF_HELP_FILE_URL_STR. */
    public static final URL SDF_HELP_FILE_URL;

    /** The Constant SLLF_HELP_FILE_URL_STR. */
    public static final URL SLLF_HELP_FILE_URL;

    /** The Constant HELP. */
    private static final String HELP = "help";

    static
    {
        SDF_HELP_FILE_URL = MantleConstants.class.getClassLoader().getResource(HELP + "/SimpleDateFormat.html");
        SLLF_HELP_FILE_URL = MantleConstants.class.getClassLoader().getResource(HELP + "/SimpleLatLonFormat.html");
    }

    /**
     * Gets the default date format config file.
     *
     * @return the default date format config file
     */
    public static File getDefaultDateFormatConfigFile()
    {
        File aFile = new File("dateFormatConfig.xml");
        String installDir = System.getProperty("InstallPath");
        if (!StringUtilities.startsWith(installDir, '$'))
        {
            aFile = new File(installDir + File.separator + CONF_DIR_NAME + File.separator + "dateFormatConfig.xml");
        }
        return aFile;
    }

    /**
     * Gets the default lat lon format config file.
     *
     * @return the default lat lon format config file
     */
    public static File getDefaultLatLonFormatConfigFile()
    {
        File aFile = new File("latLonFormatConfig.xml");
        String installDir = System.getProperty("InstallPath");
        if (!StringUtilities.startsWith(installDir, '$'))
        {
            aFile = new File(installDir + File.separator + CONF_DIR_NAME + File.separator + "latLonFormatConfig.xml");
        }
        return aFile;
    }

    /**
     * Disallow instantiation.
     */
    private MantleConstants()
    {
    }
}
