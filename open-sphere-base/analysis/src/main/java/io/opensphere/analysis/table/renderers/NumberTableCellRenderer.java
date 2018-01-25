package io.opensphere.analysis.table.renderers;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

/**
 * Number table cell renderer.
 */
public class NumberTableCellRenderer extends DefaultTableCellRenderer
{
    /** Used to log messages. */
    private static final Logger LOGGER = Logger.getLogger(NumberTableCellRenderer.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The formatter with which display values are generated. */
    private final NumberFormat myFormat;

    /** The mouse listener used to react to hyperlink / click events. */
    private final transient HyperlinkMouseListener myListener;

    /**
     * Constructor, initializing the renderer with a right horizontal alignment.
     *
     * @param hyperlinkListener the hyperlink mouse listener
     */
    public NumberTableCellRenderer(HyperlinkMouseListener hyperlinkListener)
    {
        this(DefaultTableCellRenderer.RIGHT, hyperlinkListener);
    }

    /**
     * Constructor.
     *
     * @param pHorizontalAlignment the horizontal alignment of the contents
     *            within the table cell.
     * @param hyperlinkListener the hyperlink mouse listener
     */
    public NumberTableCellRenderer(int pHorizontalAlignment, HyperlinkMouseListener hyperlinkListener)
    {
        setHorizontalAlignment(pHorizontalAlignment);
        myFormat = NumberFormat.getInstance();
        myFormat.setGroupingUsed(false);
        myFormat.setMaximumFractionDigits(9);
        myListener = hyperlinkListener;
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
        Object formattedValue = formatValue(value, row, column);
        return super.getTableCellRendererComponent(table, formattedValue, isSelected, hasFocus, row, column);
    }

    /**
     * Formats the value.
     *
     * @param value the value
     * @param row the row
     * @param column the column
     * @return the formatted value
     */
    private Object formatValue(Object value, int row, int column)
    {
        Object formattedValue = value;
        if (value instanceof Number)
        {
            try
            {
                String formattedValueString = myFormat.format(value);
                if (myListener.hasURLFor(row, column, formattedValueString))
                {
                    formattedValueString = HyperlinkMouseListener.formatUrl(formattedValueString);
                }
                formattedValue = formattedValueString;
            }
            catch (IllegalArgumentException e)
            {
                LOGGER.warn("Could not format " + value, e);
            }
        }
        return formattedValue;
    }
}
