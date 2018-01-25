package io.opensphere.myplaces.util;

import java.awt.Color;
import java.awt.Font;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.myplaces.constants.Constants;

/**
 * Migrates the map point options into the my places options.
 *
 */
public class OptionsMigrator
{
    /**
     * Migrates the options from the old point options to the new my places
     * options.
     *
     * @param accessor Used to save my places.
     * @param toolbox Used to get the preferences.
     */
    public void migrateIfNeedBe(OptionsAccessor accessor, Toolbox toolbox)
    {
        PreferencesRegistry registry = toolbox.getPreferencesRegistry();
        Preferences placesPreferences = registry.getPreferences(OptionsAccessor.class);
        String defaultPlace = placesPreferences.getString(OptionsAccessor.DEFAULT_PLACES_PROP, null);
        if (defaultPlace == null)
        {
            Preferences pointPreferences = registry
                    .getPreferences("io.opensphere.myplaces.specific.points.editor.OptionsAccessor");
            String defaultPoint = pointPreferences.getString("defaultPoint", null);

            if (defaultPoint == null)
            {
                Placemark defaultPlacemark = new Placemark();
                PlacemarkUtils.setPlacemarkTextColor(defaultPlacemark, Color.white);
                PlacemarkUtils.setPlacemarkColor(defaultPlacemark, Color.gray);
                ExtendedData extendedData = defaultPlacemark.createAndSetExtendedData();
                ExtendedDataUtils.putBoolean(extendedData, Constants.IS_TITLE, true);
                ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DISTANCE_ID, true);
                ExtendedDataUtils.putBoolean(extendedData, Constants.IS_HEADING_ID, true);
                Font defaultFont = PlacemarkUtils.DEFAULT_FONT;
                PlacemarkUtils.setPlacemarkFont(defaultPlacemark, defaultFont);

                accessor.saveDefaultPlacemark(defaultPlacemark);
            }
            else
            {
                defaultPlace = defaultPoint;
            }

            if (defaultPlace != null)
            {
                placesPreferences.putString(OptionsAccessor.DEFAULT_PLACES_PROP, defaultPlace, this);
            }
        }
    }
}
