package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/**
 * Unit test for the {@link GeoPackageTile} class.
 */
public class GeoPackageTileTest
{
    /**
     * Tests serializing and deserializing the class.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void test() throws IOException, ClassNotFoundException
    {
        GeographicPosition lowerLeft = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicPosition upperRight = new GeographicPosition(LatLonAlt.createFromDegrees(11, 11));
        GeographicBoundingBox boundingBox = new GeographicBoundingBox(lowerLeft, upperRight);

        GeoPackageTile expected = new GeoPackageTile("theLayer", 8, boundingBox, 2, 1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(expected);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        GeoPackageTile actual = (GeoPackageTile)objectIn.readObject();

        assertEquals("theLayer", actual.getLayerId());
        assertEquals(8, actual.getZoomLevel());
        assertEquals(lowerLeft, actual.getBoundingBox().getLowerLeft());
        assertEquals(upperRight, actual.getBoundingBox().getUpperRight());
        assertEquals(2, actual.getX());
        assertEquals(1, actual.getY());
    }
}
