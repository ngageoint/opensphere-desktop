package io.opensphere.mantle.data.util.purge;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;

/**
 * The Class PurgeConfirmHelper.
 */
public final class PurgeConfirmHelper
{
    /** The Constant SHOW_PURGE_CONFIRMATION. */
    public static final String SHOW_PURGE_CONFIRMATION = "PurgeSupport.ShowPurgeConfirmation";

    /** The don't show purge confirmation again check box. */
    private static JCheckBox ourDontShowPurgeConfirmationAgainCheckBox;

    /** The purge warning panel. */
    private static JPanel ourPurgeWarningPanel;

    static
    {
        ourPurgeWarningPanel = new JPanel(new BorderLayout());
        ourPurgeWarningPanel.setMinimumSize(new Dimension(360, 130));
        ourPurgeWarningPanel.setPreferredSize(new Dimension(360, 130));
        JTextArea warnTextArea = new JTextArea("Purging data removes loaded data from the tool permanently\n"
                + "it can only be recovered by reloading from the original\ndata source.\n\n"
                + "Are you sure you want to proceed?");
        warnTextArea.setEditable(false);
        warnTextArea.setBorder(BorderFactory.createEmptyBorder());
        warnTextArea.setBackground(ourPurgeWarningPanel.getBackground());
        ourPurgeWarningPanel.add(warnTextArea, BorderLayout.CENTER);
        ourDontShowPurgeConfirmationAgainCheckBox = new JCheckBox("Don't show this confirmation again", false);
        ourPurgeWarningPanel.add(ourDontShowPurgeConfirmationAgainCheckBox, BorderLayout.SOUTH);
    }

    /**
     * Confirms with the user that they wish to proceed with a purge request,
     * allows the user to choose not to see the confirmation again.
     *
     * @param tb the {@link Toolbox}
     * @param confirmDialogParentComponent the confirm dialog parent component
     * @param source the source
     * @return true, if good to proceed.
     */
    public static boolean confirmProceedWithPurge(Toolbox tb, Component confirmDialogParentComponent, Object source)
    {
        boolean proceedWithPurge = true;
        Preferences prefs = tb.getPreferencesRegistry().getPreferences(PurgeConfirmHelper.class);
        boolean showPurgeConfirmation = prefs.getBoolean(SHOW_PURGE_CONFIRMATION, true);

        if (showPurgeConfirmation)
        {
            Component w = confirmDialogParentComponent == null ? tb.getUIRegistry().getMainFrameProvider().get()
                    : SwingUtilities.getWindowAncestor(confirmDialogParentComponent) == null
                            && confirmDialogParentComponent instanceof Window ? (Window)confirmDialogParentComponent
                                    : SwingUtilities.getWindowAncestor(confirmDialogParentComponent);

            int value = JOptionPane.showConfirmDialog(w, ourPurgeWarningPanel, "Purge Data Confirmation",
                    JOptionPane.OK_CANCEL_OPTION);
            if (value == JOptionPane.CANCEL_OPTION)
            {
                proceedWithPurge = false;
            }
            else
            {
                if (ourDontShowPurgeConfirmationAgainCheckBox.isSelected())
                {
                    prefs.putBoolean(SHOW_PURGE_CONFIRMATION, false, source);
                }
            }
        }
        return proceedWithPurge;
    }

    /**
     * Instantiates a new purge confirm helper.
     */
    private PurgeConfirmHelper()
    {
    }
}
