package io.opensphere.kml.export;

import java.awt.Dimension;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.mantle.icon.impl.gui.IconChooserDialog;

/**
 * The dialog with KML options.
 */
public class KMLPreExportDialog extends OptionDialog implements KMLExportOptionsView
{
    /** The UUID. */
    private static final long serialVersionUID = 1L;

    /** The title text for the KML file. */
    private JTextField myTitleTextField;

    /** The title text. */
    private JTextField myRecordTextField;

    /** The metadata field radio button. */
    private JRadioButton myMetadataFieldRadioButton;

    /** The selector for which field in the metadata to use as the title. */
    private JComboBox<String> myMetadataFieldSelector;

    /** The icon dot radio button. */
    private JRadioButton myIconDotRadioButton;

    /** The icon file field. */
    private JTextField myIconFileField;

    /** The button that opens the icon chooser dialog. */
    private JButton myIconFileChooserButton;

    /** The system toolbox. */
    private final Toolbox myToolbox;

    /** The metadata columns. */
    private Set<String> myMetadataColumns;

    /** The selected metadata column. */
    private final String mySelectedMetadataColumn;

    /** The export options model. */
    private final KMLExportOptionsModel myModel;

    /** Keeps the options model and the options UI in sync. */
    private final KMLExportOptionsViewBinder myOptionsBinder;

    /**
     * Dialog that specifies export options before save dialog is shown.
     *
     * @param metadata the map of metadata fields to types.
     * @param toolbox the system toolbox.
     * @param model the export options model.
     */
    public KMLPreExportDialog(Toolbox toolbox, Map<String, Class<?>> metadata, KMLExportOptionsModel model)
    {
        super(toolbox.getUIRegistry().getMainFrameProvider().get());
        myToolbox = toolbox;
        myModel = model;
        mySelectedMetadataColumn = getFirstStringField(metadata);
        setComponent(createOptionsPanel());
        setTitle("KML Export Options");
        myOptionsBinder = new KMLExportOptionsViewBinder(this, myModel);
    }

    /**
     * Stops listening for UI and model changes.
     */
    public void close()
    {
        myOptionsBinder.close();
    }

    /**
     * Gets the options binder that binds view events to model.
     *
     * @return the view binder.
     */
    public KMLExportOptionsViewBinder getOptionsBinder()
    {
        return myOptionsBinder;
    }

    @Override
    public JTextField getTitleTextField()
    {
        return myTitleTextField;
    }

    @Override
    public JRadioButton getMetadataFieldRadioButton()
    {
        return myMetadataFieldRadioButton;
    }

    @Override
    public JTextField getRecordTextField()
    {
        return myRecordTextField;
    }

    @Override
    public JComboBox<String> getMetadataFieldSelector()
    {
        return myMetadataFieldSelector;
    }

    @Override
    public JRadioButton getIconDotRadioButton()
    {
        return myIconDotRadioButton;
    }

    @Override
    public JTextField getIconFileField()
    {
        return myIconFileField;
    }

