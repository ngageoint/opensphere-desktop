package io.opensphere.osh.aerialimagery.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Unit test for {@link LinkedLayers} class.
 */
public class LinkedLayersTest
{
    /**
     * Tests deserializing the xml of the linked layers.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testDeserialize() throws JAXBException
    {
        String xml = "<LinkedLayers><LinkedLayer><linkedLayersTypeKey>linkedTypeKey1</linkedLayersTypeKey>"
                + "<otherLinkedLayersTypeKey>otherLinkedTypeKey1</otherLinkedLayersTypeKey></LinkedLayer>"
                + "<LinkedLayer><linkedLayersTypeKey>linkedTypeKey2</linkedLayersTypeKey>"
                + "<otherLinkedLayersTypeKey>otherLinkedTypeKey2</otherLinkedLayersTypeKey></LinkedLayer></LinkedLayers>";

        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StringUtilities.DEFAULT_CHARSET));
        LinkedLayers linkedLayers = XMLUtilities.readXMLObject(stream, LinkedLayers.class);

        assertEquals(2, linkedLayers.getLinkedLayers().size());

        int index = 1;
        for (LinkedLayer linkedLayer : linkedLayers.getLinkedLayers())
        {
            assertEquals("linkedTypeKey" + index, linkedLayer.getLinkedLayersTypeKey());
            assertEquals("otherLinkedTypeKey" + index, linkedLayer.getOtherLinkedLayersTypeKey());
            index++;
        }
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
                + "<LinkedLayers>%n"
                + "   <LinkedLayer>%n      <linkedLayersTypeKey>linkedTypeKey1</linkedLayersTypeKey>%n"
                + "      <otherLinkedLayersTypeKey>otherLinkedTypeKey1</otherLinkedLayersTypeKey>%n   </LinkedLayer>%n"
                + "   <LinkedLayer>%n      <linkedLayersTypeKey>linkedTypeKey2</linkedLayersTypeKey>%n"
                + "      <otherLinkedLayersTypeKey>otherLinkedTypeKey2</otherLinkedLayersTypeKey>%n   </LinkedLayer>%n"
                + "</LinkedLayers>%n");

        LinkedLayers linkedLayers = new LinkedLayers();

        LinkedLayer linkedLayer = new LinkedLayer();
        linkedLayer.setLinkedLayersTypeKey("linkedTypeKey1");
        linkedLayer.setOtherLinkedLayersTypeKey("otherLinkedTypeKey1");
        linkedLayers.getLinkedLayers().add(linkedLayer);

        linkedLayer = new LinkedLayer();
        linkedLayer.setLinkedLayersTypeKey("linkedTypeKey2");
        linkedLayer.setOtherLinkedLayersTypeKey("otherLinkedTypeKey2");
        linkedLayers.getLinkedLayers().add(linkedLayer);

        String actualXml = XMLUtilities.writeXMLObjectToString(linkedLayers);

        assertEquals(xml, actualXml);
    }
}
