package io.opensphere.search.view;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import io.opensphere.search.model.SearchModel;

/**
 * A pane in which the set of facets and search types are displayed to the user.
 */
public class ConfigurationPane extends AbstractSearchPane
{
    /** The form in which the search types are configured and managed. */
    private final SearchTypeForm mySearchTypeForm;

    /**
     * Creates a new configuration pane in which the parameters of the search
     * can be modified.
     *
     * @param model the model to which the pane will be bound.
     */
    public ConfigurationPane(SearchModel model)
    {
        super(model);

        setPadding(new Insets(5));
        RowConstraints rowOne = new RowConstraints();
        rowOne.setFillHeight(false);
        rowOne.setVgrow(Priority.SOMETIMES);

        RowConstraints rowTwo = new RowConstraints();
        rowTwo.setFillHeight(false);
        rowTwo.setVgrow(Priority.NEVER);

        RowConstraints rowThree = new RowConstraints();
        rowThree.setFillHeight(true);
        rowThree.setVgrow(Priority.ALWAYS);

        ColumnConstraints column = new ColumnConstraints();
        column.setFillWidth(true);
        column.setHgrow(Priority.SOMETIMES);

        getRowConstraints().addAll(rowOne, rowTwo, rowThree);
        getColumnConstraints().add(column);

        mySearchTypeForm = new SearchTypeForm(getModel());

        add(mySearchTypeForm, 0, 0);
        Separator horizontalSpacer = new Separator(Orientation.HORIZONTAL);
        add(horizontalSpacer, 0, 1);
        GridPane.setMargin(horizontalSpacer, new Insets(5, 0, 5, 0));
    }
}
