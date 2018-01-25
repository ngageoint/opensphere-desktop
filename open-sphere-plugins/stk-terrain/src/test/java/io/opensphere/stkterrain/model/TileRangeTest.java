package io.opensphere.stkterrain.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import io.opensphere.server.util.JsonUtils;

/**
 * Unit test for {@link TileRange}.
 */
public class TileRangeTest
{
    /**
     * Tests populating the class from json.
     *
     * @throws IOException Bad IO.
     * @throws JsonMappingException Bad mapping.
     * @throws JsonParseException Bad json.
     */
    @Test
    public void testDeserialize() throws JsonParseException, JsonMappingException, IOException
    {
        String json = "{\"startX\":0,\"startY\":0,\"endX\":31,\"endY\":15}";

        ObjectMapper mapper = JsonUtils.createMapper();

        TileRange range = mapper.readValue(json, TileRange.class);

        assertEquals(0, range.getStartX());
        assertEquals(0, range.getStartY());
        assertEquals(31, range.getEndX());
        assertEquals(15, range.getEndY());
    }

    /**
     * Tests java serializing the class.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerialize() throws IOException, ClassNotFoundException
    {
        TileRange range = new TileRange();
        range.setStartX(1);
        range.setStartY(2);
        range.setEndX(3);
        range.setEndY(4);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(range);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        TileRange actual = (TileRange)objectIn.readObject();

        assertEquals(1, actual.getStartX());
        assertEquals(2, actual.getStartY());
        assertEquals(3, actual.getEndX());
        assertEquals(4, actual.getEndY());
    }
}
