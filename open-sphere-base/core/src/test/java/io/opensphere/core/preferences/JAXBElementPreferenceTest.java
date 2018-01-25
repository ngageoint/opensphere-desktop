package io.opensphere.core.preferences;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.junit.Test;
import org.w3c.dom.Element;

import io.opensphere.core.util.XMLUtilities;
import org.junit.Assert;

/**
 * Test for {@link JAXBElementPreference}.
 */
public class JAXBElementPreferenceTest
{
    /**
     * Test marshalling and unmarshalling a Boolean.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testBoolean() throws JAXBException
    {
        JAXBElementPreference<Boolean> boolPref;
        Element el;
        TestPreferenceContainer result;
        Preference<?> pref;

        String key = "key";
        boolPref = new JAXBElementPreference<Boolean>(key,
                new JAXBElement<Boolean>(new QName("value"), Boolean.class, Boolean.TRUE));
        el = XMLUtilities.marshalJAXBObjectToElement(new TestPreferenceContainer(boolPref));
        result = XMLUtilities.readXMLObject(el, TestPreferenceContainer.class);
        pref = result.getPreference();
        Assert.assertEquals(key, pref.getKey());
        Assert.assertEquals(Boolean.TRUE, pref.getBooleanValue(null));

        boolPref = new JAXBElementPreference<Boolean>(key,
                new JAXBElement<Boolean>(new QName("value"), Boolean.class, Boolean.FALSE));
        el = XMLUtilities.marshalJAXBObjectToElement(new TestPreferenceContainer(boolPref));
        result = XMLUtilities.readXMLObject(el, TestPreferenceContainer.class);
        pref = result.getPreference();
        Assert.assertEquals(key, pref.getKey());
        Assert.assertEquals(Boolean.FALSE, pref.getBooleanValue(null));
    }
}
