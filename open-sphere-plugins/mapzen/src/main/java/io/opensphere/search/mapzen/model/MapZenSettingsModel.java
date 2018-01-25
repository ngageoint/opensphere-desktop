package io.opensphere.search.mapzen.model;

import javafx.beans.property.StringProperty;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;

/** Simple model in which MapZen search configuration state is maintained. */
public class MapZenSettingsModel
{
    /** The preferences in which the state is persisted. */
    private final Preferences myPreferences;

    /** The property in which the MapZen API key is bound. */
    private final StringProperty myApiKey = new ConcurrentStringProperty();

    /** The property in which the search URL template is bound. */
    private final StringProperty mySearchUrlTemplate = new ConcurrentStringProperty();

    /**
     * Creates a new model bound to the supplied preferences.
     *
     * @param preferences The preferences in which the state is persisted.
     */
    public MapZenSettingsModel(Preferences preferences)
    {
        myPreferences = preferences;

        myApiKey.set(myPreferences.getString(MapZenPreferenceKeys.API_KEY, "search-rVgteQp"));
        mySearchUrlTemplate.set(myPreferences.getString(MapZenPreferenceKeys.SEARCH_URL_TEMPLATE,
                "https://search.mapzen.com/v1/search?text=%1$s&api_key=%2$s"));

        myApiKey.addListener((obs, ov, nv) -> persist());
        mySearchUrlTemplate.addListener((obs, ov, nv) -> persist());
    }

    /**
     * Gets the value of the myPreferences field.
     *
     * @return the value of the preferences field.
     */
    public Preferences getPreferences()
    {
        return myPreferences;
    }

    /**
     * Gets the API Key property.
     *
     * @return the API Key property.
     */
    public StringProperty apiKeyProperty()
    {
        return myApiKey;
    }

    /**
     * Gets the value of the mySearchUrlTemplate field.
     *
     * @return the value of the searchUrlTemplate field.
     */
    public StringProperty searchUrlTemplateProperty()
    {
        return mySearchUrlTemplate;
    }

    /** Persist the current state of the model to the preferences object. */
    private void persist()
    {
        myPreferences.putString(MapZenPreferenceKeys.API_KEY, myApiKey.get(), this);
    }
}
