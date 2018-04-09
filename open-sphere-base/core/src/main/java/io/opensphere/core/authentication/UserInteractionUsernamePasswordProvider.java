package io.opensphere.core.authentication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.core.util.security.CipherEncryptedUsernamePasswordProvider;
import io.opensphere.core.util.security.CipherException;
import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.security.DecryptionException;
import io.opensphere.core.util.security.EncryptedUsernamePassword;
import io.opensphere.core.util.security.UsernamePasswordProvider;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * A way to get a username/password, either from the security manager or from
 * the user.
 */
public class UserInteractionUsernamePasswordProvider extends UserInteractionAuthenticator
        implements InteractiveUsernamePasswordProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(UserInteractionUsernamePasswordProvider.class);

    /** Flag that indicates if the user has been queried yet. */
    private boolean myQueryDone;

    /**
     * Constructor.
     *
     * @param serverName The server name to present to the user in dialog text.
     * @param serverKey The unique key to use for the server in the preferences.
     * @param parentProvider The parent component provider.
     * @param securityManager The system security manager.
     * @param timeBudget Optional time budget.
     */
    public UserInteractionUsernamePasswordProvider(String serverName, String serverKey,
            Supplier<? extends Component> parentProvider, SecurityManager securityManager, PausingTimeBudget timeBudget)
    {
        super(serverName, serverKey, parentProvider, securityManager, timeBudget);
    }

    /**
     * Method called when authentication with the server fails.
     */
    @Override
    public void failedAuthentication()
    {
        getSecurityManager().clearUsernamePasswordProvider(getServerKey());
    }

    /**
     * Get the password, querying the user if necessary.
     *
     * @return The password.
     */
    @Override
    public char[] getPassword()
    {
        boolean timeBudgetPaused = getTimeBudget() != null && getTimeBudget().pause();
        try
        {
            int retryCount = 0;
            while (retryCount < 2)
            {
                UsernamePasswordProvider provider = getProvider();
                try
                {
                    return provider == null ? new char[0] : provider.getPassword();
                }
                catch (DecryptionException e)
                {
                    LOGGER.error(e, e);
                    getSecurityManager().clearUsernamePasswordProvider(getServerKey());
                    myQueryDone = false;
                    ++retryCount;
                }
            }
            return new char[0];
        }
        finally
        {
            if (timeBudgetPaused)
            {
                getTimeBudget().unpause();
            }
        }
    }

    /**
     * Get the username, querying the user if necessary.
     *
     * @return The username.
     */
    @Override
    public String getUsername()
    {
        boolean timeBudgetPaused = getTimeBudget() != null && getTimeBudget().pause();
        try
        {
            UsernamePasswordProvider provider = getProvider();
            return provider == null ? "" : provider.getUsername();
        }
        finally
        {
            if (timeBudgetPaused)
            {
                getTimeBudget().unpause();
            }
        }
    }

    /**
     * Create the panel for the fields.
     *
     * @param usernameField The username field.
     * @param passwordField The password field.
     * @param rememberCheckbox The "remember" checkbox.
     * @return The panel.
     */
    protected JPanel createPanel(final JTextField usernameField, JPasswordField passwordField, JCheckBox rememberCheckbox)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0;
        gbc.insets.top = 3;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel("<html>A username and password are being requested by <h3>" + getServerKey() + "</h3></html"), gbc);

        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.top = 15;
        panel.add(new JLabel(getUsernameLabelText()), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);

        ++gbc.gridy;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.top = 3;
        panel.add(new JLabel("Password: "), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        ++gbc.gridy;
        gbc.insets.top = 5;
        JPanel checkboxPanel = new JPanel(new BorderLayout());
        checkboxPanel.add(rememberCheckbox);
        panel.add(checkboxPanel, gbc);
        return panel;
    }

    /**
     * Gets the text to use for the username label.
     *
     * @return The username label text.
     */
    protected String getUsernameLabelText()
    {
        return "Username: ";
    }

    /**
     * Ask the user to select a username/password provider.
     *
     * @return The selected provider.
     */
    private UsernamePasswordProvider doQueryUserForProvider()
    {
        assert EventQueue.isDispatchThread();

        final JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JCheckBox rememberCheckbox = new JCheckBox("Remember this password", false);
        rememberCheckbox.setToolTipText("<html>Checking this box will cause this password to be saved encrypted to a file."
                + "<p/>Be advised that saving passwords is prohibited by some security policies.</html>");

        JPanel panel = createPanel(usernameField, passwordField, rememberCheckbox);
        panel.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                usernameField.requestFocusInWindow();
            }
        });

        CipherEncryptedUsernamePasswordProvider provider;

        JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(getParent(), "Authentication Required");
        dialog.addWindowFocusListener(new WindowAdapter()
        {
            @Override
            public void windowGainedFocus(WindowEvent e)
            {
                usernameField.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
        dialog.dispose();
        int choice = ((Integer)pane.getValue()).intValue();
        if (choice == JOptionPane.OK_OPTION)
        {
            EncryptedUsernamePassword encryptedUsernamePassword = new EncryptedUsernamePassword();
            encryptedUsernamePassword.setPurpose(getServerKey());
            encryptedUsernamePassword.setUsername(usernameField.getText());

            char[] password = passwordField.getPassword();
            try
            {
                CipherFactory cipherFactory;
                if (rememberCheckbox.isSelected())
                {
                    cipherFactory = getSecurityManager().getCipherFactory();
                }
                else
                {
                    cipherFactory = getSecurityManager().getSessionOnlyCipherFactory();
                }
                encryptedUsernamePassword.setPassword(password, cipherFactory);
                provider = new CipherEncryptedUsernamePasswordProvider(encryptedUsernamePassword, cipherFactory,
                        rememberCheckbox.isSelected());
                getSecurityManager().addUsernamePasswordProvider(provider);
            }
            catch (CipherException e)
            {
                LOGGER.error("Failed to encrypt password: " + e, e);
                provider = null;
            }
            finally
            {
                Arrays.fill(password, '\0');
            }
        }
        else
        {
            provider = null;
        }

        return provider;
    }

    /**
     * Get the provider, either from the security manager or by querying the
     * user.
     *
     * @return The username/password provider.
     */
    private UsernamePasswordProvider getProvider()
    {
        synchronized (UsernamePasswordProvider.class)
        {
            UsernamePasswordProvider provider = getSecurityManager().getUsernamePasswordProvider(getServerKey());
            if (provider == null && !myQueryDone)
            {
                provider = queryUserForProvider();
                myQueryDone = true;
            }
            return provider;
        }
    }

    /**
     * Query the user to get the username/password provider.
     *
     * @return The provider, or {@code null} if the user cancelled.
     */
    private UsernamePasswordProvider queryUserForProvider()
    {
        return EventQueueUtilities.happyOnEdt(() -> doQueryUserForProvider());
    }
}
