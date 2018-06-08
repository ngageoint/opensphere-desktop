package io.opensphere.analysis.table.renderers;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * String table cell renderer.
 */
public class StringTableCellRenderer extends DefaultTableCellRenderer
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The text area. */
    private final JTextArea myTextArea;

    /** The auto fit row height. */
    private boolean myAutoFitRowHeight;

    /** The listener. */
    private final transient HyperlinkMouseListener myListener;

    /**
     * Constructor.
     *
     * @param hyperlinkListener the hyperlink mouse listener
     */
    public StringTableCellRenderer(HyperlinkMouseListener hyperlinkListener)
    {
        super();
        myTextArea = new JTextArea();
        myTextArea.setLineWrap(true);
        myTextArea.setWrapStyleWord(true);
        myListener = hyperlinkListener;

        setVerticalAlignment(DefaultTableCellRenderer.TOP);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value != null)
        {
            String sVal = value.toString();
            if (sVal.length() > 0 && myListener.hasURLFor(row, column, sVal))
            {
                setValue(HyperlinkMouseListener.formatUrl(sVal));
            }

            // We're going to arbitrarily decide that 50 characters is the width
            // cutoff for using a text area instead of a label.
            if (sVal.length() > 50)
            {
                myTextArea.setForeground(getForeground());
                myTextArea.setBackground(getBackground());
                myTextArea.setBorder(getBorder());
                myTextArea.setFont(getFont());
                myTextArea.setText(getText());

                FontMetrics fm = myTextArea.getFontMetrics(myTextArea.getFont());
                int lineHeight = fm.getHeight();
                int h = myTextArea.getLineCount() * lineHeight + 6;

                if (myAutoFitRowHeight && h > table.getRowHeight(row))
                {
                    table.setRowHeight(row, h);
                }

                return myTextArea;
            }
        }

        return this;
    }

    /**
     * Auto fit row height.
     *
     * @param autoFit the auto fit
     */
    public void autoFitRowHeight(boolean autoFit)
    {
        myAutoFitRowHeight = autoFit;
    }
}
