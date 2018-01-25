package io.opensphere.search.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import org.junit.Test;

import io.opensphere.core.search.SearchResult;

/**
 * Unit test for {@link SearchModel}.
 */
public class SearchModelTest
{
    /**
     * Tests the model.
     */
    @SuppressWarnings("cast")
    @Test
    public void test()
    {
        SearchModel model = new SearchModel();
        assertEquals(2, model.getSortTypes().size());
        assertTrue(model.getSortTypes().contains(SearchModel.RELEVANCE_SORT));
        assertTrue(model.getSortTypes().contains(SearchModel.NAME_SORT));
        assertEquals(SearchModel.RELEVANCE_SORT, model.getSortType().get());

        SearchResult allResult = new SearchResult();
        assertTrue(model.getAllResults() instanceof ObservableList);
        model.getAllResults().add(allResult);

        assertTrue(model.getSearchTypes() instanceof ObservableList);
        model.getSearchTypes().add("Place Names");

        SearchResult selectedResult = new SearchResult();
        assertTrue(model.getSelectedResult() instanceof ObservableValue);
        model.getSelectedResult().set(selectedResult);

        SearchResult hoveredResult = new SearchResult();
        assertTrue(model.getHoveredResult() instanceof ObservableValue);
        model.getHoveredResult().set(hoveredResult);

        assertTrue(model.getSelectedSearchTypes() instanceof ObservableList);
        model.getSelectedSearchTypes().add("Layers");

        SearchResult shownResult = new SearchResult();
        assertTrue(model.getShownResults() instanceof ObservableList);
        model.getShownResults().add(shownResult);

        assertTrue(model.getSortType() instanceof StringProperty);
        model.getSortType().set(SearchModel.NAME_SORT);

        assertTrue(model.getKeyword() instanceof StringProperty);
        model.getKeyword().set("Search term");

        assertEquals(allResult, model.getAllResults().get(0));
        assertEquals("Place Names", model.getSearchTypes().get(0));
        assertEquals(selectedResult, model.getSelectedResult().get());
        assertEquals(hoveredResult, model.getHoveredResult().get());
        assertEquals("Layers", model.getSelectedSearchTypes().get(0));
        assertEquals(shownResult, model.getShownResults().get(0));
        assertEquals(SearchModel.NAME_SORT, model.getSortType().get());
        assertEquals("Search term", model.getKeyword().get());
    }
}
