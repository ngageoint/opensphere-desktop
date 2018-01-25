package io.opensphere.featureactions.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test {@link LabelAction}.
 */
public class LabelActionTest
{
    /**
     * Tests xml serializing the class.
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testClone() throws JAXBException
    {
        LabelAction action = new LabelAction();

        action.getLabelOptions().setSize(14);

        LabelAction actual = action.clone();

        assertEquals(14, actual.getLabelOptions().getSize());
    }

    /**
     * Tests xml serializing the class.
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testSerialization() throws JAXBException
    {
        LabelAction action = new LabelAction();

        action.getLabelOptions().setSize(10);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(action, output);

        LabelAction actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), LabelAction.class);

        assertEquals(10, actual.getLabelOptions().getSize());
    }
}
