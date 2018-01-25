package io.opensphere.osh.sos;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import io.opensphere.core.util.collections.New;
import io.opensphere.osh.model.MutableOffering;
import io.opensphere.osh.model.Offering;

/** SOS get capabilities SAX handler. */
public class GetCapabilitiesHandler extends BetterDefaultHandler
{
    /** Gets the offering. */
    private final List<Offering> myOfferings = New.list();

    /** The current offering. */
    private MutableOffering myCurrentOffering;

    /**
     * Parses the XML stream into a list of objects.
     *
     * @param stream the input stream
     * @return the list of FeatureMember objects
     * @throws IOException if a problem occurs parsing the stream
     */
    public static List<Offering> parse(InputStream stream) throws IOException
    {
        List<Offering> offerings;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try
        {
            SAXParser saxParser = factory.newSAXParser();
            GetCapabilitiesHandler handler = new GetCapabilitiesHandler();
            saxParser.parse(stream, handler);
            offerings = handler.getOfferings();
        }
        catch (ParserConfigurationException | SAXException e)
        {
            throw new IOException(e);
        }

        return offerings;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        super.startElement(uri, localName, qName, attributes);
        if ("ObservationOffering".equals(localName))
        {
            myCurrentOffering = new MutableOffering();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if (myCurrentOffering != null)
        {
            if ("identifier".equals(localName))
            {
                myCurrentOffering.setId(getCurrentValue());
            }
            else if ("name".equals(localName))
            {
                myCurrentOffering.setName(getCurrentValue());
            }
            else if ("description".equals(localName))
            {
                myCurrentOffering.setDescription(getCurrentValue());
            }
            else if ("procedure".equals(localName))
            {
                myCurrentOffering.setProcedure(getCurrentValue());
            }
            else if ("observableProperty".equals(localName))
            {
                myCurrentOffering.getObservableProperties().add(getCurrentValue());
            }
            else if ("beginPosition".equals(localName))
            {
                myCurrentOffering.setStartDate(ParsingUtils.parseDate(getCurrentValue(), true));
            }
            else if ("endPosition".equals(localName))
            {
                myCurrentOffering.setEndDate(ParsingUtils.parseDate(getCurrentValue(), false));
            }
            else if ("ObservationOffering".equals(localName))
            {
                myOfferings.add(new Offering(myCurrentOffering));
                myCurrentOffering = null;
            }
        }
        super.endElement(uri, localName, qName);
    }

    /**
     * Gets the offerings.
     *
     * @return the offerings
     */
    public List<Offering> getOfferings()
    {
        return myOfferings;
    }
}
