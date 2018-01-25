package io.opensphere.wms.util;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import io.opensphere.core.util.xml.WrappedContentHandler;

/**
 * Content handler that fixes missing WMS namespaces.
 */
final class NamespaceContentHandler extends WrappedContentHandler
{
    /**
     * Constructs a new namespace filter handler.
     *
     * @param contentHandler The wrapped content handler.
     */
    public NamespaceContentHandler(ContentHandler contentHandler)
    {
        super(contentHandler);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        super.startElement(StringUtils.isEmpty(uri) ? "http://www.opengis.net/wms" : uri, localName, qName, atts);
    }
}
