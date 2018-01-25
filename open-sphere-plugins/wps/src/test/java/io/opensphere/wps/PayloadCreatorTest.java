package io.opensphere.wps;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.request.WpsProcessConfiguration;
import net.opengis.ows._110.CodeType;
import net.opengis.wps._100.InputDescriptionType;
import net.opengis.wps._100.ProcessDescriptionType;
import net.opengis.wps._100.ProcessDescriptionType.DataInputs;

/**
 * Unit test for {@link PayloadCreator}.
 */
public class PayloadCreatorTest
{
    /**
     * Tests creating the payload.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testCreatePayload() throws IOException
    {
        String input1 = "column1";
        String input2 = "column2";
        String input3 = "column3";

        ProcessDescriptionType description = new ProcessDescriptionType();
        DataInputs dataInputs = new DataInputs();
        description.setDataInputs(dataInputs);

        InputDescriptionType descriptionType = new InputDescriptionType();
        CodeType codeType = new CodeType();
        codeType.setValue(input1);
        descriptionType.setIdentifier(codeType);
        dataInputs.getInput().add(descriptionType);

        descriptionType = new InputDescriptionType();
        codeType = new CodeType();
        codeType.setValue(input3);
        descriptionType.setIdentifier(codeType);
        dataInputs.getInput().add(descriptionType);

        WpsProcessConfiguration configuration = new WpsProcessConfiguration("serverId", description);
        configuration.setProcessIdentifier("processId");
        configuration.getInputs().put(input1, "value1");
        configuration.getInputs().put(input2, "value2");
        configuration.getInputs().put(input3, "value3");

        PayloadCreator creator = new PayloadCreator();
        InputStream stream = creator.createPayload(configuration, null);

        StreamReader reader = new StreamReader(stream);
        String actual = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

        String expected = XMLUtilities.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<ns2:Execute xmlns:ns2=\"http://www.opengis.net/wps/1.0.0\" "
                + "xmlns:ns1=\"http://www.opengis.net/ows/1.1\" xmlns:ns3=\"http://www.w3.org/1999/xlink\" "
                + "service=\"WPS\" version=\"1.0.0\"><ns1:Identifier>processId</ns1:Identifier><ns2:DataInputs>"
                + "<ns2:Input><ns1:Identifier>column1</ns1:Identifier><ns2:Data><ns2:LiteralData>value1</ns2:LiteralData></ns2:Data></ns2:Input>"
                + "<ns2:Input><ns1:Identifier>column3</ns1:Identifier><ns2:Data><ns2:LiteralData>value3</ns2:LiteralData></ns2:Data></ns2:Input>"
                + "</ns2:DataInputs>"
                + "<ns2:ResponseForm><ns2:RawDataOutput><ns1:Identifier>OutputData</ns1:Identifier></ns2:RawDataOutput></ns2:ResponseForm></ns2:Execute>");

        assertEquals(expected, actual);
    }

    /**
     * Tests creating the payload.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testCreatePayloadMultiBox() throws IOException
    {
        String input1 = "column1";
        String input2 = "column2";
        String input3 = "BBOX";

        ProcessDescriptionType description = new ProcessDescriptionType();
        DataInputs dataInputs = new DataInputs();
        description.setDataInputs(dataInputs);

        InputDescriptionType descriptionType = new InputDescriptionType();
        CodeType codeType = new CodeType();
        codeType.setValue(input1);
        descriptionType.setIdentifier(codeType);
        dataInputs.getInput().add(descriptionType);

        descriptionType = new InputDescriptionType();
        codeType = new CodeType();
        codeType.setValue(input3);
        descriptionType.setIdentifier(codeType);
        dataInputs.getInput().add(descriptionType);

        WpsProcessConfiguration configuration = new WpsProcessConfiguration("serverId", description);
        configuration.setProcessIdentifier("processId");
        configuration.getInputs().put(input1, "value1");
        configuration.getInputs().put(input2, "value2");
        configuration.getInputs().put(input3, "10,11,12,13 14,15,16,17");

        PayloadCreator creator = new PayloadCreator();
        InputStream stream = creator.createPayload(configuration, null);

        StreamReader reader = new StreamReader(stream);
        String actual = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

        String expected = XMLUtilities.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<ns2:Execute xmlns:ns2=\"http://www.opengis.net/wps/1.0.0\" "
                + "xmlns:ns1=\"http://www.opengis.net/ows/1.1\" xmlns:ns3=\"http://www.w3.org/1999/xlink\" "
                + "service=\"WPS\" version=\"1.0.0\"><ns1:Identifier>processId</ns1:Identifier><ns2:DataInputs>"
                + "<ns2:Input><ns1:Identifier>column1</ns1:Identifier><ns2:Data><ns2:LiteralData>value1</ns2:LiteralData></ns2:Data></ns2:Input>"
                + "<ns2:Input><ns1:Identifier>BBOX</ns1:Identifier><ns2:Data><ns2:LiteralData>10,11,12,13</ns2:LiteralData></ns2:Data></ns2:Input>"
                + "<ns2:Input><ns1:Identifier>BBOX</ns1:Identifier><ns2:Data><ns2:LiteralData>14,15,16,17</ns2:LiteralData></ns2:Data></ns2:Input>"
                + "</ns2:DataInputs>"
                + "<ns2:ResponseForm><ns2:RawDataOutput><ns1:Identifier>OutputData</ns1:Identifier></ns2:RawDataOutput></ns2:ResponseForm></ns2:Execute>");

        assertEquals(expected, actual);
    }
}