    /**
     * Gets first string field in metadata column set for gui dropdown.
     *
     * @param metadata the map of metadata fields to their type
     * @return the field, or null
     */
    private String getFirstStringField(Map<String, Class<?>> metadata)
    {
        if (metadata != null)
        {
            myMetadataColumns = metadata.keySet();
            for (Entry<String, Class<?>> entry : metadata.entrySet())
            {
                if (entry.getValue().equals(String.class))
                {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Builds the KML document title panel.
     *
     * @return the panel
     */
    private JComponent createTitlePanel()
    {
        JLabel kmlTitleLabel = new JLabel("Document Title:");
        myTitleTextField = new JTextField("OpenSphere KML");

        GridBagPanel titlePanel = new GridBagPanel();
        titlePanel.setInsets(0, 0, 2, 0).setWeightx(2).anchorWest();
        titlePanel.add(kmlTitleLabel);
        titlePanel.fillHorizontal().setWeightx(3);
        titlePanel.add(myTitleTextField);
        return titlePanel;
    }

    /**
     * Builds the Record prefix/metadata field panel.
     *
     * @return the panel
     */
    private JComponent createFieldPanel()
    {
        JRadioButton titleRadioButton = new JRadioButton("Prefix:", false);
        titleRadioButton.setToolTipText("Set the KML placemark names to this prefix followed by a one-up counter");
        titleRadioButton.addActionListener(action -> toggleMetadataField(false));
        myRecordTextField = new JTextField("KML Record");
        myRecordTextField.setEnabled(false);

        myMetadataFieldRadioButton = new JRadioButton("Use value of column:", true);
        myMetadataFieldRadioButton.setToolTipText("Set the KML placemark names to the value of this metadata field");
        myMetadataFieldRadioButton.addActionListener(action -> toggleMetadataField(true));
        myMetadataFieldSelector = new JComboBox<>();
        if (!myMetadataColumns.isEmpty())
        {
            for (String field : myMetadataColumns)
            {
                myMetadataFieldSelector.addItem(field);
            }
        }
        myMetadataFieldSelector.setSelectedItem(mySelectedMetadataColumn);
        myMetadataFieldSelector.setEnabled(true);

        ButtonGroup metadataButtonGroup = new ButtonGroup();
        metadataButtonGroup.add(titleRadioButton);
        metadataButtonGroup.add(myMetadataFieldRadioButton);

        GridBagPanel fieldPanel = new GridBagPanel();
        fieldPanel.setBorder(new TitledBorder("Placemark Names:"));
        fieldPanel.setInsets(0, 0, 2, 0).setGridy(0).fillHorizontal().anchorWest();
        fieldPanel.add(titleRadioButton);
        fieldPanel.add(myRecordTextField);
        fieldPanel.setGridy(1);
        fieldPanel.add(myMetadataFieldRadioButton);
        fieldPanel.anchorEast();
        fieldPanel.add(myMetadataFieldSelector);
        return fieldPanel;
    }

    /**
     * Builds the icon dot/file panel.
     *
     * @return the panel
     */
    private JComponent createIconPanel()
    {
        myIconDotRadioButton = new JRadioButton("Use Dot", true);
        myIconDotRadioButton.addActionListener(action -> toggleFileField(false));
        JRadioButton iconFileRadioButton = new JRadioButton("Use Icon File:", false);
        iconFileRadioButton.addActionListener(action -> toggleFileField(true));
        myIconFileField = new JTextField();
        myIconFileField.setEnabled(false);
        myIconFileChooserButton = new JButton("File...");
        myIconFileChooserButton.setEnabled(false);
        myIconFileChooserButton.addActionListener(action -> openIconChooser());
        ButtonGroup iconDotButtonGroup = new ButtonGroup();
        iconDotButtonGroup.add(myIconDotRadioButton);
        iconDotButtonGroup.add(iconFileRadioButton);

        GridBagPanel iconPanel = new GridBagPanel();
        iconPanel.setBorder(new TitledBorder("Icons:"));
        iconPanel.setGridy(0).anchorWest();
        iconPanel.add(myIconDotRadioButton);
        iconPanel.setGridy(1).setWeightx(2).fillHorizontal();
        iconPanel.add(iconFileRadioButton);
        iconPanel.setWeightx(10).anchorEast();
        iconPanel.add(myIconFileField);
        iconPanel.setWeightx(1);
        iconPanel.add(myIconFileChooserButton);
        return iconPanel;
    }

    /**
     * Build the export options panel.
     *
     * @return the export options panel.
     */
    private JComponent createOptionsPanel()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.setPreferredSize(new Dimension(500, 200));
        panel.fillHorizontal();
        panel.addRow(createTitlePanel());
        panel.addRow(createFieldPanel());
        panel.addRow(createIconPanel());
        return panel;
    }

    /**
     * Opens a dialog for selecting an icon.
     */
    private void openIconChooser()
    {
        IconChooserDialog fileDialog = new IconChooserDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), true,
                myToolbox);
        fileDialog.addActionListener(action ->
        {
            if (action.getActionCommand() == IconChooserDialog.ICON_SELECTED)
            {
                myIconFileField.setText(fileDialog.getSelectedIcon().imageURLProperty().get().getFile());
            }
        });
        fileDialog.setVisible(true);
    }

    /**
     * Enable/disables the file field/file chooser button.
     *
     * @param enabled set to true if the field/button are to be enabled.
     */
    private void toggleFileField(boolean enabled)
    {
        myIconFileChooserButton.setEnabled(enabled);
        myIconFileField.setEnabled(enabled);
    }

    /**
     * Enable/disables the metadata/record chooser buttons.
     *
     * @param metadataSelected true if the metadata option is selected.
     */
    private void toggleMetadataField(boolean metadataSelected)
    {
        myRecordTextField.setEnabled(!metadataSelected);
        myMetadataFieldSelector.setEnabled(metadataSelected);
        myMetadataFieldSelector.setSelectedItem(myMetadataFieldSelector.getSelectedItem());
    }
}
