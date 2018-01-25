package io.opensphere.osh.sos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import io.opensphere.core.util.collections.New;
import io.opensphere.osh.model.ArrayField;
import io.opensphere.osh.model.BinaryEncoding;
import io.opensphere.osh.model.Field;
import io.opensphere.osh.model.Output;
import io.opensphere.osh.model.TextEncoding;
import io.opensphere.osh.model.VectorField;

/** SOS get result template SAX handler. */
public class GetResultTemplateHandler extends BetterDefaultHandler
{
    /** The output. */
    private final Output myOutput = new Output("name");

    /** The field stack. */
    private final Deque<Field> myFieldStack = new ArrayDeque<>();

    /**
     * Parses the XML stream into an output object.
     *
     * @param stream the input stream
     * @return the output
     * @throws IOException if a problem occurs parsing the stream
     */
    public static Output parse(InputStream stream) throws IOException
    {
        Output output;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        try
        {
            SAXParser saxParser = factory.newSAXParser();
            GetResultTemplateHandler handler = new GetResultTemplateHandler();
            saxParser.parse(stream, handler);

            output = handler.getOutput();
        }
        catch (ParserConfigurationException | SAXException e)
        {
            throw new IOException(e);
        }

        return output;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        super.startElement(uri, localName, qName, attributes);

        switch (localName)
        {
            case "field":
            case "elementType":
            case "coordinate":
            {
                Field field = new Field(attributes.getValue("name"));
                field.setFullPath(getCurrentNamePath());
                myFieldStack.push(field);
            }
                break;
            case "DataArray":
                myFieldStack.push(new ArrayField(myFieldStack.pop()));
                break;
            case "elementCount":
                if (!myFieldStack.isEmpty())
                {
                    Field arrayField = myFieldStack.peek();
                    if (arrayField instanceof ArrayField)
                    {
                        String href = attributes.getValue("xlink:href");
                        if (href != null)
                        {
                            String fieldRef = href.substring(1);
                            Field field = myOutput.getFields().stream().filter(f -> fieldRef.equals(f.getId())).findAny()
                                    .orElse(null);
                            ((ArrayField)arrayField).setCountField(field.getName());
                        }
                    }
                }
                break;
            case "Vector":
                myFieldStack.push(new VectorField(myFieldStack.pop()));
                break;
            case "BinaryEncoding":
                myOutput.setEncoding(new BinaryEncoding());
                break;
            case "TextEncoding":
                myOutput.setEncoding(new TextEncoding());
                break;
            case "Component":
            case "Block":
                String ref = attributes.getValue("ref");
                if (ref != null)
                {
                    Field field = myOutput.getAllFields().stream().filter(f -> f.getFullPath().equals(ref)).findAny()
                            .orElse(null);

                    String dataType = attributes.getValue("dataType");
                    field.setDataType(dataType != null ? dataType : attributes.getValue("compression"));

                    BinaryEncoding encoding = (BinaryEncoding)myOutput.getEncoding();
                    if (encoding != null)
                    {
                        encoding.addField(field.getName(), field.getDataType());
                    }
                }
                break;
            default:
        }

        String value;
        if ((value = attributes.getValue("definition")) != null)
        {
            if (!myFieldStack.isEmpty())
            {
                myFieldStack.peek().setProperty(value);
            }
            myOutput.getProperties().add(value);
        }
        else if ((value = attributes.getValue("id")) != null)
        {
            if (!myFieldStack.isEmpty())
            {
                myFieldStack.peek().setId(value);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        switch (localName)
        {
            case "field":
                myOutput.getFields().add(myFieldStack.pop());
                break;
            case "elementType":
            {
                Field field = myFieldStack.pop();
                Field parent = myFieldStack.peek();
                if (parent instanceof ArrayField)
                {
                    ((ArrayField)parent).setField(field);
                }
            }
                break;
            case "coordinate":
            {
                Field field = myFieldStack.pop();
                Field parent = myFieldStack.peek();
                if (parent instanceof VectorField)
                {
                    ((VectorField)parent).getFields().add(field);
                }
            }
                break;
            default:
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * Gets the output.
     *
     * @return the output
     */
    public Output getOutput()
    {
        return myOutput;
    }

    /**
     * Gets the current name path.
     *
     * @return the current name path
     */
    private String getCurrentNamePath()
    {
        List<SaxElement> list = New.list(getElementStack());
        Collections.reverse(list);
        return list.stream().map(e -> e.getAttributes().get("name")).filter(n -> n != null)
                .collect(Collectors.joining("/", "/", ""));
    }

//    private boolean hasChildren(Field field)
//    {
//        return myOutput.getFields().stream()
//                .anyMatch(f -> f.getFullPath().contains(field.getFullPath()) && !f.getFullPath().equals(field.getFullPath()));
//    }
}
