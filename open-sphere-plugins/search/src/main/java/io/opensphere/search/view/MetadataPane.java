package io.opensphere.search.view;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import io.opensphere.search.model.SearchModel;

/**
 * A pane in which the result count, search terms, and sort configuration are
 * shown to the user.
 */
public class MetadataPane extends AbstractSearchPane
{
    /** The label in which the search term is placed. */
    private final Label mySearchTermLabel;

    /** The combo-box in which the sort operation is configured. */
    private final ComboBox<String> mySortDropdown;

    /**
     * Creates a new metadata pane bound to the supplied model.
     *
     * @param model the model to which the metadata pane will be bound.
     */
    public MetadataPane(SearchModel model)
    {
        super(model);

        setHgap(5);

        ColumnConstraints columnOne = new ColumnConstraints();
        columnOne.setFillWidth(false);
        columnOne.setHgrow(Priority.NEVER);
        getColumnConstraints().add(columnOne);

        ColumnConstraints columnTwo = new ColumnConstraints();
        columnTwo.setFillWidth(true);
        columnTwo.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(columnTwo);

        mySearchTermLabel = new Label();

        getModel().getKeyword().bindBidirectional(mySearchTermLabel.textProperty());

        mySortDropdown = new ComboBox<>(getModel().getSortTypes());
        mySortDropdown.valueProperty().bindBidirectional(getModel().getSortType());

        Label resultsFor = new Label("Showing Results for : ");
        Label sortLabel = new Label("Sort By:");
        Region spacer = new Region();
        spacer.setPrefWidth(100);
        add(resultsFor, 0, 0);
        add(mySearchTermLabel, 1, 0);
        add(spacer, 2, 0);
        add(sortLabel, 3, 0);
        add(mySortDropdown, 4, 0);
    }
}
