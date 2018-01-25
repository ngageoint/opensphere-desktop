package io.opensphere.core.units.length;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import io.opensphere.core.util.XMLUtilities;

/** Test for {@link JAXBLength}. */
public class JAXBLengthTest
{
    /**
     * Test marshalling and unmarshalling a {@link Length} using
     * {@link JAXBLength}.
     *
     * @throws JAXBException If there is a JAXB error.
     * @throws ClassNotFoundException If the class cannot be loaded.
     */
    @Test
    public void test() throws JAXBException, ClassNotFoundException
    {
        Length result;
        Element el;

        Length testMeters = new Meters(3324.352);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(new JAXBLength(testMeters), baos);

        result = XMLUtilities.readXMLObject(new ByteArrayInputStream(baos.toByteArray()), JAXBLength.class).getWrappedObject();
        Assert.assertEquals(result, testMeters);

        baos.reset();
        el = XMLUtilities.marshalJAXBableToElement(testMeters);
        result = XMLUtilities.readJAXBableObject(el, Length.class);
        Assert.assertEquals(result, testMeters);

        Length testFeet = new Feet(3324.352);
        baos.reset();
        XMLUtilities.writeXMLObject(new JAXBLength(testFeet), baos);

        result = XMLUtilities.readXMLObject(new ByteArrayInputStream(baos.toByteArray()), JAXBLength.class).getWrappedObject();
        Assert.assertEquals(result, testFeet);

        baos.reset();
        el = XMLUtilities.marshalJAXBableToElement(testFeet);
        result = XMLUtilities.readJAXBableObject(el, Length.class);
        Assert.assertEquals(result, testFeet);
    }
}
