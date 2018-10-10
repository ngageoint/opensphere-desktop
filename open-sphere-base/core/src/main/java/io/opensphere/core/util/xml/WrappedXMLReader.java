package io.opensphere.core.util.xml;

import java.io.IOException;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Convenience class for wrapping a {@link XMLReader}.
 *
 */
public class WrappedXMLReader implements XMLReader
{
    /**
     * The reader.
     */
    private final XMLReader myXmlReader;

    /** Optional producer of content handler. */
    private final Function<ContentHandler, ContentHandler> myContentHandlerProducer;

    /**
     * Constructs a new namespace filter.
     *
     * @param validate Indicates if it should validate.
     * @param contentHandlerProducer optional producer of content handler
     * @throws ParserConfigurationException Config error.
     * @throws SAXException SAX error.
     */
    public WrappedXMLReader(boolean validate, Function<ContentHandler, ContentHandler> contentHandlerProducer)
            throws ParserConfigurationException, SAXException
    {
        this(newXMLReader(validate), contentHandlerProducer);
    }

    /**
     * Constructs a new namespace filter.
     *
     * @param xmlReader the XML reader
     * @param contentHandlerProducer optional producer of content handler
     */
    public WrappedXMLReader(XMLReader xmlReader, Function<ContentHandler, ContentHandler> contentHandlerProducer)
    {
        myXmlReader = xmlReader;
        myContentHandlerProducer = contentHandlerProducer;
    }

    @Override
    public ContentHandler getContentHandler()
    {
        return myXmlReader.getContentHandler();
    }

    @Override
    public DTDHandler getDTDHandler()
    {
        return myXmlReader.getDTDHandler();
    }

    @Override
    public EntityResolver getEntityResolver()
    {
        return myXmlReader.getEntityResolver();
    }

    @Override
    public ErrorHandler getErrorHandler()
    {
        return myXmlReader.getErrorHandler();
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return myXmlReader.getFeature(name);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return myXmlReader.getProperty(name);
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException
    {
        myXmlReader.parse(input);
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException
    {
        myXmlReader.parse(systemId);
    }

    @Override
    public void setContentHandler(ContentHandler handler)
    {
        myXmlReader.setContentHandler(myContentHandlerProducer != null ? myContentHandlerProducer.apply(handler) : handler);
    }

    @Override
    public void setDTDHandler(DTDHandler handler)
    {
        myXmlReader.setDTDHandler(handler);
    }

    @Override
    public void setEntityResolver(EntityResolver handler)
    {
        myXmlReader.setEntityResolver(handler);
    }

    @Override
    public void setErrorHandler(ErrorHandler handler)
    {
        myXmlReader.setErrorHandler(handler);
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        myXmlReader.setFeature(name, value);
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        myXmlReader.setProperty(name, value);
    }

    /**
     * Creates a new XML reader.
     *
     * @param validate Indicates if it should validate.
     * @return the reader
     * @throws SAXException SAX error.
     * @throws ParserConfigurationException Config error.
     */
    private static XMLReader newXMLReader(boolean validate) throws SAXException, ParserConfigurationException
    {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        parserFactory.setValidating(validate);
        return parserFactory.newSAXParser().getXMLReader();
    }
}
