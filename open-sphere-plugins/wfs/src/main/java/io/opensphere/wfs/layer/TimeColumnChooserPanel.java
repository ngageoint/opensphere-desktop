package io.opensphere.wfs.layer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * The Class TimeColumnChooserPanel. This is a panel that will present a user
 * with the option to manually assign start/end time columns to a WFS only layer
 * that contains an identified start and/or end time column.
 */
final class TimeColumnChooserPanel extends GridBagPanel
{
    /** Serial Version UID. */
    private static final long serialVersionUID = 1L;

    /** The string for none. */
    private static final String NONE = "None";

    /** The Start time combo box. */
    private final JComboBox<String> myStartTimeCombo;

    /** The End time combo box. */
    private final JComboBox<String> myEndTimeCombo;

    /**
     * Helper method to get the DefaultComboBoxModel from the JComboBox.
     *
     * @param comboBox the combo box
     * @return the DefaultComboBoxModel
     */
    private static DefaultComboBoxModel<String> getModel(JComboBox<String> comboBox)
    {
        return (DefaultComboBoxModel<String>)comboBox.getModel();
    }

    /**
     * Instantiates a new time column chooser panel.
     *
     * @param layerName the layer name
     */
    public TimeColumnChooserPanel(String layerName)
    {
        super();

        myStartTimeCombo = new JComboBox<>();
        myStartTimeCombo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateEndTimeOptions();
            }
        });

        myEndTimeCombo = new JComboBox<>();

        init0();

        fillHorizontal().setGridwidth(2);
        add(new JLabel(StringUtilities.concat("Choose time columns for \"", layerName, "\":")));
        setGridwidth(1).anchorWest();

        fillNone().setInsets(10, 0, 0, 5);
        incrementGridy();
        add(new JLabel("Start Time:"));

        fillHorizontal().setInsets(10, 0, 0, 0);
        setGridx(1);
        add(myStartTimeCombo);

        fillNone().setInsets(10, 0, 0, 5);
        setGridx(0).incrementGridy();
        add(new JLabel("End Time:"));

        fillHorizontal().setInsets(10, 0, 0, 0);
        setGridx(1);
        add(myEndTimeCombo);
    }

    /**
     * Gets the selected columns. Returns a list of column names as selected by
     * the user. The first element will be the "Start time" column and the
     * second element, if populated, will be the "End time" column.
     *
     * @return the list of selected columns
     */
    public List<String> getSelectedColumns()
    {
        List<String> selectedColumns = New.list(2);
        selectedColumns.add((String)myStartTimeCombo.getSelectedItem());
        if (myEndTimeCombo.isEnabled())
        {
            String endSelection = (String)myEndTimeCombo.getSelectedItem();
            // Handle the empty string selection case
            if (endSelection != null && !endSelection.equals(NONE))
            {
                selectedColumns.add(endSelection);
            }
        }
        return selectedColumns;
    }

    /**
     * Sets the combobox options.
     *
     * @param options the new combobox options
     */
    public void setComboboxOptions(List<String> options)
    {
        DefaultComboBoxModel<String> startModel = getModel(myStartTimeCombo);

        startModel.removeAllElements();
        for (String option : options)
        {
            startModel.addElement(option);
        }

        updateEndTimeOptions();
    }

    /**
     * Sets the selected end column.
     *
     * @param column the column
     */
    public void setEndColumn(String column)
    {
        if (column == null)
        {
            getModel(myEndTimeCombo).setSelectedItem(NONE);
        }
        else
        {
            getModel(myEndTimeCombo).setSelectedItem(column);
        }
    }

    /**
     * Sets the selected start column.
     *
     * @param column the column
     */
    public void setStartColumn(String column)
    {
        getModel(myStartTimeCombo).setSelectedItem(column);
    }

    /**
     * Updates the end time options based on the start time options.
     */
    private void updateEndTimeOptions()
    {
        DefaultComboBoxModel<String> startModel = getModel(myStartTimeCombo);
        DefaultComboBoxModel<String> endModel = getModel(myEndTimeCombo);

        endModel.removeAllElements();
        endModel.addElement(NONE);
        for (int i = 0; i < startModel.getSize(); i++)
        {
            String option = startModel.getElementAt(i);
            if (!option.equals(startModel.getSelectedItem()))
            {
                endModel.addElement(option);
            }
        }

        myEndTimeCombo.setSelectedIndex(0);
    }
}
