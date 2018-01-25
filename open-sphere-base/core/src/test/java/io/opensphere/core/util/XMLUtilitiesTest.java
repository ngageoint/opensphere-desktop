package io.opensphere.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Test for {@link XMLUtilities}. */
public class XMLUtilitiesTest implements StartElementInspector
{
    @Override
    public boolean isValidStartElement(StartElement element)
    {
        return element.getName().getLocalPart().equals("immutableObjectWrapper");
    }

    /**
     * Tests the can Unmarshal call.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testCanUnmarshal() throws JAXBException
    {
        ImmutableObjectWrapper testObject = new ImmutableObjectWrapper(6);
        InputStream canStream = XMLUtilities.writeXMLObjectToInputStreamSync(testObject);

        Container container = new Container();
        container.setObject(new ImmutableObject(3));
        InputStream cantStream = XMLUtilities.writeXMLObjectToInputStreamSync(container);

        assertTrue(XMLUtilities.canUnmarshal(canStream, this));
        assertFalse(XMLUtilities.canUnmarshal(cantStream, this));
    }

    /**
     * Test for {@link XMLUtilities#marshalJAXBableToElement(JAXBable)}.
     *
     * @throws JAXBException If there is an unexpected exception.
     * @throws ClassNotFoundException If a class cannot be loaded.
     */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testMarshalJAXBObjectToElement() throws JAXBException, ClassNotFoundException
    {
        ImmutableObject testObj = new ImmutableObject(5);
        try
        {
            XMLUtilities.marshalJAXBObjectToElement(testObj);
            Assert.fail("Expected JAXBException.");
        }
        catch (JAXBException e)
        {
            // expected
        }

        Element el = XMLUtilities.marshalJAXBableToElement(testObj);
        ImmutableObject result = XMLUtilities.readJAXBableObject(el, ImmutableObject.class);
        Assert.assertEquals(testObj.getValue(), result.getValue());

        Container container = new Container();
        container.setObject(testObj);
        XMLUtilities.marshalJAXBObjectToElement(container);
    }

    /**
     * Test marshalling jaxb object to a node.
     *
     * @throws ParserConfigurationException bad parse.
     * @throws JAXBException bad jaxb.
     */
    @Test
    public void testMarshalJAXBObjectToElementToNode() throws ParserConfigurationException, JAXBException
    {
        ImmutableObjectWrapper testObj = new ImmutableObjectWrapper(5);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node testNode = doc.appendChild(doc.createElement("testNode"));

        Node node = XMLUtilities.marshalJAXBObjectToElement(testObj, testNode);

        assertEquals("immutableObjectWrapper", node.getNodeName());
        assertEquals("myValue", node.getChildNodes().item(0).getNodeName());
        assertEquals("5", node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
        assertEquals(node, testNode.getChildNodes().item(testNode.getChildNodes().getLength() - 1));
    }

    /**
     * Test marshalling jaxb object to a node.
     *
     * @throws ParserConfigurationException bad parse.
     * @throws JAXBException bad jaxb.
     */
    @Test
    public void testMarshalJAXBObjectToElementToNodeJaxbContext() throws ParserConfigurationException, JAXBException
    {
        ImmutableObjectWrapper testObj = new ImmutableObjectWrapper(5);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node testNode = doc.appendChild(doc.createElement("testNode"));

        Node node = XMLUtilities.marshalJAXBObjectToElement(testObj, testNode,
                JAXBContext.newInstance(ImmutableObjectWrapper.class));

        assertEquals("immutableObjectWrapper", node.getNodeName());
        assertEquals("myValue", node.getChildNodes().item(0).getNodeName());
        assertEquals("5", node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
        assertEquals(node, testNode.getChildNodes().item(testNode.getChildNodes().getLength() - 1));
    }

    /**
     * Test for
     * {@link XMLUtilities#mergeDuplicateElements(org.w3c.dom.Document, String)}
     * .
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws IOException If the test fails.
     * @throws SAXException If the test fails.
     * @throws TransformerFactoryConfigurationError If the test fails.
     * @throws TransformerException If the test fails.
     */
    @Test
    public void testMergeDuplicateElements()
        throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException
    {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<Earth><Oceans><Atlantic/><Indian/></Oceans><Oceans><Pacific/><Arctic/></Oceans></Earth>";
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xml)));

        XMLUtilities.mergeDuplicateElements(doc, "Oceans");

