package io.opensphere.analysis.export.view;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import io.opensphere.analysis.export.model.ColorFormat;
import io.opensphere.analysis.export.model.LatLonFormat;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * The UI that contains export options a user can select when exporting. Its
 * intended use is to be an accessory on the export file chooser.
 */
public class ExportOptionsUI extends GridBagPanel implements ExportOptionsView
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The label for the color format.
     */
    private JLabel myColorFormatLabel;

    /**
     * The combo box for a list of available color formats the user can choose.
     */
    private JComboBox<ColorFormat> myColorFormatOptionsComboBox;

    /**
     * The check box to enable/disable adding extra meta columns to the export
     * such as color.
     */
    private JCheckBox myIncludeMetaColumnsCheckBox;

    /**
     * The check box to enable/disable adding a WKT geometry column.
     */
    private JCheckBox myIncludeWKTGeometryColumnCheckBox;

    /**
     * The combo box for a list of available lat lon formats.
     */
    private JComboBox<LatLonFormat> myLatLonFormatCombo;

    /**
     * The check box to enable/disable exporting only selected rows.
     */
    private JCheckBox mySelectedRowsOnlyCheckBox;

    /**
     * The check box to enable/disable splitting the date time column.
     */
    private JCheckBox mySplitDateTimeCheckBox;

    /**
     * Constructs a new {@link ExportOptionsUI}.
     */
    public ExportOptionsUI()
    {
        initializeComponents();
    }

    @Override
    public JCheckBox getAddWKT()
    {
        return myIncludeWKTGeometryColumnCheckBox;
    }

    @Override
    public JComboBox<ColorFormat> getColorFormat()
    {
        return myColorFormatOptionsComboBox;
    }

    @Override
    public JLabel getColorFormatLabel()
    {
        return myColorFormatLabel;
    }

    @Override
    public JCheckBox getIncludeMetaColumns()
    {
        return myIncludeMetaColumnsCheckBox;
    }

    @Override
    public JComboBox<LatLonFormat> getLatLonFormat()
    {
        return myLatLonFormatCombo;
    }

    @Override
    public JCheckBox getSelectedRowsOnly()
    {
        return mySelectedRowsOnlyCheckBox;
    }

    @Override
    public JCheckBox getSeparateDateTimeColumns()
    {
        return mySplitDateTimeCheckBox;
    }

    /**
     * Creates the additional options.
     *
     * @return The additional options.
     */
    private JComponent createAdditionals()
    {
        myColorFormatLabel = new JLabel("Color Output Format:");
        myColorFormatOptionsComboBox = new JComboBox<>();
        myLatLonFormatCombo = new JComboBox<>();
        myLatLonFormatCombo.setToolTipText("The format of latitude and longitude columns");

        mySplitDateTimeCheckBox = new JCheckBox("Separate Date/Time Columns");
        mySplitDateTimeCheckBox.setToolTipText("Whether to split the TIME column into separate date and time columns");

        myIncludeWKTGeometryColumnCheckBox = new JCheckBox("Add WKT Geometry Column");
        myIncludeWKTGeometryColumnCheckBox
                .setToolTipText("Adds an additonal \"WKT Geometry\" column where the feature geometry is encoded in WKT format");

        GridBagPanel accessoryPanel = new GridBagPanel();
        accessoryPanel.init0();
        accessoryPanel.fillHorizontal().setGridwidth(2);
        accessoryPanel.addRow(mySplitDateTimeCheckBox);
        accessoryPanel.addRow(myIncludeWKTGeometryColumnCheckBox);
        accessoryPanel.setGridwidth(1);
        accessoryPanel.fillNone().setInsets(0, 0, 0, 4).add(new JLabel("Lat/Lon Format:"));
        accessoryPanel.fillHorizontal().setInsets(0, 0, 0, 0).incrementGridx().add(myLatLonFormatCombo);
        accessoryPanel.setGridwidth(2).setGridx(0).incrementGridy();
        accessoryPanel.addRow(Box.createVerticalStrut(4));
        accessoryPanel.addRow(myColorFormatLabel);
        accessoryPanel.addRow(myColorFormatOptionsComboBox);
        accessoryPanel.fillVerticalSpace();

        return accessoryPanel;
    }

    /**
     * Initializes all the UI components.
     */
    private void initializeComponents()
    {
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0),
                BorderFactory.createEtchedBorder()));
        init0();
        fillHorizontal();

        JLabel exportLabel = new JLabel("Export Options");
        exportLabel.setFont(exportLabel.getFont().deriveFont(Font.BOLD, exportLabel.getFont().getSize() + 2));
        exportLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addRow(exportLabel);

        JPanel optionsPanel = new JPanel();
        myIncludeMetaColumnsCheckBox = new JCheckBox("Include Meta Columns", false);
        myIncludeMetaColumnsCheckBox.setToolTipText("Include columns such as Index, Color, and custom labels.");
        mySelectedRowsOnlyCheckBox = new JCheckBox("Selected Rows Only", false);
        mySelectedRowsOnlyCheckBox.setToolTipText("Include only rows from the list tool that are selected.");
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(mySelectedRowsOnlyCheckBox);
        optionsPanel.add(myIncludeMetaColumnsCheckBox);
        addRow(optionsPanel);

        JComponent additionals = createAdditionals();
        if (additionals != null)
        {
            addRow(additionals);
        }
        fillVerticalSpace();
    }
}
