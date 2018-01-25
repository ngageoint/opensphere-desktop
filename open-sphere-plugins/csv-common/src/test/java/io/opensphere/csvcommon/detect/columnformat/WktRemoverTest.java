package io.opensphere.csvcommon.detect.columnformat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.csvcommon.detect.columnformat.WktRemover;

/**
 * Tests the WktRemover class.
 *
 */
public class WktRemoverTest
{
    /**
     * Tests removing wkt data.
     */
    @Test
    public void testRemoveWktData()
    {
        String line = "column1, column2, POINT (( 1 )), column4";
        String expected = "column1, column2, POINT (( 1 )), column4";

        WktRemover remover = new WktRemover();
        String actual = remover.removeWktData(line);

        assertEquals(expected, actual);

        line = "column1,column2,Polygon(( 1, 3, 5, 6, )),column4";
        expected = "COLUMN1,COLUMN2,POLYGON,COLUMN4";

        actual = remover.removeWktData(line);

        assertEquals(expected, actual);

        line = "column1,column2,\"LINESTRING( 1, 3, 5, 6, )\",column4";
        expected = "COLUMN1,COLUMN2,\"LINESTRING\",COLUMN4";

        actual = remover.removeWktData(line);

        assertEquals(expected, actual);

        line = "column1,column2,LINESTRING,column4";
        expected = "COLUMN1,COLUMN2,LINESTRING,COLUMN4";

        actual = remover.removeWktData(line);

        assertEquals(expected, actual);

        line = "column1,column2,column3,column4";
        expected = "column1,column2,column3,column4";

        actual = remover.removeWktData(line);

        assertEquals(expected, actual);

        line = "column1, column2, \"MULTILINESTRING ((22 26, 25 27, 24 28),(12 10, 13 14, 15 16),(50 51, 52 53, 54 55))\", column4";
        expected = "COLUMN1, COLUMN2, \"MULTILINESTRING\", COLUMN4";

        actual = remover.removeWktData(line);

        assertEquals(expected, actual);
    }
}