        StringWriter sw = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(sw));

        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<Earth><Oceans><Atlantic/><Indian/><Pacific/><Arctic/></Oceans></Earth>", sw.toString());
    }

    /**
     * Test for
     * {@link XMLUtilities#writeXMLObjectToInputStreamAsync(Object, Class...)}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testWriteXMLObjectToInputStreamAsync() throws JAXBException
    {
        ImmutableObjectWrapper testObj = new ImmutableObjectWrapper(5);

        InputStream is = XMLUtilities.writeXMLObjectToInputStreamAsync(testObj);

        ImmutableObjectWrapper result = XMLUtilities.readXMLObject(is, ImmutableObjectWrapper.class);
        Assert.assertEquals(testObj.getWrappedObject().getValue(), result.getWrappedObject().getValue());
        Assert.assertNotSame(testObj, result);
    }

    /**
     * Test for
     * {@link XMLUtilities#writeXMLObjectToInputStreamSync(Object, Class...)}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testWriteXMLObjectToInputStreamSync() throws JAXBException
    {
        ImmutableObjectWrapper testObj = new ImmutableObjectWrapper(5);

        InputStream is = XMLUtilities.writeXMLObjectToInputStreamSync(testObj);

        ImmutableObjectWrapper result = XMLUtilities.readXMLObject(is, ImmutableObjectWrapper.class);
        Assert.assertEquals(testObj.getWrappedObject().getValue(), result.getWrappedObject().getValue());
        Assert.assertNotSame(testObj, result);
    }

    /**
     * Test for
     * {@link XMLUtilities#jaxbClone(Object, Class)}.
     *
     * @throws JAXBException If the test fails.
     */
    @Test
    public void testJaxbClone() throws JAXBException
    {
        ImmutableObjectWrapper testObj = new ImmutableObjectWrapper(5);

        ImmutableObjectWrapper result = XMLUtilities.jaxbClone(testObj, ImmutableObjectWrapper.class);

        Assert.assertEquals(testObj.getWrappedObject().getValue(), result.getWrappedObject().getValue());
        Assert.assertNotSame(testObj, result);

        result = XMLUtilities.jaxbClone(null, ImmutableObjectWrapper.class);
        assertNull(result);
    }

    /** An XMLAdapter for {@link #testMarshalJAXBObjectToElement}. */
    protected static class Adapter extends XmlAdapter<ImmutableObjectWrapper, ImmutableObject>
    {
        @Override
        public ImmutableObjectWrapper marshal(ImmutableObject v)
        {
            return v.getWrapper();
        }

        @Override
        public ImmutableObject unmarshal(ImmutableObjectWrapper v)
        {
            return v.getWrappedObject();
        }
    }

    /** A JAXBWrapper for {@link #testMarshalJAXBObjectToElement}. */
    @XmlRootElement
    protected static class Container
    {
        /** The immutable object. */
        private ImmutableObject myObject;

        /**
         * Accessor for the object.
         *
         * @return The object.
         */
        @XmlJavaTypeAdapter(Adapter.class)
        protected ImmutableObject getObject()
        {
            return myObject;
        }

        /**
         * Mutator for the object.
         *
         * @param object The object to set.
         */
        protected void setObject(ImmutableObject object)
        {
            myObject = object;
        }
    }

    /** An immutable object for {@link #testMarshalJAXBObjectToElement()} . */
    protected static class ImmutableObject implements JAXBable<ImmutableObjectWrapper>
    {
        /** The value. */
        private final int myValue;

        /**
         * Constructor.
         *
         * @param value The value.
         */
        public ImmutableObject(int value)
        {
            myValue = value;
        }

        /**
         * Get the value.
         *
         * @return The value.
         */
        public int getValue()
        {
            return myValue;
        }

        @Override
        public ImmutableObjectWrapper getWrapper()
        {
            return new ImmutableObjectWrapper(getValue());
        }
    }

    /** A JAXBWrapper for {@link #testMarshalJAXBObjectToElement}. */
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class ImmutableObjectWrapper implements JAXBWrapper<ImmutableObject>
    {
        /** The value. */
        private int myValue;

        /**
         * Constructor that takes a value.
         *
         * @param value The value.
         */
        public ImmutableObjectWrapper(int value)
        {
            myValue = value;
        }

        /** Constructor for JAXB. */
        @SuppressWarnings("unused")
        private ImmutableObjectWrapper()
        {
        }

        @Override
        public ImmutableObject getWrappedObject()
        {
            return new ImmutableObject(myValue);
        }

        /**
         * Mutator for the value.
         *
         * @param value The value to set.
         */
        protected void setValue(int value)
        {
            myValue = value;
        }
    }
}
