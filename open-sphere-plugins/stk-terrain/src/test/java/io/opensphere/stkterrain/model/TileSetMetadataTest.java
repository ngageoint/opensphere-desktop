package io.opensphere.stkterrain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import io.opensphere.server.util.JsonUtils;

/**
 * Unit test for {@link TileSetMetadata}.
 */
public class TileSetMetadataTest
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
        String json = "{\"tilejson\":\"2.1.0\",\"name\":\"world\",\"description\":null,\"version\":\"1.16389.0\",\"format\":\"quantized-mesh-1.0\","
                + "\"attribution\":\"© Analytical Graphics Inc., © CGIAR-CSI, Produced using Copernicus"
                + " data and information funded by the European Union - EU-DEM layers\","
                + "\"scheme\":\"tms\",\"extensions\":[\"watermask\",\"vertexnormals\",\"octvertexnormals\"],\"tiles\":[\"{z}/{x}/{y}.terrain?v={version}\"],"
                + "\"minzoom\":0,\"maxzoom\":16,\"bounds\":[-179.9,-89.9,179.9,89.9],\"projection\":\"EPSG:4326\","
                + "\"available\":[[{\"startX\":0,\"startY\":0,\"endX\":1,\"endY\":0}],"
                + "[{\"startX\":0,\"startY\":0,\"endX\":2,\"endY\":0}, {\"startX\":2,\"startY\":1,\"endX\":3,\"endY\":1}]]}";

        ObjectMapper mapper = JsonUtils.createMapper();

        TileSetMetadata tileSet = mapper.readValue(json, TileSetMetadata.class);

        assertEquals("2.1.0", tileSet.getTilejson());
        assertEquals("world", tileSet.getName());
        assertNull(tileSet.getDescription());
        assertEquals("1.16389.0", tileSet.getVersion());
        assertEquals("quantized-mesh-1.0", tileSet.getFormat());
        assertEquals("© Analytical Graphics Inc., © CGIAR-CSI, Produced using Copernicus"
                + " data and information funded by the European Union - EU-DEM layers", tileSet.getAttribution());
        assertEquals("tms", tileSet.getScheme());
        assertEquals(3, tileSet.getExtensions().size());
        assertEquals("watermask", tileSet.getExtensions().get(0));
        assertEquals("vertexnormals", tileSet.getExtensions().get(1));
        assertEquals("octvertexnormals", tileSet.getExtensions().get(2));
        assertEquals(1, tileSet.getTiles().size());
        assertEquals("{z}/{x}/{y}.terrain?v={version}", tileSet.getTiles().get(0));
        assertEquals(0, tileSet.getMinzoom());
        assertEquals(16, tileSet.getMaxzoom());
        assertEquals(4, tileSet.getBounds().length);
        assertEquals(-179.9f, tileSet.getBounds()[0], 0f);
        assertEquals(-89.9f, tileSet.getBounds()[1], 0f);
        assertEquals(179.9f, tileSet.getBounds()[2], 0f);
        assertEquals(89.9f, tileSet.getBounds()[3], 0f);
        assertEquals("EPSG:4326", tileSet.getProjection());
        assertEquals(2, tileSet.getAvailable().size());

        List<TileRange> ranges = tileSet.getAvailable().get(0);
        assertEquals(1, ranges.size());

        TileRange range = ranges.get(0);

        assertEquals(0, range.getStartX());
        assertEquals(0, range.getStartY());
        assertEquals(1, range.getEndX());
        assertEquals(0, range.getEndY());

        ranges = tileSet.getAvailable().get(1);
        assertEquals(2, ranges.size());

        range = ranges.get(0);

        assertEquals(0, range.getStartX());
        assertEquals(0, range.getStartY());
        assertEquals(2, range.getEndX());
        assertEquals(0, range.getEndY());

        range = ranges.get(1);

        assertEquals(2, range.getStartX());
        assertEquals(1, range.getStartY());
        assertEquals(3, range.getEndX());
        assertEquals(1, range.getEndY());
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
        String json = "{\"tilejson\":\"2.1.0\",\"name\":\"world\",\"description\":null,\"version\":\"1.16389.0\",\"format\":\"quantized-mesh-1.0\","
                + "\"attribution\":\"© Analytical Graphics Inc., © CGIAR-CSI, Produced using Copernicus"
                + " data and information funded by the European Union - EU-DEM layers\","
                + "\"scheme\":\"tms\",\"extensions\":[\"watermask\",\"vertexnormals\",\"octvertexnormals\"],\"tiles\":[\"{z}/{x}/{y}.terrain?v={version}\"],"
                + "\"minzoom\":0,\"maxzoom\":16,\"bounds\":[-180,-90,180,90],\"projection\":\"EPSG:4326\","
                + "\"available\":[[{\"startX\":0,\"startY\":0,\"endX\":1,\"endY\":0}],"
                + "[{\"startX\":0,\"startY\":0,\"endX\":2,\"endY\":0}, {\"startX\":2,\"startY\":1,\"endX\":3,\"endY\":1}]]}";

        ObjectMapper mapper = JsonUtils.createMapper();

        TileSetMetadata tileSet = mapper.readValue(json, TileSetMetadata.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(tileSet);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        tileSet = (TileSetMetadata)objectIn.readObject();

        assertEquals("2.1.0", tileSet.getTilejson());
        assertEquals("world", tileSet.getName());
        assertNull(tileSet.getDescription());
        assertEquals("1.16389.0", tileSet.getVersion());
        assertEquals("quantized-mesh-1.0", tileSet.getFormat());
        assertEquals("© Analytical Graphics Inc., © CGIAR-CSI, Produced using Copernicus"
                + " data and information funded by the European Union - EU-DEM layers", tileSet.getAttribution());
        assertEquals("tms", tileSet.getScheme());
        assertEquals(3, tileSet.getExtensions().size());
        assertEquals("watermask", tileSet.getExtensions().get(0));
        assertEquals("vertexnormals", tileSet.getExtensions().get(1));
        assertEquals("octvertexnormals", tileSet.getExtensions().get(2));
        assertEquals(1, tileSet.getTiles().size());
        assertEquals("{z}/{x}/{y}.terrain?v={version}", tileSet.getTiles().get(0));
        assertEquals(0, tileSet.getMinzoom());
        assertEquals(16, tileSet.getMaxzoom());
        assertEquals(4, tileSet.getBounds().length);
        assertEquals(-180f, tileSet.getBounds()[0], 0f);
        assertEquals(-90f, tileSet.getBounds()[1], 0f);
        assertEquals(180f, tileSet.getBounds()[2], 0f);
        assertEquals(90f, tileSet.getBounds()[3], 0f);
        assertEquals("EPSG:4326", tileSet.getProjection());
        assertEquals(2, tileSet.getAvailable().size());

        List<TileRange> ranges = tileSet.getAvailable().get(0);
        assertEquals(1, ranges.size());

        TileRange range = ranges.get(0);

        assertEquals(0, range.getStartX());
        assertEquals(0, range.getStartY());
        assertEquals(1, range.getEndX());
        assertEquals(0, range.getEndY());

        ranges = tileSet.getAvailable().get(1);
        assertEquals(2, ranges.size());

        range = ranges.get(0);

        assertEquals(0, range.getStartX());
        assertEquals(0, range.getStartY());
        assertEquals(2, range.getEndX());
        assertEquals(0, range.getEndY());

        range = ranges.get(1);

        assertEquals(2, range.getStartX());
        assertEquals(1, range.getStartY());
        assertEquals(3, range.getEndX());
        assertEquals(1, range.getEndY());
    }
}
