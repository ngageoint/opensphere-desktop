package io.opensphere.wfs.gml311;

import java.io.IOException;
import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A specialized handler used to wrap multiple child handlers. Useful with the {@link CapturingHandler} to capture debug output
 * without affecting standard operations (The caller would create their standard handler, a {@link CapturingHandler}, and wrap
 * both in an instance of {@link CompoundXmlHandler}).
 */
public class CompoundXmlHandler extends DefaultHandler
{
    /**
     * The child handlers to which content will be echoed.
     */
    private final Collection<DefaultHandler> myChildHandlers;

    /**
     * Creates a new compound handler composed of the supplied child handlers.
     *
     * @param pChildHandlers the child handlers to include in the wrapped handler.
     */
    public CompoundXmlHandler(Collection<DefaultHandler> pChildHandlers)
    {
        super();
        myChildHandlers = pChildHandlers;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] pCh, int pStart, int pLength) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.characters(pCh, pStart, pLength);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.endDocument();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String pUri, String pLocalName, String pQName) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.endElement(pUri, pLocalName, pQName);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String pPrefix) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.endPrefixMapping(pPrefix);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException pE) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.error(pE);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(SAXParseException pE) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.fatalError(pE);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] pCh, int pStart, int pLength) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.ignorableWhitespace(pCh, pStart, pLength);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#notationDecl(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void notationDecl(String pName, String pPublicId, String pSystemId) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.notationDecl(pName, pPublicId, pSystemId);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void processingInstruction(String pTarget, String pData) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.processingInstruction(pTarget, pData);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always return null, so that the parser will use the system identifier provided in the XML document. This method implements
     * the SAX default behavior: application writers can override it in a subclass to do special translations such as catalog
     * lookups or URI redirection.
     * </p>
     *
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String pPublicId, String pSystemId) throws IOException, SAXException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#unparsedEntityDecl(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void unparsedEntityDecl(String pName, String pPublicId, String pSystemId, String pNotationName) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.unparsedEntityDecl(pName, pPublicId, pSystemId, pNotationName);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator pLocator)
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.setDocumentLocator(pLocator);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.startDocument();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String pPrefix, String pUri) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.startPrefixMapping(pPrefix, pUri);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttributes) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.startElement(pUri, pLocalName, pQName, pAttributes);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String pName) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.skippedEntity(pName);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(SAXParseException pE) throws SAXException
    {
        for (DefaultHandler childHandler : myChildHandlers)
        {
            childHandler.warning(pE);
        }
    }
}
