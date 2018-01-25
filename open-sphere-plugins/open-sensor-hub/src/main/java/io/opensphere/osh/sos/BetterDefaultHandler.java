package io.opensphere.osh.sos;

import java.util.ArrayDeque;
import java.util.Deque;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/** A better DefaultHandler. */
public class BetterDefaultHandler extends DefaultHandler
{
    /** The element stack. */
    private final Deque<SaxElement> myElementStack = new ArrayDeque<>();

    /** The value of the property currently getting parsed. */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final StringBuilder myCurrentValue = new StringBuilder();

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#startElement(String, String,
     *      String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        myElementStack.push(new SaxElement(uri, localName, qName, attributes));
        myCurrentValue.setLength(0);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#endElement(String, String,
     *      String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
    {
        myElementStack.pop();
        myCurrentValue.setLength(0);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
    {
        myCurrentValue.append(ch, start, length);
    }

    /**
     * Gets the elementStack.
     *
     * @return the elementStack
     */
    protected Deque<SaxElement> getElementStack()
    {
        return myElementStack;
    }

    /**
     * Gets the currentValue.
     *
     * @return the currentValue
     */
    protected String getCurrentValue()
    {
        return myCurrentValue.toString();
    }
}
