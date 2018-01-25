package io.opensphere.analysis.export.view;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import io.opensphere.analysis.export.model.ColorFormat;
import io.opensphere.analysis.export.model.LatLonFormat;

/**
 * Interface to the Export Options UI.
 */
public interface ExportOptionsView
{
    /**
     * Gets the check box for the Add WKT column option.
     *
     * @return The check box for the Add WKT column option.
     */
    JCheckBox getAddWKT();

    /**
     * Gets the combo box for the color format option.
     *
     * @return The combo box for the color format option.
     */
    JComboBox<ColorFormat> getColorFormat();

    /**
     * Gets the label for the color format combo box.
     *
     * @return The label for the color format combo box.
     */
    JLabel getColorFormatLabel();

    /**
     * Gets the check box for the Include Meta Columns option.
     *
     * @return the check box for the Include Meta Columns option.
     */
    JCheckBox getIncludeMetaColumns();

    /**
     * Gets the combo box for the latitude longitude format option.
     *
     * @return The combo box for the latitude longitude format option.
     */
    JComboBox<LatLonFormat> getLatLonFormat();

    /**
     * Gets the check box for the Selected Rows Only option.
     *
     * @return The check box for the Selected Rows Only option.
     */
    JCheckBox getSelectedRowsOnly();

    /**
     * Gets the check box for the Separate Date Time option.
     *
     * @return The check box for the Separate Date Time option.
     */
    JCheckBox getSeparateDateTimeColumns();
}
