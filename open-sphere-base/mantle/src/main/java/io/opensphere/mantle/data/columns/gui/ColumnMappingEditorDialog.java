package io.opensphere.mantle.data.columns.gui;

import java.awt.Dimension;

import io.opensphere.core.util.fx.JFXDialog;

/** Swing/JavaFX column mapping editor dialog. */
public class ColumnMappingEditorDialog extends JFXDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param definedColumn the defined column
     * @param resources the column mapping resources
     */
    public ColumnMappingEditorDialog(final String definedColumn, final ColumnMappingResources resources)
    {
        super(resources.getParentFrame(), getTitle(definedColumn), () -> new ColumnMappingEditorPane(definedColumn, resources));
        setMinimumSize(new Dimension(340, 300));
        setSize(728, 450);
        setLocationRelativeTo(resources.getParentFrame());
    }

    /**
     * Gets the title.
     *
     * @param definedColumn the defined column
     * @return the title
     */
    private static String getTitle(String definedColumn)
    {
        return (definedColumn == null ? "Create " : "Edit ") + Constants.COLUMN_MAPPING.pluralTitleCase();
    }
}
