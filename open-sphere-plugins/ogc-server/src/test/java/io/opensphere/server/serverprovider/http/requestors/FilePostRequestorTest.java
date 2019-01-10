package io.opensphere.server.serverprovider.http.requestors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;
import com.bitsys.common.http.entity.HttpEntity;
import com.bitsys.common.http.entity.MultipartEntity;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpResponse;
import com.google.common.collect.ListMultimap;

import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.ByteString;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Tests the FilePostRequestor class.
 *
 */
@SuppressWarnings("boxing")
public class FilePostRequestorTest
{
    /**
     * The expected content length.
     */
    private static final long ourLength = 100;

    /**
     * The expected headers.
     */
    private static final Map<String, Collection<String>> ourHeaderValues = New.map();

    /**
     * Tests posting a file to the server.
     *
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testPostFileToServer() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final String fileContents = "This is my file.";
        final String urlString = "http://somehost/fileUpload";
        final URL url = new URL(urlString);

        final File tempFile = File.createTempFile("FilePostRequestor", "Test");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), New.list(fileContents), StringUtilities.DEFAULT_CHARSET);

        final ByteArrayInputStream expectedReturn = new ByteArrayInputStream(new byte[0]);

        final HttpResponse response = createResponse(support, expectedReturn, false);

        final HttpClient client = createHttpClient(support, response, tempFile, fileContents);

        final EventManager eventManager = support.createNiceMock(EventManager.class);

        support.replayAll();

        final FilePostRequestorImpl requestor = new FilePostRequestorImpl(client, new HeaderConstantsMock(), eventManager);

        final ResponseValues responseValues = new ResponseValues();
        final CancellableInputStream actualReturn = requestor.postFileToServer(url, tempFile, responseValues);

        assertEquals(expectedReturn, actualReturn.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_OK, responseValues.getResponseCode());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());

        support.verifyAll();
    }

    /**
     * Tests post file with error.
     *
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad uri.
     */
    @Test
    public void testPostFileWithError() throws IOException, URISyntaxException
    {
        final EasyMockSupport support = new EasyMockSupport();

        final String fileContents = "This is my file.";
        final String urlString = "http://somehost/fileUpload";
        final URL url = new URL(urlString);

        final File tempFile = File.createTempFile("FilePostRequestor", "Test");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), New.list(fileContents), StringUtilities.DEFAULT_CHARSET);

        final ByteArrayInputStream expectedReturn = new ByteArrayInputStream(new byte[0]);

        final HttpResponse response = createResponse(support, expectedReturn, true);

        final HttpClient client = createHttpClient(support, response, tempFile, fileContents);

        final EventManager eventManager = support.createNiceMock(EventManager.class);

        support.replayAll();

        final FilePostRequestorImpl requestor = new FilePostRequestorImpl(client, new HeaderConstantsMock(), eventManager);

        final ResponseValues responseValues = new ResponseValues();
        final CancellableInputStream actualReturn = requestor.postFileToServer(url, tempFile, responseValues);

        assertEquals(expectedReturn, actualReturn.getWrappedInputStream());
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, responseValues.getResponseCode());
        assertEquals("Error Message", responseValues.getResponseMessage());
        assertEquals(ourLength, responseValues.getContentLength());
        assertEquals(ourHeaderValues, responseValues.getHeader());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked HttpClient.
     *
     * @param support The support object.
     * @param expectedReturn The stream to return on the Httpclient.execute
     *            call.
     * @param file The test file being posted.
     * @param fileContent The data within the file.
     * @return The expectedReturn.
     * @throws IOException Bad IO.
     */
    private HttpClient createHttpClient(final EasyMockSupport support, final HttpResponse expectedReturn, final File file,
            final String fileContent)
        throws IOException
    {
        final HttpClient client = support.createMock(HttpClient.class);
        final HttpClientOptions options = new HttpClientOptions();
        final CookieStore cookieStore = EasyMock.createNiceMock(CookieStore.class);

        EasyMock.expect(client.getOptions()).andReturn(options);
        EasyMock.expect(client.getCookieStore()).andReturn(cookieStore);

        client.execute(EasyMock.isA(HttpRequest.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            final HttpRequest request = (HttpRequest)EasyMock.getCurrentArguments()[0];
            final MultipartEntity multiPart = (MultipartEntity)request.getEntity();

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            multiPart.writeTo(outputStream);

            final byte[] bytes = outputStream.toByteArray();

            final String requestString = ByteString.getStringFromBytes(bytes);

            assertTrue(requestString.contains(file.getName()));
            assertTrue(requestString.contains(fileContent));

            return expectedReturn;
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
        entity.getContent();
        EasyMock.expectLastCall().andReturn(returnStream);
        entity.getContentLength();
        EasyMock.expectLastCall().andReturn(ourLength);

        response.getEntity();
        EasyMock.expectLastCall().andReturn(entity);

        return response;
    }
}
