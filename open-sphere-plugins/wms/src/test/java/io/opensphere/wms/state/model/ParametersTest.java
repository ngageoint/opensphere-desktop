package io.opensphere.wms.state.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Tests the parameters class verifies the getters and setters. Verifies that it
 * can serialize/deserialize and verifies that the xml format is as expected.
 *
 */
@SuppressWarnings("boxing")
public class ParametersTest
{
    /**
     * Tests the parameters class.
     *
     * @throws JAXBException Bad xml.
     * @throws IOException Bad stream.
     */
    @Test
    public void test() throws JAXBException, IOException
    {
        Parameters expected = new Parameters();
        expected.setBgColor("bgColor");
        expected.setCustom("custom");
        expected.setFormat("format");
        expected.setHeight(10);
        expected.setSrs("srs");
        expected.setStyle("style");
        expected.setTransparent(true);
        expected.setWidth(11);
        expected.setLayerName("layerName");

        InputStream stream = XMLUtilities.writeXMLObjectToInputStreamSync(expected);

        StringBuilder builder = new StringBuilder();
        int character = stream.read();
        while (character > 0)
        {
            builder.append((char)character);
            character = stream.read();
        }

        String xmlString = builder.toString();

        assertTrue(xmlString.contains("<BGCOLOR>bgColor</BGCOLOR>"));
        assertTrue(xmlString.contains("<CUSTOM>custom</CUSTOM>"));
        assertTrue(xmlString.contains("<FORMAT>format</FORMAT>"));
        assertTrue(xmlString.contains("<HEIGHT>10</HEIGHT>"));
        assertTrue(xmlString.contains("<SRS>srs</SRS>"));
        assertTrue(xmlString.contains("<STYLE>style</STYLE>"));
        assertTrue(xmlString.contains("<TRANSPARENT>true</TRANSPARENT>"));
        assertTrue(xmlString.contains("<WIDTH>11</WIDTH>"));
        assertTrue(xmlString.contains("<LAYERS>layerName</LAYERS>"));

        stream.reset();

        Parameters actual = XMLUtilities.readXMLObject(stream, Parameters.class);

        assertEquals(expected.getBgColor(), actual.getBgColor());
        assertEquals(expected.getCustom(), actual.getCustom());
        assertEquals(expected.getFormat(), actual.getFormat());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertEquals(expected.getSrs(), actual.getSrs());
        assertEquals(expected.getStyle(), actual.getStyle());
        assertEquals(expected.isTransparent(), actual.isTransparent());
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getLayerName(), actual.getLayerName());
    }
}
