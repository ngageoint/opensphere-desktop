package io.opensphere.wfs.filter;

import java.util.Map;

import org.xml.sax.Attributes;

import io.opensphere.core.util.collections.New;

/** A bean representation of a SAX element. */
public class SaxElement
{
    /** The URI. */
    private final String myUri;

    /** The local name. */
    private final String myLocalName;

    /** The Q name. */
    private final String myQName;

    /** The attributes. */
    private final Map<String, String> myAttributes = New.map();

    /**
     * Constructor.
     *
     * @param uri The URI.
     * @param localName The local name
     * @param qName The Q name
     * @param attributes The attributes
     */
    public SaxElement(String uri, String localName, String qName, Attributes attributes)
    {
        myUri = uri;
        myLocalName = localName;
        myQName = qName;
        for (int i = 0, n = attributes.getLength(); i < n; i++)
        {
            myAttributes.put(attributes.getQName(i), attributes.getValue(i));
        }
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri()
    {
        return myUri;
    }

    /**
     * Gets the localName.
     *
     * @return the localName
     */
    public String getLocalName()
    {
        return myLocalName;
    }

    /**
     * Gets the qName.
     *
     * @return the qName
     */
    public String getQName()
    {
        return myQName;
    }

    /**
     * Gets the attributes.
     *
     * @return the attributes
     */
    public Map<String, String> getAttributes()
    {
        return myAttributes;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return myLocalName;
    }
}
