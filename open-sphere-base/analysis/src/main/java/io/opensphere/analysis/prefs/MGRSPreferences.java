package io.opensphere.analysis.prefs;

import io.opensphere.core.preferences.PreferencesRegistry;

/** Holds the MGRS Preferences for the MGRS values in the analysis tools. */
public class MGRSPreferences
{
    /** The preference which defines the precision of the MGRS values. */
    public static final String MGRS_PRECISION_PREFERENCE = "MGRS_PRECISION_PREFERENCE";

    /**
     * Gets the MGRS values precision. 4, 6, 8, or 10 digits only.
     *
     * @param prefsRegistry The system preferences registry.
     * @return the mgrs precision.
     */
    public static int getToolMGRSPrecision(PreferencesRegistry prefsRegistry)
    {
        return prefsRegistry.getPreferences(MGRSPreferences.class).getInt(MGRS_PRECISION_PREFERENCE, 10);
    }

    /**
     * Sets the MGRS precision to 4, 6, 8, or 10 digits only.
     *
     * @param prefsRegistry The system preferences registry.
     * @param precision the precision. 4, 6, 8, 10 allowed, all others will
     *            throw illegal argument exception.
     * @param source the source of the change
     * @throws IllegalArgumentException if precision is not 4, 6, 8, or 10
     */
    public static void setToolMGRSPrecision(PreferencesRegistry prefsRegistry, int precision, Object source)
    {
        if (precision != 4 && precision != 6 && precision != 8 && precision != 10)
        {
            throw new IllegalArgumentException("MGRS Precision must be 4, 6, 8, or 10 only \"" + precision + "\" is not valid.");
        }
        prefsRegistry.getPreferences(MGRSPreferences.class).putInt(MGRS_PRECISION_PREFERENCE, precision, source);
    }


    /** Disallow instantiation. */
    private MGRSPreferences()
    {
    }
}
