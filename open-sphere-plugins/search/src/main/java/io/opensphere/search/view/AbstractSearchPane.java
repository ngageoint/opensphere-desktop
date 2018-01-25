package io.opensphere.search.view;

import javafx.scene.layout.GridPane;

import com.google.common.base.Preconditions;

import io.opensphere.search.model.SearchModel;

/**
 * An abstract base class of the components used to render a search panel.
 */
public abstract class AbstractSearchPane extends GridPane
{
    /** The model in which the search configuration state is maintained. */
    private final SearchModel myModel;

    /**
     * Creates a new abstract search pane, binding it to the supplied model,
     * which must not be null.
     *
     * @param model the model to which the pane will be bound, must not be null.
     */
    public AbstractSearchPane(SearchModel model)
    {
        Preconditions.checkNotNull(model);

        myModel = model;
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value stored in the {@link #myModel} field.
     */
    public SearchModel getModel()
    {
        return myModel;
    }
}
