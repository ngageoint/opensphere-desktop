package io.opensphere.core.util.swing.table;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * An easy way to create a {@link JTable} with two columns: one for a checkbox
 * and one for a value.
 */
public class CheckBoxTable extends JTable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Item listener for the checkbox that gets installed in the table header.
     */
    private final transient ItemListener myItemListener = e ->
    {
        if (e.getSource() instanceof AbstractButton && !myUpdating)
        {
            myUpdating = true;
            boolean checked = e.getStateChange() == ItemEvent.SELECTED;
            for (int row = 0; row < getRowCount(); row++)
            {
                getModel().setValueAt(Boolean.valueOf(checked), row, 0);
            }
            myUpdating = false;
        }
    };

    /**
     * Flag used to prevent undesired interaction between the header checkbox
     * and the other checkboxes.
     */
    private boolean myUpdating;

    /**
     * Construct the table.
     *
     * @param checkBoxHeader The header for the CheckBox column.
     * @param valueHeader The header for the value column.
     * @param initialCheckBoxState The initial state for the CheckBoxes.
     * @param values The values to put in the table.
     */
    public CheckBoxTable(String checkBoxHeader, String valueHeader, Boolean initialCheckBoxState,
            Collection<? extends String> values)
    {
        super(new CheckBoxTableModel(checkBoxHeader, valueHeader, initialCheckBoxState, values));

        if (checkBoxHeader.length() == 0)
        {
            getColumnModel().getColumn(0).setMaxWidth(40);
        }

        final CheckBoxHeader checkboxHeader = new CheckBoxHeader(checkBoxHeader, getTableHeader());
        checkboxHeader.addItemListener(myItemListener);
        getColumnModel().getColumn(0).setHeaderRenderer(checkboxHeader);

        getModel().addTableModelListener(e ->
        {
            if (!myUpdating)
            {
                myUpdating = true;
                checkboxHeader.setSelected(getModel().getCheckedValues().size() == getModel().getRowCount());
                getTableHeader().repaint();
                myUpdating = false;
            }
        });
    }

    /**
     * Get a list of the checked values.
     *
     * @return The checked values.
     */
    public List<String> getCheckedValues()
    {
        return getModel().getCheckedValues();
    }

    @Override
    public CheckBoxTableModel getModel()
    {
        return (CheckBoxTableModel)super.getModel();
    }

    /**
     * Set the checked values.
     *
     * @param modules The checked values.
     */
    public void setCheckedValues(Collection<? extends String> modules)
    {
        getModel().setCheckedValues(modules);
    }

    @Override
    public void setModel(TableModel model)
    {
        if (!(model instanceof CheckBoxTableModel))
        {
            throw new IllegalArgumentException("Model must be a CheckBoxTableModel.");
        }
        super.setModel(model);
    }
}
