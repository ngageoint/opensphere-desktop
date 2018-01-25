package io.opensphere.search.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;

import io.opensphere.core.search.SearchResult;
import io.opensphere.search.model.SearchModel;

/**
 * The class is responsible for sorting the results based on the type of sort
 * the user has selected.
 */
public class ResultsSorter
{
    /**
     * True if the list changing is due to us sorting it, false if the list
     * changing is being done by some other component and now it needs sorting.
     */
    private boolean myEvent;

    /**
     * Listens for any changes to the displaying list of results.
     */
    private final ListChangeListener<SearchResult> myListListener = this::onChanged;

    /**
     * The model used by the search panel.
     */
    private final SearchModel myModel;

    /**
     * Listens for the user to change the sort type.
     */
    private final ChangeListener<String> mySortTypeListener = this::sortChanged;

    /**
     * Constructs a new sorter.
     *
     * @param model The model used by the search.
     */
    public ResultsSorter(SearchModel model)
    {
        myModel = model;
        myModel.getShownResults().addListener(myListListener);
        myModel.getSortType().addListener(mySortTypeListener);
        sortChanged(myModel.getSortType(), null, myModel.getSortType().get());
    }

    /**
     * Stops listening for model changes.
     */
    public void close()
    {
        myModel.getShownResults().removeListener(myListListener);
        myModel.getSortType().removeListener(mySortTypeListener);
    }

    /**
     * Compares the result's text values to other results.
     *
     * @param result1 A result to compare.
     * @param result2 Another result to compare.
     * @return the value {@code 0} if the argument string is equal to this
     *         string; a value less than {@code 0} if this string is
     *         lexicographically less than the string argument; and a value
     *         greater than {@code 0} if this string is lexicographically
     *         greater than the string argument.
     */
    private int compareName(SearchResult result1, SearchResult result2)
    {
        return result1.getText().compareTo(result2.getText());
    }

    /**
     * Compares the result's confidence values, so higher confidence results are
     * displayed first.
     *
     * @param result1 A result to compare.
     * @param result2 Another result to compare.
     * @return -1 if result1 confidence is greater than result2, 0 if equal, and
     *         1 if result1 confidence is less than result2.
     */
    private int compareRelevance(SearchResult result1, SearchResult result2)
    {
        return Float.compare(result2.getConfidence(), result1.getConfidence());
    }

    /**
     * Occurs when the list of results has changed.
     *
     * @param change Information on the changes.
     */
    private void onChanged(Change<? extends SearchResult> change)
    {
        if (!myEvent)
        {
            sortChanged(myModel.getSortType(), null, myModel.getSortType().get());
        }
    }

    /**
     * Occurs when the user has changed the sort type.
     *
     * @param observable The observable.
     * @param oldValue The old sort value.
     * @param newValue The new sort value.
     */
    private void sortChanged(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        myEvent = true;
        if (SearchModel.RELEVANCE_SORT.equals(newValue))
        {
            myModel.getShownResults().sort(this::compareRelevance);
        }
        else if (SearchModel.NAME_SORT.equals(newValue))
        {
            myModel.getShownResults().sort(this::compareName);
        }
        myEvent = false;
    }
}
