package io.opensphere.core.util.swing.input;

import java.awt.Component;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.BooleanModel;

/**
 * Dialog that has an option to never show again. The title is used as the
 * preference key, so it should be unique.
 */
public final class DontShowDialog extends OptionDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The "don't show" GUI model. */
    private final BooleanModel myModel = new BooleanModel();

    /**
     * Reset the don't show preferences.
     *
     * @param preferencesRegistry the preferences registry
     */
    public static void resetPreferences(PreferencesRegistry preferencesRegistry)
    {
        preferencesRegistry.resetPreferences(DontShowDialog.class, DontShowDialog.class);
    }

    /**
     * Shows a confirm dialog if the preference allows it.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param message the message
     * @param title the title
     * @param isSelected whether the checkbox is selected
     * @return the user's selection
     */
    public static int showConfirmDialog(PreferencesRegistry preferencesRegistry, Component parent, String message, String title,
            boolean isSelected)
    {
        int selection = JOptionPane.OK_OPTION;

        Preferences preferences = preferencesRegistry.getPreferences(DontShowDialog.class);
        boolean dontShow = preferences.getBoolean(title, false);
        if (!dontShow)
        {
            DontShowDialog dialog = new DontShowDialog(preferencesRegistry, parent, getComponent(message), title, isSelected);
            dialog.setButtonLabels(ButtonPanel.YES_NO);
            dialog.requestFocus(ButtonPanel.YES);
            dialog.buildAndShow();
            selection = dialog.getSelection();
            if (dialog.getModel().get().booleanValue())
            {
                preferences.putBoolean(title, true, dialog);
            }
        }

        return selection;
    }

    /**
     * Shows a confirm dialog if the preference allows it. This variant asks the
     * user if they want the selected option to be remembered.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param message the message
     * @param title the title
     * @param isSelected whether the checkbox is selected
     * @return the user's selection
     */
    public static int showConfirmAndRememberDialog(PreferencesRegistry preferencesRegistry, Component parent, String message,
            String title, boolean isSelected)
    {
        int selection;

        final String prefKey = title + ".choice";
        Preferences preferences = preferencesRegistry.getPreferences(DontShowDialog.class);
        selection = preferences.getInt(prefKey, -1);
        if (selection == -1)
        {
            DontShowDialog dialog = new DontShowDialog(preferencesRegistry, parent, getComponent(message), title, isSelected,
                    "Remember this choice");
            dialog.setButtonLabels(ButtonPanel.YES_NO);
            dialog.requestFocus(ButtonPanel.YES);
            dialog.buildAndShow();
            selection = dialog.getSelection();

            // If they want us to remember, then remember
            if (dialog.getModel().get().booleanValue())
            {
                preferences.putInt(prefKey, selection, dialog);
            }
            else
            {
                preferences.remove(prefKey, dialog);
            }
        }

        return selection;
    }

    /**
     * Shows a message dialog if the preference allows it.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param message the message
     * @param title the title
     * @param isSelected whether the checkbox is selected
     */
    public static void showMessageDialog(PreferencesRegistry preferencesRegistry, Component parent, String message, String title,
            boolean isSelected)
    {
        showMessageDialog(preferencesRegistry, parent, getComponent(message), title, isSelected);
    }

    /**
     * Shows a message dialog if the preference allows it.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param component the component to display
     * @param title the title
     * @param isSelected whether the checkbox is selected
     */
    public static void showMessageDialog(PreferencesRegistry preferencesRegistry, Component parent, Component component,
            String title, boolean isSelected)
    {
        Preferences preferences = preferencesRegistry.getPreferences(DontShowDialog.class);
        boolean dontShow = preferences.getBoolean(title, false);
        if (!dontShow)
        {
            DontShowDialog dialog = new DontShowDialog(preferencesRegistry, parent, component, title, isSelected);
            dialog.setButtonLabels(Collections.singletonList(ButtonPanel.OK));
            dialog.buildAndShow();
            if (dialog.getModel().get().booleanValue())
            {
                preferences.putBoolean(title, true, dialog);
            }
        }
    }

    /**
     * Constructor for normal don't show dialogs.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param component the component to display
     * @param title the title
     * @param isSelected whether the checkbox is selected
     */
    public DontShowDialog(PreferencesRegistry preferencesRegistry, Component parent, Component component, String title,
            boolean isSelected)
    {
        this(preferencesRegistry, parent, component, title, isSelected, "Don't show this again");
    }

    /**
     * General constructor.
     *
     * @param preferencesRegistry the preferences registry, used to remember the
     *            don't show preference
     * @param parent the parent
     * @param component the component to display
     * @param title the title
     * @param isSelected whether the checkbox is selected
     * @param text the text of the checkbox
     */
    public DontShowDialog(PreferencesRegistry preferencesRegistry, Component parent, Component component, String title,
            boolean isSelected, String text)
    {
        super(parent);
        setTitle(title);
        setComponent(component);
        getContentButtonPanel().add(getCheckBox(preferencesRegistry, isSelected, text));
    }

    /**
     * Gets the checkbox model.
     *
     * @return the model
     */
    public BooleanModel getModel()
    {
        return myModel;
    }

    /**
     * Gets the checkbox.
     *
     * @param preferencesRegistry the preferences registry
     * @param isSelected whether the checkbox is selected
     * @param text the text of the checkbox
     * @return the checkbox
     */
    private Component getCheckBox(final PreferencesRegistry preferencesRegistry, boolean isSelected, String text)
    {
        myModel.setName(text);
        myModel.set(Boolean.valueOf(isSelected));

        JComponent checkBox = ControllerFactory.createComponent(myModel);
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
