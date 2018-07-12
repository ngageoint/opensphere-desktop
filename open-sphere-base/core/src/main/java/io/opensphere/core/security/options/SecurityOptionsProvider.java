package io.opensphere.core.security.options;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.QuantifyToolboxUtils;

/**
 * An {@link OptionsProvider} for security components.
 */
public class SecurityOptionsProvider extends AbstractSecurityOptionsProvider
{
    /**
     * Construct the security options provider.
     *
     * @param securityManager The system security manager.
     * @param prefsRegistry The system preferences registry.
     */
    public SecurityOptionsProvider(SecurityManager securityManager, PreferencesRegistry prefsRegistry)
    {
        super(securityManager, prefsRegistry, "Security");
        addSubTopic(new UsernamePasswordOptionsProvider(securityManager, prefsRegistry));
        addSubTopic(new PersonalCertificateOptionsProvider(securityManager, prefsRegistry));
        addSubTopic(new TrustedServersOptionsProvider(securityManager, prefsRegistry));
        addSubTopic(new TrustedCertificatesOptionsProvider(securityManager, prefsRegistry));
    }

    @Override
    public JPanel getOptionsPanel()
    {
        final Box optionsBox = Box.createVerticalBox();
        optionsBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        optionsBox.add(Box.createVerticalStrut(10));
        optionsBox.add(getClearButton(optionsBox));
        optionsBox.add(Box.createVerticalStrut(10));
        optionsBox.add(getResetButton());
        optionsBox.add(Box.createVerticalStrut(10));
        optionsBox.add(getClearCertificateAssociationsButton(optionsBox));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(optionsBox);
        return panel;
    }

    /**
     * Get the clear local encrypted data button.
     *
     * @param parentComponent the parent component
     * @return the clear local encrypted data button
     */
    private Component getClearButton(Component parentComponent)
    {
        JButton clearButton = new JButton("Clear Local Encrypted Data");
        clearButton.setToolTipText("<html>Clears any passwords, private keys, or other encrypted data stored locally."
                + " This will also allow you to set a new encryption secret.</html>");
        clearButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric("mist3d.settings.security.clear-local-encrypted-data-button");
            int option = JOptionPane.showOptionDialog(parentComponent,
                    "This will delete all usernames/passwords, private keys, and other encrypted data from your configuration. Are you sure?",
                    "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if (option == JOptionPane.OK_OPTION)
            {
                getSecurityManager().clearEncryptedData();
            }
        });
        return clearButton;
    }

    /**
     * Gets the reset encryption secret button.
     *
     * @return the reset encryption secret button
     */
    private Component getResetButton()
    {
        JButton resetButton = new JButton("Reset Encryption Secret");
        resetButton.setToolTipText("<html>Reset the secret (private key or master password) used to encrypt local data.</html>");
        resetButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric("mist3d.settings.security.reset-encryption-secret-button");
            getSecurityManager().resetSecretKey();
        });
        return resetButton;
    }

    /**
     * Gets the clear certificate associations button.
     *
     * @param parentComponent the parent component
     * @return the clear certificate associations button
     */
    private Component getClearCertificateAssociationsButton(Component parentComponent)
    {
        JButton clearCertificateAssociationsButton = new JButton("Clear Certificate Associations");
        clearCertificateAssociationsButton
                .setToolTipText("Clears the associations that have been made between personal certificates and servers.");
        clearCertificateAssociationsButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric("mist3d.settings.security.clear-cerificate-associations-button");
            int option = JOptionPane.showOptionDialog(parentComponent,
                    "When new connections are made that require certificates, you will be prompted to select the certificates to use.",
                    "Confirm Clear", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if (option == JOptionPane.OK_OPTION)
            {
                getSecurityManager().clearPreselectedAndPreferredPrivateKeyProviders();
            }
        });
    }

    @Override
    protected void handlePreferenceChange()
    {
    }
}
