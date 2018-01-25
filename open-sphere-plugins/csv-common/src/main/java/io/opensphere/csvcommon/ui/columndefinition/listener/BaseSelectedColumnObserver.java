package io.opensphere.csvcommon.ui.columndefinition.listener;

import java.util.Observable;
import java.util.Observer;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;

/**
 * The base class that observers the column definiton model for changes in the
 * currently select column.
 */
public abstract class BaseSelectedColumnObserver implements Observer
{
    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * The currently selected column.
     */
    private ColumnDefinitionRow mySelectedColumn;

    /**
     * Constructor for the base selected column observer.
     *
     * @param model The model.
     */
    public BaseSelectedColumnObserver(ColumnDefinitionModel model)
    {
        myModel = model;
        myModel.addObserver(this);
    }

    /**
     * Unsubscribes from model events.
     */
    public void close()
    {
        myModel.deleteObserver(this);
    }

    /**
     * Gets the currently selected column.
     *
     * @return The currently selected column or null if there isn't one.
     */
    public ColumnDefinitionRow getSelectedColumn()
    {
        return mySelectedColumn;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnDefinitionModel.SELECTED_DEFINITION_PROPERTY.equals(arg))
        {
            if (mySelectedColumn != null)
            {
                mySelectedColumn.deleteObserver(this);
                mySelectedColumn = null;
            }

            mySelectedColumn = myModel.getSelectedDefinition();

            if (mySelectedColumn != null)
            {
                mySelectedColumn.addObserver(this);
            }
        }
    }

    /**
     * Gets the model.
     *
     * @return The model.
     */
    protected final ColumnDefinitionModel getModel()
    {
        return myModel;
    }
}
