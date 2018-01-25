package io.opensphere.kml.envoy;

import java.util.Deque;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import io.opensphere.core.util.xml.WrappedContentHandler;

/**
 * ContentHandler adapted from the JAK implementation.
 */
final class KMLNamespaceFilterHandler extends WrappedContentHandler
{
    /** The root KML tag. */
    private static final String ROOT_KML_TAG = "kml";

    /** The KML 2.0 namespace. */
    private static final String KML_20 = "http://earth.google.com/kml/2.0";

    /** The KML 2.1 namespace. */
    private static final String KML_21 = "http://earth.google.com/kml/2.1";

    /** The KML 2.2 namespace. */
    private static final String KML_22 = "http://www.opengis.net/kml/2.2";

    /** A common incorrect KML 2.2 namespace. */
    private static final String KML_22_INCORRECT = "http://earth.google.com/kml/2.2";

    /** The default namespace. */
    public static final String KML_DEFAULT_NAMESPACE = KML_22;

    /** Some tags we know about. */
    private static final Tags TAGS = new Tags();

    /** Whether we're at document start. */
    private boolean myIsDocumentStart;

    /** Whether we're missing the root KML tag. */
    private boolean myIsMissingRootKMLTag;

    /** The tag stack. */
    private final Deque<String> myTagStack = new LinkedList<>();

    /** Whether we are currently in a tag that was artificially added. */
    private boolean myInAddedTag;

    /**
     * Constructor.
     *
     * @param contentHandler The content handler
     */
    public KMLNamespaceFilterHandler(ContentHandler contentHandler)
    {
        super(contentHandler);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        int newLength = fixBadCharacters(ch, start, length);
        super.characters(ch, start, newLength);
    }

    @Override
    public void startDocument() throws SAXException
    {
        myIsDocumentStart = true;
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException
    {
        if (myIsMissingRootKMLTag)
        {
            // need to complete our root <kml> tag
            getContentHandler().endElement(KML_DEFAULT_NAMESPACE, ROOT_KML_TAG, ROOT_KML_TAG);
        }

        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if (myIsDocumentStart && !localName.equals(ROOT_KML_TAG))
        {
            // This document does not start with a <kml> tag. Another common
            // error, so add one manually.
            myIsMissingRootKMLTag = true;
            myIsDocumentStart = false;
            startElementInternal(KML_DEFAULT_NAMESPACE, ROOT_KML_TAG, ROOT_KML_TAG, atts);
        }

        TagInfo tagInfo = TAGS.getTagInfoForAlias(localName);
        String modifiedLocalName = tagInfo != null ? tagInfo.getName() : localName;
        String modifiedQName = tagInfo != null ? tagInfo.getName() : qName;
        String modifiedUri = uri.equals(KML_20) || uri.equals(KML_21) || uri.equals(KML_22_INCORRECT) || uri.isEmpty()
                ? KML_DEFAULT_NAMESPACE : uri;

        // Add parent tag if necessary
        if (tagInfo != null && !tagInfo.getParent().equals(myTagStack.peekLast()))
        {
            startElementInternal(KML_DEFAULT_NAMESPACE, tagInfo.getParent(), tagInfo.getParent(), atts);
            myInAddedTag = true;
        }
        // Close the added tag if this tag isn't a child of the added tag
        else if (myInAddedTag && tagInfo == null)
        {
            String tag = myTagStack.peekLast();
            endElementInternal(KML_DEFAULT_NAMESPACE, tag, tag);
            myInAddedTag = false;
        }

        startElementInternal(modifiedUri, modifiedLocalName, modifiedQName, atts);

        /* Add a default scale of 1 for IconStyle because JAK doesn't properly
         * set the default. */
        if ("IconStyle".equals(localName))
        {
            insertTag(modifiedUri, "scale", atts, "1");
        }

        myIsDocumentStart = false;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        TagInfo tagInfo = TAGS.getTagInfoForAlias(localName);
        String modifiedLocalName = tagInfo != null ? tagInfo.getName() : localName;
        String modifiedQName = tagInfo != null ? tagInfo.getName() : qName;

        // Close any added tags
        String tag;
        while (!(tag = myTagStack.peekLast()).equals(modifiedLocalName))
        {
            endElementInternal(KML_DEFAULT_NAMESPACE, tag, tag);
            myInAddedTag = false;
        }

        endElementInternal(uri, modifiedLocalName, modifiedQName);
    }

    /**
     * Inserts a tag/value.
     *
     * @param uri the namespace URI
     * @param localName the local name (without prefix)
     * @param atts the attributes attached to the element
     * @param value the value
     * @throws SAXException any SAX exception
     */
    private void insertTag(String uri, String localName, Attributes atts, String value) throws SAXException
    {
        startElementInternal(uri, localName, localName, atts);
        characters(value.toCharArray(), 0, value.length());
        endElementInternal(uri, localName, localName);
    }

    /**
     * Wrapper for startElement() that manages the tag stack.
     *
     * @param uri the namespace URI
     * @param localName the local name (without prefix)
     * @param qName the qualified name (with prefix)
     * @param atts the attributes attached to the element
     * @throws SAXException any SAX exception
     */
    private void startElementInternal(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        super.startElement(uri, localName, qName, atts);
        myTagStack.addLast(localName);
    }

    /**
     * Wrapper for endElement() that manages the tag stack.
     *
     * @param uri the namespace URI
     * @param localName the local name (without prefix)
     * @param qName the qualified name (with prefix)
     * @throws SAXException any SAX exception
     */
    private void endElementInternal(String uri, String localName, String qName) throws SAXException
    {
        super.endElement(uri, localName, qName);
        myTagStack.removeLast();
    }

    /**
     * Fixes bad characters by updating the character array and returning the
     * new length.
     *
     * @param ch The character array
     * @param start The start index
     * @param length The length
     * @return The new length
     */
    private int fixBadCharacters(char[] ch, int start, int length)
    {
        int newLength = length;
        String currentTag = myTagStack.peekLast();
        if (currentTag != null)
        {
            TagInfo tagInfo = TAGS.getTagInfoForName(currentTag);
            if (tagInfo != null)
            {
                Class<?> type = tagInfo.getType();

                // Boolean values
                if (type == Boolean.class)
                {
                    // Replace true/false with 1/0
                    if (ContentHandlerUtilities.equalsString(ch, start, length, "true"))
                    {
                        newLength = ContentHandlerUtilities.assign(ch, start, length, "1", currentTag);
                    }
                    else if (ContentHandlerUtilities.equalsString(ch, start, length, "false"))
                    {
                        newLength = ContentHandlerUtilities.assign(ch, start, length, "0", currentTag);
                    }
                }
                // Numeric values
                else if (type == Integer.class || type == Float.class || type == Double.class)
                {
                    // Convert incorrect "inf" to "INF"
                    if (ContentHandlerUtilities.equalsString(ch, start, length, "inf"))
                    {
                        newLength = ContentHandlerUtilities.assign(ch, start, length, "INF", currentTag);
                    }
                    // Convert values like "2+2" to "4"
                    else if (ContentHandlerUtilities.contains(ch, start, length, '+')
                            && !ContentHandlerUtilities.contains(ch, start, length, "e+"))
                    {
                        newLength = ContentHandlerUtilities.assign(ch, start, length,
                                ContentHandlerUtilities.sumOfNumbers(ch, start, length), currentTag);
                    }
                }
                // Enum values
                else if (type == Enum.class)
                {
                    // Trim whitespace
                    newLength = ContentHandlerUtilities.assign(ch, start, length, new String(ch, start, length).trim(),
                            currentTag);
                }
            }
        }
        return newLength;
    }
}
