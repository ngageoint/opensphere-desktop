package io.opensphere.core.preferences;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.w3c.dom.Element;

import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.SupplierX;
import io.opensphere.core.util.XMLUtilities;
import org.junit.Assert;

/**
 * Test for {@link JAXBElementPreference}.
 */
public class JAXBObjectPreferenceTest
{
    /**
     * Test marshalling and unmarshalling a JAXB object.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void test() throws JAXBException
    {
        String key = "key";
        ValueClass value = new ValueClass(14);
        Preference<ValueClass> jaxbPref = new JAXBObjectPreference<>(key, value);
        Element el = XMLUtilities.marshalJAXBObjectToElement(new TestPreferenceContainer(jaxbPref));
        TestPreferenceContainer result = XMLUtilities.readXMLObject(el, TestPreferenceContainer.class);
        Preference<?> pref = result.getPreference();
        Assert.assertEquals(key, pref.getKey());

        Assert.assertEquals(value.getValue(), ((ValueClass)pref.getValue(null, null)).getValue());

        Element el2 = XMLUtilities.marshalJAXBObjectToElement(result);
        TestPreferenceContainer result2 = XMLUtilities.readXMLObject(el2, TestPreferenceContainer.class);
        Preference<?> pref2 = result2.getPreference();
        Assert.assertEquals(key, pref2.getKey());

        Assert.assertEquals(value.getValue(), ((ValueClass)pref2.getValue(null, null)).getValue());
    }

    /**
     * Test marshalling and unmarshalling a JAXB object with an abstract member.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testAbstractMember() throws JAXBException
    {
        SupplierX<JAXBContext, JAXBException> contextSupplier = new SupplierX<JAXBContext, JAXBException>()
        {
            @Override
            public JAXBContext get() throws JAXBException
            {
                return JAXBContextHelper.getCachedContext(JAXBObjectPreferenceTest.class.getPackage());
            }
        };

        String key = "key";
        TestClass value = new TestClass(new ValueClass(14));
        Preference<TestClass> jaxbPref = new JAXBObjectPreference<>(key, contextSupplier, value);
        Element el = XMLUtilities.marshalJAXBObjectToElement(new TestPreferenceContainer(jaxbPref));
        TestPreferenceContainer result = XMLUtilities.readXMLObject(el, TestPreferenceContainer.class);
        Preference<?> pref = result.getPreference();
        Assert.assertEquals(key, pref.getKey());

        Assert.assertEquals(value.getValue(), ((TestClass)pref.getValue(null, contextSupplier)).getValue());

        Element el2 = XMLUtilities.marshalJAXBObjectToElement(result);
        TestPreferenceContainer result2 = XMLUtilities.readXMLObject(el2, TestPreferenceContainer.class);
        Preference<?> pref2 = result2.getPreference();
        Assert.assertEquals(key, pref2.getKey());

        Assert.assertEquals(value.getValue(), ((TestClass)pref2.getValue(null, contextSupplier)).getValue());
    }
}
