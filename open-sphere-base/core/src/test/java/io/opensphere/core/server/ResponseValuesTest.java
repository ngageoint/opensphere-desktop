package io.opensphere.core.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import io.opensphere.core.util.collections.New;

/**
 * Tests the ResponseValues class.
 */
public class ResponseValuesTest
{
    /**
     * Tests the get header value method.
     */
    @Test
    public void testGetHeaderValue()
    {
        Map<String, Collection<String>> headerValues = New.map();
        headerValues.put("headeR", New.list("headerValue"));
        headerValues.put("header2", New.list("header2Value1", "header2Value2"));
        headerValues.put("Content-Type", New.list("ct"));

        ResponseValues response = new ResponseValues();
        response.setHeader(headerValues);

        assertEquals("headerValue", response.getHeaderValue("Header"));
        assertEquals("header2Value1,header2Value2", response.getHeaderValue("header2"));
        assertEquals("ct", response.getContentType());

        assertNull(response.getHeaderValue("header3"));
    }
}
