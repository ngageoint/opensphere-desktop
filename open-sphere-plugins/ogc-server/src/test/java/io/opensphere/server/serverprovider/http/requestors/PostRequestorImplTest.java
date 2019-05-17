package io.opensphere.server.serverprovider.http.requestors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.CookieStore;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;
import com.bitsys.common.http.entity.FormEntity;
import com.bitsys.common.http.entity.HttpEntity;
import com.bitsys.common.http.header.ContentType;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpResponse;
import com.google.common.collect.ListMultimap;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Tests the GetRequestor class.
 */
@SuppressWarnings("boxing")
public class PostRequestorImplTest
{
    /**
     * The expected headers.
     */
    private static final Map<String, Collection<String>> ourHeaderValues = New.map();

    /**
     * The expected content length.
     */
    private static final long ourLength = 100;

    /**
     * The test url.
     */
    private static final String ourTestUrl = "http://somehost/getStuff?param=stuff";

    /**
     * Converts an input stream into a string.
     *
     * @param stream the input stream
     * @return the string
     * @throws IOException If there is an exception reading from the stream.
     */
    private static String streamToString(final InputStream stream) throws IOException
    {
        return new StreamReader(stream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
    }

    /**
     * Tests the post request.
     *
     * @throws IOException Bad io.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testPostRequest() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final URL url = new URL(ourTestUrl);

        final ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
        final ByteArrayInputStream postStream = new ByteArrayInputStream(new byte[0]);

        final HttpResponse response = createResponse(support, stream, false);
        final HttpClient client = createClient(support, response, url.toURI(), postStream, null, ContentType.APPLICATION_XML,
                false);
        final EventManager eventManager = support.createNiceMock(EventManager.class);
        final NetworkConfigurationManager networkConfigurationManager = support.createNiceMock(NetworkConfigurationManager.class);

        support.replayAll();

        final ResponseValues responseValues = new ResponseValues();

        final PostRequestorImpl requestor = new PostRequestorImpl(client, new HeaderConstantsMock(), eventManager, networkConfigurationManager);
        final CancellableInputStream actual = requestor.sendPost(url, postStream, responseValues);

        assertEquals(stream, actual.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_OK, responseValues.getResponseCode());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());

        support.verifyAll();
    }

    /**
     * Tests the post request with a different content type..
     *
     * @throws IOException Bad io.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testPostRequestContentType() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final URL url = new URL(ourTestUrl);

        final ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
        final ByteArrayInputStream postStream = new ByteArrayInputStream(new byte[0]);

        final HttpResponse response = createResponse(support, stream, false);
        final HttpClient client = createClient(support, response, url.toURI(), postStream, null, ContentType.APPLICATION_JSON,
                false);
        final EventManager eventManager = support.createNiceMock(EventManager.class);
        final NetworkConfigurationManager networkConfigurationManager = support.createNiceMock(NetworkConfigurationManager.class);

        support.replayAll();

        final ResponseValues responseValues = new ResponseValues();

        final PostRequestorImpl requestor = new PostRequestorImpl(client, new HeaderConstantsMock(), eventManager, networkConfigurationManager);
        final CancellableInputStream actual = requestor.sendPost(url, postStream, responseValues, ContentType.APPLICATION_JSON);

        assertEquals(stream, actual.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_OK, responseValues.getResponseCode());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());

        support.verifyAll();
    }

    /**
     * Tests the post request.
     *
     * @throws IOException Bad io.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testPostRequestForm() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final URL url = new URL(ourTestUrl);

        final ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);

        final Map<String, String> formData = New.map();
        formData.put("key", "value");

        final HttpResponse response = createResponse(support, stream, false);
        final HttpClient client = createClient(support, response, url.toURI(), null, formData, ContentType.APPLICATION_XML,
                false);
        final EventManager eventManager = support.createNiceMock(EventManager.class);
        final NetworkConfigurationManager networkConfigurationManager = support.createNiceMock(NetworkConfigurationManager.class);

        support.replayAll();

        final ResponseValues responseValues = new ResponseValues();

        final PostRequestorImpl requestor = new PostRequestorImpl(client, new HeaderConstantsMock(), eventManager, networkConfigurationManager);
        final CancellableInputStream actual = requestor.sendPost(url, formData, responseValues);

        assertEquals(stream, actual.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_OK, responseValues.getResponseCode());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());

        support.verifyAll();
    }

    /**
     * Tests the post request.
     *
     * @throws IOException Bad io.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testPostRequestFormExtraHeaders() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final URL url = new URL(ourTestUrl);

        final ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);

        final Map<String, String> formData = New.map();
        formData.put("key", "value");

        final HttpResponse response = createResponse(support, stream, false);
        final HttpClient client = createClient(support, response, url.toURI(), null, formData, ContentType.APPLICATION_XML, true);
        final EventManager eventManager = support.createNiceMock(EventManager.class);
        final NetworkConfigurationManager networkConfigurationManager = support.createNiceMock(NetworkConfigurationManager.class);

        support.replayAll();

        final Map<String, String> extraHeaderValues = New.map();
        extraHeaderValues.put("key1", "value1");
        extraHeaderValues.put("key2", "value2");

        final ResponseValues responseValues = new ResponseValues();

        final PostRequestorImpl requestor = new PostRequestorImpl(client, new HeaderConstantsMock(), eventManager, networkConfigurationManager);
        final CancellableInputStream actual = requestor.sendPost(url, extraHeaderValues, formData, responseValues);

        assertEquals(stream, actual.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_OK, responseValues.getResponseCode());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());

        support.verifyAll();
    }

    /**
     * Tests the post request with a different content type..
     *
     * @throws IOException Bad io.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testPostRequestHeader() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final URL url = new URL(ourTestUrl);

        final ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
        final ByteArrayInputStream postStream = new ByteArrayInputStream(new byte[0]);

        final HttpResponse response = createResponse(support, stream, false);
        final HttpClient client = createClient(support, response, url.toURI(), postStream, null, ContentType.APPLICATION_JSON,
                true);
        final EventManager eventManager = support.createNiceMock(EventManager.class);
        final NetworkConfigurationManager networkConfigurationManager = support.createNiceMock(NetworkConfigurationManager.class);

        support.replayAll();

        final ResponseValues responseValues = new ResponseValues();

        final Map<String, String> extraHeaderValues = New.map();
        extraHeaderValues.put("key1", "value1");
        extraHeaderValues.put("key2", "value2");

        final PostRequestorImpl requestor = new PostRequestorImpl(client, new HeaderConstantsMock(), eventManager, networkConfigurationManager);
        final CancellableInputStream actual = requestor.sendPost(url, postStream, extraHeaderValues, responseValues,
                ContentType.APPLICATION_JSON);

        assertEquals(stream, actual.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_OK, responseValues.getResponseCode());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());

        support.verifyAll();
    }

    /**
     * Tests the post request with an error code.
     *
     * @throws IOException Bad io.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testPostWithError() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final URL url = new URL(ourTestUrl);

        final ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
        final ByteArrayInputStream postStream = new ByteArrayInputStream(new byte[0]);

        final HttpResponse response = createResponse(support, stream, true);
        final HttpClient client = createClient(support, response, url.toURI(), postStream, null, ContentType.APPLICATION_XML,
                false);
        final EventManager eventManager = support.createNiceMock(EventManager.class);
        final NetworkConfigurationManager networkConfigurationManager = support.createNiceMock(NetworkConfigurationManager.class);

        support.replayAll();

        final ResponseValues responseValues = new ResponseValues();

        final PostRequestorImpl requestor = new PostRequestorImpl(client, new HeaderConstantsMock(), eventManager, networkConfigurationManager);
        final CancellableInputStream actual = requestor.sendPost(url, postStream, responseValues);

        assertEquals(stream, actual.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, responseValues.getResponseCode());
        assertEquals("Error Message", responseValues.getResponseMessage());

        support.verifyAll();
    }

    /**
     * Create easy mocked HttpClient.
     *
     * @param support The easy mock support.
     * @param response The HttpResponse to return.
     * @param expectedUri The expected uri in the request.
     * @param expectedStream The expected post data.
     * @param formData The expected form data.
     * @param expectedContentType The expected content type.
     * @param expectHeader True if we should expect extra header data, false
     *            otherwise.
     * @return The easy mocked HttpClient.
     * @throws IOException Bad io.
     */
    private HttpClient createClient(final EasyMockSupport support, final HttpResponse response, final URI expectedUri,
            final InputStream expectedStream, final Map<String, String> formData, final ContentType expectedContentType,
            final boolean expectHeader)
        throws IOException
    {
        final HttpClient client = support.createMock(HttpClient.class);
        final HttpClientOptions options = new HttpClientOptions();
        final CookieStore cookieStore = EasyMock.createNiceMock(CookieStore.class);

        EasyMock.expect(client.getOptions()).andReturn(options).anyTimes();
        EasyMock.expect(client.getCookieStore()).andReturn(cookieStore).anyTimes();

        client.execute(EasyMock.isA(HttpRequest.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            final HttpRequest request = (HttpRequest)EasyMock.getCurrentArguments()[0];
            assertEquals(HttpRequest.POST, request.getMethod());

            assertEquals(expectedUri, request.getURI());

            if (expectHeader)
            {
                assertTrue(request.getHeaders().asMap().get("key1").contains("value1"));
                assertTrue(request.getHeaders().asMap().get("key2").contains("value2"));
            }

            if (expectedStream != null)
            {
                try
                {
                    assertEquals(streamToString(expectedStream), streamToString(request.getEntity().getContent()));
                }
                catch (final IOException e)
                {
                    Assert.fail(e.getMessage());
                }
                assertEquals(expectedContentType, request.getEntity().getContentType());
            }
            else
            {
                final FormEntity formEntity = (FormEntity)request.getEntity();
                final String formString = formEntity.asString();

                for (final Entry<String, String> entry : formData.entrySet())
                {
                    assertTrue(formString.contains(entry.getKey()));
                    assertTrue(formString.contains(entry.getValue()));
                }

                assertEquals(ContentType.APPLICATION_FORM_URLENCODED, request.getEntity().getContentType());
            }

            return response;
        });

        return client;
    }

    /**
     * Creates the easy mocked response object.
     *
     * @param support The easy mock support.
     * @param returnStream The stream to return.
     * @param isError True if the response code should be an error.
     * @return The easy mocked support.
     */
    private HttpResponse createResponse(final EasyMockSupport support, final InputStream returnStream, final boolean isError)
    {
        final HttpResponse response = support.createMock(HttpResponse.class);

        response.getStatusCode();

        if (!isError)
        {
            EasyMock.expectLastCall().andReturn(HttpURLConnection.HTTP_OK);
        }
        else
        {
            EasyMock.expectLastCall().andReturn(HttpURLConnection.HTTP_FORBIDDEN);
        }

        response.getStatusMessage();

        if (!isError)
        {
            EasyMock.expectLastCall().andReturn(null);
        }
        else
        {
            EasyMock.expectLastCall().andReturn("Error Message");
        }

        @SuppressWarnings("unchecked")
        final ListMultimap<String, String> headerValues = support.createMock(ListMultimap.class);
        headerValues.asMap();
        EasyMock.expectLastCall().andReturn(ourHeaderValues);

        response.getHeaders();
        EasyMock.expectLastCall().andReturn(headerValues);

        final HttpEntity entity = support.createMock(HttpEntity.class);
        EasyMock.expect(entity.getContent()).andReturn(returnStream);
        EasyMock.expect(entity.getContentLength()).andReturn(ourLength);

        response.getEntity();
        EasyMock.expectLastCall().andReturn(entity);

        return response;
    }
}
