package io.opensphere.mantle.data.geom.style.dialog;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.units.length.Feet;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.mantle.data.geom.style.config.v1.DataTypeStyleConfig;
import io.opensphere.mantle.data.geom.style.config.v1.FeatureTypeStyleConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleManagerConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleParameterConfig;
import io.opensphere.mantle.data.geom.style.config.v1.StyleParameterSetConfig;

/**
 * Helper class for migrating the style manager config file.
 */
final class StyleManagerConfigMigrationHelper
{
    /**
     * Check for the old config topic and try to load from there.
     *
     * @param preferencesRegistry The preferences registry.
     * @param prefKey The preference key.
     * @return The migrated config, or a default config if an old one was not
     *         found.
     */
    @Nonnull
    public StyleManagerConfig loadFromDeprecatedTopic(PreferencesRegistry preferencesRegistry, String prefKey)
    {
        StyleManagerConfig config;
        Preferences oldPrefs = preferencesRegistry.getPreferences(StyleManagerConfig.class);
        config = oldPrefs.getJAXBObject(StyleManagerConfig.class, prefKey, null);
        if (config == null)
        {
            config = new StyleManagerConfig();
        }
        else
        {
            handleDistanceUnitMigration(config);
            preferencesRegistry.getPreferences(StyleManagerController.class).putJAXBObject(prefKey, config, false, null);
        }
        return config;
    }

    /**
     * Check the config for old DistanceUnits and migrate them if found.
     *
     * @param config The config.
     */
    private void handleDistanceUnitMigration(StyleManagerConfig config)
    {
        for (DataTypeStyleConfig dataTypeStyleConfig : config.getDataTypeStyles())
        {
            for (FeatureTypeStyleConfig ftsConfig : dataTypeStyleConfig.getFeatureTypeStyleConfigList())
            {
                for (StyleParameterSetConfig styleParameterSetConfig : ftsConfig.getStyleParameterSetConfigList())
                {
                    Collection<StyleParameterConfig> adds = null;
                    for (Iterator<StyleParameterConfig> iter = styleParameterSetConfig.getParameterSet().iterator(); iter
                            .hasNext();)
                    {
                        StyleParameterConfig styleParameterConfig = iter.next();
                        if ("io.opensphere.mantle.data.impl.specialkey.DistanceUnit"
                                .equals(styleParameterConfig.getParameterValueClass()))
                        {
                            String value;
                            if ("METERS".equals(styleParameterConfig.getParameterValue()))
                            {
                                value = Length.getSelectionLabel(Meters.class);
                            }
                            else if ("KILOMETERS".equals(styleParameterConfig.getParameterValue()))
                            {
                                value = Length.getSelectionLabel(Kilometers.class);
                            }
                            else if ("FEET".equals(styleParameterConfig.getParameterValue()))
                            {
                                value = Length.getSelectionLabel(Feet.class);
                            }
                            else if ("MILES".equals(styleParameterConfig.getParameterValue()))
                            {
                                value = Length.getSelectionLabel(StatuteMiles.class);
                            }
                            else if ("NAUTICAL_MILES".equals(styleParameterConfig.getParameterValue()))
                            {
                                value = Length.getSelectionLabel(NauticalMiles.class);
                            }
                            else
                            {
                                value = null;
                            }
                            if (value != null)
                            {
                                iter.remove();
                                adds = CollectionUtilities.lazyAdd(new StyleParameterConfig(
                                        styleParameterConfig.getParameterKey(), String.class.getName(), value), adds);
                            }
                        }
                    }
                    if (adds != null)
                    {
                        styleParameterSetConfig.getParameterSet().addAll(adds);
                    }
                }
            }
        }
    }
}
