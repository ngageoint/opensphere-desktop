package io.opensphere.core.net;

import java.awt.GridBagConstraints;
import java.util.Arrays;
import java.util.stream.Collectors;

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
import io.opensphere.core.Toolbox;
import io.opensphere.core.net.config.ConfigurationType;
import io.opensphere.core.net.config.ManualProxyConfiguration;
import io.opensphere.core.net.config.SystemProxyConfiguration;
import io.opensphere.core.net.config.UrlProxyConfiguration;
import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.swing.GhostTextField;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * Options provider for the network configuration.
 */
public class NetworkConfigurationOptionsProvider extends AbstractPreferencesOptionsProvider
{
    /** The text entry field for the auto config URL. */
    private JTextField myAutoConfigProxyUrlField;

    /** The text entry field for the proxy exclusions. */
    private JTextField myManualProxyExclusionsField;

    /** The text entry field for the manual proxy host. */
    private JTextField myManualProxyHostField;

    /** The text entry field for the manual proxy port. */
    private JTextField myManualProxyPortField;

    /** The network configuration manager. */
    private final NetworkConfigurationManager myNetworkConfigurationManager;

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

    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param networkConfigurationManager The network configuration manager.
     * @param prefsRegistry the preferences registry
     */
    public NetworkConfigurationOptionsProvider(Toolbox toolbox, NetworkConfigurationManager networkConfigurationManager,
            PreferencesRegistry prefsRegistry)
    {
        super(prefsRegistry, "Network");
        myToolbox = toolbox;
        myNetworkConfigurationManager = networkConfigurationManager;
    }

    @Override
    public void applyChanges()
    {
        if (myUseSystemProxiesButton.isSelected())
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.network.use-system-proxy-settings");
            myNetworkConfigurationManager.setSelectedProxyType(ConfigurationType.SYSTEM);
        }
        else if (myUseAutoProxyButton.isSelected())
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.network.automatic-proxy-configuration");
            myNetworkConfigurationManager.setSelectedProxyType(ConfigurationType.URL);
        }
        else if (myUseManualProxyButton.isSelected())
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.network.manual-proxy-configuration");
            myNetworkConfigurationManager.setSelectedProxyType(ConfigurationType.MANUAL);
        }
        else
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.network.no-proxy");
            myNetworkConfigurationManager.setSelectedProxyType(ConfigurationType.NONE);
        }

        SystemProxyConfiguration systemConfiguration = myNetworkConfigurationManager.getSystemConfiguration();
        systemConfiguration.getExclusionPatterns().clear();
        systemConfiguration.getExclusionPatterns().addAll(Arrays.asList(mySystemProxyExclusionsField.getText().split(",\\s*|\\s+")));

        UrlProxyConfiguration urlConfiguration = myNetworkConfigurationManager.getUrlConfiguration();
        urlConfiguration.setProxyUrl(myAutoConfigProxyUrlField.getText());

        try
        {
            // parse the number BEFORE MAKING ANY CHANGES:
            int port = Integer.parseInt(myManualProxyPortField.getText());

            ManualProxyConfiguration configuration = myNetworkConfigurationManager.getManualConfiguration();
            configuration.setHost(myManualProxyHostField.getText());
            configuration.setPort(port);

            configuration.getExclusionPatterns().clear();
            configuration.getExclusionPatterns().addAll(Arrays.asList(myManualProxyExclusionsField.getText().split(",\\s*|\\s+")));
        }
        catch (@SuppressWarnings("unused") NumberFormatException e)
        {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(myManualProxyHostField),
                    "Could not parse port number.");
        }

        myNetworkConfigurationManager.persistConfiguration();
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
        panel.style(null, labelStyle, controlStyle).addRow(null, new JLabel("Proxy Exclusions:"), mySystemProxyExclusionsField);
        panel.style(buttonStyle).addRow(myUseAutoProxyButton);
        panel.style(null, labelStyle, controlStyle).addRow(null, new JLabel("Configuration URL:"), myAutoConfigProxyUrlField);
        panel.style(buttonStyle).addRow(myUseManualProxyButton);
        panel.style(null, labelStyle, controlStyle).addRow(null, new JLabel("Proxy Host:"), myManualProxyHostField);
        panel.style(null, labelStyle, controlStyle).addRow(null, new JLabel("Proxy Port:"), myManualProxyPortField);
        panel.style(null, labelStyle, controlStyle).addRow(null, new JLabel("Proxy Exclusions:"), myManualProxyExclusionsField);

        revertToSavedConfiguration();

        return panel;
    }

    @Override
    public void restoreDefaults()
    {
        myNetworkConfigurationManager.restoreDefaults();
        revertToSavedConfiguration();
    }

    /** Initializes components. */
    private void initializeComponents()
    {
        myUseNoProxyButton = new JRadioButton("No proxy");
        myUseSystemProxiesButton = new JRadioButton("Use system proxy settings");
        myUseManualProxyButton = new JRadioButton("Manual proxy configuration");
        myUseAutoProxyButton = new JRadioButton("Automatic proxy configuration");

        ButtonGroup group = new ButtonGroup();
        group.add(myUseNoProxyButton);
        group.add(myUseSystemProxiesButton);
        group.add(myUseAutoProxyButton);
        group.add(myUseAutoProxyButton);

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
    }

    /**
     * Set the proxy enabled.
     */
    private void revertToSavedConfiguration()
    {
        // populate all proxy fields from configuration:
        SystemProxyConfiguration systemConfiguration = myNetworkConfigurationManager.getSystemConfiguration();
        mySystemProxyExclusionsField
                .setText(systemConfiguration.getExclusionPatterns().stream().collect(Collectors.joining(" ")));

        UrlProxyConfiguration urlConfiguration = myNetworkConfigurationManager.getUrlConfiguration();
        myAutoConfigProxyUrlField.setText(urlConfiguration.getProxyUrl());

        ManualProxyConfiguration manualConfiguration = myNetworkConfigurationManager.getManualConfiguration();
        myManualProxyHostField.setText(manualConfiguration.getHost());
        myManualProxyPortField.setText(Integer.toString(manualConfiguration.getPort()));
        myManualProxyExclusionsField
                .setText(manualConfiguration.getExclusionPatterns().stream().collect(Collectors.joining(" ")));

        ConfigurationType selectedProxyType = myNetworkConfigurationManager.getSelectedProxyType();
        switch (selectedProxyType)
        {
            case NONE:
                myUseNoProxyButton.setSelected(true);
                break;
            case SYSTEM:
                myUseSystemProxiesButton.setSelected(true);
                break;
            case URL:
                myUseAutoProxyButton.setSelected(true);
                break;
            case MANUAL:
                myUseManualProxyButton.setSelected(true);
                break;
            default:
                throw new UnsupportedOperationException("Unrecognized proxy type: " + selectedProxyType.name());
        }
    }
}
