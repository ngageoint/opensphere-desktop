package io.opensphere.analysis.table.renderers;

import java.awt.Component;
import java.text.SimpleDateFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * TimeSpan table cell renderer.
 */
public class TimeSpanTableCellRenderer extends DefaultTableCellRenderer
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
    public TimeSpanTableCellRenderer(int precision)
    {
        super();
        myFormat = ListToolPreferences.getSimpleDateFormatForPrecision(precision);
        setVerticalAlignment(DefaultTableCellRenderer.TOP);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        return super.getTableCellRendererComponent(table, TimeSpanUtility.formatTimeSpan(myFormat, (TimeSpan)value), isSelected,
                hasFocus, row, column);
    }
}
