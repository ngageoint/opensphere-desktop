package io.opensphere.search.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;
import io.opensphere.search.model.SearchModel;

/**
 * Unit test for {@link ResultsSorter}.
 */
public class ResultsSorterTest
{
    /**
     * Tests when the user changes the sorting mechanism.
     */
    @Test
    public void testSortChanged()
    {
        SearchResult lowConfidence = new SearchResult();
        lowConfidence.setConfidence(.4f);
        lowConfidence.setText("A");

        SearchResult highConfidence = new SearchResult();
        highConfidence.setConfidence(.9f);
        highConfidence.setText("Z");

        SearchModel model = new SearchModel();
        model.getShownResults().addAll(New.list(lowConfidence, highConfidence));

        ResultsSorter sorter = new ResultsSorter(model);

        assertEquals(highConfidence, model.getShownResults().get(0));
        assertEquals(lowConfidence, model.getShownResults().get(1));

        model.getSortType().set(SearchModel.NAME_SORT);

        assertEquals(highConfidence, model.getShownResults().get(1));
        assertEquals(lowConfidence, model.getShownResults().get(0));

        sorter.close();

        model.getSortType().set(SearchModel.RELEVANCE_SORT);

        assertEquals(highConfidence, model.getShownResults().get(1));
        assertEquals(lowConfidence, model.getShownResults().get(0));
    }

    /**
     * Tests sorting default sorts at beginning.
     */
    @Test
    public void testSortDefault()
    {
        SearchResult lowConfidence = new SearchResult();
        lowConfidence.setConfidence(.4f);
        lowConfidence.setText("A");

        SearchResult highConfidence = new SearchResult();
        highConfidence.setConfidence(.9f);
        highConfidence.setText("Z");

        SearchModel model = new SearchModel();
        model.getShownResults().addAll(New.list(lowConfidence, highConfidence));

        ResultsSorter sorter = new ResultsSorter(model);

        assertEquals(highConfidence, model.getShownResults().get(0));
        assertEquals(lowConfidence, model.getShownResults().get(1));

        sorter.close();

        model.getSortType().set(SearchModel.NAME_SORT);

        assertEquals(highConfidence, model.getShownResults().get(0));
        assertEquals(lowConfidence, model.getShownResults().get(1));
    }

    /**
     * Tests sorting when items are added.
     */
    @Test
    public void testSortNewItems()
    {
        SearchResult lowConfidence = new SearchResult();
        lowConfidence.setConfidence(.4f);
        lowConfidence.setText("A");

        SearchResult highConfidence = new SearchResult();
        highConfidence.setConfidence(.9f);
        highConfidence.setText("Z");

        SearchModel model = new SearchModel();

        ResultsSorter sorter = new ResultsSorter(model);

        model.getShownResults().addAll(New.list(lowConfidence, highConfidence));

        assertEquals(highConfidence, model.getShownResults().get(0));
        assertEquals(lowConfidence, model.getShownResults().get(1));

        sorter.close();

        model.getSortType().set(SearchModel.NAME_SORT);

        assertEquals(highConfidence, model.getShownResults().get(0));
        assertEquals(lowConfidence, model.getShownResults().get(1));
    }
}
