package io.opensphere.wfs.filter;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

/**
 * Test used to exercise the functionality of the {@link SaxElement} class.
 */
@SuppressWarnings("boxing")
public class SaxElementTest
{
    /**
     * The object on which tests are performed.
     */
    private SaxElement myTestObject;

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
        myTestObject = new SaxElement(myUri, myLocalName, myQName, myAttributes);
    }

    /**
     * Test method for
     * {@link SaxElement#SaxElement(String, String, String, Attributes)} .
     */
    @Test
    public void testSaxElement()
    {
        assertNotNull(myTestObject);
    }

    /**
     * Test method for {@link SaxElement#getUri()}.
     */
    @Test
    public void testGetUri()
    {
        assertEquals(myUri, myTestObject.getUri());
    }

    /**
     * Test method for {@link SaxElement#getLocalName()}.
     */
    @Test
    public void testGetLocalName()
    {
        assertEquals(myLocalName, myTestObject.getLocalName());
    }

    /**
     * Test method for {@link SaxElement#getQName()}.
     */
    @Test
    public void testGetQName()
    {
        assertEquals(myQName, myTestObject.getQName());
    }

    /**
     * Test method for {@link SaxElement#getAttributes()}.
     */
    @Test
    public void testGetAttributes()
    {
        Map<String, String> attributes = myTestObject.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals(attributes.get("zero"), "zero-value");
        assertEquals(attributes.get("one"), "one-value");
    }

    /**
     * Test method for {@link SaxElement#toString()}.
     */
    @Test
    public void testToString()
    {
        assertEquals(myLocalName, myTestObject.toString());
    }
}
