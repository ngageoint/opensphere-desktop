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
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.MutableOffering;
import io.opensphere.osh.model.Output;

/** SOS get capabilities SAX handler. */
public class DescribeSensorHandler extends BetterDefaultHandler
{
    /** Gets the outputs. */
    private final List<Output> myOutputs = New.list();

    /** The current output. */
    private Output myCurrentOutput;

    /** The current output. */
    private Field myCurrentField;

    /** The starting tag name for the field. */
    private String myStartTagName;

    /** Abuse the offering object because it has some helpful methods. */
    private final MutableOffering myOffering = new MutableOffering();

    /**
     * Parses the XML stream into a list of objects.
     *
     * @param stream the input stream
     * @return the list of FeatureMember objects
     * @throws IOException if a problem occurs parsing the stream
     */
    public static List<Output> parse(InputStream stream) throws IOException
    {
        List<Output> outputs;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try
        {
            SAXParser saxParser = factory.newSAXParser();
            DescribeSensorHandler handler = new DescribeSensorHandler();
            saxParser.parse(stream, handler);
            outputs = handler.getOutputs();
        }
        catch (ParserConfigurationException | SAXException e)
        {
            throw new IOException(e);
        }

        return outputs;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        super.startElement(uri, localName, qName, attributes);

        if (myCurrentOutput != null)
        {
            String name = attributes.getValue("name");
            if (name != null)
            {
                myStartTagName = qName;
                myCurrentField = new Field(name);
            }
            else
            {
                String definition = attributes.getValue("definition");
                if (definition != null)
                {
                    if (myCurrentField != null)
                    {
                        myCurrentField.setProperty(definition);
                    }
                    myCurrentOutput.getProperties().add(definition);
                }
            }
        }
        else if ("output".equals(localName))
        {
            myCurrentOutput = new Output(attributes.getValue("name"));
            myCurrentOutput.setSpan(myOffering.getSpan());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if (myCurrentOutput != null)
        {
            if ("output".equals(localName))
            {
                myOutputs.add(myCurrentOutput);
                myCurrentOutput = null;
            }
            else if (myCurrentField != null)
            {
                if (qName.equals(myStartTagName))
                {
                    myCurrentOutput.getFields().add(myCurrentField);
                    myCurrentField = null;
                }
                else if ("label".equals(localName))
                {
                    myCurrentField.setLabel(getCurrentValue());
                }
            }
        }
        else
        {
            if ("beginPosition".equals(localName))
            {
                myOffering.setStartDate(ParsingUtils.parseDate(getCurrentValue(), true));
            }
            else if ("endPosition".equals(localName))
            {
                myOffering.setEndDate(ParsingUtils.parseDate(getCurrentValue(), false));
            }
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * Gets the outputs.
     *
     * @return the outputs
     */
    public List<Output> getOutputs()
    {
        return myOutputs;
    }
}
