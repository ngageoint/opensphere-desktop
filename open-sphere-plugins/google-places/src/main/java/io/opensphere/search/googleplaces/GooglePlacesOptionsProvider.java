package io.opensphere.search.googleplaces;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * Options provider for Google Places Search.
 */
public class GooglePlacesOptionsProvider extends AbstractPreferencesOptionsProvider
{
    /** Show auto-complete preference key. */
    private static final String API_KEY = "ApiKey";

    /**
     * The api key field.
     */
    private JTextField myKeyField;

    /** The panel. */
    private JPanel myPanel;

    /** The searcher. */
    private final GooglePlacesSearch mySearcher;

    /**
     * Constructor.
     *
     * @param prefRegistry The preferences registry
     * @param searcher The searcher
     */
    public GooglePlacesOptionsProvider(PreferencesRegistry prefRegistry, GooglePlacesSearch searcher)
    {
        super(prefRegistry, "Google Places");
        mySearcher = searcher;
        mySearcher.setAPIKey(getAPIKeyPreferenceValue());
    }

    @Override
    public void applyChanges()
    {
        setAPIKeyPreferenceValue(myKeyField.getText());
        mySearcher.setAPIKey(getAPIKeyPreferenceValue());
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myPanel = new OptionsPanel(createOptionsContent());
        }
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
        Preferences preferences = getPreferencesRegistry().getPreferences(GooglePlacesOptionsProvider.class);
        preferences.remove(API_KEY, this);
        myKeyField.setText(getAPIKeyPreferenceValue());
        mySearcher.setAPIKey(getAPIKeyPreferenceValue());
    }

    @Override
    public boolean usesApply()
    {
        return true;
    }

    @Override
    public boolean usesRestore()
    {
        return true;
    }

    /**
     * Creates the options content panel.
     *
     * @return The options content.
     */
    private GridBagPanel createOptionsContent()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();

        JLabel label = new JLabel("API Key: ");
        myKeyField = new JTextField(getAPIKeyPreferenceValue());

        panel.addRow(label, myKeyField);

        return panel;
    }

    /**
     * Gets the show auto-complete preference value.
     *
     * @return the value
     */
    private String getAPIKeyPreferenceValue()
    {
        Preferences preferences = getPreferencesRegistry().getPreferences(GooglePlacesOptionsProvider.class);
        return preferences.getString(API_KEY, "AIzaSyATWGTb8QetL4n1IMYPBbIueRxZTqxTyFM");
    }

    /**
     * Sets the api key value.
     *
     * @param newValue The new value.
     */
    private void setAPIKeyPreferenceValue(String newValue)
    {
        Preferences preferences = getPreferencesRegistry().getPreferences(GooglePlacesOptionsProvider.class);
        preferences.putString(API_KEY, newValue, this);
    }
}
