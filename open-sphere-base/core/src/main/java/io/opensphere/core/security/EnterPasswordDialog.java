package io.opensphere.core.security;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import io.opensphere.core.util.security.Digest;
import io.opensphere.core.util.security.SecurityUtilities;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.OptionDialog;

/**
 * Generates a dialog that prompts the user for a password.
 */
public final class EnterPasswordDialog
{
    /**
     * Prompt for a password.
     *
     * @param dialogParent A parent component used to position the dialog.
     * @param prompt The prompt.
     * @param passwordDigest The digest for the requested password.
     * @return The password, or {@code null} if the user cancelled.
     */
    public static char[] promptForPassword(Component dialogParent, String prompt, Digest passwordDigest)
    {
        assert EventQueue.isDispatchThread();

        do
        {
            JPasswordField passwordField = new JPasswordField(20);

            GridBagPanel panel = new GridBagPanel();
            panel.anchorWest();
            panel.addRow(new JLabel("Master password:"));
            panel.addRow(passwordField);

            try
            {
                OptionDialog dialog = new OptionDialog(JOptionPane.getFrameForComponent(dialogParent), panel, prompt);
                dialog.getRootPane().setDefaultButton(dialog.getDialogButtonPanel().getButton(ButtonPanel.OK));
                dialog.buildAndShow();
                if (dialog.getSelection() == JOptionPane.OK_OPTION)
                {
                    char[] password = passwordField.getPassword();
                    byte[] salt = passwordDigest.getSalt();
                    byte[] hash = SecurityUtilities.getPBEHash(password, salt);
                    if (Arrays.equals(hash, passwordDigest.getMessageDigest()))
                    {
                        return password;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(dialogParent, "The password is incorrect.");
                    }
                }
                else
                {
                    return null;
                }
            }
            finally
            {
                passwordField.setText("");
            }
        }
        while (true);
    }

    /** Disallow instantiation. */
    private EnterPasswordDialog()
    {
    }
}
