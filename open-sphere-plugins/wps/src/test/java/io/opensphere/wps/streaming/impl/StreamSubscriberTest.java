package io.opensphere.wps.streaming.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;
import org.w3c.dom.Node;

import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.datafilter.impl.ImmutableDataFilter;
import io.opensphere.core.datafilter.impl.ImmutableDataFilterCriteria;
import io.opensphere.core.datafilter.impl.ImmutableDataFilterGroup;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.streaming.StreamingConstants;
import io.opensphere.wps.streaming.SubscriptionContext;
import net.opengis.ogc._110.BinaryComparisonOpType;
import net.opengis.ogc._110.BinaryLogicOpType;
import net.opengis.ogc._110.FilterType;
import net.opengis.ogc._110.LiteralType;
import net.opengis.ogc._110.PropertyNameType;
import net.opengis.wps._100.Execute;
import net.opengis.wps._100.InputType;

/**
 * Tests the {@link StreamSubscriber} class.
 */
public class StreamSubscriberTest
{
    /**
     * The expected filtered column name.
     */
    private static final String ourColumnName = "column1";

    /**
     * The expected filtered column value.
     */
    private static final String ourColumnValue = "value1";

    /**
     * The expected layer name.
     */
    private static final String ourLayerName = "streamLayer";

    /**
     * The expected wps url.
     */
    private static final String ourWpsUrl = "https://somehost/ogc/wpsServer";

