package io.opensphere.wfs.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.wfs.filter.FilterReader;

/**
 * Unit test for {@link FilterReader}.
 */
public class FilterReaderTest
{
    /**
     * Tests reading a bounding box filter returned from the server.
     */
    @Test
    public void testParse()
    {
        String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" maxFeatures=\"10000\" outputFormat=\"text/csv\" "
                + "resultType=\"results\" service=\"WFS\" version=\"1.1.0\"><wfs:Query srsName=\"EPSG:4326\" "
                + "typeName=\"twitter_all\"><Filter xmlns=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\""
                + " combinator=\"true\"><And><And><PropertyIsGreaterThanOrEqualTo><PropertyName>validTime</PropertyName>"
                + "<Sub><Function name=\"GET_CURRENT_DATE_TIME\"/><Literal>PT10M</Literal></Sub></PropertyIsGreaterThanOrEqualTo>"
                + "</And><Or><And><BBOX areanamehint=\"temp area 1\"><PropertyName>GEOM</PropertyName><gml:Envelope srsName=\"CRS:84\">"
                + "<gml:lowerCorner>-121.82080507278442 30.111331343650818</gml:lowerCorner><gml:upperCorner>-73.30518007278442 51.20508134365082</gml:upperCorner>"
                + "</gml:Envelope></BBOX></And></Or></And></Filter></wfs:Query></wfs:GetFeature>";

        List<AbstractMapGeometrySupport> geoms = FilterReader.parse(xmlText);
        assertEquals(1, geoms.size());

        DefaultMapPolygonGeometrySupport bbox = (DefaultMapPolygonGeometrySupport)geoms.get(0);
        assertEquals(4, bbox.getLocations().size());

        assertEquals(LatLonAlt.createFromDegrees(30.111331343650818, -121.82080507278442), bbox.getLocations().get(0));
        assertEquals(LatLonAlt.createFromDegrees(30.111331343650818, -73.30518007278442), bbox.getLocations().get(1));
        assertEquals(LatLonAlt.createFromDegrees(51.20508134365082, -73.30518007278442), bbox.getLocations().get(2));
        assertEquals(LatLonAlt.createFromDegrees(51.20508134365082, -121.82080507278442), bbox.getLocations().get(3));
    }
}
