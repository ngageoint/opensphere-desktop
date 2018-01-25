package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * Unit test for the {@link TileMatrix} class.
 */
public class TileMatrixTest
{
    /**
     * Tests serializing and deserializing the {@link TileMatrix} class.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void test() throws IOException, ClassNotFoundException
    {
        TileMatrix matrix = new TileMatrix(10, 11);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(matrix);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        TileMatrix actual = (TileMatrix)objectIn.readObject();

        assertEquals(10, actual.getMatrixHeight());
        assertEquals(11, actual.getMatrixWidth());
    }
}
