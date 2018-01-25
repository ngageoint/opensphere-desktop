package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.awt.Component;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

/**
 * Renders a combo box in a cell when the row is selected.
 *
 */
public class EditableTextCellRenderer extends DefaultTableCellRenderer
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(EditableTextCellRenderer.class);

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The icon to use to show that the name column is editable.
     */
    private ImageIcon myIcon;

    /**
     * Constructs a new cell renderer that renders a edit pen next to the text
     * value.
     */
    public EditableTextCellRenderer()
    {
        try
        {
            myIcon = new ImageIcon(ImageIO.read(EditableTextCellRenderer.class.getResource("/images/edit.png")));
        }
        catch (IOException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        Component renderComponent = null;

        if (isSelected && myIcon != null)
        {
            String stringValue = "";

            if (value != null)
            {
                stringValue = value.toString();
            }

            JLabel label = new JLabel(stringValue, myIcon, JLabel.LEADING);
            label.setOpaque(true);
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());

            renderComponent = label;
        }

        if (renderComponent == null)
        {
            renderComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        return renderComponent;
    }
}
