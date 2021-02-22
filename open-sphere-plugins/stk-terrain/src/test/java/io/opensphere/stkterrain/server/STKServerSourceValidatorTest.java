package io.opensphere.stkterrain.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.mantle.datasources.impl.UrlDataSource;

/**
 * Unit test for the {@link STKServerSourceValidator}.
 */
public class STKServerSourceValidatorTest
{
    /**
     * The expected test server url.
     */
    private static final String ourTestServer = "http://somehost/stk-terrain";

    /**
     * Tests when the server is an STK terrain server.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testGetValidationStatus() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createServerProviderRegistry(support, HttpURLConnection.HTTP_OK, false);

        support.replayAll();

        STKServerSourceValidator validator = new STKServerSourceValidator(registry);
        UrlDataSource server = new UrlDataSource();
        server.setBaseUrl(ourTestServer);
        validator.setSource(server);
        ValidationStatus status = validator.getValidationStatus();

        assertEquals(ValidationStatus.VALID, status);

        support.verifyAll();
    }

    /**
     * Tests when the server is not an STK terrain server.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testGetValidationStatusNotTerrain() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createServerProviderRegistry(support, HttpURLConnection.HTTP_NOT_FOUND, false);

        support.replayAll();

        STKServerSourceValidator validator = new STKServerSourceValidator(registry);
        UrlDataSource server = new UrlDataSource();
        server.setBaseUrl(ourTestServer);
        validator.setSource(server);
        ValidationStatus status = validator.getValidationStatus();

        assertEquals(ValidationStatus.ERROR, status);
        assertFalse(validator.getValidationMessage().isEmpty());

        support.verifyAll();
    }

    /**
     * Tests when the server source has not been set.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testGetValidationStatusNullServerSource() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);

        support.replayAll();

        STKServerSourceValidator validator = new STKServerSourceValidator(registry);
        ValidationStatus status = validator.getValidationStatus();

        assertEquals(ValidationStatus.ERROR, status);
        assertFalse(validator.getValidationMessage().isEmpty());

        support.verifyAll();
    }

    /**
     * Tests when we cannot reach the server.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testGetValidationStatusTimeout() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createServerProviderRegistry(support, HttpURLConnection.HTTP_OK, true);

        support.replayAll();

        STKServerSourceValidator validator = new STKServerSourceValidator(registry);
        UrlDataSource server = new UrlDataSource();
        server.setBaseUrl(ourTestServer);
        validator.setSource(server);
        ValidationStatus status = validator.getValidationStatus();

        assertEquals(ValidationStatus.ERROR, status);
        assertFalse(validator.getValidationMessage().isEmpty());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ServerProviderRegistry}.
     *
     * @param support Used to create the mock.
     * @param responseCode The http response code to return.
     * @param isTimeout True if the http server should throw an
     *            {@link IOException}.
     * @return The mocked {@link ServerProviderRegistry}.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad uri.
     */
    private ServerProviderRegistry createServerProviderRegistry(EasyMockSupport support, int responseCode, boolean isTimeout)
        throws IOException, URISyntaxException
    {
        URL expectedUrl = new URL(ourTestServer + "/world/layer.json");

        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(expectedUrl), EasyMock.isA(ResponseValues.class)))
                .andAnswer(() -> sendGetAnswer(responseCode, isTimeout));

        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMockHelper.eq(expectedUrl))).andReturn(server);

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(registry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(provider);

        return registry;
    }

    /**
     * The answer to the mocked sendGet call.
     *
     * @param responseCode The http response code to return.
     * @param isTimeout True if the http server should throw an
     *            {@link IOException}.
     * @return Null.
     * @throws IOException If isTimeout is true.
     */
    private CancellableInputStream sendGetAnswer(int responseCode, boolean isTimeout) throws IOException
    {
        if (isTimeout)
        {
            throw new IOException("I timed out.");
        }

        ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
        response.setResponseCode(responseCode);

        return null;
    }
}
