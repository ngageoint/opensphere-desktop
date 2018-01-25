package io.opensphere.wps.streaming.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.datafilter.impl.ImmutableDataFilter;
import io.opensphere.core.datafilter.impl.ImmutableDataFilterCriteria;
import io.opensphere.core.datafilter.impl.ImmutableDataFilterGroup;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import net.opengis.ogc._110.FilterType;
import net.opengis.wps._100.DataInputsType;
import net.opengis.wps._100.Execute;

/**
 * Tests the {@link FilterHandler} class.
 */
public class FilterHandlerTest
{
    /**
     * Tests and verifies a serialized ogc filter.
     *
     * @throws JAXBException Bad jaxb.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSerializeFilter() throws JAXBException, IOException
    {
        FilterHandler handler = new FilterHandler();

        DataInputsType dataInputs = new DataInputsType();

        ImmutableDataFilterCriteria criteria = new ImmutableDataFilterCriteria("column1", "value1", Conditional.EQ, null);
        ImmutableDataFilterGroup group = new ImmutableDataFilterGroup("group", Logical.AND, New.list(criteria),
                New.<DataFilterGroup>list(), null);
        ImmutableDataFilter filter = new ImmutableDataFilter("filter", "type", New.list("column1"), group, null, null);

        handler.serializeFilter(dataInputs, filter, null, "layer");

        Execute execute = new Execute();
        execute.setDataInputs(dataInputs);

        InputStream stream = XMLUtilities.writeXMLObjectToInputStreamSync(execute, Execute.class, FilterType.class);

        String xml = new StreamReader(stream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

        // NOTE: Do not change the format of the expected XML. It is not valid XML, as it does not contain the namespace
        // mappings. The order namespace mappings is not consistent from execution to execution, so the test will remove the
        // mappings from the returned string before comparison.
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:Execute>\n" + "   <ns2:DataInputs>\n"
                + "      <ns2:Input>\n" + "         <ns1:Identifier>layerFilter</ns1:Identifier>\n" + "         <ns2:Data>\n"
                + "            <ns2:ComplexData mimeType=\"text/xml\">\n" + "               <ns5:Filter>\n"
                + "                  <ns5:And>\n" + "                     <ns5:And>\n"
                + "                        <ns5:PropertyIsEqualTo>\n"
                + "                           <ns5:PropertyName>column1</ns5:PropertyName>\n"
                + "                           <ns5:Literal>value1</ns5:Literal>\n"
                + "                        </ns5:PropertyIsEqualTo>\n" + "                     </ns5:And>\n"
                + "                  </ns5:And>\n" + "               </ns5:Filter>\n" + "            </ns2:ComplexData>\n"
                + "         </ns2:Data>\n" + "      </ns2:Input>\n" + "   </ns2:DataInputs>\n" + "</ns2:Execute>\n";

        // this is a really weird way to test the results, but the JAXB Namespace support doesn't always add the namespaces in
        // the same order to the 'Execute' tag. Instead, we'll replace known sequences of text, and then check to determine if the
        // results are matching with a known modified sequence.

        xml = xml.replace("xmlns:ns1=\"http://www.opengis.net/ows/1.1\"", "");
        xml = xml.replace("xmlns:ns2=\"http://www.opengis.net/wps/1.0.0\"", "");
        xml = xml.replace("xmlns:ns3=\"http://www.w3.org/1999/xlink\"", "");
        xml = xml.replace("xmlns:ns5=\"http://www.opengis.net/ogc\"", "");
        xml = xml.replace("xmlns:ns6=\"http://www.opengis.net/gml\"", "");
        xml = xml.replace("xmlns:ns7=\"http://www.w3.org/2001/SMIL20/\"", "");
        xml = xml.replace("xmlns:ns8=\"http://www.w3.org/2001/SMIL20/Language\"", "");
        xml = xml.replaceAll("\\<ns2:Execute\\s+\\>", "<ns2:Execute>");

        assertEquals(expectedXml, xml);
    }
}
