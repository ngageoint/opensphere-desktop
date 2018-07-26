package io.opensphere.search;

import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.swing.input.DontShowDialog;

/**
 * A basic options provider used to group settings for search providers into a single location.
 */
public class SearchOptionsProvider extends AbstractOptionsProvider
{
    /** The name of the root search options provider. */
    public static final String PROVIDER_NAME = "Search";

    /** The preference key. */
    private static final String PREFERENCE_KEY = "Search Notification.choice";

    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /** The panel. */
    private JPanel myPanel;

    /** The requery option combo box. */
    private JComboBox<RequeryOption> myComboBox;

    /**
     * Creates a new options provider.
     *
     * @param preferencesRegistry The preferences registry
     */
    public SearchOptionsProvider(PreferencesRegistry preferencesRegistry)
    {
        super(PROVIDER_NAME);
        myPreferencesRegistry = preferencesRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#applyChanges()
     */
    @Override
    public void applyChanges()
    {
        // intentionally blank
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#getOptionsPanel()
     */
    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            JPanel panel = new JPanel();
            panel.add(new JLabel("Action to take when search results may have changed:"));
            myComboBox = new JComboBox<>(RequeryOption.values());
            myComboBox.addActionListener(e ->
            {
                Quantify.collectMetric("mist3d.settings.search.action-if-search-results-changed-selection");
                saveSelection();
            });
            panel.add(myComboBox);
            myPanel = new OptionsPanel(panel);
        }

        Preferences preferences = myPreferencesRegistry.getPreferences(DontShowDialog.class);
        int selection = preferences.getInt(PREFERENCE_KEY, -1);
        myComboBox.setSelectedItem(RequeryOption.fromValue(selection));

        return myPanel;
    }

    /**
     * Saves the selection to the preferences.
     */
    private void saveSelection()
    {
        Preferences preferences = myPreferencesRegistry.getPreferences(DontShowDialog.class);
        RequeryOption selectedItem = (RequeryOption)myComboBox.getSelectedItem();
        if (selectedItem != null)
        {
            preferences.putInt(PREFERENCE_KEY, selectedItem.getValue(), this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.OptionsProvider#restoreDefaults()
     */
    @Override
    public void restoreDefaults()
    {
        // intentionally blank
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.impl.AbstractOptionsProvider#usesApply()
     */
    @Override
    public boolean usesApply()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.options.impl.AbstractOptionsProvider#usesRestore()
     */
    @Override
    public boolean usesRestore()
    {
        return false;
    }

    /** Enumeration for requery options. */
    private enum RequeryOption
    {
        /** Ask. */
        ASK("Ask", -1),

        /** Requery. */
        YES("Always Requery", JOptionPane.YES_OPTION),

        /** Don't requery. */
        NO("Never Requery", JOptionPane.CANCEL_OPTION);

        /** The display text. */
        private final String myText;

        /** The value. */
        private final int myValue;

        /**
         * Gets the enum value for the integer value.
         *
         * @param value the integer value
         * @return the enum value, or null
         */
        public static RequeryOption fromValue(int value)
        {
            return Arrays.stream(RequeryOption.values()).filter(v -> v.getValue() == value).findAny().orElse(null);
        }

        /**
         * Constructor.
         *
         * @param text The display text
         * @param value The value
         */
        private RequeryOption(String text, int value)
        {
            myText = text;
            myValue = value;
        }

        @Override
        public String toString()
        {
            return myText;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public int getValue()
        {
            return myValue;
        }
    }
}
