package io.opensphere.mantle.data.analysis.impl;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * The Class DataAnalysisReporterOptionsProvider.
 */
public class AdvancedDataAnalysisReporterOptionsProvider extends AbstractOptionsProvider
{
    /** The Data analysis reporter impl. */
    private final DataAnalysisReporterImpl myDataAnalysisReporterImpl;

    /** The Panel. */
    private GridBagPanel myPanel;

    /**
     * Instantiates a new data analysis reporter options provider.
     *
     * @param reporter the reporter
     */
    public AdvancedDataAnalysisReporterOptionsProvider(DataAnalysisReporterImpl reporter)
    {
        super("Advanced");
        myDataAnalysisReporterImpl = reporter;
    }

    @Override
    public void applyChanges()
    {
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myPanel = new GridBagPanel();
            myPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            myPanel.anchorWest().setWeightx(1).setInsets(10, 10, 0, 0);

            myPanel.addRow(getRegistryContentsReportTextButton());
            myPanel.addRow(getRegistryContentsReportXMLButton());
            myPanel.addRow(getClearRegistryDataButton());
            myPanel.fillVerticalSpace();
        }
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
     * Gets the clear registry data button.
     *
     * @return the clear registry data button
     */
    private JButton getClearRegistryDataButton()
    {
        JButton clearRegistryDataButton = new JButton("Clear Data Analysis Registry Data");
        clearRegistryDataButton.addActionListener(e ->
        {
            Quantify.collectMetric("mist3d.settings.data-analyzer.advanced.clear-registry-data-button");
            int option = JOptionPane.showConfirmDialog(getOptionsPanel(),
                "Are you sure you want to delete the registry contents?\nThis action cannot be undone.",
                "Clear Data Analysis Registry", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION)
            {
                myDataAnalysisReporterImpl.clearAllColumnAnalysisData();
            }
        });
        return clearRegistryDataButton;
    }

    /**
     * Gets the registry contents report text button.
     *
     * @return the registry contents report text button
     */
    private JButton getRegistryContentsReportTextButton()
    {
        JButton registryContentsReportTextButton = new JButton("Registry Contents Report (Text)");
        registryContentsReportTextButton.addActionListener(e ->
        {
            Quantify.collectMetric("mist3d.settings.data-analyzer.advanced.contents-report-text-button");
            myDataAnalysisReporterImpl.showRegistryReportText();
        });
        return registryContentsReportTextButton;
    }

    /**
     * Gets the registry contents report xml button.
     *
     * @return the registry contents report xml button
     */
    private JButton getRegistryContentsReportXMLButton()
    {
        JButton registryContentsReportXMLButton = new JButton("Registry Contents Report (XML)");
        registryContentsReportXMLButton.addActionListener(e ->
        {
            Quantify.collectMetric("mist3d.settings.data-analyzer.advanced.contents-report-xml-button");
            myDataAnalysisReporterImpl.showXMLRegistryReport();
        });
        return registryContentsReportXMLButton;
    }
}
