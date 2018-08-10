package io.opensphere.overlay.query;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A manager used to maintain query action definition registrations.
 */
public class QueryActionManager
{
    /**
     * The set of query actions currently registered with the manager.
     */
    private final ObservableList<QueryActionDefinition> myQueryActions = FXCollections.observableArrayList();

    /**
     * Gets the value of the {@link #myQueryActions} field.
     *
     * @return the value stored in the {@link #myQueryActions} field.
     */
    public ObservableList<QueryActionDefinition> getQueryActions()
    {
        return myQueryActions;
    }
}
