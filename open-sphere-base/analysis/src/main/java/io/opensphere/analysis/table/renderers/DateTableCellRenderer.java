package io.opensphere.analysis.table.renderers;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import io.opensphere.core.preferences.ListToolPreferences;

/**
 * Date table cell renderer.
 */
public class DateTableCellRenderer extends DefaultTableCellRenderer
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The format. */
    private final SimpleDateFormat myFormat;

    /**
     * Constructor.
     *
     * @param precision the precision
     */
    public DateTableCellRenderer(int precision)
    {
        super();
        myFormat = ListToolPreferences.getSimpleDateFormatForPrecision(precision);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        Object formattedValue = value instanceof Date ? myFormat.format((Date)value) : value;
        return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }
}
