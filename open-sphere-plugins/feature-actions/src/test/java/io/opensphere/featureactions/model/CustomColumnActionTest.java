package io.opensphere.featureactions.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link CustomColumnAction}.
 */
public class CustomColumnActionTest
{
    /**
     * Tests serializing the action.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void test() throws JAXBException
    {
        CustomColumnAction action = new CustomColumnAction();
        action.setColumn("column1");
        action.setValue("value1");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(action, output);

        CustomColumnAction actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), CustomColumnAction.class);

        assertEquals("column1", actual.columnProperty().get());
        assertEquals("value1", actual.valueProperty().get());
    }
}
