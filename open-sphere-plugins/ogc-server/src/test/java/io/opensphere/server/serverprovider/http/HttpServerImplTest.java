package io.opensphere.server.serverprovider.http;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.server.ContentType;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.server.serverprovider.http.requestors.FilePostRequestor;
import io.opensphere.server.serverprovider.http.requestors.GetRequestor;
import io.opensphere.server.serverprovider.http.requestors.PostRequestor;
import io.opensphere.server.serverprovider.http.requestors.RequestorProvider;

/**
 * Tests the HttpServerImpl class.
 *
 */
public class HttpServerImplTest
{
    /**
     * The expected url.
     */
    private static final String ourUrl = "http://somehost/";

    /**
     * Tests setting the timeouts.
     */
    @Test
    public void testBufferSize()
    {
        EasyMockSupport support = new EasyMockSupport();

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.setBufferSize(EasyMock.eq(1500));

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        server.setBufferSize(1500);

        support.verifyAll();
    }

    /**
     * Tests the get host.
     */
    @Test
    public void testGetHost()
    {
        HttpServerImpl server = new HttpServerImpl("host", null, null);
        assertEquals("host", server.getHost());
    }

    /**
     * Tests the get protocol.
     */
    @Test
    public void testGetProtocol()
    {
        HttpServerImpl server = new HttpServerImpl(null, "https", null);
        assertEquals("https", server.getProtocol());
    }

    /**
     * Verifies the post file call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testPostFile() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        File tempFile = new File(".");
        ResponseValues response = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        FilePostRequestor requestor = support.createMock(FilePostRequestor.class);
        requestor.postFileToServer(EasyMock.eq(url), EasyMock.eq(tempFile), EasyMock.eq(response));
        EasyMock.expectLastCall().andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.getFilePoster();
        EasyMock.expectLastCall().andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        InputStream actualStream = server.postFile(url, tempFile, response);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Verifies the send get call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSendGet() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        ResponseValues responseValues = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        GetRequestor requestor = support.createMock(GetRequestor.class);
        EasyMock.expect(requestor.sendGet(url, responseValues)).andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        EasyMock.expect(provider.getRequestor()).andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        InputStream actualStream = server.sendGet(url, responseValues);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Verifies the send get call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSendGetHeaders() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        ResponseValues responseValues = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        Map<String, String> extraHeaders = New.map();
        extraHeaders.put("key1", "value1");

        GetRequestor requestor = support.createMock(GetRequestor.class);
        EasyMock.expect(requestor.sendGet(url, extraHeaders, responseValues)).andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        EasyMock.expect(provider.getRequestor()).andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        InputStream actualStream = server.sendGet(url, extraHeaders, responseValues);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Verifies the send post call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSendPost() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        InputStream postData = new ByteArrayInputStream(new byte[0]);
        ResponseValues responseValues = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        PostRequestor requestor = support.createMock(PostRequestor.class);
        EasyMock.expect(requestor.sendPost(EasyMock.eq(url), EasyMock.eq(postData), EasyMock.eq(responseValues)))
                .andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.getPostRequestor();
        EasyMock.expectLastCall().andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        CancellableInputStream actualStream = server.sendPost(url, postData, responseValues);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Verifies the send post call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSendPostContentType() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        InputStream postData = new ByteArrayInputStream(new byte[0]);
        ResponseValues responseValues = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        PostRequestor requestor = support.createMock(PostRequestor.class);
        EasyMock.expect(requestor.sendPost(EasyMock.eq(url), EasyMock.eq(postData), EasyMock.eq(responseValues),
                EasyMock.eq(com.bitsys.common.http.header.ContentType.APPLICATION_JSON))).andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.getPostRequestor();
        EasyMock.expectLastCall().andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        CancellableInputStream actualStream = server.sendPost(url, postData, responseValues, ContentType.JSON);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Verifies the send post call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSendPostForm() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        Map<String, String> postData = New.map();
        ResponseValues responseValues = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        PostRequestor requestor = support.createMock(PostRequestor.class);
        requestor.sendPost(EasyMock.eq(url), EasyMock.eq(postData), EasyMock.eq(responseValues));
        EasyMock.expectLastCall().andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.getPostRequestor();
        EasyMock.expectLastCall().andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        InputStream actualStream = server.sendPost(url, postData, responseValues);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Verifies the send post call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSendPostFormHeader() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        Map<String, String> postData = New.map();
        ResponseValues responseValues = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        Map<String, String> extraHeaders = New.map();
        extraHeaders.put("key1", "value1");

        PostRequestor requestor = support.createMock(PostRequestor.class);
        requestor.sendPost(EasyMock.eq(url), EasyMock.eq(extraHeaders), EasyMock.eq(postData), EasyMock.eq(responseValues));
        EasyMock.expectLastCall().andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.getPostRequestor();
        EasyMock.expectLastCall().andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        InputStream actualStream = server.sendPost(url, extraHeaders, postData, responseValues);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Verifies the send post call.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testSendPostHeader() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        InputStream postData = new ByteArrayInputStream(new byte[0]);
        ResponseValues responseValues = new ResponseValues();

        CancellableInputStream expectedReturn = new CancellableInputStream(new ByteArrayInputStream(new byte[0]), null);

        Map<String, String> headerData = New.map();
        headerData.put("key1", "value1");

        PostRequestor requestor = support.createMock(PostRequestor.class);
        EasyMock.expect(requestor.sendPost(EasyMock.eq(url), EasyMock.eq(postData), EasyMock.eq(headerData),
                EasyMock.eq(responseValues), EasyMock.eq(com.bitsys.common.http.header.ContentType.APPLICATION_JSON)))
                .andReturn(expectedReturn);

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.getPostRequestor();
        EasyMock.expectLastCall().andReturn(requestor);

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        CancellableInputStream actualStream = server.sendPost(url, postData, headerData, responseValues, ContentType.JSON);

        assertEquals(expectedReturn, actualStream);

        support.verifyAll();
    }

    /**
     * Tests setting the timeouts.
     */
    @Test
    public void testTimeouts()
    {
        EasyMockSupport support = new EasyMockSupport();

        RequestorProvider provider = support.createMock(RequestorProvider.class);
        provider.setTimeouts(EasyMock.eq(120), EasyMock.eq(200));

        support.replayAll();

        HttpServerImpl server = new HttpServerImpl(null, null, provider);
        server.setTimeouts(120, 200);

        support.verifyAll();
    }
}
