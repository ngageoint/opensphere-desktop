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
 * Tests the ability for the Popout models to serialize and deserialize.
 *
 */
public class PopoutModelsTest
{
    /**
     * Tests the ability to marshal and unmarshal the Popou model classes.
     *
     * @throws JAXBException A jaxb exception.
     */
    @Test
    public void test() throws JAXBException
    {
        PopoutModels models = new PopoutModels();

        PopoutModel model1 = new PopoutModel();
        model1.setHeight(1);
        model1.setWidth(2);
        model1.setX(3);
        model1.setY(4);
        model1.setTitle("model1");
        model1.getDataGroupInfoKeys().add("key1");
        model1.getDataGroupInfoKeys().add("key2");
        model1.getDataGroupInfoKeys().add("key3");

        models.getModels().put(model1.getId(), model1);

        PopoutModel model2 = new PopoutModel();
        model2.setHeight(21);
        model2.setWidth(22);
        model2.setX(23);
        model2.setY(24);
        model2.setTitle("model2");
        model2.getDataGroupInfoKeys().add("key21");
        model2.getDataGroupInfoKeys().add("key22");
        model2.getDataGroupInfoKeys().add("key23");

        models.getModels().put(model2.getId(), model2);

        JAXBContext context = JAXBContext.newInstance(PopoutModels.class);
        Marshaller marshaller = context.createMarshaller();

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(models, stringWriter);

        StringReader stringReader = new StringReader(stringWriter.getBuffer().toString());
        Unmarshaller unmarshaller = context.createUnmarshaller();

        PopoutModels actualModels = (PopoutModels)unmarshaller.unmarshal(stringReader);

        PopoutModel actualModel1 = actualModels.getModels().get(model1.getId());
        assertEquals(model1.getHeight(), actualModel1.getHeight());
        assertEquals(model1.getWidth(), actualModel1.getWidth());
        assertEquals(model1.getX(), actualModel1.getX());
        assertEquals(model1.getY(), actualModel1.getY());
        assertEquals(model1.getTitle(), actualModel1.getTitle());
        assertTrue(actualModel1.getDataGroupInfoKeys().contains("key1"));
        assertTrue(actualModel1.getDataGroupInfoKeys().contains("key2"));
        assertTrue(actualModel1.getDataGroupInfoKeys().contains("key3"));

        PopoutModel actualModel2 = actualModels.getModels().get(model2.getId());
        assertEquals(model2.getHeight(), actualModel2.getHeight());
        assertEquals(model2.getWidth(), actualModel2.getWidth());
        assertEquals(model2.getX(), actualModel2.getX());
        assertEquals(model2.getY(), actualModel2.getY());
        assertEquals(model2.getTitle(), actualModel2.getTitle());
        assertTrue(actualModel2.getDataGroupInfoKeys().contains("key21"));
        assertTrue(actualModel2.getDataGroupInfoKeys().contains("key22"));
        assertTrue(actualModel2.getDataGroupInfoKeys().contains("key23"));
    }
}
