package io.opensphere.myplaces.util;

import java.awt.Color;
import java.awt.Font;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.kml.marshal.KmlMarshaller;
import io.opensphere.myplaces.constants.Constants;

/**
 * Responsible for saving and getting the point options data.
 *
 */
public class OptionsAccessor
{
    /**
     * Default point property.
     */
    public static final String DEFAULT_PLACES_PROP = "defaultPlace";

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new options accessor.
     *
     * @param toolbox The toolbox.
     */
    public OptionsAccessor(Toolbox toolbox)
    {
        myToolbox = toolbox;
        OptionsMigrator migrator = new OptionsMigrator();
        migrator.migrateIfNeedBe(this, toolbox);
    }

    /**
     * Gets the user defined default point.
     *
     * @return The default point.
     */
    public Placemark getDefaultPlacemark()
    {
        PreferencesRegistry registry = myToolbox.getPreferencesRegistry();
        Preferences preferences = registry.getPreferences(OptionsAccessor.class);

        Placemark placemark = new Placemark();
        String kmlString = preferences.getString(DEFAULT_PLACES_PROP, null);
        if (kmlString != null)
        {
            Kml kml = KmlMarshaller.getInstance().unmarshal(kmlString);
            Feature feature = kml.getFeature();
            if (feature instanceof Document)
            {
                Document document = (Document)feature;
                List<Feature> features = document.getFeature();
                if (!features.isEmpty())
                {
                    Feature aFeature = features.get(0);
                    if (aFeature instanceof Folder)
                    {
                        Folder folder = (Folder)aFeature;
                        List<Feature> folderFeatures = folder.getFeature();
                        if (!folderFeatures.isEmpty())
                        {
                            Feature firstFeature = folderFeatures.get(0);
                            if (firstFeature instanceof Placemark)
                            {
                                placemark = (Placemark)firstFeature;
                            }
                        }
                    }
                }
            }
        }

        Color color = PlacemarkUtils.getPlacemarkTextColor(placemark);
        if (color == null)
        {
            PlacemarkUtils.setPlacemarkTextColor(placemark, Color.white);
        }

        color = PlacemarkUtils.getPlacemarkColor(placemark, null);
        if (color == null)
        {
            PlacemarkUtils.setPlacemarkColor(placemark, Color.gray);
        }

        if (placemark.getExtendedData() == null)
        {
            ExtendedData extendedData = placemark.createAndSetExtendedData();
            ExtendedDataUtils.putBoolean(extendedData, Constants.IS_TITLE, true);
            Font defaultFont = PlacemarkUtils.DEFAULT_FONT;
            PlacemarkUtils.setPlacemarkFont(placemark, defaultFont);
        }

        ensureExists(placemark.getExtendedData(), Constants.IS_BUBBLE_FILLED_ID);
        ExtendedDataUtils.putBoolean(placemark.getExtendedData(), Constants.IS_FEATURE_ON_ID, true);

        Placemark newPoint = new Placemark();
        PlacemarkUtils.copyPlacemark(placemark, newPoint);
        newPoint.setId(UUID.randomUUID().toString());

        return newPoint;
    }

    /**
     * Saves the default point.
     *
     * @param placemark The placemark to save for defaults.
     */
    public void saveDefaultPlacemark(Placemark placemark)
    {
        PreferencesRegistry registry = myToolbox.getPreferencesRegistry();
        Preferences preferences = registry.getPreferences(OptionsAccessor.class);

        Kml kml = new Kml();
        Document document = kml.createAndSetDocument();
        Folder folder = document.createAndAddFolder();

        folder.addToFeature(placemark);

        StringWriter writer = new StringWriter();
        KmlMarshaller.getInstance().marshal(kml, writer);

        preferences.putString(DEFAULT_PLACES_PROP, writer.getBuffer().toString(), this);
    }

    /**
     * Ensures the boolean value exists in the extended data.
     *
     * @param extendedData The extended data.
     * @param propertyName The name of the property.
     */
    private void ensureExists(ExtendedData extendedData, String propertyName)
    {
        if (StringUtils.isEmpty(ExtendedDataUtils.getString(extendedData, propertyName)))
        {
            ExtendedDataUtils.putBoolean(extendedData, propertyName, true);
        }
    }
}
