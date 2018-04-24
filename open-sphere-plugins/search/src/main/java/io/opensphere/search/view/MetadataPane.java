package io.opensphere.search.view;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;

import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.fx.FXUtilities;
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

    /** The button to clear search results without affecting the search box. */
    private final Button myClearButton;

    /**
     * Creates a new metadata pane bound to the supplied model.
     *
     * @param model the model to which the metadata pane will be bound.
     */
    public MetadataPane(SearchModel model)
    {
        super(model);

        setHgap(5);

        ColumnConstraints noGrow = new ColumnConstraints();
        noGrow.setFillWidth(false);
        noGrow.setHgrow(Priority.NEVER);

        ColumnConstraints grow = new ColumnConstraints();
        grow.setFillWidth(true);
        grow.setHgrow(Priority.ALWAYS);

        getColumnConstraints().add(noGrow);
        getColumnConstraints().add(noGrow);
        getColumnConstraints().add(noGrow);
        getColumnConstraints().add(grow);

        mySearchTermLabel = new Label();
        Label resultCountLabel = new Label();

        getModel().getKeyword().bindBidirectional(mySearchTermLabel.textProperty());
        getModel().getShownResults().addListener((ListChangeListener<SearchResult>)c -> resultsChanged(c, resultCountLabel));

        mySortDropdown = new ComboBox<>(getModel().getSortTypes());
        mySortDropdown.valueProperty().bindBidirectional(getModel().getSortType());

        myClearButton = new Button("Clear");
        myClearButton.setOnAction((evt) -> getModel().getKeyword().setValue(null));

        Label resultsFor = new Label("Showing Results for : ");
        Label sortLabel = new Label("Sort By:");
        int col = 0;
        add(resultsFor, col++, 0);
        add(mySearchTermLabel, col++, 0);
        add(resultCountLabel, col++, 0);
        add(FXUtilities.newHSpacer(), col++, 0);
        add(sortLabel, col++, 0);
        add(mySortDropdown, col++, 0);
        add(myClearButton, col++, 0);
    }

    /**
     * Updates the provider result counts.
     *
     * @param change The change event.
     * @param label The label to update
     */
    private void resultsChanged(ListChangeListener.Change<? extends SearchResult> change, Label label)
    {
        FXUtilities.runOnFXThread(() ->
        {
            if (getModel().getKeyword().get() != null)
            {
                int total = getModel().getShownResults().size();
                label.setText("(" + total + ")");
            }
            else
            {
                label.setText("");
            }
        });
    }
}
