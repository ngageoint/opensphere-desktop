package io.opensphere.filterbuilder2.manager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.RadioButtonPanel;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder2.common.Constants;

/**
 * Panel for exporting a filter.
 */
public class ExportFilterPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The only active filters constant. */
    private static final String ACTIVE_FILTERS = "Only active filters";

    /** The file text field. */
    private JTextField myFileField;

    /** The radio buttons for choosing to save the active or all filters. */
    private RadioButtonPanel<String> myRadioButtonPanel;

    /**
     * Constructor.
     *
     * @param fbToolbox the filter builder toolbox
     */
    public ExportFilterPanel(FilterBuilderToolbox fbToolbox)
    {
        init0();
        anchorWest();
        fillHorizontal();
        addRow(buildFilePanel(fbToolbox));
        fillNone();
        setInsets(Constants.DOUBLE_INSET, 0, 0, 0);
        addRow(buildActivePanel());
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile()
    {
        String pathname = myFileField.getText();
        if (!pathname.endsWith(".xml"))
        {
            pathname = StringUtilities.concat(pathname, ".xml");
        }
        return new File(pathname);
    }

    /**
     * Whether to export only active filter.
     *
     * @return whether to export only active filter
     */
    public boolean isActiveOnly()
    {
        return ACTIVE_FILTERS.equals(myRadioButtonPanel.getSelection());
    }

    /**
     * Builds the active panel.
     *
     * @return the active panel
     */
    private JPanel buildActivePanel()
    {
        myRadioButtonPanel = new RadioButtonPanel<>(New.list(ACTIVE_FILTERS, "All filters"), ACTIVE_FILTERS);

        GridBagPanel panel = new GridBagPanel();
        panel.add(new JLabel("Export:"));
        panel.add(myRadioButtonPanel);
        return panel;
    }

    /**
     * Builds the file panel.
     *
     * @param fbToolbox the filter builder toolbox
     * @return the file panel
     */
    private JPanel buildFilePanel(final FilterBuilderToolbox fbToolbox)
    {
        final GridBagPanel panel = new GridBagPanel();

        File file = fbToolbox.getConfiguration().getLastFile();

        // Pick a default
        if (file == null)
        {
            StringBuilder filePath = new StringBuilder(System.getProperty("user.home"));
            filePath.append(File.separatorChar).append("filters.xml");
            file = new File(filePath.toString());
        }

        final File finalFile = file;

        myFileField = new JTextField(finalFile.getAbsolutePath());
        myFileField.setColumns(20);

        JButton fileButton = new JButton("Choose");
        fileButton.setMargin(ButtonPanel.INSETS_MEDIUM);
        fileButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MnemonicFileChooser chooser = new MnemonicFileChooser(fbToolbox.getMainToolBox().getPreferencesRegistry(),
                        ExportFilterPanel.class.getName());
                chooser.setSelectedFile(finalFile);
                chooser.setDialogTitle("Choose File");
                chooser.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
                if (chooser.showDialog(SwingUtilities.windowForComponent(panel), "OK") == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile = chooser.getSelectedFile();
                    if (selectedFile != null)
                    {
                        myFileField.setText(selectedFile.getAbsolutePath());
                    }
                }
            }
        });

        panel.setInsets(0, 0, 0, Constants.INSET);
        panel.add(new JLabel("File:"));
        panel.fillHorizontal();
        panel.add(myFileField);
        panel.fillNone().setInsets(0, 0, 0, 0);
        panel.add(fileButton);
        return panel;
    }
}
