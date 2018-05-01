package io.opensphere.analysis.table.renderers;

import java.awt.Component;

import javax.swing.JTable;

import io.opensphere.analysis.table.functions.ColumnFunction;

/** Rendering class for Column Function objects. */
public class ColumnFunctionTableCellRenderer extends NumberTableCellRenderer
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor, initializing the renderer with a right horizontal alignment.
     *
     * @param hyperlinkListener the hyperlink mouse listener
     */
    public ColumnFunctionTableCellRenderer(HyperlinkMouseListener hyperlinkListener)
    {
        super(hyperlinkListener);
    }

    /**
     * Constructor.
     *
     * @param pHorizontalAlignment the horizontal alignment of the contents
     *            within the table cell.
     * @param hyperlinkListener the hyperlink mouse listener
     */
    public ColumnFunctionTableCellRenderer(int pHorizontalAlignment, HyperlinkMouseListener hyperlinkListener)
    {
        super(pHorizontalAlignment, hyperlinkListener);
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        if (value instanceof ColumnFunction)
        {
            ColumnFunction colFunc = (ColumnFunction)value;
            int[] cols = colFunc.getColumns();
            Object[] vals = new Object[cols.length];

            for (int i = 0; i < cols.length; i++)
            {
                vals[i] = table.getModel().getValueAt(row, cols[i]);
            }

            // Format as Label
            value = colFunc.getValue(vals);
        }

        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
