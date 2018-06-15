package io.opensphere.infinity.json;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.util.jts.JTSUtilities;

/** Tests for {@link BoundingBox}. */
public class BoundingBoxTest
{
    /** Test for {@link BoundingBox#BoundingBox(Geometry)}. */
    @Test
    public void testConstructor()
    {
        Geometry geometry = JTSUtilities.createPolygon(-100, 100, -45, 45, new GeometryFactory());
        BoundingBox bbox = new BoundingBox(geometry);
        Assert.assertEquals(-45, bbox.getBottom_right().getLat(), 0.0001);
        Assert.assertEquals(100, bbox.getBottom_right().getLon(), 0.0001);
        Assert.assertEquals(45, bbox.getTop_left().getLat(), 0.0001);
        Assert.assertEquals(-100, bbox.getTop_left().getLon(), 0.0001);
    }
}
