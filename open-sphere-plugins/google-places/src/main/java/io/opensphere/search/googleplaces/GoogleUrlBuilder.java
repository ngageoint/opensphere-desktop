package io.opensphere.search.googleplaces;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.net.UrlBuilder;

/**
 * Builds the appropriate search url based on the specified parameters.
 */
public class GoogleUrlBuilder
{
    /** URL template for GooglePlaces API. */
    private static final String SEARCH_URL_TEMPLATE =
            "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%1$s&location=%2$s&radius=%3$s&key=%4$s";

    /**
     * Builds the appropriate search url base on the specified parameters.
     *
     * @param keyword the keyword for the search.
     * @param lowerLeft The lower left location of the search.
     * @param upperRight The upper right location of the search.
     * @param apiKey The user's api key.
     * @return The search url.
     * @throws MalformedURLException If a parameter was in some incorrect
     *             format.
     * @throws UnsupportedEncodingException If a parameter was in some incorrect
     *             format.
     */
    public URL buildUrl(String keyword, LatLonAlt lowerLeft, LatLonAlt upperRight, String apiKey)
        throws MalformedURLException, UnsupportedEncodingException
    {
        String encodedSearchString = URLEncoder.encode(keyword, "UTF-8");

        GeographicBoundingBox box = new GeographicBoundingBox(lowerLeft, upperRight);
        LatLonAlt center = box.getCenter().getLatLonAlt();
        double radiusMeters = GeographicBody3D.greatCircleDistanceM(lowerLeft, center, WGS84EarthConstants.RADIUS_EQUATORIAL_M);
        String locationString = center.getLatD() + "," + center.getLonD();

        String searchUrl = String.format(SEARCH_URL_TEMPLATE, encodedSearchString, locationString, Double.valueOf(radiusMeters),
                apiKey);

        URL url = new UrlBuilder(searchUrl).toURL();

        return url;
    }
}
