package io.opensphere.mantle.data.analysis.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.data.analysis.DataAnalysisReporter;

/**
 * The Class DataAnalysisReporterOptionsProvider.
 */
public class DataAnalysisReporterOptionsProvider extends AbstractPreferencesOptionsProvider
{
    /** The Analyzer enabled check box. */
    private JCheckBox myAnalyzerEnabledCheckBox;

    /** The Panel. */
    private JPanel myPanel;

    /**
     * Instantiates a new data analysis reporter options provider.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public DataAnalysisReporterOptionsProvider(PreferencesRegistry prefsRegistry)
    {
        super(prefsRegistry, "Data Analyzer");
    }

    @Override
    public void applyChanges()
    {
        setDataAnalysisReporterEnabledPreference(getAnalyzerEnabledCheckBox().isSelected());
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myPanel = new JPanel();
            myPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            myPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
            myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));

            JTextArea ta = new JTextArea();
            ta.setBackground(TRANSPARENT_COLOR);
            ta.setBorder(BorderFactory.createEmptyBorder());
            ta.setFont(ta.getFont().deriveFont(Font.PLAIN, ta.getFont().getSize() + 1));
            ta.setEditable(false);
            ta.setMaximumSize(new Dimension(3000, 200));
            ta.setFocusable(true);
            ta.setWrapStyleWord(true);
            ta.setLineWrap(true);
            ta.setText("The Automatic Data Analyzer examines feature meta-data (column data)"
                    + " as it is imported by the application in order to characterize the"
                    + " values.\n\nOnce a sufficient knowledge base is generated, it can"
                    + " be used to optimize memory utilization by each data type. It is also"
                    + " used to identify empty columns and enumerated types."
                    + "\n\nYou can disable this feature here if you are experiencing"
                    + " problems. The application will clear everything it has learned, "
                    + "and will immediately stop analyzing incoming data. "
                    + "However, the application will need to be restarted for changes" + " to take full affect."
                    + "\n\nAdditionally, data analysis can be cleared, but analysis "
                    + "will continue, if the user presses the Clear Local Encrypted Data button in the Security panel.");
            myPanel.add(ta);

            myPanel.add(Box.createVerticalStrut(10));

            JPanel subPanel = new JPanel(new BorderLayout());
            subPanel.setMaximumSize(new Dimension(3000, 30));
            subPanel.setPreferredSize(new Dimension(300, 30));
            subPanel.add(getAnalyzerEnabledCheckBox(), BorderLayout.WEST);
            subPanel.setBackground(TRANSPARENT_COLOR);

            myPanel.add(subPanel);
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(TRANSPARENT_COLOR);
            myPanel.add(emptyPanel);
            myPanel.add(Box.createVerticalGlue());
        }
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
        getAnalyzerEnabledCheckBox().setSelected(true);
        setDataAnalysisReporterEnabledPreference(true);
    }

    /**
     * Gets the analyzer enabled check box.
     *
     * @return the analyzer enabled check box
     */
    private JCheckBox getAnalyzerEnabledCheckBox()
    {
        if (myAnalyzerEnabledCheckBox == null)
        {
            myAnalyzerEnabledCheckBox = new JCheckBox("Automatic Data Analyzer Enabled",
                    isDataAnalysisReporterEnabledPreferenceValue());
            myAnalyzerEnabledCheckBox.setFocusable(false);
        }
        return myAnalyzerEnabledCheckBox;
    }

    /**
     * Gets the data analysis reporter enabled preference value.
     *
     * @return the data analysis reporter enabled preference value
     */
    private boolean isDataAnalysisReporterEnabledPreferenceValue()
    {
        return getPreferencesRegistry().getPreferences(DataAnalysisReporter.class).getBoolean("DataAnalsysisReporterEnabled",
                true);
    }

    /**
     * Sets the data analysis reporter enabled preference.
     *
     * @param enabled the new data analysis reporter enabled preference
     */
    private void setDataAnalysisReporterEnabledPreference(boolean enabled)
    {
        getPreferencesRegistry().getPreferences(DataAnalysisReporter.class).putBoolean("DataAnalsysisReporterEnabled", enabled,
                this);
    }
}
