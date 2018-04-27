package io.opensphere.search.controller;

import java.awt.EventQueue;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.swing.input.DontShowDialog;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.search.model.SearchModel;

/**
 * Performs keyword or area searches and populates the models with those results.
 */
public class SearchController
{
    /**
     * The keyword listener.
     */
    private final ChangeListener<String> myKeywordListener = this::onKeywordChange;

    /**
     * The main search model.
     */
    private final SearchModel myModel;

    /**
     * Listens for any new results.
     */
    private final ListChangeListener<SearchResult> myResultsListener = this::onResultsChanged;

    /**
     * Executes searches.
     */
    private final SearchExecutor mySearcher;

    /**
     * Used to get all the installed search providers.
     */
    private final SearchRegistry mySearchRegistry;

    /**
     * Listens for any changes to the different search types.
     */
    private ListChangeListener<String> mySearchTypeListener;

    /**
     * Handles selection and hover changes within the model.
     */
    private final SelectedResultHandler mySelectedResultsHandler;

    /**
     * Ensures the results are in proper sorted order.
     */
    private final ResultsSorter mySorter;

    /**
     * Displays the results on the map.
     */
    private final SearchTransformer myTransformer;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Listen to events from the main viewer. */
    private final ViewChangeListener myViewListener = (viewer, type) -> handleSpatialTemporalChange();

    /** Listen to time changes. */
    private final PrimaryTimeSpanChangeListener myTimeListener = PrimaryTimeSpanChangeListener
            .newChangedListener(spans -> handleSpatialTemporalChange());

    /** The executor to throttle notifications to the user. */
    private final ProcrastinatingExecutor myNotificationExecutor = new ProcrastinatingExecutor("SearchNotification", 1000);

    /** Whether the notification dialog is showing. */
    private boolean myNotificationDialogShowing;

    /**
     * Constructs a new search controller.
     *
     * @param toolbox The system toolbox.
     * @param searchModel The main search model.
     */
    public SearchController(Toolbox toolbox, SearchModel searchModel)
    {
        myToolbox = toolbox;
        mySearchRegistry = toolbox.getSearchRegistry();
        myModel = searchModel;
        mySearcher = new SearchExecutor(myModel, mySearchRegistry, toolbox.getTimeManager(), toolbox.getMapManager());

        BiMap<SearchResult, Geometry> resultToGeometries = Maps.synchronizedBiMap(HashBiMap.create());
        BiMap<SearchResult, Geometry> resultToLabelGeometries = Maps.synchronizedBiMap(HashBiMap.create());
        mySelectedResultsHandler = new SelectedResultHandler(myModel, toolbox.getControlRegistry(), toolbox.getMapManager(),
                resultToGeometries, resultToLabelGeometries, new ViewerAnimatorCreator());
        myTransformer = new SearchTransformer(myModel, toolbox.getGeometryRegistry(), resultToGeometries,
                resultToLabelGeometries);
        myModel.getAllResults().addListener(myResultsListener);
        mySorter = new ResultsSorter(myModel);
        myModel.getKeyword().addListener(myKeywordListener);
    }

    /**
     * Stops listening to model changes.
     */
    public void close()
    {
        mySorter.close();
        myTransformer.close();
        mySelectedResultsHandler.close();
        myModel.getSelectedSearchTypes().removeListener(mySearchTypeListener);
        myModel.getAllResults().removeListener(myResultsListener);
        myModel.getKeyword().removeListener(myKeywordListener);
    }

    /**
     * Performs the search based on the inputs within the search model.
     */
    public void performSearch()
    {
        if (mySearchTypeListener == null)
        {
            mySearchTypeListener = this::onSearchTypesChanged;
            myModel.getSelectedSearchTypes().addListener(mySearchTypeListener);
        }

        clearSearch();
        mySearcher.performSearch();
    }

