package io.opensphere.search.model;

import java.util.Collections;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;

/**
 * The model used by the search's result panel.
 */
public class SearchModel
{
    /**
     * Sorts by the results text.
     */
    public static final String NAME_SORT = "Name";

    /**
     * Sorts by confidence value.
     */
    public static final String RELEVANCE_SORT = "Relevance";

    /**
     * A list of all the results that were returned from all the search
     * providers.
     */
    private final ObservableList<SearchResult> myAllResults = FXCollections
            .synchronizedObservableList(FXCollections.observableArrayList());

    /**
     * The keyword to use in the search.
     */
    private final StringProperty myKeyword = new SimpleStringProperty();

    /**
     * The list of the different search types the providers provide.
     */
    private final ObservableList<String> mySearchTypes = FXCollections.observableArrayList();

    /**
     * A single selected result.
     */
    private final ObjectProperty<SearchResult> mySelectedResult = new SimpleObjectProperty<>();

    /**
     * A double selected result.
     */
    private final ObjectProperty<SearchResult> myDoubleSelectedResult = new SimpleObjectProperty<>();

    /**
     * The result on which the application is focused.
     */
    private final ObjectProperty<SearchResult> myFocusedResult = new SimpleObjectProperty<>();

    /**
     * A result the user is just hovering over.
     */
    private final ObjectProperty<SearchResult> myHoveredResult = new SimpleObjectProperty<>();

    /**
     * The user selected search types to show results for.
     */
    private final ObservableList<String> mySelectedSearchTypes = FXCollections.observableArrayList();

    /**
     * The sorted and filtered list of the results the user can see.
     */
    private final ObservableList<SearchResult> myShownResults = FXCollections.observableArrayList();

    /**
     * The user selected sort.
     */
    private final StringProperty mySortType = new SimpleStringProperty(RELEVANCE_SORT);

    /**
     * The different sorting available to the user.
     */
    private final ObservableList<String> mySortTypes = FXCollections
            .unmodifiableObservableList(FXCollections.observableArrayList(RELEVANCE_SORT, NAME_SORT));

    /** Map of provider type to result count. */
    private final ObservableMap<String, Integer> myResultCount = FXCollections
            .synchronizedObservableMap(FXCollections.observableHashMap());

    /** Map of provider type to total result count (including skipped results). */
    private final Map<String, Integer> myTotalResultCount = Collections.synchronizedMap(New.map());

    /**
     * Creates a new model, initializing all necessary fields.
     */
    public SearchModel()
    {
        mySelectedResult.addListener(this::selectionChanged);
        myFocusedResult.addListener(this::focusChanged);
        myHoveredResult.addListener(this::hoverChanged);
    }

    /**
     * An event handler used to update the selected property on the selected
     * result instance.
     *
     * @param observable ignored, but the item which changed.
     * @param oldValue the previously selected item, which may be null if
     *            nothing was previously selected. This will receive a de-select
     *            event.
     * @param newValue the newly selected item, which may be null if nothing was
     *            selected. This will receive a selection event.
     */
    private void selectionChanged(ObservableValue<? extends SearchResult> observable, SearchResult oldValue,
            SearchResult newValue)
    {
        if (oldValue != null)
        {
            oldValue.selectedProperty().set(false);
        }
        if (newValue != null)
        {
            newValue.selectedProperty().set(true);
        }
    }

    /**
     * An event handler used to update the focused property on the hovered
     * result instance.
     *
     * @param observable ignored, but the item which changed.
     * @param oldValue the previously selected item, which may be null if
     *            nothing was previously selected. This will receive a de-focus
     *            event.
     * @param newValue the newly selected item, which may be null if nothing was
     *            focused. This will receive a focusing event.
     */
    private void focusChanged(ObservableValue<? extends SearchResult> observable, SearchResult oldValue, SearchResult newValue)
    {
        if (oldValue != null)
        {
            oldValue.focusedProperty().set(false);
        }
        if (newValue != null)
        {
            newValue.focusedProperty().set(true);
        }
    }

    /**
     * An event handler used to update the focused property on the hovered
     * result instance.
     *
     * @param observable ignored, but the item which changed.
     * @param oldValue the previously selected item, which may be null if
     *            nothing was previously selected. This will receive a de-focus
     *            event.
     * @param newValue the newly selected item, which may be null if nothing was
     *            focused. This will receive a focusing event.
     */
    private void hoverChanged(ObservableValue<? extends SearchResult> observable, SearchResult oldValue, SearchResult newValue)
    {
        if (oldValue != null)
        {
            oldValue.hoveredProperty().set(false);
        }
        if (newValue != null)
        {
            newValue.hoveredProperty().set(true);
        }
    }

    /**
     * Gets a list of all the results that were returned from all the search
     * providers.
     *
     * @return the allResults.
     */
    public ObservableList<SearchResult> getAllResults()
    {
        return myAllResults;
    }

    /**
     * Gets the keyword to use in the search.
     *
     * @return the keyword The keyword to use in the search.
     */
    public StringProperty getKeyword()
    {
        return myKeyword;
    }

    /**
     * Gets the list of the different search types the providers provide.
     *
     * @return the searchTypes.
     */
    public ObservableList<String> getSearchTypes()
    {
        return mySearchTypes;
    }

    /**
     * Get a single selected result.
     *
     * @return the selectedResult.
     */
    public ObjectProperty<SearchResult> getSelectedResult()
    {
        return mySelectedResult;
    }

    /**
     * Get a double selected result.
     *
     * @return the selectedResult.
     */
    public ObjectProperty<SearchResult> getDoubleSelectedResult()
    {
        return myDoubleSelectedResult;
    }

    /**
     * Get a single selected result.
     *
     * @return the selectedResult.
     */
    public ObjectProperty<SearchResult> getFocusedResult()
    {
        return myFocusedResult;
    }

    /**
     * Gets a handful of selected results.
     *
     * @return the selectedResults.
     */
    public ObjectProperty<SearchResult> getHoveredResult()
    {
        return myHoveredResult;
    }

    /**
     * Gets the list of the different search types the providers provide.
     *
     * @return the selectedSearchTypes.
     */
    public ObservableList<String> getSelectedSearchTypes()
    {
        return mySelectedSearchTypes;
    }

    /**
     * Gets the sorted and filtered list of the results the user can see.
     *
     * @return the shownResults.
     */
    public ObservableList<SearchResult> getShownResults()
    {
        return myShownResults;
    }

    /**
     * Gets the user selected sort.
     *
     * @return the sortType.
     */
    public StringProperty getSortType()
    {
        return mySortType;
    }

    /**
     * Gets the different sorting available to the user.
     *
     * @return the sortTypes.
     */
    public ObservableList<String> getSortTypes()
    {
        return mySortTypes;
    }

    /**
     * Gets the result count map.
     *
     * @return the result count map
     */
    public ObservableMap<String, Integer> getResultCount()
    {
        return myResultCount;
    }

    /**
     * Gets the total result count map.
     *
     * @return the total result count map
     */
    public Map<String, Integer> getTotalResultCount()
    {
        return myTotalResultCount;
    }
}