    /**
     * Verifies we can deserialize this request.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testDeserializeRequest() throws JAXBException
    {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<ns2:Execute xmlns:ns2=\"http://www.opengis.net/wps/1.0.0\" xmlns:ns6=\"http://www.opengis.net/gml\""
                + " xmlns:ns5=\"http://www.opengis.net/ogc\""
                + " xmlns:ns8=\"http://www.w3.org/2001/SMIL20/Language\" xmlns:ns7=\"http://www.w3.org/2001/SMIL20/\""
                + " xmlns:ns1=\"http://www.opengis.net/ows/1.1\" xmlns:ns3=\"http://www.w3.org/1999/xlink\">"
                + "<ns1:Identifier>SubscribeToNRTLayerProcess</ns1:Identifier><ns2:DataInputs><ns2:Input><ns1:Identifier>layerName</ns1:Identifier>"
                + "<ns2:Data><ns2:LiteralData>streamLayer</ns2:LiteralData></ns2:Data></ns2:Input><ns2:Input>"
                + "<ns1:Identifier>outputFormat</ns1:Identifier><ns2:Data><ns2:ComplexData mimeType=\"text/xml; subtype=gml/3.1.1\"/>"
                + "</ns2:Data></ns2:Input><ns2:Input><ns1:Identifier>layerFilter</ns1:Identifier><ns2:Data>"
                + "<ns2:ComplexData mimeType=\"text/xml\"><ns5:Filter><ns5:And><ns5:And><ns5:PropertyIsEqualTo>"
                + "<ns5:PropertyName>column1</ns5:PropertyName><ns5:Literal>value1</ns5:Literal></ns5:PropertyIsEqualTo>"
                + "</ns5:And></ns5:And></ns5:Filter></ns2:ComplexData></ns2:Data></ns2:Input></ns2:DataInputs><ns2:ResponseForm>"
                + "<ns2:RawDataOutput><ns1:Identifier>subscriptionConfig</ns1:Identifier></ns2:RawDataOutput></ns2:ResponseForm></ns2:Execute>";

        ByteArrayInputStream stream = new ByteArrayInputStream(request.getBytes(StringUtilities.DEFAULT_CHARSET));

        Execute execute = XMLUtilities.readXMLObject(stream, Execute.class, New.<Class<?>>list(Execute.class, FilterType.class));

        assertEquals("WPS", execute.getService());
        assertEquals("1.0.0", execute.getVersion());

        assertEquals("subscriptionConfig", execute.getResponseForm().getRawDataOutput().getIdentifier().getValue());
        assertEquals("SubscribeToNRTLayerProcess", execute.getIdentifier().getValue());

        assertEquals("layerName", execute.getDataInputs().getInput().get(0).getIdentifier().getValue());
        assertEquals(ourLayerName, execute.getDataInputs().getInput().get(0).getData().getLiteralData().getValue());

        assertEquals("outputFormat", execute.getDataInputs().getInput().get(1).getIdentifier().getValue());
        assertEquals(StreamingConstants.OUTPUT_MIME_TYPE,
                execute.getDataInputs().getInput().get(1).getData().getComplexData().getMimeType());

        InputType filterInput = execute.getDataInputs().getInput().get(2);
        assertEquals("layerFilter", filterInput.getIdentifier().getValue());
        FilterType filter = XMLUtilities.readXMLObject((Node)filterInput.getData().getComplexData().getContent().get(0),
                FilterType.class);

        assertEquals("And", filter.getLogicOps().getName().getLocalPart());
        assertEquals("And", ((BinaryLogicOpType)filter.getLogicOps().getValue()).getComparisonOpsOrSpatialOpsOrLogicOps().get(0)
                .getName().getLocalPart());
        BinaryLogicOpType opType = (BinaryLogicOpType)((BinaryLogicOpType)filter.getLogicOps().getValue())
                .getComparisonOpsOrSpatialOpsOrLogicOps().get(0).getValue();

        assertEquals("PropertyIsEqualTo", opType.getComparisonOpsOrSpatialOpsOrLogicOps().get(0).getName().getLocalPart());

        List<JAXBElement<?>> expression = ((BinaryComparisonOpType)opType.getComparisonOpsOrSpatialOpsOrLogicOps().get(0)
                .getValue()).getExpression();

        PropertyNameType columnName = (PropertyNameType)expression.get(0).getValue();
        assertEquals(ourColumnName, columnName.getValue());

        LiteralType columnValue = (LiteralType)expression.get(1).getValue();
        assertEquals(ourColumnValue, columnValue.getContent().get(0).toString());
    }

    /**
     * Tests subscribing to a stream.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSubscribeToStream() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createRegistry(support, false, false);

        support.replayAll();

        StreamSubscriber subscriber = new StreamSubscriber(registry);
        SubscriptionContext context = subscriber.subscribeToStream(new URL(ourWpsUrl), ourLayerName, null, null);

        assertEquals("29d25e04-6884-4ed5-88e9-1a93773a6743", context.getStreamId().toString());
        assertEquals("https://somehost/nrt/streamingServlet?filterId=29d25e04-6884-4ed5-88e9-1a93773a6743&pollInterval=5000",
                context.getStreamUrl().toString());
        assertEquals("filterId", context.getFilterIdParameterName());
        assertEquals(5000, context.getPollInterval());

        support.verifyAll();
    }

    /**
     * Tests subscribing to a stream and server returns a bad response.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSubscribeToStreamBadResponse() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createRegistry(support, true, false);

        support.replayAll();

        StreamSubscriber subscriber = new StreamSubscriber(registry);
        SubscriptionContext context = subscriber.subscribeToStream(new URL(ourWpsUrl), ourLayerName, null, null);

        assertNull(context);

        support.verifyAll();
    }

    /**
     * Tests subscribing to a stream.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSubscribeToStreamWithFilter() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createRegistry(support, false, true);

        support.replayAll();

        ImmutableDataFilterCriteria criteria = new ImmutableDataFilterCriteria(ourColumnName, ourColumnValue, Conditional.EQ,
                null);
        ImmutableDataFilterGroup group = new ImmutableDataFilterGroup("group", Logical.AND, New.list(criteria),
                New.<DataFilterGroup>list(), null);
        ImmutableDataFilter filter = new ImmutableDataFilter("filter", "type", New.list("column1"), group, null, null);

        StreamSubscriber subscriber = new StreamSubscriber(registry);
        SubscriptionContext context = subscriber.subscribeToStream(new URL(ourWpsUrl), ourLayerName, filter, null);

        assertEquals("29d25e04-6884-4ed5-88e9-1a93773a6743", context.getStreamId().toString());
        assertEquals("https://somehost/nrt/streamingServlet?filterId=29d25e04-6884-4ed5-88e9-1a93773a6743&pollInterval=5000",
                context.getStreamUrl().toString());
        assertEquals("filterId", context.getFilterIdParameterName());

        support.verifyAll();
    }

    /**
     * Creates the easy mocked {@link ServerProviderRegistry}.
     *
     * @param support Used to create the mock.
     * @param isBadResponse True if the server should return a bad response
     *            code.
     * @param hasFilter Indicates if there should be a filter expected.
     * @return The {@link ServerProviderRegistry}.
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    private ServerProviderRegistry createRegistry(EasyMockSupport support, final boolean isBadResponse, final boolean hasFilter)
        throws IOException, URISyntaxException
    {
        IAnswer<CancellableInputStream> answer = new IAnswer<CancellableInputStream>()
        {
            @Override
            public CancellableInputStream answer() throws JAXBException
            {
                InputStream stream = (InputStream)EasyMock.getCurrentArguments()[1];
                ResponseValues responseValues = (ResponseValues)EasyMock.getCurrentArguments()[2];

                byte[] responseBytes = new byte[0];
                if (isBadResponse)
                {
                    responseValues.setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED);
                }
                else
                {
                    responseValues.setResponseCode(HttpURLConnection.HTTP_OK);
                    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><configuration><url base=\"https://somehost/nrt/streamingServlet\">"
                            + "<requestParameter><!--Required. The name of the message container that contains the queue of messages."
                            + "  This value was provided from the server when a subscription request was initiated.-->filterId</requestParameter>"
                            + "<requestParameter><!--Optional. This setting will tell the server how long (in milliseconds)"
                            + " to keep the socket connection  open to stream messages. Defaults to 30000 milliseconds.-->pollInterval</requestParameter>"
                            + "<requestParameter><!--Optional. If this is set to true then only debug information for all message containers will be returned."
                            + "  Default is false.-->debug</requestParameter></url>"
                            + "<filterId>29d25e04-6884-4ed5-88e9-1a93773a6743</filterId></configuration>";

                    responseBytes = xml.getBytes(StringUtilities.DEFAULT_CHARSET);
                }

                Execute execute = XMLUtilities.readXMLObject(stream, Execute.class,
                        New.<Class<?>>list(Execute.class, FilterType.class));

                assertEquals("WPS", execute.getService());
                assertEquals("1.0.0", execute.getVersion());

                assertEquals("subscriptionConfig", execute.getResponseForm().getRawDataOutput().getIdentifier().getValue());
                assertEquals("SubscribeToNRTLayerProcess", execute.getIdentifier().getValue());

                assertEquals("layerName", execute.getDataInputs().getInput().get(0).getIdentifier().getValue());
                assertEquals(ourLayerName, execute.getDataInputs().getInput().get(0).getData().getLiteralData().getValue());

                assertEquals("outputFormat", execute.getDataInputs().getInput().get(1).getIdentifier().getValue());
                assertEquals(StreamingConstants.OUTPUT_MIME_TYPE,
                        execute.getDataInputs().getInput().get(1).getData().getComplexData().getMimeType());

                if (hasFilter)
                {
                    InputType filterInput = execute.getDataInputs().getInput().get(2);
                    assertEquals("layerFilter", filterInput.getIdentifier().getValue());
                    FilterType filter = XMLUtilities
                            .readXMLObject((Node)filterInput.getData().getComplexData().getContent().get(0), FilterType.class);

                    assertEquals("And", filter.getLogicOps().getName().getLocalPart());
                    assertEquals("And", ((BinaryLogicOpType)filter.getLogicOps().getValue())
                            .getComparisonOpsOrSpatialOpsOrLogicOps().get(0).getName().getLocalPart());
                    BinaryLogicOpType opType = (BinaryLogicOpType)((BinaryLogicOpType)filter.getLogicOps().getValue())
                            .getComparisonOpsOrSpatialOpsOrLogicOps().get(0).getValue();

                    assertEquals("PropertyIsEqualTo",
                            opType.getComparisonOpsOrSpatialOpsOrLogicOps().get(0).getName().getLocalPart());

                    List<JAXBElement<?>> expression = ((BinaryComparisonOpType)opType.getComparisonOpsOrSpatialOpsOrLogicOps()
                            .get(0).getValue()).getExpression();

                    PropertyNameType columnName = (PropertyNameType)expression.get(0).getValue();
                    assertEquals(ourColumnName, columnName.getValue());

                    LiteralType columnValue = (LiteralType)expression.get(1).getValue();
                    assertEquals(ourColumnValue, columnValue.getContent().get(0).toString());
                }

                ByteArrayInputStream responseStream = new ByteArrayInputStream(responseBytes);
                return new CancellableInputStream(responseStream, null);
            }
        };

        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendPost(EasyMockHelper.eq(new URL(ourWpsUrl)), EasyMock.isA(InputStream.class),
                EasyMock.isA(ResponseValues.class))).andAnswer(answer);

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMockHelper.eq(new URL(ourWpsUrl)))).andReturn(server);

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(registry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(provider);

        return registry;
    }
}
