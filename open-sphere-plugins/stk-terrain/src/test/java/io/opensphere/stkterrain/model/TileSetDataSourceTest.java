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
 * Unit test for {@link TileSetDataSource}.
 */
public class TileSetDataSourceTest
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
        String json = "{\"name\":\"Whitemt\",\"description\":\"a description\",\"attribution\":\"attrib\",\"_rev\":4}";

        ObjectMapper mapper = JsonUtils.createMapper();

        TileSetDataSource dataSource = mapper.readValue(json, TileSetDataSource.class);

        assertEquals("Whitemt", dataSource.getName());
        assertEquals("a description", dataSource.getDescription());
        assertEquals("attrib", dataSource.getAttribution());
        assertEquals("4", dataSource.getRevisionNumber());
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
        TileSetDataSource dataSource = new TileSetDataSource();
        dataSource.setName("name1");
        dataSource.setDescription("descript");
        dataSource.setAttribution("from me");
        dataSource.setRevisionNumber("6");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(dataSource);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        TileSetDataSource actual = (TileSetDataSource)objectIn.readObject();

        assertEquals("name1", actual.getName());
        assertEquals("descript", actual.getDescription());
        assertEquals("from me", actual.getAttribution());
        assertEquals("6", actual.getRevisionNumber());
    }
}
