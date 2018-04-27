package io.opensphere.wps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.wfs.layer.WFSDataType;
import io.opensphere.wps.request.WpsProcessConfiguration;
import net.opengis.ows._110.CodeType;
import net.opengis.wps._100.DataInputsType;
import net.opengis.wps._100.DataType;
import net.opengis.wps._100.Execute;
import net.opengis.wps._100.InputType;
import net.opengis.wps._100.LiteralDataType;
import net.opengis.wps._100.OutputDefinitionType;
import net.opengis.wps._100.ResponseFormType;

/**
 * Creates the request stream to send to the server to execute a WPS process.
 */
public class PayloadCreator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PayloadCreator.class);

    /**
     * Creates a new {@link Execute} payload {@link InputStream} that can be
     * transmitted as part of an WPS execute request. The supplied parameters
     * are used to generate a new Execute object, which is then serialized into
     * an input stream.
     *
     * @param configuration the configuration from which the set of parameters
     *            to encode into the payload are extracted.
     * @param resultType The {@link WFSDataType} with which the results will be
     *            associated, modified here by the supplied configuration's
     *            TYPENAME input.
     * @return an input stream in which the request payload is serialized.
     * @throws IOException if the request cannot be serialized to a stream.
     */
    public InputStream createPayload(WpsProcessConfiguration configuration, WFSDataType resultType) throws IOException
    {
        Execute executePayload = new Execute();
        executePayload.setService("WPS");
        executePayload.setVersion("1.0.0");

        CodeType identifier = new CodeType();
        identifier.setValue(configuration.getProcessIdentifier());
        executePayload.setIdentifier(identifier);

        DataInputsType inputs = new DataInputsType();
        Set<String> serverColumns = getServerColumns(configuration);
        for (Entry<String, String> entry : configuration.getInputs().entrySet())
        {
            if (serverColumns.contains(entry.getKey()))
            {
                List<InputType> input = generateInput(entry.getKey(), entry.getValue());
                inputs.getInput().addAll(input);

                if (StringUtils.equals("TYPENAME", entry.getKey()))
                {
                    resultType.setTypeName(input.get(0).getData().getLiteralData().getValue());
                }
            }
        }
        executePayload.setDataInputs(inputs);

        ResponseFormType response = new ResponseFormType();
        OutputDefinitionType outputDefinition = new OutputDefinitionType();

        CodeType outputIdentifier = new CodeType();
        outputIdentifier.setValue("OutputData");
        outputDefinition.setIdentifier(outputIdentifier);
        response.setRawDataOutput(outputDefinition);

        executePayload.setResponseForm(response);

        return serializeToStream(executePayload);
    }

    /**
     * Generates a new input type for the supplied key / value pair.
     *
     * @param key the name of the identifier to apply to the input.
     * @param value the value of the input.
     * @return a new {@link InputType} generated for the supplied key / value
     *         pair.
     */
    private List<InputType> generateInput(String key, String value)
    {
        List<InputType> inputs = New.list();

        String[] values = new String[] { value };
        if ("BBOX".equalsIgnoreCase(key))
        {
            values = value.split(" ");
        }
        else if ("FILTER".equalsIgnoreCase(key))
        {
            try
            {
                values[0] = FilterCreator.bboxToFilterString(value);
            }
            catch (IllegalArgumentException | JAXBException e)
            {
                LOGGER.error(e, e);
            }
        }

        for (String aValue : values)
        {
            InputType input = new InputType();
            CodeType inputIdentifier = new CodeType();
            inputIdentifier.setValue(key);
            input.setIdentifier(inputIdentifier);

            DataType data = new DataType();
            LiteralDataType literal = new LiteralDataType();

            literal.setValue(aValue);
            data.setLiteralData(literal);
            input.setData(data);

            inputs.add(input);
        }

        return inputs;
    }

    /**
     * Gets the data inputs the server cares about.
     *
     * @param configuration Contains information about the wps process.
     * @return The data inputs the server cares about.
     */
    private Set<String> getServerColumns(WpsProcessConfiguration configuration)
    {
        Set<String> serverColumns = New.set();

        if (configuration.getProcessDescription().getDataInputs() != null
                && configuration.getProcessDescription().getDataInputs().getInput() != null)
        {
            serverColumns = configuration.getProcessDescription().getDataInputs().getInput().stream()
                    .map(i -> i.getIdentifier().getValue()).collect(Collectors.toSet());
        }

        return serverColumns;
    }

    /**
     * Serializes the supplied {@link Execute} JAXB payload to an
     * {@link InputStream} that can be processed with a request.
     *
     * @param payload the payload to serialize to an {@link InputStream}.
     * @return an {@link InputStream} in which the supplied JAXB object has been
     *         serialized.
     * @throws IOException if the supplied object cannot be serialized.
     */
    private InputStream serializeToStream(Execute payload) throws IOException
    {
        ByteArrayInputStream bodyStream;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            XMLUtilities.writeXMLObject(payload, out);
            out.flush();
            bodyStream = new ByteArrayInputStream(out.toByteArray());
        }
        catch (JAXBException | IOException e)
        {
            throw new IOException("Unable to serialize execute request to stream.", e);
        }
        return bodyStream;
    }
}