    /**
     *
     * @param visible whether the dialog is visible
     */
    public void setDialogVisible(boolean visible)
    {
        if (visible)
        {
            myToolbox.getMapManager().getViewChangeSupport().addViewChangeListener(myViewListener);
            myToolbox.getTimeManager().addPrimaryTimeSpanChangeListener(myTimeListener);
        }
        else
        {
            myToolbox.getMapManager().getViewChangeSupport().removeViewChangeListener(myViewListener);
            myToolbox.getTimeManager().removePrimaryTimeSpanChangeListener(myTimeListener);
        }
    }

    /**
     * Clears all the results from the previous search.
     */
    private void clearSearch()
    {
        myModel.getAllResults().clear();
        myModel.getShownResults().clear();
        mySearcher.clearSearch();
    }

    /**
     * Handles changes to the map (viewer) or time.
     */
    private void handleSpatialTemporalChange()
    {
        myNotificationExecutor.execute(() ->
        {
            if (!myNotificationDialogShowing)
            {
                myNotificationDialogShowing = true;

                EventQueue.invokeLater(() ->
                {
                    JFrame parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
                    int response = DontShowDialog.showConfirmAndRememberDialog(myToolbox.getPreferencesRegistry(), parent,
                            "Search results may have changed. Re-query?", "Search Notification", false);
                    if (response == JOptionPane.OK_OPTION)
                    {
                        Platform.runLater(this::performSearch);
                    }
                });

                myNotificationDialogShowing = false;
            }
        });
    }

    /**
     * Handles when search types are selected and adds the results of those types from the view.
     *
     * @param types The types that have been selected.
     */
    private void handleSearchTypesAdded(Set<String> types)
    {
        Set<String> typesToSearch = New.set(types);
        List<SearchResult> toShow = New.list();
        for (SearchResult result : myModel.getAllResults())
        {
            if (types.contains(result.getSearchType()))
            {
                toShow.add(result);
                typesToSearch.remove(result.getSearchType());
            }
        }

        if (!toShow.isEmpty())
        {
            myModel.getShownResults().addAll(toShow);
        }

        if (!typesToSearch.isEmpty())
        {
            mySearcher.performSearch(typesToSearch);
        }
    }

    /**
     * Handles when search types are unselected and removes the results of those types from the view.
     *
     * @param types The types that have been unselected.
     */
    private void handleSearchTypesRemoved(Set<String> types)
    {
        List<SearchResult> toRemove = New.list();
        for (SearchResult result : myModel.getShownResults())
        {
            if (types.contains(result.getSearchType()))
            {
                toRemove.add(result);
            }
        }

        myModel.getShownResults().removeAll(toRemove);
    }

    /**
     * Handles when the keyword has changed and either performs a new search or clears out the search.
     *
     * @param observable The observable.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void onKeywordChange(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        if (StringUtils.isEmpty(newValue))
        {
            clearSearch();
        }
        else
        {
            performSearch();
        }
    }

    /**
     * Handles when results are added and figures out if we should show them or not.
     *
     * @param change The change event.
     */
    private void onResultsChanged(Change<? extends SearchResult> change)
    {
        List<SearchResult> resultsToShow = New.list();
        while (change.next())
        {
            for (SearchResult result : change.getAddedSubList())
            {
                if (myModel.getSelectedSearchTypes().contains(result.getSearchType()))
                {
                    resultsToShow.add(result);
                }
            }
        }

        myModel.getShownResults().addAll(resultsToShow);
    }

    /**
     * Handles when the selected search types change and removes or adds results based on the changes.
     *
     * @param change The change event.
     */
    private void onSearchTypesChanged(Change<? extends String> change)
    {
        Set<String> removed = New.set();
        Set<String> added = New.set();
        while (change.next())
        {
            removed.addAll(change.getRemoved());
            added.addAll(change.getAddedSubList());
        }

        if (!removed.isEmpty())
        {
            handleSearchTypesRemoved(removed);
        }

        if (!added.isEmpty())
        {
            handleSearchTypesAdded(added);
        }
    }
}
