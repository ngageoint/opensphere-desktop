package io.opensphere.analysis.table.renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Color table cell renderer.
 */
public class ColorTableCellRenderer extends DefaultTableCellRenderer
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The panel. */
    private final JPanel myPanel;

    /**
     * Instantiates a new color table cell renderer.
     */
    public ColorTableCellRenderer()
    {
        super();
        myPanel = new JPanel();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        if (value instanceof Color)
        {
            myPanel.setBackground((Color)value);
            return myPanel;
        }
        else
        {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
