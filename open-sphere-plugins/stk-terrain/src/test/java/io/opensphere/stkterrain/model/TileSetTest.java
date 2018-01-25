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
 * Unit test for the {@link TileSet} class.
 */
public class TileSetTest
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
        String json = "{\"name\":\"FODAR\",\"description\":\"a description\",\"dataSources\":"
                + "[{\"name\":\"Whitemt\",\"description\":\"\",\"attribution\":\"\",\"_rev\":4}"
                + ",{\"name\":\"Wales\",\"description\":\"\",\"attribution\":\"\",\"_rev\":4}]}";

        ObjectMapper mapper = JsonUtils.createMapper();

        TileSet tileSet = mapper.readValue(json, TileSet.class);

        assertEquals("FODAR", tileSet.getName());
        assertEquals("a description", tileSet.getDescription());

        assertEquals(2, tileSet.getDataSources().size());

        assertEquals("Whitemt", tileSet.getDataSources().get(0).getName());
        assertEquals("Wales", tileSet.getDataSources().get(1).getName());
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
        TileSet tileSet = new TileSet();
        tileSet.setName("name1");
        tileSet.setDescription("descript");

        TileSetDataSource dataSource1 = new TileSetDataSource();
        dataSource1.setName("source1");

        TileSetDataSource dataSource2 = new TileSetDataSource();
        dataSource2.setName("source2");

        tileSet.getDataSources().add(dataSource1);
        tileSet.getDataSources().add(dataSource2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(tileSet);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        TileSet actual = (TileSet)objectIn.readObject();

        assertEquals("name1", actual.getName());
        assertEquals("descript", actual.getDescription());

        assertEquals(2, actual.getDataSources().size());

        assertEquals("source1", actual.getDataSources().get(0).getName());
        assertEquals("source2", actual.getDataSources().get(1).getName());
    }
}
