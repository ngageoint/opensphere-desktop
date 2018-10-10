package io.opensphere.core.security.options;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.security.config.v1.SecurityConfiguration;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.security.EncryptedUsernamePassword;

/**
 * An {@link OptionsProvider} for usernames/passwords.
 */
public class UsernamePasswordOptionsProvider extends AbstractTableOptionsProvider
{
    /**
     * Construct the security options provider.
     *
     * @param securityManager The security manager.
     * @param prefsRegistry The system preferences registry.
     */
    public UsernamePasswordOptionsProvider(SecurityManager securityManager, PreferencesRegistry prefsRegistry)
    {
        super(securityManager, prefsRegistry, "Passwords");
    }

    @Override
    protected TableModel buildTableModel()
    {
        Collection<? extends EncryptedUsernamePassword> usernamePasswords = getConfig().getUsernamePasswords();
        Object[][] usernamePasswordData = new Object[usernamePasswords.size()][];
        int index = 0;
        for (EncryptedUsernamePassword encryptedUsernamePassword : usernamePasswords)
        {
            usernamePasswordData[index] = new Object[2];
            usernamePasswordData[index][0] = encryptedUsernamePassword.getPurpose();
            usernamePasswordData[index][1] = encryptedUsernamePassword.getUsername();
            ++index;
        }
        return new DefaultTableModel(usernamePasswordData, new Object[] { "Purpose", "Username" })
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
    }

    @Override
    protected void deleteRow(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.passwords.delete-button");

        SecurityConfiguration config = getConfig().clone();
        Collection<? extends EncryptedUsernamePassword> usernamePasswords = config.getUsernamePasswords();
        EncryptedUsernamePassword item = CollectionUtilities.getItem(usernamePasswords, row);
        usernamePasswords.remove(item);
        saveConfig(config);
    }

    @Override
    protected Component getDescriptionComponent()
    {
        return new JLabel(
                "<html>These are saved usernames and passwords associated with servers. The passwords are stored encrypted.</html>");
    }

    @Override
    protected boolean showDeleteMessageDialog()
    {
        assert EventQueue.isDispatchThread();

        return JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(getTable()),
                "This will delete usernames and passwords from your configuration. Are you sure?", "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null) == JOptionPane.OK_OPTION;
    }

    @Override
    protected void showDetails(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.passwords.details-button");

        assert EventQueue.isDispatchThread();

        EncryptedUsernamePassword item = CollectionUtilities.getItem(getConfig().getUsernamePasswords(), row);
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()),
                "An encrypted password is stored for username \"" + item.getUsername() + "\" associated with " + item.getPurpose()
                + ".");
    }
}
