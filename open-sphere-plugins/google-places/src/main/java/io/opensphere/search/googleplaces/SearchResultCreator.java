package io.opensphere.search.googleplaces;

import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.collections.New;
import io.opensphere.search.googleplaces.json.GeoJsonObject;
import io.opensphere.search.googleplaces.json.Result;

/**
 * Creates a search result from the {@link GeoJsonObject}.
 */
public class SearchResultCreator
{
    /**
     * Creates the {@link SearchResult} that represent the results in the
     * specified {@link GeoJsonObject}.
     *
     * @param geoJson The google search results.
     * @return The search results in the google search results.
     */
    public List<SearchResult> createResult(GeoJsonObject geoJson)
    {
        List<SearchResult> results = New.list();

        for (Result geoJsonResult : geoJson.getResults())
        {
            SearchResult result = new SearchResult();

            result.setConfidence(geoJsonResult.getRating() / 5);
            result.setText(geoJsonResult.getName());
            result.setDescription(buildDescription(geoJsonResult));
            result.getLocations().add(LatLonAlt.createFromDegrees(geoJsonResult.getGeometry().getLocation().getLat(),
                    geoJsonResult.getGeometry().getLocation().getLng()));

            results.add(result);
        }

        return results;
    }

    /**
     * Builds the description string of the search results.
     *
     * @param result The result to build a description for.
     * @return The description.
     */
    private String buildDescription(Result result)
    {
        StringBuilder description = new StringBuilder();

        description.append(result.getFormattedAddress());
        description.append("\nRating : ");
        description.append(result.getRating());

        return description.toString();
    }
}
