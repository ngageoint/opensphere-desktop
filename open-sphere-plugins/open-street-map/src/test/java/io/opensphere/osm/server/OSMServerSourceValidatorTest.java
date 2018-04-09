package io.opensphere.osm.server;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.test.core.matchers.EasyMockHelper;

/**
 * Unit test for {@link OSMServerSourceValidator}.
 */
public class OSMServerSourceValidatorTest
{
    /**
     * An invalid test url that will be sent to the server.
     */
    private static final String ourInvalidUrl = "http://osm.geointservices.io/osm_tiles_p/{z}/{x}/{y}.png";

    /**
     * The valid test url.
     */
    private static final String ourValidUrl = "http://osm.geointservices.io/osm_tiles_pc/{z}/{x}/{y}.png";

    /**
     * Tests an invalid url.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testInvalid() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createServerRegistry(support, ourInvalidUrl);

        support.replayAll();

        OSMServerSourceValidator validator = new OSMServerSourceValidator(registry);
        UrlDataSource urlDataSource = new UrlDataSource();
        urlDataSource.setBaseUrl(ourInvalidUrl);
        validator.setSource(urlDataSource);

        assertEquals(ValidationStatus.ERROR, validator.getValidationStatus());

        support.verifyAll();
    }

    /**
     * Tests a valid url.
     *
     * @throws URISyntaxException Bad URI.
     * @throws IOException Bad IO.
     */
    @Test
    public void testValid() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerProviderRegistry registry = createServerRegistry(support, ourValidUrl);

        support.replayAll();

        OSMServerSourceValidator validator = new OSMServerSourceValidator(registry);
        UrlDataSource urlDataSource = new UrlDataSource();
        urlDataSource.setBaseUrl(ourValidUrl);
        validator.setSource(urlDataSource);

        assertEquals(ValidationStatus.VALID, validator.getValidationStatus());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ServerProviderRegistry}.
     *
     * @param support Used to create the mock.
     * @param urlString The expected url string.
     * @return The mocked class.
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad IO.
     */
    private ServerProviderRegistry createServerRegistry(EasyMockSupport support, String urlString)
        throws IOException, URISyntaxException
    {
        URL url = new URL(urlString.replace("{z}", "0").replace("{x}", "0").replace("{y}", "0"));
        HttpServer server = support.createMock(HttpServer.class);
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(url), EasyMock.isA(ResponseValues.class)))
                .andAnswer(this::sendGetAnswer);

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        EasyMock.expect(provider.getServer(EasyMockHelper.eq(url))).andReturn(server);

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        EasyMock.expect(registry.getProvider(EasyMock.eq(HttpServer.class))).andReturn(provider);

        return registry;
    }

    /**
     * The answer to the mocked sendGet call.
     *
     * @return An empty stream.
     */
    private CancellableInputStream sendGetAnswer()
    {
        String url = EasyMock.getCurrentArguments()[0].toString();
        ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
        if (url.equals(ourValidUrl.replace("{z}", "0").replace("{x}", "0").replace("{y}", "0")))
        {
            response.setResponseCode(HttpURLConnection.HTTP_OK);
        }
        else
        {
            response.setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
        }

        return new CancellableInputStream(new ByteArrayInputStream(new byte[] {}), null);
    }
}
