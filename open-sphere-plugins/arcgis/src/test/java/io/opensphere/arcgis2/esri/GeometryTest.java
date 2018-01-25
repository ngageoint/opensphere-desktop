package io.opensphere.arcgis2.esri;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import io.opensphere.server.util.JsonUtils;

/**
 * Unit test for the {@link Geometry} class.
 */
public class GeometryTest
{
    /**
     * Tests reading paths from a json string.
     *
     * @throws JsonParseException Bad parse.
     * @throws JsonMappingException Bad mapping.
     * @throws IOException Bad IO.
     */
    @Test
    public void testGetPaths() throws JsonParseException, JsonMappingException, IOException
    {
        String json = "{\"paths\" : [[[-105.510322, 39.962340999999981], [-105.510352, 39.959592999999984]],[[1,2],[3, 4]]]}";
        ObjectMapper mapper = JsonUtils.createMapper();

        Geometry geometry = mapper.readValue(json, Geometry.class);

        assertEquals(2, geometry.getPaths().length);

        double[][] firstPath = geometry.getPaths()[0];
        assertEquals(2, firstPath.length);

        assertEquals(-105.510322, firstPath[0][0], 0d);
        assertEquals(39.962340999999981, firstPath[0][1], 0d);
        assertEquals(-105.510352, firstPath[1][0], 0d);
        assertEquals(39.959592999999984, firstPath[1][1], 0d);

        double[][] secondPath = geometry.getPaths()[1];
        assertEquals(2, secondPath.length);

        assertEquals(1, secondPath[0][0], 0d);
        assertEquals(2, secondPath[0][1], 0d);
        assertEquals(3, secondPath[1][0], 0d);
        assertEquals(4, secondPath[1][1], 0d);
    }
}
