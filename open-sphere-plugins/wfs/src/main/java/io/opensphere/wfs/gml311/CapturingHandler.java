package io.opensphere.wfs.gml311;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.opensphere.core.util.collections.New;

/**
 * A specialized handler, in which an XML document is captured to a buffer, and made accessible to external users.
 */
public class CapturingHandler extends DefaultHandler
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(CapturingHandler.class);

    /**
     * The stream to which output is written, which wraps the stream specified in {@link #myCaptureStream}.
     */
    private final PrintWriter myOutputStream;

    /**
     * The stream with which output is captured.
     */
    private final ByteArrayOutputStream myCaptureStream;

    /**
     * The set of mappings enabled in the XML document.
     */
    private final Map<String, String> myUriMappings;

    /**
     * The current indentation level of content.
     */
    private int myIndentLevel;

    /**
     * A flag used to indicate that the last printed information was CDATA / content.
     */
    private boolean myLastOutputWasContent;

    /**
     * Creates a new handler.
     */
    public CapturingHandler()
    {
        myCaptureStream = new ByteArrayOutputStream();
        myOutputStream = new PrintWriter(new OutputStreamWriter(myCaptureStream, Charset.forName("UTF-8")));
        myUriMappings = New.map();
    }

    /**
     * Gets the captured output of the document.
     *
     * @return the captured output of the document.
     */
    public String getContent()
    {
        try
        {
            return myCaptureStream.toString("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            LOG.error("Unable to get stream encoded from UTF-8", e);
            return null;
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
        myOutputStream.print("<?xml version='1.0' encoding='UTF-8'?>");
        myLastOutputWasContent = false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException
    {
        myOutputStream.flush();
        myOutputStream.close();
        myLastOutputWasContent = false;
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
        myOutputStream.println();
        for (int i = 0; i < myIndentLevel; i++)
        {
            myOutputStream.print("    ");
        }
        myOutputStream.print('<');
        myOutputStream.print(getName(pUri, pLocalName, pQName));

        if (pAttributes != null)
        {
            for (int i = 0; i < pAttributes.getLength(); i++)
            {
                myOutputStream.print(' ');
                myOutputStream.print(getName(pAttributes.getURI(i), pAttributes.getLocalName(i), pAttributes.getQName(i)));
                myOutputStream.print("=\"");
                myOutputStream.print(pAttributes.getValue(i));
                myOutputStream.print('"');
            }
        }
        myOutputStream.print('>');
        myIndentLevel++;
        myLastOutputWasContent = false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] pCh, int pStart, int pLength) throws SAXException
    {
        myOutputStream.print(new String(pCh, pStart, pLength));
        myLastOutputWasContent = false;
    }

    /**
     * Gets the correct name, choosing between the local and the qualified name. The qualified name is chosen when the local name
     * is missing or blank.
     *
     * @param pUri the URI of the element, used to de-reference namespace mappings.
     * @param pLocalName the local name, used if not null or blank.
     * @param pQName the fully qualified name, used if the local name is missing.
     * @return the name of the element.
     */
    protected String getName(String pUri, String pLocalName, String pQName)
    {
        String elementName;
        if (StringUtils.isBlank(pLocalName))
        {
            elementName = pQName;
        }
        else
        {
            if (myUriMappings.containsKey(pUri))
            {
                elementName = myUriMappings.get(pUri) + ":" + pLocalName;
            }
            else
            {
                elementName = pLocalName;
            }
        }
        return elementName;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String pUri, String pLocalName, String pQName) throws SAXException
    {
        myIndentLevel--;
        if (!myLastOutputWasContent)
        {
            myOutputStream.println();
            for (int i = 0; i < myIndentLevel; i++)
            {
                myOutputStream.print("    ");
            }
        }

        myOutputStream.print("</");
        myOutputStream.print(getName(pUri, pLocalName, pQName));
        myOutputStream.print('>');
        myLastOutputWasContent = false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] pCh, int pStart, int pLength) throws SAXException
    {
        myOutputStream.print(new String(pCh, pStart, pLength));
        myLastOutputWasContent = true;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String pPrefix, String pUri) throws SAXException
    {
        myUriMappings.put(pUri, pPrefix);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String pPrefix) throws SAXException
    {
        String targetUri = null;
        for (Entry<String, String> entry : myUriMappings.entrySet())
        {
            if (StringUtils.equals(pPrefix, entry.getValue()))
            {
                targetUri = entry.getKey();
                break;
            }
        }

        myUriMappings.remove(targetUri);
    }
}
