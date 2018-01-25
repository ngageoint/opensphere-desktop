package io.opensphere.core.util.swing.table;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * A check box renderer for a table header.
 */
public class CheckBoxHeader implements TableCellRenderer
{
    /** The checkbox used for rendering. */
    private final JCheckBox myCheckBox = new JCheckBox();

    /** The Column. */
    private int myColumn;

    /** The label for the checkbox. */
    private final String myLabel;

    /** A container to put the checkbox in so it's lined up. */
    private final Container myContainer = new JPanel();

    /** The Pressed flag. */
    private boolean myPressed;

    /**
     * Instantiates a new checkbox header.
     *
     * @param label The label for the checkbox.
     * @param comp The component this header is associated with.
     */
    public CheckBoxHeader(String label, Component comp)
    {
        myCheckBox.setSelected(true);
        myLabel = label;

        myContainer.setLayout(new GridBagLayout());
        myContainer.add(myCheckBox);

        comp.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                handleClickEvent(e);
                ((Component)e.getSource()).repaint();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                myPressed = true;
            }
        });
    }

    /**
     * Add an item listener to be notified when the state of the checkbox
     * changes.
     *
     * @param itemListener The item listener.
     */
    public void addItemListener(ItemListener itemListener)
    {
        myCheckBox.addItemListener(itemListener);
    }

    /**
     * Gets the column.
     *
     * @return the column
     */
    public int getColumn()
    {
        return myColumn;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
        if (table != null)
        {
            JTableHeader header = table.getTableHeader();
            if (header != null)
            {
                myCheckBox.setForeground(header.getForeground());
                myCheckBox.setBackground(header.getBackground());
                myCheckBox.setFont(header.getFont());
            }
        }
        setColumn(column);
        myCheckBox.setText(myLabel);
        myCheckBox.setBorder(UIManager.getBorder("TableHeader.cellBorder"));

        return myContainer;
    }

    /**
     * Set the checkbox selected.
     *
     * @param b The selected state.
     */
    public void setSelected(boolean b)
    {
        if (myCheckBox.isSelected() != b)
        {
            myCheckBox.doClick();
        }
    }

    /**
     * Handle click event.
     *
     * @param e the e
     */
    private void handleClickEvent(MouseEvent e)
    {
        if (myPressed)
        {
            myPressed = false;
            JTableHeader header = (JTableHeader)e.getSource();
            JTable tableView = header.getTable();
            TableColumnModel columnModel = tableView.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = tableView.convertColumnIndexToModel(viewColumn);
            if (viewColumn == myColumn && column != -1)
            {
                myCheckBox.doClick();
            }
        }
    }

    /**
     * Sets the column.
     *
     * @param column the new column
     */
    private void setColumn(int column)
    {
        myColumn = column;
    }
}
