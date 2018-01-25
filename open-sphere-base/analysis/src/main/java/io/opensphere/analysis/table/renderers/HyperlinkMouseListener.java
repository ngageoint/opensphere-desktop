package io.opensphere.analysis.table.renderers;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.awt.BrowserUtilities;
import io.opensphere.core.util.net.Linker;
import io.opensphere.core.util.net.PreferencesLinkerFactory;
import io.opensphere.core.util.swing.RadioButtonPanel;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Implementation of the {@link MouseAdapter} for hyperlinks.
 */
public class HyperlinkMouseListener extends MouseAdapter
{
    /** The key provider. */
    private final transient KeyProvider myKeyProvider;

    /**
     * The formatter used to conver numeric values into formatted strings.
     */
    private final NumberFormat myFormat;

    /** Generates URLs from table values. */
    private  Linker myLinker;

    /**
     * Formats a URL string into HTML code for a link.
     *
     * @param url the URL
     * @return the HTML link code
     */
    public static String formatUrl(String url)
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("<html><a href=\"").append(url).append("\" style=\"color:#FFFF33\">").append(url).append("</a></html>");
        return sb.toString();
    }

    /**
     * Instantiates a new hyperlink mouse listener.
     *
     * @param keyProvider the key provider
     */
    public HyperlinkMouseListener(KeyProvider keyProvider)
    {
        myKeyProvider = keyProvider;

        // used for numeric fields, as the renderers will reformat numbers, and
        // links are defined with the displayed value
        // rather than the exact numeric value.
        myFormat = NumberFormat.getInstance();
        myFormat.setGroupingUsed(false);
        myFormat.setMaximumFractionDigits(9);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (myLinker != null && e != null && e.getButton() == MouseEvent.BUTTON1 && e.getComponent() instanceof JTable)
        {
            JTable table = (JTable)e.getComponent();
            Point point = e.getPoint();
            int row = table.rowAtPoint(point);
            int column = table.columnAtPoint(point);
            if (row != -1 && column != -1)
            {
                Object value = table.getValueAt(row, column);
                if (value != null)
                {
                    Rectangle rect = getRendererRect(table, row, column, value);
                    if (rect.contains(point))
                    {
                        String columnName = myKeyProvider.getKeyName(row, column);
                        Map<String, URL> urls = myLinker.getURLs(columnName, getValue(value));
                        if (!urls.isEmpty())
                        {
                            Window parentWindow = SwingUtilities.getWindowAncestor(table);
                            URL url = getUrl(urls, parentWindow);
                            if (url != null)
                            {
                                BrowserUtilities.browse(url, parentWindow);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the linker.
     *
     * @param linker the linker
     */
    public void setLinker(Linker linker)
    {
        myLinker = linker;
    }

    /**
     * Helper method to set the linker.
     *
     * @param dataType the data type
     * @param prefsRegistry the preferences registry
     */
    public void setLinker(DataTypeInfo dataType, PreferencesRegistry prefsRegistry)
    {
        myLinker = dataType != null ? PreferencesLinkerFactory.getLinker(dataType.getTypeKey(), prefsRegistry) : null;
    }

    /**
     * Get if the Linker can provide URLs for a given key and value.
     *
     * @param row the row
     * @param column the column
     * @param value the value
     * @return {@code true} if URLs can be generated.
     */
    public boolean hasURLFor(int row, int column, String value)
    {
        return myLinker != null && myLinker.hasURLFor(myKeyProvider.getKeyName(row, column), value);
    }

    /**
     * Gets the finished value as displayed to for link determination. If the
     * supplied value is a Number, the {@link NumberFormat} instance is used to
     * calculate the value as displayed.
     *
     *
     * @param pValue the raw value for which to get the finished value.
     * @return the finished value, converted from the raw value.
     */
    private String getValue(Object pValue)
    {
        if (pValue instanceof Number)
        {
            return myFormat.format(pValue);
        }
        return String.valueOf(pValue);
    }

    /**
     * Gets the rectangle of the rendered label.
     *
     * @param table the table
     * @param row the row
     * @param column the column
     * @param value the value
     * @return the rectangle
     */
    private static Rectangle getRendererRect(JTable table, int row, int column, Object value)
    {
        Rectangle rect = table.getCellRect(row, column, true);
        int rendererWidth = table.getCellRenderer(row, column)
                .getTableCellRendererComponent(table, value, false, true, row, column).getPreferredSize().width;
        rect.width = rendererWidth;
        return rect;
    }

    /**
     * Gets the URL to use.
     *
     * @param urls A map of service descriptions to URLs.
     * @param parentWindow the parent window
     * @return the URL to use
     */
    private static URL getUrl(Map<String, URL> urls, Window parentWindow)
    {
        URL url;
        if (urls.size() == 1)
        {
            url = urls.values().iterator().next();
        }
        else
        {
            JLabel label = new JLabel("Please select an action for this link:");
            RadioButtonPanel<String> buttonPanel = new RadioButtonPanel<>(urls.keySet(), urls.keySet().iterator().next());
            int choice = JOptionPane.showConfirmDialog(parentWindow, new Object[] { label, buttonPanel }, "Link",
                    JOptionPane.OK_CANCEL_OPTION);
            if (choice == JOptionPane.OK_OPTION)
            {
                url = urls.get(buttonPanel.getSelection());
            }
            else
            {
                url = null;
            }
        }
        return url;
    }
}
