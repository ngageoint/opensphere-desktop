package io.opensphere.featureactions.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link StyleAction}.
 */
public class StyleActionTest
{
    /**
     * Tests the class.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void test() throws JAXBException
    {
        StyleAction action = new StyleAction();
        action.getStyleOptions().setIconId(22);

        assertEquals("Set Style", action.toString());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(action, output);

        StyleAction actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), StyleAction.class);

        assertEquals("Set Style", actual.toString());
        assertEquals(22, actual.getStyleOptions().getIconId());
    }

    /**
     * Tests the class.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testClone() throws JAXBException
    {
        StyleAction action = new StyleAction();
        action.getStyleOptions().setIconId(22);

        assertEquals("Set Style", action.toString());

        StyleAction actual = action.clone();

        assertEquals("Set Style", actual.toString());
        assertEquals(22, actual.getStyleOptions().getIconId());
    }
}
