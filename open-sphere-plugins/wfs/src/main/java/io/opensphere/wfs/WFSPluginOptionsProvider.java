package io.opensphere.wfs;

import java.awt.Color;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;

import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.DocumentListenerAdapter;
import io.opensphere.wfs.util.WFSConstants;
import io.opensphere.wfs.util.WFSPreferenceUtilities;

/**
 * The Class WFSPluginOptionsProvider.
 */
public class WFSPluginOptionsProvider extends AbstractOptionsProvider
{
    /** The panel. */
    private JPanel myPanel;

    /** The max features text field. */
    private JTextField myMaxFeaturesTF;

    /** The original max features text field border. */
    private Border myOriginalMaxFeaturesTFBorder;

    /** The WFS preferences. */
    private final Preferences myPreferences;

    /**
     * Instantiates a new WFS plugin options provider.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public WFSPluginOptionsProvider(PreferencesRegistry preferencesRegistry)
    {
        super("WFS");
        myPreferences = preferencesRegistry.getPreferences(WFSPlugin.class);
    }

    @Override
    public void applyChanges()
    {
        // Save all the values to the preferences if they are valid.
        if (isMaxFeaturesValid())
        {
            int features = NumberUtilities.parseInt(myMaxFeaturesTF.getText(), -1);
            myPreferences.putInt(WFSConstants.MAX_FEATURES_PREFERENCE, features, this);
        }
        else
        {
            String message = "Max features must be a number between 1 and "
                    + NumberFormat.getIntegerInstance().format(WFSConstants.MAX_MAX_FEATURES) + ".";
            JOptionPane.showMessageDialog(myPanel, message, "Invalid Max Features Value", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myPanel = new OptionsPanel(getMaxFeaturesPanel());
        }
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
        getMaxFeaturesTF().setText(Integer.toString(WFSConstants.DEFAULT_MAX_FEATURES));
        myPreferences.putInt(WFSConstants.MAX_FEATURES_PREFERENCE, WFSConstants.DEFAULT_MAX_FEATURES, this);
    }

    /**
     * Gets the max features panel.
     *
     * @return the max features panel
     */
    private JPanel getMaxFeaturesPanel()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Max Features For WFS Requests: "));
        panel.add(getMaxFeaturesTF());
        return panel;
    }

    /**
     * Gets the max features text field.
     *
     * @return the max features text field
     */
    private JTextField getMaxFeaturesTF()
    {
        if (myMaxFeaturesTF == null)
        {
            myMaxFeaturesTF = new JTextField();
            ComponentUtilities.setPreferredWidth(myMaxFeaturesTF, 100);
            myOriginalMaxFeaturesTFBorder = myMaxFeaturesTF.getBorder();
            myMaxFeaturesTF.setText(Integer.toString(WFSPreferenceUtilities.getMaxFeaturesFromPreferences(myPreferences)));
            myMaxFeaturesTF.getDocument().addDocumentListener(new DocumentListenerAdapter()
            {
                @Override
                protected void updateAction(DocumentEvent e)
                {
                    validateMaxFeaturesInput();
                }
            });
        }
        return myMaxFeaturesTF;
    }

    /**
     * Validate max features input.
     */
    private void validateMaxFeaturesInput()
    {
        if (isMaxFeaturesValid())
        {
            myMaxFeaturesTF.setBorder(myOriginalMaxFeaturesTFBorder);
        }
        else
        {
            myMaxFeaturesTF.setBorder(BorderFactory.createLineBorder(Color.red));
        }
    }

    /**
     * Determines if the max features value entered by the user is valid.
     *
     * @return whether it's valid
     */
    private boolean isMaxFeaturesValid()
    {
        boolean valid = true;
        try
        {
            int features = Integer.parseInt(myMaxFeaturesTF.getText());
            if (features < 1 || features > WFSConstants.MAX_MAX_FEATURES)
            {
                valid = false;
            }
        }
        catch (NumberFormatException e)
        {
            valid = false;
        }
        return valid;
    }
}
