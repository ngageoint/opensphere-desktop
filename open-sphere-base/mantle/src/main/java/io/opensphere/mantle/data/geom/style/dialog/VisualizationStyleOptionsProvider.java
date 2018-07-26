package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.opensphere.core.control.ui.ToolbarManager;
import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;

/**
 * The Class VisualizationStyleOptionsProvider.
 */
public class VisualizationStyleOptionsProvider extends AbstractPreferencesOptionsProvider
{
    /** Preference key for the user preference to show the icon button text. */
    private static final String SHOW_ICON_BUTTON_TEXT_PREF_KEY = "showIconButtonText";

    /** The Panel. */
    private JPanel myPanel;

    /** The Reset all style data button. */
    private JButton myResetAllStyleDataButton;

    /** The `Show Toolbar Labels` checkbox. */
    private JCheckBox myShowToolbarLabelsCheckBox;

    /** The Style controller. */
    private final VisualizationStyleController myStyleController;

    /** Preferences for the toolbar. */
    private final Preferences myToolbarPreferences;

    /** Flag indicating if the text should be shown on the toolbar buttons. */
    private boolean myShowToolbarLabels;

    /**
     * Listener for showIconButtonText preference updates.
     */
    private final PreferenceChangeListener myTextListener = new PreferenceChangeListener()
    {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt)
        {
            myShowToolbarLabels = evt.getValueAsBoolean(!myShowToolbarLabels);
            if (myShowToolbarLabelsCheckBox != null)
            {
                myShowToolbarLabelsCheckBox.setSelected(myShowToolbarLabels);
            }
        }
    };

    /**
     * Instantiates a new visualization style options provider.
     *
     * @param controller the controller
     * @param prefsRegistry the prefs registry
     */
    public VisualizationStyleOptionsProvider(VisualizationStyleController controller, PreferencesRegistry prefsRegistry)
    {
        super(prefsRegistry, VisualizationStyleControlDialog.TITLE);
        myStyleController = controller;

        myToolbarPreferences = prefsRegistry.getPreferences(ToolbarManager.class);
        myShowToolbarLabels = myToolbarPreferences.getBoolean(SHOW_ICON_BUTTON_TEXT_PREF_KEY, true);
        myToolbarPreferences.addPreferenceChangeListener(SHOW_ICON_BUTTON_TEXT_PREF_KEY, myTextListener);

        initializePanel();
    }

    /**
     * Initialized {@link #myPanel}.
     */
    private void initializePanel()
    {
        myPanel = new JPanel();
        myPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
        myPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));

        myShowToolbarLabelsCheckBox = createShowToolbarLabelsCheckBox();
        Box selectionPanel = Box.createHorizontalBox();
        selectionPanel.setMaximumSize(new Dimension(3000, 30));
        selectionPanel.setPreferredSize(new Dimension(300, 30));
        selectionPanel.add(myShowToolbarLabelsCheckBox);
        selectionPanel.add(Box.createHorizontalGlue());
        selectionPanel.setBackground(TRANSPARENT_COLOR);
        myPanel.add(selectionPanel);

        myPanel.add(Box.createVerticalStrut(30));

        JTextArea ta = new JTextArea();
        ta.setBackground(TRANSPARENT_COLOR);
        ta.setBorder(BorderFactory.createEmptyBorder());
        ta.setFont(ta.getFont().deriveFont(Font.PLAIN, ta.getFont().getSize() + 1));
        ta.setEditable(false);
        ta.setMaximumSize(new Dimension(3000, 200));
        ta.setFocusable(true);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setText("Use the button below to reset all style settings and restore all types to use the default styles.");
        myPanel.add(ta);

        myPanel.add(Box.createVerticalStrut(10));

        myResetAllStyleDataButton = createResetAllStyleButton();
        Box subPanel = Box.createHorizontalBox();
        subPanel.setMaximumSize(new Dimension(3000, 30));
        subPanel.setPreferredSize(new Dimension(300, 30));
        subPanel.add(Box.createHorizontalGlue());
        subPanel.add(myResetAllStyleDataButton);
        subPanel.add(Box.createHorizontalGlue());
        subPanel.setBackground(TRANSPARENT_COLOR);
        myPanel.add(subPanel);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(TRANSPARENT_COLOR);
        myPanel.add(emptyPanel);

        myPanel.add(Box.createVerticalGlue());
    }

    @Override
    public void applyChanges()
    {
    }

    @Override
    public JPanel getOptionsPanel()
    {
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return false;
    }

    /**
     * Creates a `Reset All Styles` button.
     *
     * @return the button
     */
    private JButton createResetAllStyleButton()
    {
        JButton button = new JButton("Reset All Styles");
        button.setFocusable(false);
        button.addActionListener(e ->
        {
            Quantify.collectMetric("mist3d.settings.styles.reset-all-styles-button");
            int option = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(myResetAllStyleDataButton),
                    "Are you sure you want to reset all styles to default and clear all style data?",
                    "Reset All Style Confirmation", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION)
            {
                myStyleController.resetAllStyleSettings(this);
            }
        });

        return button;
    }

    /**
     * Creates a `Show Toolbar Labels` checkbox.
     *
     * @return the checkbox
     */
    private JCheckBox createShowToolbarLabelsCheckBox()
    {
        JCheckBox checkbox = new JCheckBox("Show Icon Labels on Toolbar", myShowToolbarLabels);
        checkbox.addActionListener(e ->
        {
            Quantify.collectEnableDisableMetric("mist3d.settings.styles.show-icon-labels-on-toolbar",
                    myShowToolbarLabelsCheckBox.isSelected());
            myToolbarPreferences.putBoolean(SHOW_ICON_BUTTON_TEXT_PREF_KEY, !myShowToolbarLabels, this);
        });

        return checkbox;
    }
}
