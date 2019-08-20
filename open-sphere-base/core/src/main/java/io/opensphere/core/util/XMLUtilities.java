package io.opensphere.core.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Utilities for handling XML documents.
 */
@SuppressWarnings("PMD.GodClass")
public final class XMLUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(XMLUtilities.class);

    /**
     * Peeks at the input stream to see if the stream can be unmarshalled as the
     * specified class.
     *
     * @param stream The stream to peek.
     * @param inspector The start element inspector.
     * @return True if the stream can be unmarshalled into the specified class,
     *         false if it cannot.
     */
    public static boolean canUnmarshal(InputStream stream, StartElementInspector inspector)
    {
        boolean canUnmarshal = false;

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try
        {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(stream);

            XMLEvent event = null;

            while ((event = xmlEventReader.peek()) != null)
            {
                if (event.isStartElement())
                {
                    canUnmarshal = inspector.isValidStartElement((StartElement)event);

                    break;
                }

                xmlEventReader.next();
            }
        }
        catch (XMLStreamException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e.getMessage(), e);
            }
        }
        finally
        {
            try
            {
                if (stream.markSupported())
                {
                    stream.reset();
                }
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return canUnmarshal;
    }

    /**
     * Create an unmarshaller for the given JAXB context. This will also set up
     * a listener on the marshaller if trace logging is enabled.
     *
     * @param context The JAXB context.
     * @return The unmarshaller.
     * @throws JAXBException If there was an error creating the unmarshaller.
     */
    public static Unmarshaller createUnmarshaller(JAXBContext context) throws JAXBException
    {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        if (LOGGER.isTraceEnabled())
        {
            unmarshaller.setListener(new Unmarshaller.Listener()
            {
                @Override
                public void afterUnmarshal(Object obj, Object parent)
                {
                    LOGGER.trace("After unmarshal target object " + obj + " for parent " + parent);
                }

                @Override
                public void beforeUnmarshal(Object obj, Object parent)
                {
                    LOGGER.trace("Before unmarshal target object " + obj + " for parent " + parent);
                }
            });
        }

        return unmarshaller;
    }

    /**
     * Convert a {@link Properties} object into an XML {@link Document} suitable
     * for reading by the {@link Properties#loadFromXML(InputStream)} method.
     *
     * @param props The properties.
     * @param comment An optional comment.
     * @return The document, or {@code null} if there was an error.
     */
    public static Document createXMLFromProperties(Properties props, String comment)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try
        {
            db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            LOGGER.error("Failed to create document builder: " + e, e);
            return null;
        }

        Document doc = db.newDocument();
        Element properties = (Element)doc.appendChild(doc.createElement("properties"));

        if (comment != null)
        {
            Element comments = (Element)properties.appendChild(doc.createElement("comment"));
            comments.appendChild(doc.createTextNode(comment));
        }

        Set<Entry<Object, Object>> entrySet = props.entrySet();
        for (Entry<Object, Object> entry : entrySet)
        {
            Element elem = (Element)properties.appendChild(doc.createElement("entry"));
            elem.setAttribute("key", (String)entry.getKey());
            elem.appendChild(doc.createTextNode((String)entry.getValue()));
        }
        return doc;
    }

    /**
     * Format XML from a {@link Node} and return it as a {@link String}.
     *
     * @param node The DOM node.
     * @return The formatted XML string.
     */
    public static String format(Node node)
    {
        StringWriter sw = new StringWriter();
        format(new DOMSource(node), new StreamResult(sw), (String)null);
        return sw.toString();
    }

    /**
     * Format XML from a {@link Node} and send it to an output stream.
     *
     * @param node The DOM node.
     * @param output The output.
     * @param doctypeSystem Optional system identifier for the document type
     *            declaration.
     */
    public static void format(Node node, OutputStream output, String doctypeSystem)
    {
        format(new DOMSource(node), new StreamResult(output), doctypeSystem);
    }

    /**
     * Format XML from a {@link Node} and return it as a {@link String}.
     *
     * @param node The DOM node.
     * @param doctypeSystem Optional system identifier for the document type
     *            declaration.
     * @return The formatted XML string.
     */
    public static String format(Node node, String doctypeSystem)
    {
        StringWriter sw = new StringWriter();
        format(new DOMSource(node), new StreamResult(sw), doctypeSystem);
        return sw.toString();
    }

    /**
     * Format XML from a reader and send it to an output stream.
     *
     * @param reader The input.
     * @param output The output.
     * @param doctypeSystem Optional system identifier for the document type
     *            declaration.
     */
    public static void format(Reader reader, OutputStream output, String doctypeSystem)
    {
        format(new StreamSource(reader), new StreamResult(output), doctypeSystem);
    }

    /**
     * Format XML from a reader and send it to a writer.
     *
     * @param reader The input.
     * @param writer The output.
     * @param doctypeSystem Optional system identifier for the document type
     *            declaration.
     */
    public static void format(Reader reader, Writer writer, String doctypeSystem)
    {
        format(new StreamSource(reader), new StreamResult(writer), doctypeSystem);
    }

    /**
     * Format XML.
     *
     * @param xmlInput XML source.
     * @param xmlOutput XML result.
     * @param doctypeSystem Optional system identifier for the document type
     *            declaration.
     */
    public static void format(Source xmlInput, Result xmlOutput, String doctypeSystem)
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            if (doctypeSystem != null)
            {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
            }
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(3));
            transformer.transform(xmlInput, xmlOutput);
        }
        catch (TransformerFactoryConfigurationError | TransformerException e)
        {
            LOGGER.error("Failed to transform XML: " + e, e);
        }
    }

    /**
     * Format an XML string.
     *
     * @param input The XML string.
     * @return A formatted string.
     */
    public static String format(String input)
    {
        Source xmlInput = new StreamSource(new StringReader(input));
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        format(xmlInput, xmlOutput, null);
        return xmlOutput.getWriter().toString();
    }

    /**
     * Gets the attribute nodes in the document for the given tag/attribute
     * name.
     *
     * @param document the document
     * @param tagName the tag name
     * @param attributeName the attribute name
     * @return the list of matching attribute nodes
     */
    public static List<Node> getAttributes(Document document, String tagName, String attributeName)
    {
        List<Node> attributes = new ArrayList<>();
        NodeList elements = document.getElementsByTagName(tagName);
        for (int i = 0; i < elements.getLength(); ++i)
        {
            Node attribute = elements.item(i).getAttributes().getNamedItem(attributeName);
            if (attribute != null)
            {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    /**
     * Extracts the value of the named attribute from the supplied {@link Node},
     * if present. If the supplied node does not contain an attribute with the
     * supplied name, a null value is returned.
     *
     * @param node the node from which to get the attribute's value.
     * @param attributeName the name of the attribute for which to retrieve the
     *            value.
     * @return the indicated attribute value, if it exists, or null
     */
    public static String getAttributeValue(Node node, String attributeName)
    {
        if (!node.hasAttributes())
        {
            return null;
        }
        Node attributeNode = node.getAttributes().getNamedItem(attributeName);
        if (attributeNode == null)
        {
            return null;
        }
        return attributeNode.getNodeValue();
    }

    /**
     * Check for the occurrence of a colon-separated namespace and, if one is
     * found, remove it.
     *
     * @param text original text field from which to remove the namespace.
     * @return text with namespace removed, if applicable
     */
    public static String stripNamespace(String text)
    {
        String[] split = text.split(":");
        if (split.length > 1)
        {
            return split[1];
        }
        return text;
    }

    /**
     * Get the document associated with a {@link Node}. This is either the
     * {@link Node} itself, or the document returned by
     * {@link Node#getOwnerDocument()}.
     *
     * @param node The node.
     * @return The document.
     */
    public static Document getDocument(Node node)
    {
        return node instanceof Document ? (Document)node : node.getOwnerDocument();
    }

    /**
     * Reflect on the generic interfaces of the given type and determine the
     * generic argument for JAXBWrapper.
     *
     * @param <S> The type of the wrapper object.
     * @param <T> The type of the wrapped object.
     * @param target The type of the wrapped object.
     * @return The type of the wrapper object.
     * @throws IllegalArgumentException If the wrapper type cannot be inferred
     *             from the input class.
     */
    @SuppressWarnings("unchecked")
    public static <S extends JAXBWrapper<T>, T extends JAXBable<S>> Class<? extends S> getJAXBWrapperType(Class<T> target)
            throws IllegalArgumentException
    {
        for (Type intf : target.getGenericInterfaces())
        {
            if (intf instanceof ParameterizedType)
            {
                if (((ParameterizedType)intf).getRawType().equals(JAXBable.class))
                {
                    Type type = ((ParameterizedType)intf).getActualTypeArguments()[0];
                    if (type.equals(JAXBWrapper.class))
                    {
                        throw new IllegalArgumentException("Input class [" + target + "] must implement "
                                + JAXBable.class.getSimpleName() + " with a concrete generic argument.");
                    }
                    return (Class<? extends S>)(type instanceof Class ? type : ((ParameterizedType)type).getRawType());
                }
                else if (((ParameterizedType)intf).getRawType() instanceof Class
                        && JAXBable.class.isAssignableFrom((Class<?>)((ParameterizedType)intf).getRawType()))
                {
                    return getJAXBWrapperType((Class<T>)((ParameterizedType)intf).getRawType());
                }
            }
            else if (intf instanceof Class && JAXBable.class.isAssignableFrom((Class<?>)intf))
            {
                return getJAXBWrapperType((Class<T>)intf);
            }
        }
        throw new IllegalArgumentException(
                "Wrapper type cound not be inferred from generic type arguments for class " + target.getName());
    }

    /**
     * Marshal a JAXBable object to an {@link Element}. Use a JAXB context that
     * contains only the declared class of the input object's wrapper.
     *
     * @param jaxbable The object.
     * @return The DOM element.
     * @throws JAXBException If the object cannot be marshalled.
     * @throws IllegalArgumentException If the object is {@code null}.
     */
    public static Element marshalJAXBableToElement(JAXBable<?> jaxbable) throws JAXBException
    {
        Object wrapper = jaxbable.getWrapper();
        if (wrapper == null)
        {
            throw new JAXBException("Wrapper for JAXBable cannot be null.");
        }
        return marshalJAXBObjectToElement(wrapper);
    }

    /**
     * Marshal a JAXBable object to an {@link Element}.
     *
     * @param jaxbable The object.
     * @param context The JAXB context.
     * @return The DOM element.
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static Element marshalJAXBableToElement(JAXBable<?> jaxbable, JAXBContext context) throws JAXBException
    {
        return marshalJAXBObjectToElement(jaxbable.getWrapper(), context);
    }

    /**
     * Marshal a JAXB object to an {@link Element}. Use a JAXB context that
     * contains only the declared class of the input object.
     *
     * @param jaxbElement The object.
     * @return The DOM element.
     * @throws JAXBException If the object cannot be marshalled.
     * @throws IllegalArgumentException If the object is {@code null}.
     */
    public static Element marshalJAXBObjectToElement(Object jaxbElement) throws JAXBException
    {
        Utilities.checkNull(jaxbElement, "jaxbElement");
        Class<?>[] classes = jaxbElement instanceof JAXBElement ? new Class<?>[0] : new Class<?>[] { jaxbElement.getClass() };
        JAXBContext context = JAXBContextHelper.getCachedContext(classes);
        return marshalJAXBObjectToElement(jaxbElement, context);
    }

    /**
     * Marshal a JAXB object to an {@link Element}.
     *
     * @param jaxbElement The object.
     * @param context The JAXB context.
     * @return The DOM element.
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static Element marshalJAXBObjectToElement(Object jaxbElement, JAXBContext context) throws JAXBException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try
        {
            db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new JAXBException(e.getMessage(), e);
        }
        Document doc = db.newDocument();
        try
        {
            context.createMarshaller().marshal(jaxbElement, doc);
        }
        catch (ConcurrentModificationException e)
        {
            throw new JAXBException("Concurrent modification during marshalling [" + jaxbElement + "]: " + e, e);
        }
        Element element = doc.getDocumentElement();
        return element;
    }

    /**
     * Marshal a JAXB object to an {@link Element}. Use a JAXB context that
     * contains only the declared class of the input object.
     *
     * @param jaxbElement The object.
     * @param nodeToMarshalTo The node to add the marshalled object to.
     * @return The DOM element.
     * @throws JAXBException If the object cannot be marshalled.
     * @throws IllegalArgumentException If the object is {@code null}.
     */
    public static Node marshalJAXBObjectToElement(Object jaxbElement, Node nodeToMarshalTo) throws JAXBException
    {
        Utilities.checkNull(jaxbElement, "jaxbElement");
        Class<?>[] classes = jaxbElement instanceof JAXBElement ? new Class<?>[0] : new Class<?>[] { jaxbElement.getClass() };
        JAXBContext context = JAXBContextHelper.getCachedContext(classes);
        return marshalJAXBObjectToElement(jaxbElement, nodeToMarshalTo, context);
    }

    /**
     * Marshal a JAXB object to an {@link Element}.
     *
     * @param jaxbElement The object.
     * @param nodeToMarshalTo The node to add the marshalled object to.
     * @param context The JAXB context.
     * @return The DOM element.
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static Node marshalJAXBObjectToElement(Object jaxbElement, Node nodeToMarshalTo, JAXBContext context)
            throws JAXBException
    {
        Node newNode = null;
        try
        {
            context.createMarshaller().marshal(jaxbElement, nodeToMarshalTo);
            NodeList children = nodeToMarshalTo.getChildNodes();
            newNode = children.item(children.getLength() - 1);
        }
        catch (ConcurrentModificationException e)
        {
            throw new JAXBException("Concurrent modification during marshalling [" + jaxbElement + "]: " + e, e);
        }

        return newNode;
    }

    /**
     * Find the elements in a document that have the given tag name and merge
     * them into a single element.
     *
     * @param doc The document.
     * @param tagName The tag name.
     */
    public static void mergeDuplicateElements(Document doc, String tagName)
    {
        NodeList elements = doc.getElementsByTagName(tagName);
        if (elements.getLength() > 1)
        {
            Node toNode = elements.item(0);
            for (int index = elements.getLength() - 1; index >= 1; --index)
            {
                Node fromNode = elements.item(index);
                while (fromNode.hasChildNodes())
                {
                    toNode.appendChild(fromNode.removeChild(fromNode.getFirstChild()));
                }
                fromNode.getParentNode().removeChild(fromNode);
            }
        }
    }

    /**
     * Create a {@link Document} using the default {@link DocumentBuilder} .
     *
     * @return The document.
     * @throws ParserConfigurationException If the document builder cannot be
     *             created.
     */
    public static Document newDocument() throws ParserConfigurationException
    {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    /**
     * Create a {@link DocumentBuilder} using a namespace-aware
     * {@link DocumentBuilderFactory}.
     *
     * @return The builder.
     * @throws ParserConfigurationException If the document builder cannot be
     *             created.
     */
    public static DocumentBuilder newDocumentBuilderNS() throws ParserConfigurationException
    {
        return docBuilder(true);
    }

    /**
     * Fix the heinous, stupid API. Yes, I'm talking about you, XML.
     *
     * @param nsAware for setNamespaceAware
     * @return DocumentBuilder
     * @throws ParserConfigurationException never, I would expect
     */
    public static DocumentBuilder docBuilder(boolean nsAware) throws ParserConfigurationException
    {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(nsAware);
        return f.newDocumentBuilder();
    }

    /**
     * Creates a new XML reader.
     *
     * @return the reader
     */
    public static XMLReader newXMLReader()
    {
        final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        parserFactory.setValidating(false);

        XMLReader xmlReader = null;
        try
        {
            xmlReader = parserFactory.newSAXParser().getXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }
        catch (SAXException e)
        {
            LOGGER.error("Failed to generate SAX parser that ignores DTD in schema: " + e, e);
        }
        catch (ParserConfigurationException e)
        {
            LOGGER.error("Failed to properly configure XML Parser: " + e, e);
        }
        return xmlReader;
    }

    /**
     * Read a JAXBable object from a {@link Node}.
     *
     * @param node The node.
     * @param target The type of object being read.
     *
     * @param <S> The type of the wrapper object.
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled or the file
     *             cannot be read.
     * @throws ClassNotFoundException If the class for the JAXBable object
     *             cannot be loaded.
     */
    public static <S extends JAXBWrapper<T>, T extends JAXBable<S>> T readJAXBableObject(Node node, Class<T> target)
            throws JAXBException, ClassNotFoundException
    {
        Class<? extends JAXBWrapper<T>> wrapperType = getJAXBWrapperType(target);
        JAXBWrapper<T> obj = readXMLObject(node, wrapperType);
        return obj.getWrappedObject();
    }

    /**
     * Read a JAXB object from a file.
     *
     * @param fileToLoad The file.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled or the file
     *             cannot be read.
     */
    public static <T> T readXMLObject(File fileToLoad, Class<T> target) throws JAXBException
    {
        return readXMLObject(new StreamSource(fileToLoad), target);
    }

    /**
     * Read a JAXB object from a file.
     *
     * @param fileToLoad The file.
     * @param target The type of object being read.
     * @param classes The JAXB classes to use to unmarshal the XML.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled or the file
     *             cannot be read.
     */
    public static <T> T readXMLObject(File fileToLoad, Class<T> target, Collection<Class<?>> classes) throws JAXBException
    {
        return readXMLObject(new StreamSource(fileToLoad), target, classes);
    }

    /**
     * Read a JAXB object from a file.
     *
     * @param fileToLoad The file.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @param context The JAXB context.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled or the file
     *             cannot be read.
     */
    public static <T> T readXMLObject(File fileToLoad, Class<T> target, JAXBContext context) throws JAXBException
    {
        return readXMLObject(new StreamSource(fileToLoad), target, context);
    }

    /**
     * Read a JAXB object from a file.
     *
     * @param fileToLoad The file.
     * @param target The type of object being read.
     * @param packages The packages to include in the JAXB context. The packages
     *            must contain jaxb.index files or object factories.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled or the file
     *             cannot be read.
     */
    public static <T> T readXMLObject(File fileToLoad, Class<T> target, Package... packages) throws JAXBException
    {
        return readXMLObject(new StreamSource(fileToLoad), target, packages);
    }

    /**
     * Read a JAXB object from the stream. Use a JAXB context that contains only
     * the target class.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(InputStream stream, Class<T> target) throws JAXBException
    {
        return readXMLObject(getSAXSource(stream), target);
    }

    /**
     * Use JAXB serialization to clone an object. If the serialization or
     * deserialization fails, then the original is returned.
     *
     * @param jaxbObject the object to be cloned
     * @param target the target class
     * @param <T> The type of the jaxb object.
     * @return a clone of <i>jaxbObject</i>, if possible, or <i>jaxbObject</i>
     *         itself.
     */
    public static <T> T jaxbClone(T jaxbObject, Class<T> target)
    {
        if (jaxbObject != null)
        {
            try
            {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                writeXMLObject(jaxbObject, output);
                return readXMLObject(new ByteArrayInputStream(output.toByteArray()), target);
            }
            catch (JAXBException e)
            {
                LOGGER.error(e, e);
            }
        }

        return jaxbObject;
    }

    /**
     * Read a JAXB object from the stream.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     * @param classes The JAXB classes to use to unmarshal the XML.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(InputStream stream, Class<T> target, Collection<Class<?>> classes) throws JAXBException
    {
        return readXMLObject(getSAXSource(stream), target, classes);
    }

    /**
     * Read a JAXB object from the stream.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     * @param context The JAXB context.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(InputStream stream, Class<T> target, JAXBContext context) throws JAXBException
    {
        return readXMLObject(getSAXSource(stream), target, context);
    }

    /**
     * Read a JAXB object from the stream. Use a JAXB context that contains only
     * the target class.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     * @param packages The packages to include in the JAXB context. The packages
     *            must contain jaxb.index files or object factories.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(InputStream stream, Class<T> target, Package... packages) throws JAXBException
    {
        return readXMLObject(getSAXSource(stream), target, packages);
    }

    /**
     * Read a JAXB object from a {@link Node}.
     *
     * @param node The node.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled or the file
     *             cannot be read.
     */
    public static <T> T readXMLObject(Node node, Class<T> target) throws JAXBException
    {
        return readXMLObject(node, JAXBContextHelper.getCachedContext(target), target);
    }

    /**
     * Read a JAXB object from a {@link Node}.
     *
     * @param node The node.
     * @param context The JAXB context to use.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled or the file
     *             cannot be read.
     */
    public static <T> T readXMLObject(Node node, JAXBContext context, Class<T> target) throws JAXBException
    {
        Unmarshaller unmarshaller = createUnmarshaller(context);
        try
        {
            JAXBElement<? extends T> unmarshal = unmarshaller.unmarshal(node, target);
            return unmarshal.getValue();
        }
        catch (RuntimeException e)
        {
            throw new JAXBException("Failed to unmarshal object for target class " + target + ": " + e, e);
        }
    }

    /**
     * Read a JAXB object from the stream. Use a JAXB context that contains only
     * the target class.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(Source stream, Class<T> target) throws JAXBException
    {
        JAXBContext context = JAXBContextHelper.getCachedContext(target);
        T result = readXMLObject(stream, target, context);
        return result;
    }

    /**
     * Read a JAXB object from the stream. Use a JAXB context that contains only
     * the target class.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObjectNoDTD(Source stream, Class<T> target) throws JAXBException
    {
        JAXBContext context = JAXBContextHelper.getCachedContext(target);
        T result = readXMLObject(stream, target, context, true);
        return result;
    }

    /**
     * Read a JAXB object from the stream.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @param classes The classes to use in the JAXB context.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(Source stream, Class<T> target, Collection<Class<?>> classes) throws JAXBException
    {
        try
        {
            @SuppressWarnings("unchecked")
            T result = (T)createUnmarshaller(JAXBContextHelper.getCachedContext(classes))
            .unmarshal(createWhitespaceDiscardingEventReader(stream));
            return result;
        }
        catch (RuntimeException e)
        {
            throw new JAXBException("Failed to unmarshal object for target class " + target + ": " + e, e);
        }
    }

    /**
     * Creates a new {@link XMLEventReader}, populated with an event filter to
     * discard unimportant whitespace.
     *
     * @param stream the stream to wrap in the event reader.
     * @return a new {@link XMLEventReader} in which the supplied stream has
     *         been wrapped, that contains an event filter to remove unimportant
     *         whitespace.
     * @throws JAXBException if the XMLEventReader cannot be created.
     */
    public static XMLEventReader createWhitespaceDiscardingEventReader(Source stream) throws JAXBException
    {
        return createWhitespaceDiscardingEventReader(stream, false);
    }

    /**
     * Creates a new {@link XMLEventReader}, populated with an event filter to
     * discard unimportant whitespace.
     *
     * @param stream the stream to wrap in the event reader.
     * @param skipDTD True if the xml document has DTD elements then we will
     *            skip those, false if you want the DTD elements to be resolved.
     * @return a new {@link XMLEventReader} in which the supplied stream has
     *         been wrapped, that contains an event filter to remove unimportant
     *         whitespace.
     * @throws JAXBException if the XMLEventReader cannot be created.
     */
    public static XMLEventReader createWhitespaceDiscardingEventReader(Source stream, boolean skipDTD) throws JAXBException
    {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader;
        try
        {
            if (skipDTD)
            {
                factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            }

            XMLEventReader rawReader = factory.createXMLEventReader(stream);
            reader = factory.createFilteredReader(rawReader, (EventFilter)event -> !(event.isCharacters() && ((Characters)event).isWhiteSpace()
                    && !((Characters)event).getData().contains("\t") && !((Characters)event).getData().contains(" ")));
        }
        catch (XMLStreamException e)
        {
            throw new JAXBException("Failed to create XMLEventReader for source.", e);
        }

        return reader;
    }

    /**
     * Read a JAXB object from the stream.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     * @param context The JAXB context.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(Source stream, Class<T> target, JAXBContext context) throws JAXBException
    {
        return readXMLObject(stream, target, context, false);
    }

    /**
     * Read a JAXB object from the stream.
     *
     * @param stream The stream.
     * @param skipDTD True if the xml document has DTD elements then we will
     *            skip those, false if you want the DTD elements to be resolved.
     * @param target The type of object being read.
     * @param context The JAXB context.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(Source stream, Class<T> target, JAXBContext context, boolean skipDTD) throws JAXBException
    {
        try
        {
            T result = createUnmarshaller(context).unmarshal(createWhitespaceDiscardingEventReader(stream, skipDTD), target)
                    .getValue();
            return result;
        }
        catch (RuntimeException e)
        {
            throw new JAXBException("Failed to unmarshal object for target class " + target + ": " + e, e);
        }
    }

    /**
     * Read a JAXB object from the stream. Use a JAXB context created from the
     * provided packages.
     *
     * @param stream The stream.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @param packages The packages to include in the JAXB context. The packages
     *            must contain jaxb.index files or object factories.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(Source stream, Class<T> target, Package... packages) throws JAXBException
    {
        JAXBContext context = JAXBContextHelper.getCachedContext(packages);
        T result = readXMLObject(stream, target, context);
        return result;
    }

    /**
     * Read a JAXB object from the URL. Use a JAXB context that contains only
     * the target class.
     *
     * @param url The URL.
     * @param target The type of object being read.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(URL url, Class<T> target) throws JAXBException
    {
        JAXBContext context = JAXBContextHelper.getCachedContext(target);
        return readXMLObject(url, target, context);
    }

    /**
     * Read a JAXB object from the URL. Cast the result to the target type.
     *
     * @param url The URL.
     * @param target The type of object being read.
     * @param classes The classes to use in the JAXB context.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(URL url, Class<T> target, Collection<Class<? extends T>> classes) throws JAXBException
    {
        JAXBContext context = JAXBContextHelper.getCachedContext(classes);
        return readXMLObject(url, target, context);
    }

    /**
     * Read a JAXB object from the URL. Cast the result to the target type.
     *
     * @param url The URL.
     * @param target The type of object being read.
     * @param context The JAXB context.
     *
     * @param <T> The type of object being read.
     * @return The object.
     * @throws JAXBException If the object cannot be unmarshalled.
     */
    public static <T> T readXMLObject(URL url, Class<T> target, JAXBContext context) throws JAXBException
    {
        try
        {
            @SuppressWarnings("unchecked")
            T result = (T)createUnmarshaller(context).unmarshal(url);
            return result;
        }
        catch (RuntimeException e)
        {
            throw new JAXBException("Failed to unmarshal object for target class " + target + ": " + e, e);
        }
    }

    /**
     * Write a {@link Properties} object to an {@link OutputStream} as XML
     * {@link Document} suitable for reading by the
     * {@link Properties#loadFromXML(InputStream)} method.
     *
     * @param props The properties.
     * @param comment An optional comment.
     * @param output The output stream.
     */
    public static void writePropertiesAsXML(Properties props, String comment, OutputStream output)
    {
        format(createXMLFromProperties(props, comment), output, "http://java.sun.com/dtd/properties.dtd");
    }

    /**
     * Write a JAXB object to a file.
     *
     * @param jaxbElement The object.
     * @param output The file.
     * @param classes The classes to load in the JAXB context. If no classes are
     *            provided, the class of the <tt>jaxbElement</tt> will be used
     *            for the context.
     *
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static void writeXMLObject(Object jaxbElement, File output, Class<?>... classes) throws JAXBException
    {
        OutputStream outputStream;
        try
        {
            outputStream = new BufferedOutputStream(new FileOutputStream(output));
        }
        catch (FileNotFoundException e)
        {
            throw new JAXBException(e.getMessage(), e);
        }
        try
        {
            writeXMLObject(jaxbElement, outputStream, classes);
        }
        finally
        {
            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                LOGGER.warn("Failed to close output stream for XML object: " + e, e);
            }
        }
    }

    /**
     * Write a JAXB object to a stream.
     *
     * @param jaxbElement The object.
     * @param output The stream.
     *
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static void writeXMLObject(Object jaxbElement, OutputStream output) throws JAXBException
    {
        writeXMLObject(jaxbElement, output, (Class<?>[])null);
    }

    /**
     * Write a JAXB object to a stream.
     *
     * @param jaxbElement The object.
     * @param output The stream.
     * @param classes The classes to load in the JAXB context. If no classes are
     *            provided, the class of the <tt>jaxbElement</tt> will be used
     *            for the context.
     *
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static void writeXMLObject(Object jaxbElement, OutputStream output, Class<?>... classes) throws JAXBException
    {
        Class<?>[] contextClasses;
        if (classes == null || classes.length == 0)
        {
            contextClasses = new Class<?>[] { jaxbElement.getClass() };
        }
        else
        {
            contextClasses = classes;
        }
        StringWriter stringWriter = new StringWriter();
        JAXBContextHelper.getCachedContext(contextClasses).createMarshaller().marshal(jaxbElement, stringWriter);
        format(new StringReader(stringWriter.toString()), output, null);
    }

    /**
     * Write a JAXB object to a stream.
     *
     * @param jaxbElement The object.
     * @param output The stream.
     * @param packages The classes to load in the JAXB context. If no classes
     *            are provided, the class of the <tt>jaxbElement</tt> will be
     *            used for the context.
     *
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static void writeXMLObject(Object jaxbElement, OutputStream output, Package... packages) throws JAXBException
    {
        StringWriter stringWriter = new StringWriter();
        JAXBContextHelper.getCachedContext(packages).createMarshaller().marshal(jaxbElement, stringWriter);
        format(new StringReader(stringWriter.toString()), output, null);
    }

    /**
     * Get an input stream that provides the marshalled content of a JAXB
     * object. Using this method will create a background thread to do the
     * marshalling.
     *
     * @param jaxbElement The object.
     * @param classes The classes to load in the JAXB context. If no classes are
     *            provided, the class of the <tt>jaxbElement</tt> will be used
     *            for the context.
     * @return The input stream that will provide the XML.
     */
    public static InputStream writeXMLObjectToInputStreamAsync(final Object jaxbElement, Class<?>... classes)
    {
        final Class<?>[] contextClasses;
        if (classes == null || classes.length == 0)
        {
            contextClasses = new Class<?>[] { jaxbElement.getClass() };
        }
        else
        {
            contextClasses = classes;
        }
        PipedInputStream is = new PipedInputStream();
        final OutputStream os;
        try
        {
            os = new PipedOutputStream(is);
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to create output stream: " + e, e);
            return null;
        }
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    JAXBContextHelper.getCachedContext(contextClasses).createMarshaller().marshal(jaxbElement, os);
                }
                catch (JAXBException e)
                {
                    LOGGER.error("Error marshalling JAXB object: " + e, e);
                }
                finally
                {
                    try
                    {
                        os.close();
                    }
                    catch (IOException e)
                    {
                        LOGGER.warn("Failed to close output stream: " + e, e);
                    }
                }
            }
        }.start();
        return is;
    }

    /**
     * Get an input stream that provides the marshalled content of a JAXB
     * object. Using this method will marshal the JAXB object inline and then
     * return a stream that provides the marshalled data.
     *
     * @param jaxbElement The object.
     * @param classes The classes to load in the JAXB context. If no classes are
     *            provided, the class of the <tt>jaxbElement</tt> will be used
     *            for the context.
     * @return The input stream that will provide the XML.
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static InputStream writeXMLObjectToInputStreamSync(final Object jaxbElement, Class<?>... classes) throws JAXBException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeXMLObject(jaxbElement, os, classes);
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * Get a String that contains the marshalled content of a JAXB object. Using
     * this method will marshal the JAXB object inline and then return a String
     * that contains the marshalled data.
     *
     * @param jaxbElement The object.
     * @param classes The classes to load in the JAXB context. If no classes are
     *            provided, the class of the <tt>jaxbElement</tt> will be used
     *            for the context.
     * @return The String that will provide the XML.
     * @throws JAXBException If the object cannot be marshalled.
     */
    public static String writeXMLObjectToString(final Object jaxbElement, Class<?>... classes) throws JAXBException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeXMLObject(jaxbElement, os, classes);
        return new String(os.toByteArray(), Charset.forName("UTF-8"));
    }

    /**
     * Get a {@link SAXSource} for an {@link InputStream}.
     *
     * @param stream The stream.
     * @return The source.
     */
    private static SAXSource getSAXSource(InputStream stream)
    {
        final SAXSource source = new SAXSource(newXMLReader(), new InputSource(stream));
        return source;
    }

    /** Disallow instantiation. */
    private XMLUtilities()
    {
    }
}
