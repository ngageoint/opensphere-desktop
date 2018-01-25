package io.opensphere.wfs.util;

import io.opensphere.core.preferences.Preferences;

/** WFS Preference Utilities. */
public final class WFSPreferenceUtilities
{
    /**
     * Gets the max features from preferences.
     *
     * @param preferences the WFS preferences
     * @return the max features from preferences
     */
    public static int getMaxFeaturesFromPreferences(Preferences preferences)
    {
        int maxFeatures = preferences.getInt(WFSConstants.MAX_FEATURES_PREFERENCE, WFSConstants.DEFAULT_MAX_FEATURES);
        if (maxFeatures > WFSConstants.MAX_MAX_FEATURES)
        {
            maxFeatures = WFSConstants.MAX_MAX_FEATURES;
        }
        return maxFeatures;
    }

    /** Private constructor. */
    private WFSPreferenceUtilities()
    {
    }
}
