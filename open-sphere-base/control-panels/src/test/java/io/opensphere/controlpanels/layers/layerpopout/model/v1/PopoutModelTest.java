package io.opensphere.controlpanels.layers.layerpopout.model.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

/**
 * Tests the ability to serialize and deserialize a PopoutModel.
 */
public class PopoutModelTest
{
    /**
     * Tests the ability to marshal and unmarshal the Popou model classes.
     *
     * @throws JAXBException A jaxb exception.
     */
    @Test
    public void test() throws JAXBException
    {
        PopoutModel model1 = new PopoutModel();
        model1.setHeight(1);
        model1.setWidth(2);
        model1.setX(3);
        model1.setY(4);
        model1.setTitle("model1");
        model1.getDataGroupInfoKeys().add("key1");
        model1.getDataGroupInfoKeys().add("key2");
        model1.getDataGroupInfoKeys().add("key3");

        JAXBContext context = JAXBContext.newInstance(PopoutModel.class);
        Marshaller marshaller = context.createMarshaller();

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(model1, stringWriter);

        StringReader stringReader = new StringReader(stringWriter.getBuffer().toString());
        Unmarshaller unmarshaller = context.createUnmarshaller();

        PopoutModel actualModel1 = (PopoutModel)unmarshaller.unmarshal(stringReader);

        assertEquals(model1.getId(), actualModel1.getId());
        assertEquals(model1.getHeight(), actualModel1.getHeight());
        assertEquals(model1.getWidth(), actualModel1.getWidth());
        assertEquals(model1.getX(), actualModel1.getX());
        assertEquals(model1.getY(), actualModel1.getY());
        assertEquals(model1.getTitle(), actualModel1.getTitle());
        assertTrue(actualModel1.getDataGroupInfoKeys().contains("key1"));
        assertTrue(actualModel1.getDataGroupInfoKeys().contains("key2"));
        assertTrue(actualModel1.getDataGroupInfoKeys().contains("key3"));
    }
}
