package io.opensphere.core.appl.versions.view;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.opensphere.core.appl.versions.NewVersionPlugin;
import io.opensphere.core.appl.versions.model.AutoUpdatePreferenceKeys;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.BooleanModel;

/**
 * A simple extension of the {@link OptionDialog} class to prompt the user to
 * install a new version. Includes a checkbox to allow the user to automatically
 * install updates without being prompted.
 */
public class AutoUpdateDialog extends OptionDialog
{
    /** Default serialization ID. */
    private static final long serialVersionUID = 1L;

    /** The template to use for the message portion of the dialog. */
    private static final String DIALOG_MESSAGE_TEMPLATE = "Auto-Download %1$s?";

    /** The "automatically install" GUI model. */
    private final BooleanModel myAutomaticInstall = new BooleanModel();

    /**
     * Constructor.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param component the component to display
     * @param title the title
     */
    private AutoUpdateDialog(PreferencesRegistry preferencesRegistry, Component parent, Component component, String title)
    {
        super(parent);

        boolean automaticUpdate = preferencesRegistry.getPreferences(NewVersionPlugin.class)
                .getBoolean(AutoUpdatePreferenceKeys.UPDATE_WITHOUT_PROMPT_ENABLED_KEY, false);
        myAutomaticInstall.set(Boolean.valueOf(automaticUpdate));

        setTitle(title);
        setComponent(component);
        getContentButtonPanel().add(getCheckBox(preferencesRegistry));
    }

    /**
     * Shows a confirm dialog to prompt the user to download a new version. The
     * dialog will contain an option to allow the user to automatically download
     * new versions from now on, or to completely disable auto-update.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param version the version for which to prompt the user.
     * @return the user's selection
     */
    public static int showConfirmDialog(PreferencesRegistry preferencesRegistry, Component parent, String version)
    {
        int selection = JOptionPane.OK_OPTION;

        boolean automaticUpdate = preferencesRegistry.getPreferences(NewVersionPlugin.class)
                .getBoolean(AutoUpdatePreferenceKeys.UPDATE_WITHOUT_PROMPT_ENABLED_KEY, false);
        if (!automaticUpdate)
        {
            AutoUpdateDialog dialog = new AutoUpdateDialog(preferencesRegistry, parent,
                    getComponent(String.format(DIALOG_MESSAGE_TEMPLATE, version)), "New Version Available");
            dialog.setButtonLabels(ButtonPanel.YES_NO);
            dialog.requestFocus(ButtonPanel.YES);
            dialog.buildAndShow();
            selection = dialog.getSelection();
        }

        return selection;
    }

    /**
     * Gets the checkbox.
     *
     * @param preferencesRegistry the preferences registry
     * @return the checkbox
     */
    protected Component getCheckBox(final PreferencesRegistry preferencesRegistry)
    {
        myAutomaticInstall.setName("Always install updates without asking me (may re-enabled in settings)");
        myAutomaticInstall.addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                preferencesRegistry.getPreferences(NewVersionPlugin.class).putBoolean(
                        AutoUpdatePreferenceKeys.UPDATE_WITHOUT_PROMPT_ENABLED_KEY, myAutomaticInstall.get().booleanValue(),
                        this);
            }
        });

        JComponent checkBox = ControllerFactory.createComponent(myAutomaticInstall);
        checkBox.setFocusable(false);
        return checkBox;
    }

    /**
     * Gets the component.
     *
     * @param message the message
     * @return the component
     */
    private static Component getComponent(String message)
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel(message));
        return panel;
    }
}
