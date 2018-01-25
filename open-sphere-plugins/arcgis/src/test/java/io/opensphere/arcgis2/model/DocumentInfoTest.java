package io.opensphere.arcgis2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import io.opensphere.server.util.JsonUtils;

/**
 * Unit test for {@link DocumentInfo}.
 */
public class DocumentInfoTest
{
    /**
     * Tests deserializing the {@link DocumentInfo}.
     *
     * @throws JsonParseException Bad json.
     * @throws JsonMappingException Bad json.
     * @throws IOException Bad IO.
     */
    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException
    {
        String json = "{\r\n" + "  \"Title\": \"Canvas Base\",\r\n" + "  \"Author\": \"Esri\",\r\n" + "  \"Comments\": \"a comment\",\r\n"
                + "  \"Subject\": \"political boundaries, populated places, water, roads, urban areas, building footprints, parks\",\r\n"
                + "  \"Category\": \"transportation(Transportation Networks) \",\r\n" + "  \"AntialiasingMode\": \"Best\",\r\n"
                + "  \"TextAntialiasingMode\": \"Force\",\r\n"
                + "  \"Keywords\": \"thematic basemap,thematic map,choropleth,dot map,dot density,flow map,heat map,graphs,"
                + "infographic,graduated symbols,proportional symbols,neutral,subdued,canvas,shaded,hot spot,color coded,colorful"
                + ",World,Global,Europe,Andorra,Austria,Belgium,Czech Republic,Denmark,France,Germany,Great Britain,Greece,Hungary,"
                + "Ireland,Italy,Luxembourg,Netherlands,Norway,Poland,Portugal,San Marino,Slovakia,Spain,Sweden,Switzerland,North America,"
                + "United States,Canada,Mexico,Southern Africa,Botswana,Lesotho,Namibia,South Africa and Swaziland,Asia,India,South America,"
                + "Central America,Argentina,Bolivia,Brazil,Chile,Colombia,Peru,Uruguay,Venezuela,Australia,New Zealand\"\r\n"
                + " }";

        DocumentInfo info = JsonUtils.createMapper().readValue(json, DocumentInfo.class);

        assertEquals("Canvas Base", info.getTitle());
        assertEquals("Esri", info.getAuthor());
        assertEquals("a comment", info.getComments());
        assertEquals("political boundaries, populated places, water, roads, urban areas, building footprints, parks", info.getSubject());
        assertEquals("transportation(Transportation Networks) ", info.getCategory());
        assertEquals("Best", info.getAntialiasingMode());
        assertEquals("Force", info.getTextAntialiasingMode());
        assertTrue(info.getKeywords().startsWith("thematic basemap,"));
    }
}
