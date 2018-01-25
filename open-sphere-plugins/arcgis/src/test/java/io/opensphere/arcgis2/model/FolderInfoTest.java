package io.opensphere.arcgis2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import io.opensphere.server.util.JsonUtils;

/**
 * Unit test for {@link FolderInfo}.
 */
public class FolderInfoTest
{
    /**
     * Tests deserializing a folder info with a tile cache.
     *
     * @throws IOException Bad IO.
     * @throws JsonMappingException Bad json.
     * @throws JsonParseException Bad json.
     */
    @Test
    public void testDeserializeCached() throws JsonParseException, JsonMappingException, IOException
    {
        String json = "{\"currentVersion\":10.05,\"serviceDescription\": \"service description\","
                + "\"mapName\":\"Layers\",\"description\":\"\",\"copyrightText\":\"\","
                + "\"layers\":[{\"id\":0,\"name\":\"Incidents\",\"parentLayerId\":-1,\"defaultVisibility\":true,"
                + "\"subLayerIds\":null,\"minScale\":0,\"maxScale\":0}],"
                + "\"tables\":[{\"id\":1,\"name\":\"Incident Priority\"}],"
                + "\"spatialReference\":{\"wkid\":4326},\"singleFusedMapCache\":true,"
                + "\"initialExtent\":{\"xmin\":-126.060026464775,\"ymin\":27.285241223675,"
                + "\"xmax\":-108.305411069225,\"ymax\":44.2934163323251,\"spatialReference\":{\"wkid\":4326}},"
                + "\"fullExtent\":{\"xmin\":-340.15,\"ymin\":-111.787173011,\"xmax\":7236019.39469463,"
                + "\"ymax\":8530301.53811686,\"spatialReference\":{\"wkid\":4326}},\"units\":\"esriDecimalDegrees\","
                + "\"supportedImageFormatTypes\":\"PNG32,PNG24,PNG,JPG,DIB,TIFF,EMF,PS,PDF,GIF,SVG,SVGZ,BMP\","
                + "\"documentInfo\":{\"Title\":\"Base map\",\"Author\":\"\",\"Comments\":\"\",\"Subject\":\"\","
                + "\"Category\":\"\",\"Keywords\":\"\",\"AntialiasingMode\":\"None\",\"TextAntialiasingMode\":\"Force\"},"
                + "\"capabilities\":\"Map,Data,Query\"}";
        ObjectMapper mapper = JsonUtils.createMapper();
        FolderInfo layer = mapper.readValue(json, FolderInfo.class);

        assertTrue(layer.isSingleFusedMapCache());
        assertEquals("Base map", layer.getDocumentInfo().getTitle());
    }

    /**
     * Tests deserializing a folder info with a tile cache.
     *
     * @throws IOException Bad IO.
     * @throws JsonMappingException Bad json.
     * @throws JsonParseException Bad json.
     */
    @Test
    public void testDeserializeNotCached() throws JsonParseException, JsonMappingException, IOException
    {
        String json = "{\"currentVersion\":10.05,\"serviceDescription\": \"service description\","
                + "\"mapName\":\"Layers\",\"description\":\"\",\"copyrightText\":\"\","
                + "\"layers\":[{\"id\":0,\"name\":\"Incidents\",\"parentLayerId\":-1,\"defaultVisibility\":true,"
                + "\"subLayerIds\":null,\"minScale\":0,\"maxScale\":0}],"
                + "\"tables\":[{\"id\":1,\"name\":\"Incident Priority\"}],"
                + "\"spatialReference\":{\"wkid\":4326},\"singleFusedMapCache\":false,"
                + "\"initialExtent\":{\"xmin\":-126.060026464775,\"ymin\":27.285241223675,"
                + "\"xmax\":-108.305411069225,\"ymax\":44.2934163323251,\"spatialReference\":{\"wkid\":4326}},"
                + "\"fullExtent\":{\"xmin\":-340.15,\"ymin\":-111.787173011,\"xmax\":7236019.39469463,"
                + "\"ymax\":8530301.53811686,\"spatialReference\":{\"wkid\":4326}},\"units\":\"esriDecimalDegrees\","
                + "\"supportedImageFormatTypes\":\"PNG32,PNG24,PNG,JPG,DIB,TIFF,EMF,PS,PDF,GIF,SVG,SVGZ,BMP\","
                + "\"documentInfo\":{\"Title\":\"\",\"Author\":\"\",\"Comments\":\"\",\"Subject\":\"\","
                + "\"Category\":\"\",\"Keywords\":\"\",\"AntialiasingMode\":\"None\",\"TextAntialiasingMode\":\"Force\"},"
                + "\"capabilities\":\"Map,Data,Query\"}";
        ObjectMapper mapper = JsonUtils.createMapper();
        FolderInfo layer = mapper.readValue(json, FolderInfo.class);

        assertFalse(layer.isSingleFusedMapCache());
    }

    /**
     * Tests deserializing a folder info with a tile cache.
     *
     * @throws IOException Bad IO.
     * @throws JsonMappingException Bad json.
     * @throws JsonParseException Bad json.
     */
    @Test
    public void testDeserializeNullCached() throws JsonParseException, JsonMappingException, IOException
    {
        String json = "{\"currentVersion\":10.05,\"serviceDescription\": \"service description\","
                + "\"mapName\":\"Layers\",\"description\":\"\",\"copyrightText\":\"\","
                + "\"layers\":[{\"id\":0,\"name\":\"Incidents\",\"parentLayerId\":-1,\"defaultVisibility\":true,"
                + "\"subLayerIds\":null,\"minScale\":0,\"maxScale\":0}],"
                + "\"tables\":[{\"id\":1,\"name\":\"Incident Priority\"}]," + "\"spatialReference\":{\"wkid\":4326},"
                + "\"initialExtent\":{\"xmin\":-126.060026464775,\"ymin\":27.285241223675,"
                + "\"xmax\":-108.305411069225,\"ymax\":44.2934163323251,\"spatialReference\":{\"wkid\":4326}},"
                + "\"fullExtent\":{\"xmin\":-340.15,\"ymin\":-111.787173011,\"xmax\":7236019.39469463,"
                + "\"ymax\":8530301.53811686,\"spatialReference\":{\"wkid\":4326}},\"units\":\"esriDecimalDegrees\","
                + "\"supportedImageFormatTypes\":\"PNG32,PNG24,PNG,JPG,DIB,TIFF,EMF,PS,PDF,GIF,SVG,SVGZ,BMP\","
                + "\"documentInfo\":{\"Title\":\"\",\"Author\":\"\",\"Comments\":\"\",\"Subject\":\"\","
                + "\"Category\":\"\",\"Keywords\":\"\",\"AntialiasingMode\":\"None\",\"TextAntialiasingMode\":\"Force\"},"
                + "\"capabilities\":\"Map,Data,Query\"}";
        ObjectMapper mapper = JsonUtils.createMapper();
        FolderInfo layer = mapper.readValue(json, FolderInfo.class);

        assertFalse(layer.isSingleFusedMapCache());
    }
}
