package io.opensphere.core.search;

import javafx.scene.Node;

/**
 * Interface to a {@link ResultsSearchProvider} that provides its on UI to
 * represent a single {@link SearchResult}.
 */
public interface ResultsViewSearchProvider extends ResultsSearchProvider
{
    /**
     * Gets the UI that represents the single {@link SearchResult}.
     *
     * @param result The result to get the UI for.
     * @return A UI representing the result.
     */
    Node getView(SearchResult result);
}
