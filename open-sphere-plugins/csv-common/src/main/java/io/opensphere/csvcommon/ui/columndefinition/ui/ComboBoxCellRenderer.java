package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang3.StringUtils;

/**
 * Renders a combo box in a cell when the row is selected.
 *
 */
public class ComboBoxCellRenderer extends DefaultTableCellRenderer
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The real combo box model.
     */
    private final JComboBox<String> myRealCombo;

    /**
     * The edit message to display to the user.
     */
    private final String myEditMessage;

    /**
     * Constructs a combo box renderer.
     *
     * @param theRealComboBox The real combo box that may or may not contain
     *            data for the user to choose.
     * @param editMessage The edit message to display to the user or null if the
     *            user shouldn't see one.
     */
    public ComboBoxCellRenderer(JComboBox<String> theRealComboBox, String editMessage)
    {
        myRealCombo = theRealComboBox;
        myEditMessage = editMessage;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        Component renderComponent;

        if (isSelected && myRealCombo.getModel().getSize() > 1 && (value == null || value instanceof String))
        {
            JComboBox<String> combo = new JComboBox<>();

            combo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, table.getBackground()));

            if (value != null && StringUtils.isNotEmpty(value.toString()))
            {
                combo.addItem((String)value);
                combo.setForeground(table.getSelectionForeground());
            }
            else
            {
                combo.addItem(myEditMessage);
                combo.setForeground(table.getSelectionForeground().darker());
                combo.setFont(new Font(combo.getFont().getName(), Font.ITALIC, combo.getFont().getSize()));
            }

            combo.setSelectedIndex(0);
            combo.setBackground(table.getSelectionBackground());

            combo.setOpaque(true);

            renderComponent = combo;
        }
        else
        {
            renderComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        return renderComponent;
    }
}
