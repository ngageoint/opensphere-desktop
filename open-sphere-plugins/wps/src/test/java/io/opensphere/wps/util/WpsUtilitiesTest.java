package io.opensphere.wps.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;

/** Tests for {@link WpsUtilities}. */
public class WpsUtilitiesTest
{
    /** Test for {@link WpsUtilities} bbox utilities. */
    @Test
    public void testBbox()
    {
        GeographicBoundingBox bbox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 1),
                LatLonAlt.createFromDegrees(2, 3));
        String bboxString = WpsUtilities.boundingBoxToString(bbox);
        assertEquals("1.0,0.0,3.0,2.0", bboxString);
        GeographicBoundingBox bbox2 = WpsUtilities.parseBoundingBox(bboxString);
        assertEquals(bbox, bbox2);
    }
}
