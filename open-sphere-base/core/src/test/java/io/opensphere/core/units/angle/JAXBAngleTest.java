package io.opensphere.core.units.angle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import io.opensphere.core.util.XMLUtilities;

/** Test for {@link JAXBAngle}. */
public class JAXBAngleTest
{
    /**
     * Test marshalling and unmarshalling a {@link Coordinates} using
     * {@link JAXBAngle}.
     *
     * @throws JAXBException If there is a JAXB error.
     * @throws ClassNotFoundException If the class cannot be loaded.
     */
    @Test
    public void test() throws JAXBException, ClassNotFoundException
    {
        Coordinates result;
        Element el;

        Coordinates testDD = new DecimalDegrees(24.352);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(new JAXBAngle(testDD), baos);

        result = XMLUtilities.readXMLObject(new ByteArrayInputStream(baos.toByteArray()), JAXBAngle.class).getWrappedObject();
        Assert.assertEquals(result, testDD);

        baos.reset();
        el = XMLUtilities.marshalJAXBableToElement(testDD);
        result = XMLUtilities.readJAXBableObject(el, Coordinates.class);
        Assert.assertEquals(result, testDD);

        Coordinates testDMS = new DegreesMinutesSeconds(24.352);
        baos.reset();
        XMLUtilities.writeXMLObject(new JAXBAngle(testDMS), baos);

        result = XMLUtilities.readXMLObject(new ByteArrayInputStream(baos.toByteArray()), JAXBAngle.class).getWrappedObject();
        Assert.assertEquals(result, testDMS);

        baos.reset();
        el = XMLUtilities.marshalJAXBableToElement(testDMS);
        result = XMLUtilities.readJAXBableObject(el, Coordinates.class);
        Assert.assertEquals(result, testDMS);
    }
}
