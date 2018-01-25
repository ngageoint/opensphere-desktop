package io.opensphere.search.view;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.ListCell;

import io.opensphere.core.search.ResultsViewSearchProvider;
import io.opensphere.core.search.SearchProvider;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;

/**
 * A simple UI that shows the values of a {@link SearchResult}.
 */
public class SearchResultUI extends ListCell<SearchResult>
{
    /**
     * The model for this ui.
     */
    private SearchResult myModel;

    /**
     * Used to display any custom search result UIs.
     */
    private final SearchRegistry mySearchRegistry;

    /**
     * Constructs a new search result UI.
     *
     * @param searchRegistry Used to display any custom search result UIs.
     */
    public SearchResultUI(SearchRegistry searchRegistry)
    {
        mySearchRegistry = searchRegistry;
        setOnMouseEntered(getOnDragDetected());
    }

    @Override
    protected void updateItem(SearchResult item, boolean empty)
    {
        super.updateItem(item, empty);
        myModel = item;
        if (myModel == null)
        {
            setGraphic(null);
        }
        else
        {
            setGraphic(createUI(false));
        }
    }

    /**
     * Create the ui components.
     *
     * @param detailed whether to create the detailed version
     * @return The created UI.
     */
    public Node createUI(boolean detailed)
    {
        Node rowUI = null;
        List<SearchProvider> providers = mySearchRegistry.getProviders(New.list(myModel.getSearchType()));
        if (providers.isEmpty() || !(providers.get(0) instanceof ResultsViewSearchProvider))
        {
            rowUI = new SearchResultBasic(myModel, detailed);
        }
        else
        {
            ResultsViewSearchProvider provider = (ResultsViewSearchProvider)providers.get(0);
            rowUI = provider.getView(myModel);
        }

        return rowUI;
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value stored in the {@link #myModel} field.
     */
    public SearchResult getResult()
    {
        return myModel;
    }
}
