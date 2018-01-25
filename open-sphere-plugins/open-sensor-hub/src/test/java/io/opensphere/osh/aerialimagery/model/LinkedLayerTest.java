package io.opensphere.osh.aerialimagery.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Unit test for {@link LinkedLayer} class.
 */
public class LinkedLayerTest
{
    /**
     * Tests deserializing the xml of the linked layers.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testDeserialize() throws JAXBException
    {
        String xml = "<LinkedLayer><linkedLayersTypeKey>linkedTypeKey</linkedLayersTypeKey>"
                + "<otherLinkedLayersTypeKey>otherLinkedTypeKey</otherLinkedLayersTypeKey></LinkedLayer>";

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StringUtilities.DEFAULT_CHARSET));
        LinkedLayer linkedLayer = XMLUtilities.readXMLObject(stream, LinkedLayer.class);

        assertEquals("linkedTypeKey", linkedLayer.getLinkedLayersTypeKey());
        assertEquals("otherLinkedTypeKey", linkedLayer.getOtherLinkedLayersTypeKey());
    }

    /**
     * Tests deserializing the xml of the linked layers.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testSerialize() throws JAXBException
    {
        String xml = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<LinkedLayer>%n   <linkedLayersTypeKey>linkedTypeKey</linkedLayersTypeKey>%n"
                + "   <otherLinkedLayersTypeKey>otherLinkedTypeKey</otherLinkedLayersTypeKey>%n</LinkedLayer>%n");

        LinkedLayer linkedLayer = new LinkedLayer();
        linkedLayer.setLinkedLayersTypeKey("linkedTypeKey");
        linkedLayer.setOtherLinkedLayersTypeKey("otherLinkedTypeKey");

        String actualXml = XMLUtilities.writeXMLObjectToString(linkedLayer);

        assertEquals(xml, actualXml);
    }
}
