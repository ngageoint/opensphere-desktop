package io.opensphere.analysis.baseball;

import java.awt.Component;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import io.opensphere.analysis.table.renderers.DateTableCellRenderer;
import io.opensphere.analysis.table.renderers.HyperlinkMouseListener;
import io.opensphere.analysis.table.renderers.KeyProvider;
import io.opensphere.analysis.table.renderers.LatLonTableCellRenderer;
import io.opensphere.analysis.table.renderers.LatLonTableCellRenderer.CoordType;
import io.opensphere.analysis.table.renderers.NumberTableCellRenderer;
import io.opensphere.analysis.table.renderers.StringTableCellRenderer;
import io.opensphere.analysis.table.renderers.TimeSpanTableCellRenderer;
import io.opensphere.core.model.GeographicPositionFormat;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;

/**
 * The baseball card table renderer.
 */
class BaseballRenderer extends DefaultTableCellRenderer implements AutoCloseable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The table model. */
    private final BaseballTableModel myTableModel;

    /** The table. */
    private final JTable myTable;

    /** The latitude renderer. */
    private final LatLonTableCellRenderer myLatRenderer = new LatLonTableCellRenderer(GeographicPositionFormat.DECDEG,
            CoordType.LAT);

    /** The longitude renderer. */
    private final LatLonTableCellRenderer myLonRenderer = new LatLonTableCellRenderer(GeographicPositionFormat.DECDEG,
            CoordType.LON);

    /** The string renderer. */
    private final StringTableCellRenderer myStringRenderer;

    /** The number renderer. */
    private final NumberTableCellRenderer myNumberRenderer;

    /** The date renderer. */
    private final DateTableCellRenderer myDateRenderer;

    /** The time span renderer. */
    private final TimeSpanTableCellRenderer myTimeSpanRenderer;

    /** The hyperlink listener. */
    private final transient HyperlinkMouseListener myListener;

    /**
     * Constructor.
     *
     * @param tableModel The table model
     * @param table The table
     * @param prefsRegistry The preferences registry
     */
    public BaseballRenderer(BaseballTableModel tableModel, JTable table, PreferencesRegistry prefsRegistry)
    {
        super();
        myTableModel = tableModel;
        myTable = table;

        myListener = createHyperlinkListener(tableModel, table, prefsRegistry);

        myStringRenderer = new StringTableCellRenderer(myListener);
        myNumberRenderer = new NumberTableCellRenderer(SwingConstants.LEFT, myListener);
        int timePrecision = prefsRegistry.getPreferences(ListToolPreferences.class)
                .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
        myDateRenderer = new DateTableCellRenderer(timePrecision);
        myTimeSpanRenderer = new TimeSpanTableCellRenderer(timePrecision);

        table.addMouseListener(myListener);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        Component component;
        if (value instanceof Double)
        {
            SpecialKey specialType = myTableModel.getSpecialKey(row);
            if (specialType instanceof LatitudeKey)
            {
                component = myLatRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            else if (specialType instanceof LongitudeKey)
            {
                component = myLonRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            else
            {
                component = myNumberRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
        else if (value instanceof Number)
        {
            component = myNumberRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        else if (value instanceof Date)
        {
            component = myDateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        else if (value instanceof TimeSpan)
        {
            component = myTimeSpanRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        else
        {
            component = myStringRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        return component;
    }

    @Override
    public void close()
    {
        myTable.removeMouseListener(myListener);
    }

    /**
     * Creates the hyperlink mouse listener.
     *
     * @param tableModel the table model
     * @param table the table
     * @param prefsRegistry the preferences registry
     * @return the listener
     */
    private HyperlinkMouseListener createHyperlinkListener(BaseballTableModel tableModel, JTable table,
            PreferencesRegistry prefsRegistry)
    {
        KeyProvider keyProvider = (row, column) -> (String)table.getValueAt(row, 0);
        HyperlinkMouseListener listener = new HyperlinkMouseListener(keyProvider);
        listener.setLinker(tableModel.getDataElement().getDataTypeInfo(), prefsRegistry);
        return listener;
    }
}
