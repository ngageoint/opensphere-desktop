package io.opensphere.core.security;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.security.CertificateUtilities;
import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.security.PrivateKeyProvider;
import io.opensphere.core.util.security.PrivateKeyProviderException;
import io.opensphere.core.util.security.SecurityUtilities;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * Simple certificate entry panel.
 */
public class CertificateEntryPanel extends GridBagPanel
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(CertificateEntryPanel.class);

    /** Title for the error dialogs. */
    private static final String ERROR_DIALOG_TITLE = "Error";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** How many columns to put in the text fields. */
    private static final int TEXT_FIELD_COLUMNS = 25;

    /** The certificate browse button. */
    private JButton myBrowseButton;

    /** The certificate file text field. */
    private JTextField myCertificateFileField;

    /** The certificate password text field. */
    private JPasswordField myCertificatePassword;

    /**
     * The factory used to get a cipher to encrypt imported private keys.
     */
    private final transient CipherFactory myCipherFactory;

    /** The toolbox. */
    private final transient PreferencesRegistry myPreferencesRegistry;

    /**
     * Constructor.
     *
     * @param preferencesRegistry the preferences registry
     * @param cipherFactory The factory used to get ciphers for
     *            encrypting/decrypting private keys.
     */
    public CertificateEntryPanel(PreferencesRegistry preferencesRegistry, CipherFactory cipherFactory)
    {
        myPreferencesRegistry = Utilities.checkNull(preferencesRegistry, "preferencesRegistry");
        myCipherFactory = Utilities.checkNull(cipherFactory, "cipherFactory");
        initialize();
    }

    /** Clear the password field. */
    public void clearPassword()
    {
        myCertificatePassword.setText(null);
    }

    /**
     * Get private key providers for the selected certificate file. If no
     * private keys can be extracted, present a message to the user and return
     * {@code null}.
     *
     * @return The private key with its certificate chain.
     */
    public Collection<? extends PrivateKeyProvider> getPrivateKeyProviders()
    {
        Collection<PrivateKeyProvider> providers = New.collection();

        final String certFileName = myCertificateFileField.getText();
        final char[] password = myCertificatePassword.getPassword();
        clearPassword();
        try
        {
            // Load a keystore
            KeyStore keystore = loadKeyStore(certFileName, password);
            if (keystore == null)
            {
                return providers;
            }

            try
            {
                String errorMessage = null;
                Enumeration<String> aliases = keystore.aliases();
                while (aliases.hasMoreElements())
                {
                    String alias = aliases.nextElement();
                    try
                    {
                        if (keystore.isKeyEntry(alias))
                        {
                            providers.add(SecurityUtilities.createCipherEncryptedPrivateKeyProvider(keystore, alias, "User",
                                    password, myCipherFactory, true));
                        }
                    }
                    catch (PrivateKeyProviderException e)
                    {
                        LOGGER.debug("Private key provider exception encountered.", e);
                        LOGGER.warn(e.getMessage(), e.getCause());
                        errorMessage = e.getMessage();
                    }
                    catch (NoSuchAlgorithmException e)
                    {
                        LOGGER.error(e, e);
                    }
                }

                if (providers.isEmpty())
                {
                    if (errorMessage == null)
                    {
                        JOptionPane.showMessageDialog(this, "No private keys were found in the file.", ERROR_DIALOG_TITLE,
                                JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(this, errorMessage, ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            catch (KeyStoreException e)
            {
                LOGGER.error("Unexpected error while loading certificate file.", e);
                JOptionPane.showMessageDialog(this, "The file could not be loaded.", ERROR_DIALOG_TITLE,
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        finally
        {
            // Clear the password for better security.
            Arrays.fill(password, '\0');
        }

        return providers;
    }

    /**
     * Initializes the panel.
     */
    private void initialize()
    {
        myCertificateFileField = new JTextField(TEXT_FIELD_COLUMNS);

        myBrowseButton = new JButton("Browse");
        myBrowseButton.setMargin(new Insets(3, 6, 3, 6));
        myBrowseButton.addActionListener(new ActionListener()
        {
            private MnemonicFileChooser myFileChooser;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                // If the file chooser is null, initialize it.
                if (myFileChooser == null)
                {
                    myFileChooser = new MnemonicFileChooser(myPreferencesRegistry, "CertificateEntryPanel");

                    myFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Java KeyStores", "jks", "keystore"));
                    myFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PKCS#12 Files", "p12", "pfx"));
                    FileNameExtensionFilter allSupportedFilter = new FileNameExtensionFilter("All Supported Certificates Types",
                            "p12", "pfx", "jks", "keystore");
                    myFileChooser.addChoosableFileFilter(allSupportedFilter);
                    myFileChooser.setFileFilter(allSupportedFilter);
                }

                // Show the file chooser dialog and update the filename if a
                // file is selected.
                if (myFileChooser.showOpenDialog(CertificateEntryPanel.this) == JFileChooser.APPROVE_OPTION)
                {
                    File file = myFileChooser.getSelectedFile();
                    myCertificateFileField.setText(file.getAbsolutePath());
                }
            }
        });

        myCertificatePassword = new JPasswordField(TEXT_FIELD_COLUMNS);

        init0();
        style("label").anchorWest();
        style("input").fillHorizontal().setInsets(0, 3, 5, 0);
        style("button").setInsets(0, 3, 5, 0);
        style("label", "input", "button").addRow(new JLabel("File:"), myCertificateFileField, myBrowseButton);
        style("label", "input").addRow(new JLabel("Password:"), myCertificatePassword);
    }

    /**
     * Load a keystore if possible. Notify the user if the keystore cannot be
     * loaded.
     *
     * @param filename The filename of the keystore.
     * @param password The password for the keystore.
     * @return The loaded keystore, or {@code null}.
     */
    private KeyStore loadKeyStore(final String filename, final char[] password)
    {
        Throwable problem = null;
        KeyStore keystore;
        for (String keyStoreType : CertificateUtilities.getAvailableKeyStoreAlgorithms())
        {
            try
            {
                keystore = KeyStore.getInstance(keyStoreType);
            }
            catch (KeyStoreException e)
            {
                LOGGER.error("Failed to load keystore.", e);
                return null;
            }
            FileInputStream keyStorefileInputStream;
            try
            {
                keyStorefileInputStream = new FileInputStream(filename);
            }
            catch (FileNotFoundException e)
            {
                LOGGER.error("Failed to load file [" + filename + "]", e);
                JOptionPane.showMessageDialog(this, "Could not open file " + filename + " (file is missing)");
                return null;
            }
            try
            {
                keystore.load(keyStorefileInputStream, password);
                return keystore;
            }
            catch (NoSuchAlgorithmException e)
            {
                LOGGER.error(e, e);
                JOptionPane.showMessageDialog(this, "An appropriate algorithm could not be found to load that file.",
                        ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                return null;
            }
            catch (CertificateException e)
            {
                LOGGER.error(e, e);
                JOptionPane.showMessageDialog(this, "The certificates in that file seem to be corrupted.", ERROR_DIALOG_TITLE,
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
            catch (IOException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e, e);
                }
                if (problem == null)
                {
                    problem = e.getCause();
                }
            }
            finally
            {
                try
                {
                    keyStorefileInputStream.close();
                }
                catch (IOException e)
                {
                    LOGGER.warn(e);
                }
            }
        }

        if (problem == null)
        {
            JOptionPane.showMessageDialog(this, "The certificate file format is not recognized.", ERROR_DIALOG_TITLE,
                    JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(this, "The provided password is incorrect or the file is corrupt.", ERROR_DIALOG_TITLE,
                    JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }
}
