package io.opensphere.core.net;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.GhostTextField;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * Options provider for the network configuration.
 */
public class NetworkConfigurationOptionsProvider extends AbstractPreferencesOptionsProvider
{
    /** The label for the auto-config URL box. */
    private JLabel myAutoConfigProxyLabel;

    /** The text entry field for the auto config URL. */
    private JTextField myAutoConfigProxyUrlField;

    /** The text entry field for the proxy exclusions. */
    private JTextField myManualProxyExclusionsField;

    /** The text entry field for the manual proxy host. */
    private JTextField myManualProxyHostField;

    /** The label for the manual proxy host box. */
    private JLabel myManualProxyHostLabel;

    /** The text entry field for the manual proxy port. */
    private JTextField myManualProxyPortField;

    /** The label for the manual proxy port box. */
    private JLabel myManualProxyPortLabel;

    /** The network configuration manager. */
    private final NetworkConfigurationManager myNetworkConfigurationManager;

    /** The label for the proxy exclusions box. */
    private JLabel myManualProxyExclusionsLabel;

    /** The label for the proxy exclusions box. */
    private JLabel mySystemProxyExclusionsLabel;

    /** The text entry field for the proxy exclusions. */
    private JTextField mySystemProxyExclusionsField;

    /** Button indicating if an automatic proxy should be used. */
    private JRadioButton myUseAutoProxyButton;

    /** Button indicating if a manual proxy should be used. */
    private JRadioButton myUseManualProxyButton;

    /** Button indicating if no proxy should be used. */
    private JRadioButton myUseNoProxyButton;

    /** Button indicating if system proxies should be used. */
    private JRadioButton myUseSystemProxiesButton;

    /** Default Map if a key is not in the preference list. */
    private final HashMap<String, String> myDefaultMap = new HashMap<String, String>();

    /**
     * Constructor.
     *
     * @param networkConfigurationManager The network configuration manager.
     * @param prefsRegistry the preferences registry
     */
    public NetworkConfigurationOptionsProvider(NetworkConfigurationManager networkConfigurationManager,
            PreferencesRegistry prefsRegistry)
    {
        super(prefsRegistry, "Network");
        myNetworkConfigurationManager = networkConfigurationManager;
    }

