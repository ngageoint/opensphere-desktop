package io.opensphere.core.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Convenience class for wrapping a {@link ContentHandler}.
 *
 */
public class WrappedContentHandler implements ContentHandler
{
    /**
     * The content handler.
     */
    private final ContentHandler myContentHandler;

    /**
     * Constructs a new namespace filter handler.
     *
     * @param contentHandler The wrapped content handler.
     */
    public WrappedContentHandler(ContentHandler contentHandler)
    {
        myContentHandler = contentHandler;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        myContentHandler.characters(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException
    {
        myContentHandler.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        myContentHandler.endElement(uri, localName, qName);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
        myContentHandler.endPrefixMapping(prefix);
    }

    /**
     * Accessor for the wrapped content handler.
     *
     * @return The content handler.
     */
    public ContentHandler getContentHandler()
    {
        return myContentHandler;
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        myContentHandler.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        myContentHandler.processingInstruction(target, data);
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
        myContentHandler.setDocumentLocator(locator);
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
        myContentHandler.skippedEntity(name);
    }

    @Override
    public void startDocument() throws SAXException
    {
        myContentHandler.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        myContentHandler.startElement(uri, localName, qName, atts);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        myContentHandler.startPrefixMapping(prefix, uri);
    }
}
