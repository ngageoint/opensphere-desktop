package io.opensphere.search.view;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.search.model.SearchModel;

/**
 * A simple form full of checkboxes, bound to a {@link SearchModel} instance.
 * The checkboxes are derived from the bound
 * {@link SearchModel#getSearchTypes()} method.
 */
public class SearchTypeForm extends AbstractSearchPane
{
    /**
     * A dictionary of choices, using the text of the choice as the key, and the
     * checkbox as the value.
     */
    private final ObservableMap<String, CheckBox> myChoices;

    /** The UI container in which checkboxes are displayed. */
    private final VBox myContainer;

    /**
     * Creates a new search type form, bound to the supplied model.
     *
     * @param model the model to which to bind the form.
     */
    public SearchTypeForm(SearchModel model)
    {
        super(model);
        getModel().getSearchTypes().addListener(this::searchTypesChanged);
        getModel().getSelectedSearchTypes().addListener(this::selectedSearchTypesChanged);
        getModel().getResultCount().addListener(this::resultCountChanged);

        myChoices = FXCollections.observableMap(New.map());
        myChoices.addListener(this::searchTypeMapChanged);

        ColumnConstraints column = new ColumnConstraints();
        column.setFillWidth(true);
        column.setHgrow(Priority.SOMETIMES);

        Label searchTypesLabel = new Label("Search In:");
        myContainer = new VBox(2);
        add(searchTypesLabel, 0, 0);
        add(myContainer, 0, 1);
        addSearchTypes(getModel().getSearchTypes());
    }

    /**
     * Adds the search types to the UI as a list of checkboxes.
     *
     * @param searchTypes The search types to add.
     */
    private void addSearchTypes(List<? extends String> searchTypes)
    {
        for (String text : searchTypes)
        {
            CheckBox checkBox = new CheckBox(text);
            checkBox.setSelected(getModel().getSelectedSearchTypes().contains(text));
            checkBox.selectedProperty().addListener((o, oV, nV) -> updateSelectedSearchTypes(text, nV.booleanValue()));
            myChoices.put(text, checkBox);
        }
    }

    /**
     * Updates the provider result counts.
     *
     * @param change The change event.
     */
    private void resultCountChanged(MapChangeListener.Change<? extends String, ? extends Integer> change)
    {
        FXUtilities.runOnFXThread(() ->
        {
            Integer addedCount = change.getValueAdded();
            if (addedCount != null)
            {
                CheckBox checkBox = myChoices.get(change.getKey());
                StringBuilder builder = new StringBuilder(change.getKey());
                builder.append(" (").append(addedCount);
                Integer totalCount = getModel().getTotalResultCount().get(change.getKey());
                if (totalCount != null && totalCount.intValue() != -1 && totalCount.intValue() != addedCount.intValue())
                {
                    builder.append(" of ").append(totalCount);
                }
                builder.append(')');
                checkBox.setText(builder.toString());
            }

            Integer removedCount = change.getValueRemoved();
            if (removedCount != null)
            {
                CheckBox checkBox = myChoices.get(change.getKey());
                checkBox.setText(change.getKey() + " (0)");
            }
        });
    }

    /**
     * Adds and removes checkboxes in response to changes to {@link #myChoices}.
     * Note that this will not change any bindings to the checkboxes, only add
     * or remove checkboxes from the UI.
     *
     * @param change the change fired by the underlying choices map.
     */
    private void searchTypeMapChanged(MapChangeListener.Change<? extends String, ? extends CheckBox> change)
    {
        FXUtilities.runOnFXThread(() ->
        {
            CheckBox addedCheckbox = change.getValueAdded();
            if (addedCheckbox != null)
            {
                myContainer.getChildren().add(addedCheckbox);
            }

            CheckBox removedCheckbox = change.getValueRemoved();
            if (removedCheckbox != null)
            {
                myContainer.getChildren().remove(removedCheckbox);
            }
        });
    }

    /**
     * An event handler used to modify the available choices when the set of
     * search types change in the model.
     *
     * @param change the metadata object describing the change.
     */
    private void searchTypesChanged(ListChangeListener.Change<? extends String> change)
    {
        while (change.next())
        {
            // remove existing checkboxes and unbind them from the model:
            change.getRemoved().stream().filter(s -> myChoices.containsKey(s))
                    .forEach(s -> myChoices.remove(s).selectedProperty().unbind());

            addSearchTypes(change.getAddedSubList());
        }
    }

    /**
     * An event handler used to modify the selections when the set of selected search types change in the model.
     *
     * @param change the metadata object describing the change.
     */
    private void selectedSearchTypesChanged(ListChangeListener.Change<? extends String> change)
    {
        while (change.next())
        {
            for (String type : change.getAddedSubList())
            {
                CheckBox checkBox = myChoices.get(type);
                if (checkBox != null)
                {
                    checkBox.setSelected(true);
                }
            }
            for (String type : change.getRemoved())
            {
                CheckBox checkBox = myChoices.get(type);
                if (checkBox != null)
                {
                    checkBox.setSelected(false);
                }
            }
        }
    }

    /**
     * Updates the underlying model (as retrieved from in {@link #getModel()})
     * with the selected search types.
     *
     * @param type the type with which to update the model.
     * @param selected the selected state of the type.
     */
    private void updateSelectedSearchTypes(String type, boolean selected)
    {
        if (selected)
        {
            CollectionUtilities.addIfNotContained(getModel().getSelectedSearchTypes(), type);
        }
        else
        {
            getModel().getSelectedSearchTypes().remove(type);
        }
    }
}
