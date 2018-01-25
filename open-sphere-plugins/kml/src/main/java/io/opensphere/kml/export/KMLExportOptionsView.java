package io.opensphere.kml.export;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Interface to the Export Options UI.
 */
public interface KMLExportOptionsView
{
    /**
     * Text field for the title text of the KML file.
     *
     * @return the JTextFiel.
     */
    JTextField getTitleTextField();

    /**
     * Determines whether KML record title keys off a metadata field or is a
     * one-up counter with a prefix.
     *
     * @return the JRadioButton
     */
    JRadioButton getMetadataFieldRadioButton();

    /**
     * Text field for the individual KML record.
     *
     * @return the text field.
     */
    JTextField getRecordTextField();

    /**
     * ComboBox represeting the metadata field KML record titles should key off
     * of.
     *
     * @return the comboBox
     */
    JComboBox<String> getMetadataFieldSelector();

    /**
     * Whether or not KML icon should be a dot or icon.
     *
     * @return the radio button
     */
    JRadioButton getIconDotRadioButton();

    /**
     * Text Field with the icon for KML records.
     *
     * @return the iconRecord representing the icon
     */
    JTextField getIconFileField();
}
