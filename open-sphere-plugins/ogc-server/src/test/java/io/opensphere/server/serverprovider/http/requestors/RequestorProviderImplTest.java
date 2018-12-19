package io.opensphere.server.serverprovider.http.requestors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;
import com.bitsys.common.http.client.ProxyConfig;
import com.bitsys.common.http.proxy.ProxyHostConfig;
import com.bitsys.common.http.proxy.ProxyHostConfig.ProxyType;
import com.bitsys.common.http.proxy.ProxyResolver;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Tests the Requestor provider class.
 *
 */
public class RequestorProviderImplTest
{
    /**
     * Verifies the provider does not return any nulls.
     */
    @Test
    public void test()
    {
        HttpClient client = EasyMock.createMock(HttpClient.class);

        RequestorProviderImpl provider = new RequestorProviderImpl(client, new HeaderConstantsMock(), null);

        BaseRequestor requestor = (BaseRequestor)provider.getFilePoster();

        assertNotNull(requestor);
        assertEquals(client, requestor.getClient());

        requestor = (BaseRequestor)provider.getPostRequestor();

        assertNotNull(requestor);
        assertEquals(client, requestor.getClient());

        requestor = (BaseRequestor)provider.getRequestor();

        assertNotNull(requestor);
        assertEquals(client, requestor.getClient());
    }

    /**
     * Tests setting the buffer size.
     */
    @Test
    public void testBufferSize()
    {
        HttpClientOptions options = new HttpClientOptions();

        HttpClient client = EasyMock.createMock(HttpClient.class);
        client.getOptions();
        EasyMock.expectLastCall().andReturn(options);

        EasyMock.replay(client);

        RequestorProviderImpl requestorProvider = new RequestorProviderImpl(client, new HeaderConstantsMock(), null);
        requestorProvider.setBufferSize(1530);

        assertEquals(1530, requestorProvider.getClient().getOptions().getSocketBufferSize());

        EasyMock.verify(client);
    }

    /**
     * Tests setting the timeouts.
     */
    @Test
    public void testTimeouts()
    {
        HttpClientOptions options = new HttpClientOptions();

        HttpClient client = EasyMock.createMock(HttpClient.class);
        client.getOptions();
        EasyMock.expectLastCall().andReturn(options);

        EasyMock.replay(client);

        RequestorProviderImpl requestorProvider = new RequestorProviderImpl(client, new HeaderConstantsMock(), null);
        requestorProvider.setTimeouts(120000, 200000);

        assertEquals(120, requestorProvider.getClient().getOptions().getReadTimeout());
        assertEquals(200, requestorProvider.getClient().getOptions().getConnectTimeout());

        EasyMock.verify(client);
    }

    /**
     * Tests the resolve proxy call.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testResolveProxy() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL testUrl = new URL("http://somehost");

        ProxyResolver resolver = support.createMock(ProxyResolver.class);
        EasyMock.expect(resolver.getProxyServer(testUrl))
                .andReturn(New.list(new ProxyHostConfig(ProxyType.PROXY, "proxyhost", 80)));

        HttpClientOptions options = new HttpClientOptions();
        options.setProxyConfig(new ProxyConfig());
        options.getProxyConfig().setProxyResolver(resolver);

        HttpClient client = support.createMock(HttpClient.class);
        EasyMock.expect(client.getOptions()).andReturn(options);

        support.replayAll();

        RequestorProviderImpl provider = new RequestorProviderImpl(client, new HeaderConstantsMock(), null);

        Pair<String, Integer> hostAndPort = provider.resolveProxy(testUrl);

        assertEquals("proxyhost", hostAndPort.getFirstObject());
        assertEquals(80, hostAndPort.getSecondObject().intValue());

        support.verifyAll();
    }

    /**
     * Tests the resolve proxy call, when a proxy is not configured.
     *
     * @throws MalformedURLException Bad url.
     */
    @Test
    public void testResolveProxyNoProxy() throws MalformedURLException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL testUrl = new URL("http://somehost");

        HttpClientOptions options = new HttpClientOptions();

        HttpClient client = support.createMock(HttpClient.class);
        EasyMock.expect(client.getOptions()).andReturn(options);

        support.replayAll();

        RequestorProviderImpl provider = new RequestorProviderImpl(client, new HeaderConstantsMock(), null);

        Pair<String, Integer> hostAndPort = provider.resolveProxy(testUrl);

        assertNull(hostAndPort);

        support.verifyAll();
    }
}