    @Override
    public void applyChanges()
    {
        Map<String, String> proxyOptionPreference = new HashMap<String, String>();
        if (myUseSystemProxiesButton.isSelected())
        {
            myNetworkConfigurationManager.setProxyConfiguration("", -1, true, "", mySystemProxyExclusionsField.getText());
            proxyOptionPreference.put(mySystemProxyExclusionsLabel.getText(), mySystemProxyExclusionsField.getText());
            setProxyPreferenceValue(myUseSystemProxiesButton.getText(), proxyOptionPreference, this);
        }
        else if (myUseAutoProxyButton.isSelected())
        {
            myNetworkConfigurationManager.setProxyConfiguration("", -1, false, myAutoConfigProxyUrlField.getText(), "");
            proxyOptionPreference.put(myAutoConfigProxyLabel.getText(), myAutoConfigProxyUrlField.getText());
            setProxyPreferenceValue(myUseAutoProxyButton.getText(), proxyOptionPreference, this);
        }
        else if (myUseManualProxyButton.isSelected())
        {
            try
            {
                String portText = myManualProxyPortField.getText();
                int port = Integer.parseInt(portText);
                myNetworkConfigurationManager.setProxyConfiguration(myManualProxyHostField.getText(), port, false, "",
                        myManualProxyExclusionsField.getText());
                proxyOptionPreference.put(myManualProxyPortLabel.getText(), portText);
                proxyOptionPreference.put(myManualProxyHostLabel.getText(), myManualProxyHostField.getText());
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(myManualProxyHostField),
                        "Could not parse port number.");
            }
            proxyOptionPreference.put(myManualProxyExclusionsLabel.getText(), myManualProxyExclusionsField.getText());
            setProxyPreferenceValue(myUseManualProxyButton.getText(), proxyOptionPreference, this);
        }
        else
        {
            myNetworkConfigurationManager.setProxyConfiguration("", -1, false, "", "");
        }
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myUseNoProxyButton == null)
        {
            initializeComponents();
        }

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(myUseNoProxyButton);
        buttonGroup.add(myUseSystemProxiesButton);
        buttonGroup.add(myUseAutoProxyButton);
        buttonGroup.add(myUseManualProxyButton);

        String buttonStyle = "button";
        String labelStyle = "label";
        String controlStyle = "control";

        GridBagPanel panel = new GridBagPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.style(buttonStyle).anchorWest().setGridwidth(2);
        panel.style(labelStyle).anchorEast();
        panel.style(controlStyle).anchorWest().setFill(GridBagConstraints.HORIZONTAL).setWeightx(1);
        panel.style(buttonStyle).addRow(myUseNoProxyButton);
        panel.style(buttonStyle).addRow(myUseSystemProxiesButton);
        panel.style(null, labelStyle, controlStyle).addRow(null, mySystemProxyExclusionsLabel, mySystemProxyExclusionsField);
        panel.style(buttonStyle).addRow(myUseAutoProxyButton);
        panel.style(null, labelStyle, controlStyle).addRow(null, myAutoConfigProxyLabel, myAutoConfigProxyUrlField);
        panel.style(buttonStyle).addRow(myUseManualProxyButton);
        panel.style(null, labelStyle, controlStyle).addRow(null, myManualProxyHostLabel, myManualProxyHostField);
        panel.style(null, labelStyle, controlStyle).addRow(null, myManualProxyPortLabel, myManualProxyPortField);
        panel.style(null, labelStyle, controlStyle).addRow(null, myManualProxyExclusionsLabel, myManualProxyExclusionsField);

        setProxyEnabled(myNetworkConfigurationManager.isUseSystemProxies(),
                !myNetworkConfigurationManager.getProxyConfigUrl().isEmpty(),
                !myNetworkConfigurationManager.getProxyHost().isEmpty());

        return panel;
    }

    @Override
    public void restoreDefaults()
    {
        myNetworkConfigurationManager.restoreDefaults();
        setProxyEnabled(myNetworkConfigurationManager.isUseSystemProxies(),
                !myNetworkConfigurationManager.getProxyConfigUrl().isEmpty(),
                !myNetworkConfigurationManager.getProxyHost().isEmpty());
        restoreDefaultPreferences(myUseSystemProxiesButton.getText(), myUseAutoProxyButton.getText(),
                myUseManualProxyButton.getText());
    }

    /**
     * Restores default preferences for network proxy settings.
     *
     * @param myUseSystemProxiesKey textfield label
     * @param myUseAutoProxyKey textfield label
     * @param myUseManualProxyKey textfield label
     */
    private void restoreDefaultPreferences(String myUseSystemProxiesKey, String myUseAutoProxyKey, String myUseManualProxyKey)
    {
        mySystemProxyExclusionsField.setText("");
        myAutoConfigProxyUrlField.setText("");
        myManualProxyHostField.setText("");
        myManualProxyPortField.setText("80");
        myManualProxyExclusionsField.setText("");

        getPreferences().removeMap(myUseSystemProxiesKey, this);
        getPreferences().removeMap(myUseAutoProxyKey, this);
        getPreferences().removeMap(myUseManualProxyKey, this);
    }

    /** Initializes components. */
    private void initializeComponents()
    {
        ActionListener actionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setProxyEnabled(myUseSystemProxiesButton.isSelected(), myUseAutoProxyButton.isSelected(),
                        myUseManualProxyButton.isSelected());
            }
        };
        myUseNoProxyButton = new JRadioButton("No proxy");
        myUseNoProxyButton.addActionListener(actionListener);
        myUseSystemProxiesButton = new JRadioButton("Use system proxy settings");
        myUseSystemProxiesButton.addActionListener(actionListener);
        myUseManualProxyButton = new JRadioButton("Manual proxy configuration");
        myUseManualProxyButton.addActionListener(actionListener);
        myUseAutoProxyButton = new JRadioButton("Automatic proxy configuration");
        myUseAutoProxyButton.addActionListener(actionListener);
        myManualProxyHostField = new JTextField(20);
        myManualProxyPortField = new JTextField(5);
        DocumentFilter filter = new DocumentFilter()
        {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
            {
                if (string.matches("\\d+"))
                {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException
            {
                if (text.matches("\\d+"))
                {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        };
        ((AbstractDocument)myManualProxyPortField.getDocument()).setDocumentFilter(filter);
        myAutoConfigProxyUrlField = new JTextField(20);
        String exTxt = "e.g.: *.first.com *.second.com";
        String helpTxt = "Space or comma separated hosts to exclude from the proxy. Use '*' to match any string.";
        myManualProxyExclusionsField = new GhostTextField(exTxt);
        myManualProxyExclusionsField.setColumns(20);
        myManualProxyExclusionsField.setToolTipText(helpTxt);
        mySystemProxyExclusionsField = new GhostTextField(exTxt);
        mySystemProxyExclusionsField.setColumns(20);
        mySystemProxyExclusionsField.setToolTipText(helpTxt);
        myAutoConfigProxyLabel = new JLabel("Configuration URL:");
        myManualProxyHostLabel = new JLabel("Proxy Host:");
        myManualProxyPortLabel = new JLabel("Proxy Port:");
        myManualProxyExclusionsLabel = new JLabel("Proxy Exclusions:");
        mySystemProxyExclusionsLabel = new JLabel("Proxy Exclusions:");

        myDefaultMap.put(mySystemProxyExclusionsLabel.getText(), " ");
        myDefaultMap.put(myAutoConfigProxyLabel.getText(), " ");
        myDefaultMap.put(myManualProxyPortLabel.getText(), "80");
        myDefaultMap.put(myManualProxyHostLabel.getText(), " ");
    }

    /**
     * Set the proxy enabled.
     *
     * @param systemProxiesEnabled If system proxies are enabled.
     * @param autoProxyEnabled If the auto proxy is enabled.
     * @param manualProxyEnabled If the manual proxy is enabled.
     */
    private void setProxyEnabled(boolean systemProxiesEnabled, boolean autoProxyEnabled, boolean manualProxyEnabled)
    {
        if (autoProxyEnabled)
        {
            myUseAutoProxyButton.setSelected(true);
            myAutoConfigProxyLabel.setEnabled(true);
            myAutoConfigProxyUrlField.setEnabled(true);
            myAutoConfigProxyUrlField.setText(getPreferences().getStringMap(myUseAutoProxyButton.getText(), myDefaultMap)
                    .get(myAutoConfigProxyLabel.getText()));
            myManualProxyHostLabel.setEnabled(false);
            myManualProxyHostField.setEnabled(false);
            myManualProxyPortLabel.setEnabled(false);
            myManualProxyPortField.setEnabled(false);
            myManualProxyExclusionsField.setEnabled(false);
            myManualProxyExclusionsLabel.setEnabled(false);
            mySystemProxyExclusionsField.setEnabled(false);
            mySystemProxyExclusionsLabel.setEnabled(false);
        }
        else if (manualProxyEnabled)
        {
            myUseManualProxyButton.setSelected(true);
            myAutoConfigProxyLabel.setEnabled(false);
            myAutoConfigProxyUrlField.setEnabled(false);
            myManualProxyHostLabel.setEnabled(true);
            myManualProxyHostField.setEnabled(true);
            myManualProxyPortLabel.setEnabled(true);
            myManualProxyPortField.setEnabled(true);
            myManualProxyExclusionsField.setEnabled(true);
            myManualProxyExclusionsLabel.setEnabled(true);
            mySystemProxyExclusionsField.setEnabled(false);
            mySystemProxyExclusionsLabel.setEnabled(false);
            myManualProxyHostField.setText(getPreferences().getStringMap(myUseManualProxyButton.getText(), myDefaultMap)
                    .get(myManualProxyHostLabel.getText()));
            myManualProxyPortField.setText(getPreferences().getStringMap(myUseManualProxyButton.getText(), myDefaultMap)
                    .get(myManualProxyPortLabel.getText()));

            int proxyPort = myNetworkConfigurationManager.getProxyPort();
            if (proxyPort == -1)
            {
                proxyPort = 80;
            }

            myManualProxyExclusionsField.setText(getPreferences().getStringMap(myUseManualProxyButton.getText(), myDefaultMap)
                    .get(myManualProxyExclusionsLabel.getText()));
        }
        else
        {
            if (systemProxiesEnabled)
            {
                myUseSystemProxiesButton.setSelected(true);

                mySystemProxyExclusionsField
                        .setText(getPreferences().getStringMap(myUseSystemProxiesButton.getText(), myDefaultMap)
                                .get(mySystemProxyExclusionsLabel.getText()));

                mySystemProxyExclusionsField.setEnabled(true);
                mySystemProxyExclusionsLabel.setEnabled(true);
            }
            else
            {
                myUseNoProxyButton.setSelected(true);
                mySystemProxyExclusionsField.setEnabled(false);
                mySystemProxyExclusionsLabel.setEnabled(false);
            }
            myAutoConfigProxyLabel.setEnabled(false);
            myAutoConfigProxyUrlField.setEnabled(false);
            myManualProxyHostLabel.setEnabled(false);
            myManualProxyHostField.setEnabled(false);
            myManualProxyPortLabel.setEnabled(false);
            myManualProxyPortField.setEnabled(false);
            myManualProxyExclusionsField.setEnabled(false);
            myManualProxyExclusionsLabel.setEnabled(false);
        }
    }

    /**
     * Save the fields as preferences in Proxy Settings.
     *
     * @param myKey Preference Key for saving
     * @param myProxyOptionPreferences A map of preferences
     * @param source - the source
     *
     */
    private void setProxyPreferenceValue(String myKey, Map<String, String> myProxyOptionPreferences, Object source)
    {
        if (myProxyOptionPreferences.containsValue(null))
        {
            myProxyOptionPreferences.replace(myKey, null, " ");
        }
        if (getPreferences().getStringMap(myKey, myDefaultMap) != myDefaultMap)
        {
            getPreferences().removeMap(myKey, source);
        }
        getPreferences().putStringMap(myKey, myProxyOptionPreferences, source);
    }

    /**
     * Return preferences registry.
     *
     * @return - preferences registry
     */
    private Preferences getPreferences()
    {
        return getPreferencesRegistry().getPreferences(NetworkConfigurationOptionsProvider.class);
    }
}
