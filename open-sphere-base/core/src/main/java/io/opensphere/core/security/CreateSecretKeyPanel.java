package io.opensphere.core.security;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Window;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.security.config.v1.CryptoConfig;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.ValidatorSupport.ValidationStatusChangeListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.security.Digest;
import io.opensphere.core.util.security.PrivateKeyProvider;
import io.opensphere.core.util.security.PrivateKeyProviderFilter;
import io.opensphere.core.util.security.SecurityUtilities;
import io.opensphere.core.util.security.WrappedSecretKey;
import io.opensphere.core.util.swing.RadioButtonPanel;

/**
 * A panel that allows entry of a master password and/or a certificate to be
 * used to derive a secret key.
 */
public class CreateSecretKeyPanel extends JPanel implements Validatable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CreateSecretKeyPanel.class);

    /** The password option. */
    private static final String PASSWORD_OPTION = "Master Password";

    /** The private key option. */
    private static final String PRIVATE_KEY_OPTION = "Private Key";

    /** Length of the salt in bytes for PBE secret keys. */
    private static final int SALT_LENGTH = Utilities.parseSystemProperty("opensphere.security.pbe.saltlength", 16);

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The button panel. */
    private RadioButtonPanel<String> myButtonPanel;

    /** The certificate panel. */
    private CertificateSelectionPanel myCertificateSelectionPanel;

    /** The password panel. */
    private CreatePasswordPanel myPasswordPanel;

    /**
     * Callback called when the radio button is changed.
     */
    private final transient Callback<ValidationStatusChangeListener> mySelectionChangeCallback = new Callback<ValidationStatusChangeListener>()
    {
        @Override
        public void notify(ValidationStatusChangeListener listener)
        {
            if (isPasswordOptionSelected())
            {
                myValidatorSupport.setValidationResult(myPasswordPanel.getValidatorSupport());
            }
            else
            {
                myValidatorSupport.setValidationResult(myCertificateSelectionPanel.getValidatorSupport().getValidationStatus(),
                        null);
            }
        }
    };

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Construct the panel.
     *
     * @param promptMessage The prompt to display for the user.
     * @param allowPrivateKey Indicates if a private key can be selected.
     * @param filter Optional filter on private keys that can be selected.
     * @param securityManager The system security manager.
     * @param preferencesRegistry The system preferences registry.
     */
    public CreateSecretKeyPanel(String promptMessage, boolean allowPrivateKey, PrivateKeyProviderFilter filter,
            SecurityManager securityManager, PreferencesRegistry preferencesRegistry)
    {
        super(new BorderLayout());
        initialize(promptMessage, allowPrivateKey, filter, securityManager, preferencesRegistry);
    }

    /**
     * Generate a secret key and crypto config based on the user selections.
     *
     * @param algorithm The crypto algorithm.
     * @return The secret key and crypto config, or {@code null} if there is a
     *         recoverable error.
     * @throws NoSuchAlgorithmException If an algorithm cannot be loaded.
     */
    public Pair<SecretKey, CryptoConfig> generateSecretKeyAndCryptoConfig(String algorithm) throws NoSuchAlgorithmException
    {
        if (isPasswordOptionSelected())
        {
            final char[] pw = getPassword();
            try
            {
                final byte[] salt = new byte[SALT_LENGTH];
                SecurityUtilities.generateSalt(salt);
                final SecretKey result = SecurityUtilities.getAESSecretKey(pw, salt);

                final byte[] digestSalt = new byte[SALT_LENGTH];
                SecurityUtilities.generateSalt(digestSalt);
                final byte[] pwDigest = SecurityUtilities.getPBEHash(pw, digestSalt);

                final CryptoConfig config = new CryptoConfig(new Digest("PBE", pwDigest, digestSalt), salt);
                return Pair.create(result, config);
            }
            catch (final NoSuchAlgorithmException e)
            {
                throw new NoSuchAlgorithmException("Failed to generate password hash: " + e, e);
            }
            finally
            {
                Arrays.fill(pw, '\0');
            }
        }
        else
        {
            final PrivateKeyProvider privateKeyProvider = getSelectedPrivateKeyProvider();
            try
            {
                final SecretKey result = KeyGenerator.getInstance(algorithm).generateKey();
                final CryptoConfig config = new CryptoConfig(new WrappedSecretKey(result, privateKeyProvider));
                return Pair.create(result, config);
            }
            catch (final NoSuchAlgorithmException e)
            {
                throw new NoSuchAlgorithmException("Failed to get AES key generator: " + e, e);
            }
            catch (final GeneralSecurityException e)
            {
                showPrivateKeyErrorDialog(privateKeyProvider, e);
                return null;
            }
        }
    }

    /**
     * Get the password from the password field. For better security the array
     * should be cleared after the password is used.
     *
     * @return The password.
     */
    public char[] getPassword()
    {
        return getPasswordPanel().getPassword();
    }

    /**
     * Get the selected private key provider.
     *
     * @return The private key provider.
     */
    public PrivateKeyProvider getSelectedPrivateKeyProvider()
    {
        return myCertificateSelectionPanel.getSelectedItem();
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Get if the password option is selected.
     *
     * @return {@code true} if the password option is selected.
     */
    public final boolean isPasswordOptionSelected()
    {
        return myButtonPanel == null || PASSWORD_OPTION.equals(myButtonPanel.getSelection());
    }

    /**
     * Get the certificate selection panel.
     *
     * @param filter The filter on the available certificates.
     * @param securityManager The system security manager.
     * @param preferencesRegistry The system preferences registry.
     * @return The certificate selection panel.
     */
    private CertificateSelectionPanel getCertSelectorPanel(PrivateKeyProviderFilter filter, SecurityManager securityManager,
            PreferencesRegistry preferencesRegistry)
    {
        if (myCertificateSelectionPanel == null)
        {
            myCertificateSelectionPanel = new CertificateSelectionPanel(securityManager, preferencesRegistry, filter, null,
                    false);
            // Do not pass the message here because the certificate
            // selection panel displays it already.
            myCertificateSelectionPanel.getValidatorSupport().addAndNotifyValidationListener((Object object,
                    final ValidationStatus valid, final String message) -> myValidatorSupport.setValidationResult(valid, null));
        }
        return myCertificateSelectionPanel;
    }

    /**
     * Get the password panel used to enter a new master password.
     *
     * @return The password panel.
     */
    private CreatePasswordPanel getPasswordPanel()
    {
        if (myPasswordPanel == null)
        {
            final int minPasswordLength = Utilities.parseSystemProperty("opensphere.password.minLength", 0);
            final int minPasswordCharacterCategories = Utilities.parseSystemProperty("opensphere.password.minCharacterCategories", 0);
            myPasswordPanel = new CreatePasswordPanel(minPasswordLength, minPasswordCharacterCategories);
            myPasswordPanel.getValidatorSupport().addAndNotifyValidationListener((Object object, final ValidationStatus valid,
                    final String message) -> myValidatorSupport.setValidationResult(valid, message));
        }
        return myPasswordPanel;
    }

    /**
     * Initialize the display.
     *
     * @param promptMessage The prompt to display for the user.
     * @param allowPrivateKey Indicates if a private key can be selected.
     * @param filter Optional filter on private keys that can be selected.
     * @param securityManager The system security manager.
     * @param preferencesRegistry The system preferences registry.
     */
    private void initialize(String promptMessage, boolean allowPrivateKey, PrivateKeyProviderFilter filter,
            SecurityManager securityManager, PreferencesRegistry preferencesRegistry)
    {
        final CardLayout cardLayout = new CardLayout();
        final JPanel centerPane = new JPanel(cardLayout);
        centerPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        centerPane.add(getPasswordPanel(), PASSWORD_OPTION);

        final JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        if (promptMessage != null)
        {
            final JLabel label = new JLabel(promptMessage);
            final JPanel labelPanel = new JPanel(new BorderLayout());
            labelPanel.add(label);
            northPanel.add(labelPanel);
        }

        if (allowPrivateKey)
        {
            centerPane.add(getCertSelectorPanel(filter, securityManager, preferencesRegistry), PRIVATE_KEY_OPTION);

            myButtonPanel = new RadioButtonPanel<>(New.list(PRIVATE_KEY_OPTION, PASSWORD_OPTION), PRIVATE_KEY_OPTION);
            myButtonPanel.addActionListener(e ->
            {
                final JRadioButton btn = (JRadioButton)e.getSource();
                final String selectedItem = btn.getText();
                cardLayout.show(centerPane, selectedItem);

                myValidatorSupport.notifyListeners(mySelectionChangeCallback);

                final Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null)
                {
                    window.pack();
                }
            });

            final JLabel label = new JLabel("Please select an encryption method for your local data:");
            final JPanel labelPanel = new JPanel();
            labelPanel.add(label);

            final JPanel panel = new JPanel();
            panel.add(labelPanel);
            panel.add(myButtonPanel);
            northPanel.add(panel);

            cardLayout.show(centerPane, myButtonPanel.getSelection());
        }
        else
        {
            cardLayout.show(centerPane, PASSWORD_OPTION);
            myButtonPanel = null;
        }

        add(northPanel, BorderLayout.NORTH);
        add(centerPane, BorderLayout.CENTER);
    }

    /**
     * Show private key error dialog.
     *
     * @param privateKeyProvider the private key provider
     * @param e the e
     */
    private void showPrivateKeyErrorDialog(PrivateKeyProvider privateKeyProvider, Exception e)
    {
        LOGGER.error("Failed to wrap secret key using private key with alias " + privateKeyProvider.getAlias() + ": " + e, e);
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "There was an error using that private key.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
