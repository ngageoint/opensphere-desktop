package io.opensphere.kml.marshal;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import io.opensphere.core.util.xml.WrappedContentHandler;

/**
 * The namespace filter handler.
 *
 */
final class NamespaceFilterHandler extends WrappedContentHandler
{
    /**
     * KML 2.0.
     */
    private static final String KML_20 = "http://earth.google.com/kml/2.0";

    /**
     * KML 2.1.
     */
    private static final String KML_21 = "http://earth.google.com/kml/2.1";

    /**
     * KML 2.2.
     */
    private static final String KML_22 = "http://www.opengis.net/kml/2.2";

    /**
     * Constructs a new namespace filter handler.
     *
     * @param contentHandler The wrapped content handler.
     */
    public NamespaceFilterHandler(ContentHandler contentHandler)
    {
        super(contentHandler);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if (uri.equals(KML_20) || uri.equals(KML_21))
        {
            super.startElement(KML_22, localName, qName, atts);
        }
        else
        {
            super.startElement(uri, localName, qName, atts);
        }
    }
}
