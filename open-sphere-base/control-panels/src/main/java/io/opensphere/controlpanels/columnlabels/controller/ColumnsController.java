package io.opensphere.controlpanels.columnlabels.controller;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.collections.New;

/**
 * Populates the available columns and sets a default.
 */
public class ColumnsController implements ListDataListener<ColumnLabel>
{
    /**
     * The list of available columns the user can apply to a label.
     */
    private final List<String> myAvailableColumns;

    /**
     * The model.
     */
    private final ColumnLabels myModel;

    /**
     * Constructs a new columns controller.
     *
     * @param model The model.
     * @param availableColumns The available columns.
     */
    public ColumnsController(ColumnLabels model, List<String> availableColumns)
    {
        myModel = model;
        myAvailableColumns = availableColumns;
        for (ColumnLabel label : model.getColumnsInLabel())
        {
            populateColumns(label);
        }
        myModel.getColumnsInLabel().addChangeListener(this);
    }

    /**
     * Stops populating the model with columns.
     */
    public void close()
    {
        myModel.getColumnsInLabel().removeChangeListener(this);
    }

    @Override
    public void elementsAdded(ListDataEvent<ColumnLabel> e)
    {
        for (ColumnLabel added : e.getChangedElements())
        {
            populateColumns(added);
            determineDefault(added);
        }
    }

    @Override
    public void elementsChanged(ListDataEvent<ColumnLabel> e)
    {
        // No need to handle.
    }

    @Override
    public void elementsRemoved(ListDataEvent<ColumnLabel> e)
    {
        // No need to handle.
    }

    /**
     * Determines a good default value.
     *
     * @param columnLabel The column label to apply a default value to.
     */
    private void determineDefault(ColumnLabel columnLabel)
    {
        if (StringUtils.isEmpty(columnLabel.getColumn()) && !myAvailableColumns.isEmpty())
        {
            String defaultColumn = myAvailableColumns.get(0);
            Set<String> currentColumns = New.set();
            for (ColumnLabel label : myModel.getColumnsInLabel())
            {
                currentColumns.add(label.getColumn());
            }

            for (String available : myAvailableColumns)
            {
                if (!currentColumns.contains(available))
                {
                    defaultColumn = available;
                    break;
                }
            }

            columnLabel.setColumn(defaultColumn);
        }
    }

    /**
     * Populates the available columns.
     *
     * @param columnLabel The model to populate the available columns for.
     */
    private void populateColumns(ColumnLabel columnLabel)
    {
        if (columnLabel.getAvailableColumns().isEmpty())
        {
            columnLabel.getAvailableColumns().addAll(myAvailableColumns);
        }
    }
}
