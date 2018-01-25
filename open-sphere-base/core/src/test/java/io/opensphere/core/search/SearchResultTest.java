package io.opensphere.core.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.core.model.LatLonAlt;

/**
 * Unit test for {@link SearchResult}.
 */
public class SearchResultTest
{
    /**
     * Tests the setters and getters.
     */
    @Test
    public void test()
    {
        SearchResult result = new SearchResult();
        result.setConfidence(.8f);
        result.setDescription("Over the rainbow");
        result.setSearchType("Place names");
        result.setText("Somewhere");
        result.getLocations().add(LatLonAlt.createFromDegrees(10, 11));

        assertEquals(.8f, result.getConfidence(), 0f);
        assertEquals("Over the rainbow", result.getDescription());
        assertEquals("Place names", result.getSearchType());
        assertEquals("Somewhere", result.getText());
        assertEquals(LatLonAlt.createFromDegrees(10, 11), result.getLocations().get(0));
    }
}
