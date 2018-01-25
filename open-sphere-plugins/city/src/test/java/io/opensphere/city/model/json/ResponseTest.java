package io.opensphere.city.model.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import io.opensphere.server.util.JsonUtils;

/**
 * Unit test for {@link Response}.
 */
public class ResponseTest
{
    /**
     * Tests parsing the json.
     *
     * @throws JsonParseException bad json.
     * @throws JsonMappingException bad json.
     * @throws IOException Bad IO.
     */
    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException
    {
        String json = "{\"status\":1,\"results\":[{\"id\":4258230,\"building_id\":\"Cambridge_02141_42_1418\","
                + "\"position_x\":329688.63286998,\"position_y\":4692910.08175256,\"position_z\":3.77704478,"
                + "\"position_utm_zone\":19,\"position_utm_hemisphere\":\"N\",\"dae_filename\":\"Cambridge_02141_42_1418.dae\","
                + "\"dae_file_exists\":0,\"latitude\":42.369816,\"longitude\":-71.068441,\"date_added\":\"2015-04-24 14:34:53\","
                + "\"date_updated\":\"2015-04-24 15:21:40\",\"timestamp\":1429888900,\"area\":42.81,\"volume\":131.5,"
                + "\"block_volume\":131.5,\"ground_z\":3.8,\"top_z\":6.8,\"flat_height\":3.1,\"roof_min_decline_angle\":0,"
                + "\"city\":{\"id\":23,\"name\":\"Cambridge\"},\"state\":{\"id\":12,\"name\":\"Massachusetts\"},"
                + "\"country\":{\"id\":3,\"name\":\"USA\"},\"type\":{\"id\":1,\"name\":\"Building\"},\"distance\":0.001,"
                + "\"dae_url\":\"http://app.cc3dnow.com/public/objects/dae/download/4258230?"
                + "key=a5014122-a04a-52ba-be0d-8a06c85b5fab&stamp=f54fa1b572f834c0161db3efb9bf0f0e4c03ed8a&inline=1\","
                + "\"dae_download_url\":\"http://app.cc3dnow.com/public/objects/dae/download/4258230?"
                + "key=a5014122-a04a-52ba-be0d-8a06c85b5fab&stamp=f54fa1b572f834c0161db3efb9bf0f0e4c03ed8a\"},"
                + "{\"id\":4258182,\"building_id\":\"Cambridge_02141_42_1370\",\"position_x\":329677.28225326,"
                + "\"position_y\":4692920.38885151,\"position_z\":3.76199317,\"position_utm_zone\":19,\"position_utm_hemisphere\":\"N\","
                + "\"dae_filename\":\"Cambridge_02141_42_1370.dae\",\"dae_file_exists\":0,\"latitude\":42.369907,\"longitude\":-71.068582,"
                + "\"date_added\":\"2015-04-24 14:34:53\",\"date_updated\":\"2015-04-24 15:21:34\",\"timestamp\":1429888894,\"area\":40.26,"
                + "\"volume\":146.38,\"block_volume\":146.38,\"ground_z\":3.8,\"top_z\":7.4,\"flat_height\":3.6,\"roof_min_decline_angle\":0,"
                + "\"city\":{\"id\":23,\"name\":\"Cambridge\"},\"state\":{\"id\":12,\"name\":\"Massachusetts\"},"
                + "\"country\":{\"id\":3,\"name\":\"USA\"},\"type\":{\"id\":1,\"name\":\"Building\"},\"distance\":0.001,"
                + "\"dae_url\":\"http://app.cc3dnow.com/public/objects/dae/download/4258182?"
                + "key=a5014122-a04a-52ba-be0d-8a06c85b5fab&stamp=f54fa1b572f834c0161db3efb9bf0f0e4c03ed8a&inline=1\","
                + "\"dae_download_url\":\"http://app.cc3dnow.com/public/objects/dae/download/4258182?"
                + "key=a5014122-a04a-52ba-be0d-8a06c85b5fab&stamp=f54fa1b572f834c0161db3efb9bf0f0e4c03ed8a\"}]}";

        ObjectMapper mapper = JsonUtils.createMapper();
        Response response = mapper.readValue(json, Response.class);

        assertEquals(1, response.getStatus());
        assertEquals(2, response.getResults().size());

        assertEquals(
                "http://app.cc3dnow.com/public/objects/dae/download/4258230?"
                        + "key=a5014122-a04a-52ba-be0d-8a06c85b5fab&stamp=f54fa1b572f834c0161db3efb9bf0f0e4c03ed8a",
                response.getResults().get(0).getDaeDownloadUrl());

        assertEquals(
                "http://app.cc3dnow.com/public/objects/dae/download/4258182?"
                        + "key=a5014122-a04a-52ba-be0d-8a06c85b5fab&stamp=f54fa1b572f834c0161db3efb9bf0f0e4c03ed8a",
                response.getResults().get(1).getDaeDownloadUrl());
    }
}
