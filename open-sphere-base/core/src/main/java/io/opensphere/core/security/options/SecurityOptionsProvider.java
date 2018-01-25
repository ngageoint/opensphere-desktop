package io.opensphere.core.security.options;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;

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

        JButton clearButton = new JButton("Clear Local Encrypted Data");
        clearButton.setToolTipText("<html>Clears any passwords, private keys, or other encrypted data stored locally."
                + " This will also allow you to set a new encryption secret.</html>");
        clearButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int option = JOptionPane.showOptionDialog(optionsBox,
                        "This will delete all usernames/passwords, private keys, and other encrypted data from your configuration. Are you sure?",
                        "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (option == JOptionPane.OK_OPTION)
                {
                    getSecurityManager().clearEncryptedData();
                }
            }
        });
        optionsBox.add(Box.createVerticalStrut(10));
        optionsBox.add(clearButton);

        JButton resetButton = new JButton("Reset Encryption Secret");
        resetButton.setToolTipText("<html>Reset the secret (private key or master password) used to encrypt local data.</html>");
        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getSecurityManager().resetSecretKey();
            }
        });
        optionsBox.add(Box.createVerticalStrut(10));
        optionsBox.add(resetButton);

        JButton clearCertificateAssociationsButton = new JButton("Clear Certificate Associations");
        clearCertificateAssociationsButton
                .setToolTipText("Clears the associations that have been made between personal certificates and servers.");
        clearCertificateAssociationsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int option = JOptionPane.showOptionDialog(optionsBox,
                        "When new connections are made that require certificates, you will be prompted to select the certificates to use.",
                        "Confirm Clear", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (option == JOptionPane.OK_OPTION)
                {
                    getSecurityManager().clearPreselectedAndPreferredPrivateKeyProviders();
                }
            }
        });
        optionsBox.add(Box.createVerticalStrut(10));
        optionsBox.add(clearCertificateAssociationsButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(optionsBox);
        return panel;
    }

    @Override
    protected void handlePreferenceChange()
    {
    }
}
