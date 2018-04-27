package io.opensphere.wfs.filter;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

/**
 * Test used to exercise the functionality of the
 * {@link BetterDefaultHandlerTest} class.
 */
public class BetterDefaultHandlerTest
{
    /**
     * The object on which tests are performed.
     */
    private BetterDefaultHandler myTestObject;

    /**
     * Value used in testing.
     */
    private String myUri;

    /**
     * Value used in testing.
     */
    private String myLocalName;

    /**
     * Value used in testing.
     */
    private String myQName;

    /**
     * Value used in testing.
     */
    private Attributes myAttributes;

    /**
     * Creates the resources needed to execute the tests.
     */
    @Before
    public void setUp()
    {
        myUri = "http://www.bitsys.com";
        myLocalName = "testNode";
        myQName = "http://www.bitsys.com/testNode";
        myAttributes = createStrictMock(Attributes.class);

        expect(myAttributes.getLength()).andReturn(2);
        expect(myAttributes.getQName(0)).andReturn("zero");
        expect(myAttributes.getValue(0)).andReturn("zero-value");
        expect(myAttributes.getQName(1)).andReturn("one");
        expect(myAttributes.getValue(1)).andReturn("one-value");

        replay(myAttributes);
        myTestObject = new BetterDefaultHandler();
    }

    /**
     * Test method to verify that there are no private methods in the
     * {@link BetterDefaultHandler} class.
     */
    @Test
    public void testNonPrivateMethods()
    {
        Method[] declaredMethods = BetterDefaultHandler.class.getDeclaredMethods();

        for (Method method : declaredMethods)
        {
            if (!method.getName().startsWith("$") && !method.getName().startsWith("lambda$"))
            {
                assertFalse(method.getName() + " is private. No private methods are permitted.",
                        Modifier.isPrivate(method.getModifiers()));
            }
        }
    }

    /**
     * Test method for {@link BetterDefaultHandler#characters(char[], int, int)}
     * .
     */
    @Test
    public void testCompleteOperation()
    {
        assertEquals(0, myTestObject.getElementStack().size());
        myTestObject.startElement(myUri, myLocalName, myQName, myAttributes);
        assertEquals(1, myTestObject.getElementStack().size());

        SaxElement element = myTestObject.getElementStack().getFirst();
        assertEquals(myUri, element.getUri());
        assertEquals(myLocalName, element.getLocalName());
        assertEquals(myQName, element.getQName());

        Map<String, String> attributes = element.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals(attributes.get("zero"), "zero-value");
        assertEquals(attributes.get("one"), "one-value");

        assertEquals("", myTestObject.getCurrentValue());

        String value = RandomStringUtils.randomAlphabetic(25);
        myTestObject.characters(value.toCharArray(), 0, 25);
        assertEquals(value, myTestObject.getCurrentValue());

        myTestObject.endElement(myUri, myLocalName, myQName);
        assertEquals("", myTestObject.getCurrentValue());
        assertEquals(0, myTestObject.getElementStack().size());
    }

    /**
     * Test method for
     * {@link BetterDefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * .
     */
    @Test
    public void testStartElement()
    {
        myTestObject.startElement(myUri, myLocalName, myQName, myAttributes);
        assertEquals(1, myTestObject.getElementStack().size());

        SaxElement element = myTestObject.getElementStack().getFirst();
        assertEquals(myUri, element.getUri());
        assertEquals(myLocalName, element.getLocalName());
        assertEquals(myQName, element.getQName());

        Map<String, String> attributes = element.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals(attributes.get("zero"), "zero-value");
        assertEquals(attributes.get("one"), "one-value");

        assertEquals("", myTestObject.getCurrentValue());
    }

    /**
     * Test method for {@link BetterDefaultHandler#getElementStack()}.
     */
    @Test
    public void testGetElementStack()
    {
        assertEquals(0, myTestObject.getElementStack().size());
        myTestObject.startElement(myUri, myLocalName, myQName, myAttributes);
        assertEquals(1, myTestObject.getElementStack().size());

        SaxElement element = myTestObject.getElementStack().getFirst();
        assertEquals(myUri, element.getUri());
        assertEquals(myLocalName, element.getLocalName());
        assertEquals(myQName, element.getQName());

        Map<String, String> attributes = element.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals(attributes.get("zero"), "zero-value");
        assertEquals(attributes.get("one"), "one-value");

        assertEquals("", myTestObject.getCurrentValue());
    }
}
