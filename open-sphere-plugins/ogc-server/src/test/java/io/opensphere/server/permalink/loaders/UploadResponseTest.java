package io.opensphere.server.permalink.loaders;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import io.opensphere.server.util.JsonUtils;

/**
 * Tests the UploadResponse class.
 */
public class UploadResponseTest
{
    /**
     * Tests reading UploadResponse from json and gettings its values.
     *
     * @throws IOException Bad io.
     * @throws JsonMappingException Bad mapping.
     * @throws JsonParseException Bad parse.
     *
     */
    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException
    {
        String jsonString = "{ \"success\": true, \"url\": \"theUrl\" }";

        ObjectMapper mapper = JsonUtils.createMapper();
        UploadResponse response = mapper.readValue(jsonString, UploadResponse.class);

        assertEquals(true, response.isSuccess());
        assertEquals("theUrl", response.getUrl());
    }
}
