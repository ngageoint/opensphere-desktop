package io.opensphere.core.units.duration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import io.opensphere.core.util.XMLUtilities;

/** Test for {@link JAXBDuration}. */
public class JAXBDurationTest
{
    /**
     * Test marshalling and unmarshalling a {@link Duration} using
     * {@link JAXBDuration}.
     *
     * @throws JAXBException If there is a JAXB error.
     * @throws ClassNotFoundException If the class cannot be loaded.
     */
    @Test
    public void test() throws JAXBException, ClassNotFoundException
    {
        Duration result;
        Element el;

        Duration testSeconds = new Seconds(new BigDecimal("3324.352"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(new JAXBDuration(testSeconds), baos);

        result = XMLUtilities.readXMLObject(new ByteArrayInputStream(baos.toByteArray()), JAXBDuration.class).getWrappedObject();
        Assert.assertEquals(result, testSeconds);

        el = XMLUtilities.marshalJAXBableToElement(testSeconds);
        result = XMLUtilities.readJAXBableObject(el, Duration.class);
        Assert.assertEquals(result, testSeconds);

        Duration testMonths = new Months(new BigDecimal("3324.352"));
        baos.reset();
        XMLUtilities.writeXMLObject(new JAXBDuration(testMonths), baos);

        result = XMLUtilities.readXMLObject(new ByteArrayInputStream(baos.toByteArray()), JAXBDuration.class).getWrappedObject();
        Assert.assertEquals(result, testMonths);

        el = XMLUtilities.marshalJAXBableToElement(testMonths);
        result = XMLUtilities.readJAXBableObject(el, Duration.class);
        Assert.assertEquals(result, testMonths);
    }
}
