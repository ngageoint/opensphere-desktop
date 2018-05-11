package io.opensphere.wms.state.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Tests the WMSLayerState class verifies the getters and setters. Verifies that
 * it can serialize/deserialize and verifies that the xml format is as expected.
 *
 */
@SuppressWarnings("boxing")
public class WMSLayerStateTest
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
        WMSLayerState expected = new WMSLayerState();
        expected.setColorizeStyle("colorizeStyle");
        expected.setFixedHeight(true);
        expected.setFixedWidth(true);
        expected.setGetMapUrl("getMapUrl");
        expected.setHoldLevel(1);
        expected.setId("id");
        expected.setMaxDisplaySize(2);
        expected.setMinDisplaySize(3);
        expected.setSplitLevels(4);
        expected.setType("type");
        expected.setUrl("url");
        expected.setVisible(true);
        expected.getTags().add("tag1");
        expected.getTags().add("tag2");
        expected.getParameters().setBgColor("bgColor");
        expected.setTitle("title");
        expected.setIsAnimate(true);

        InputStream stream = XMLUtilities.writeXMLObjectToInputStreamSync(expected);

        StringBuilder builder = new StringBuilder();
        int character = stream.read();
        while (character > 0)
        {
            builder.append((char)character);
            character = stream.read();
        }

        String xmlString = builder.toString();

        assertTrue(xmlString.contains("<title>title</title>"));
        assertTrue(xmlString.contains("<animate>true</animate>"));
        assertTrue(xmlString.contains("<colorizeStyle>colorizeStyle</colorizeStyle>"));
        assertTrue(xmlString.contains("<fixedHeight>true</fixedHeight>"));
        assertTrue(xmlString.contains("<fixedWidth>true</fixedWidth>"));
        assertTrue(xmlString.contains("<getMapUrl>getMapUrl</getMapUrl>"));
        assertTrue(xmlString.contains("<holdLevel>1</holdLevel>"));
        assertTrue(xmlString.contains("<id>id</id>"));
        assertTrue(xmlString.contains("<maxDisplaySize>2</maxDisplaySize>"));
        assertTrue(xmlString.contains("<minDisplaySize>3</minDisplaySize>"));
        assertTrue(xmlString.contains("<splitLevels>4</splitLevels>"));
        assertTrue(xmlString.contains("<type>type</type>"));
        assertTrue(xmlString.contains("<url>url</url>"));
        assertTrue(xmlString.contains("<visible>true</visible>"));
        assertTrue(xmlString.matches("(?s).*<tags>.*<tag>tag1</tag>.*<tag>tag2</tag>.*</tags>.*"));
        assertTrue(xmlString.contains("<params>"));
        assertTrue(xmlString.contains("<layer xmlns=\"http://www.bit-sys.com/state/v2\" type=\"wms\">"));

        stream.reset();

        WMSLayerState actual = XMLUtilities.readXMLObject(stream, WMSLayerState.class);

        assertEquals(expected.getColorizeStyle(), actual.getColorizeStyle());
        assertEquals(expected.getGetMapUrl(), actual.getGetMapUrl());
        assertEquals(expected.getHoldLevel(), actual.getHoldLevel());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMaxDisplaySize(), actual.getMaxDisplaySize());
        assertEquals(expected.getMinDisplaySize(), actual.getMinDisplaySize());
        assertEquals(expected.getSplitLevels(), actual.getSplitLevels());
        assertEquals(expected.getTags().get(0), actual.getTags().get(0));
        assertEquals(expected.getTags().get(1), actual.getTags().get(1));
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getUrl(), actual.getUrl());
        assertEquals(expected.isFixedHeight(), actual.isFixedHeight());
        assertEquals(expected.isFixedWidth(), actual.isFixedWidth());
        assertEquals(expected.isVisible(), actual.isVisible());
        assertEquals(expected.getParameters().getBgColor(), actual.getParameters().getBgColor());
        assertEquals(expected.getLayerType(), actual.getLayerType());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.isAnimate(), actual.isAnimate());
    }
}
