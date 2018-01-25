package io.opensphere.server.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.common.geospatial.model.DataObjectResult;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * Tests the OGCServerConnector class.
 *
 */
public class OGCServerConnectorTest
{
    /**
     * The error message.
     */
    private static final String ourErrorMessage = "Error";

    /**
     * The expected url.
     */
    private static final String ourUrl = "http://somehost/getObject";

    /**
     * Tests request stream with post.
     *
     * @throws OGCServerException Bad server.
     * @throws URISyntaxException Bad uri.
     * @throws IOException Bad io.
     * @throws GeneralSecurityException Bad security.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testRequestStreamPost()
        throws GeneralSecurityException, IOException, URISyntaxException, OGCServerException, ClassNotFoundException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        ByteArrayInputStream postParams = new ByteArrayInputStream(new byte[0]);
        DataObjectResult object = new DataObjectResult();
        object.setErrorMessage(ourErrorMessage);

        ServerProviderRegistry serverProvider = createProvider(support, url, postParams, object, false, null);

        support.replayAll();

        OGCServerConnector connector = new OGCServerConnector(url, postParams, serverProvider);
        InputStream stream = connector.requestStream();

        ObjectInputStream ois = new ObjectInputStream(stream);
        try
        {
            DataObjectResult actualObject = (DataObjectResult)ois.readObject();

            assertEquals(object.getErrorMessage(), actualObject.getErrorMessage());

            support.verifyAll();
        }
        finally
        {
            ois.close();
        }
    }

    /**
     * Tests request stream with post.
     *
     * @throws OGCServerException Bad server.
     * @throws URISyntaxException Bad uri.
     * @throws IOException Bad io.
     * @throws GeneralSecurityException Bad security.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testRequestStreamPostForm()
        throws GeneralSecurityException, IOException, URISyntaxException, OGCServerException, ClassNotFoundException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        Map<String, String> postParams = New.map();
        DataObjectResult object = new DataObjectResult();
        object.setErrorMessage(ourErrorMessage);

        ServerProviderRegistry serverProvider = createProvider(support, url, null, object, false, postParams);

        support.replayAll();

        OGCServerConnector connector = new OGCServerConnector(url, postParams, serverProvider);
        InputStream stream = connector.requestStream();

        ObjectInputStream ois = new ObjectInputStream(stream);
        try
        {
            DataObjectResult actualObject = (DataObjectResult)ois.readObject();

            assertEquals(object.getErrorMessage(), actualObject.getErrorMessage());

            support.verifyAll();
        }
        finally
        {
            ois.close();
        }
    }

    /**
     * Tests requests stream with get.
     *
     * @throws OGCServerException Bad server.
     * @throws URISyntaxException Bad uri.
     * @throws IOException Bad io.
     * @throws GeneralSecurityException Bad security.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testRequestStreamGet()
        throws GeneralSecurityException, IOException, URISyntaxException, OGCServerException, ClassNotFoundException
    {
        EasyMockSupport support = new EasyMockSupport();

        URL url = new URL(ourUrl);
        DataObjectResult object = new DataObjectResult();
        object.setErrorMessage(ourErrorMessage);

        ServerProviderRegistry serverProvider = createProvider(support, url, null, object, false, null);

        support.replayAll();

        OGCServerConnector connector = new OGCServerConnector(url, serverProvider);
        InputStream stream = connector.requestStream();

        ObjectInputStream ois = new ObjectInputStream(stream);
        try
        {
            DataObjectResult actualObject = (DataObjectResult)ois.readObject();

            assertEquals(object.getErrorMessage(), actualObject.getErrorMessage());

            support.verifyAll();
        }
        finally
        {
            ois.close();
        }
    }

    /**
     * Creates a server provider registry.
     *
     * @param support Used to create the mock.
     * @param url The expected url.
     * @param postData The expected post data or null if a get request.
     * @param object The object to return in the request.
     * @param isZip Indicates if the return needs to be compressed or not.
     * @param params The expected params.
     * @return The server provider registry.
     * @throws URISyntaxException Bad uri.
     * @throws IOException Bad io.
     * @throws GeneralSecurityException Bad security.
     */
    private ServerProviderRegistry createProvider(EasyMockSupport support, URL url, InputStream postData,
            final DataObjectResult object, final boolean isZip, Map<String, String> params)
                throws GeneralSecurityException, IOException, URISyntaxException
    {
        HttpServer server = support.createMock(HttpServer.class);

        if (postData != null)
        {
            server.sendPost(EasyMock.eq(url), EasyMock.eq(postData), EasyMock.isA(ResponseValues.class));
        }
        else if (params != null)
        {
            server.sendPost(EasyMock.eq(url), EasyMock.eq(params), EasyMock.isA(ResponseValues.class));
        }
        else
        {
            server.sendGet(EasyMock.eq(url), EasyMock.isA(ResponseValues.class));
        }

        EasyMock.expectLastCall().andAnswer(new IAnswer<CancellableInputStream>()
        {
            @Override
            public CancellableInputStream answer() throws IOException
            {
                ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[EasyMock.getCurrentArguments().length
                        - 1];
                response.setResponseCode(HttpURLConnection.HTTP_OK);

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                ObjectOutputStream objectOut = null;
                if (!isZip)
                {
                    objectOut = new ObjectOutputStream(out);
                }
                else
                {
                    Map<String, Collection<String>> headerValues = New.map();
                    headerValues.put("Content-Type", New.list("gzip", "default"));
                    response.setHeader(headerValues);
                    objectOut = new ObjectOutputStream(new GZIPOutputStream(out));
                }

                objectOut.writeObject(object);
                objectOut.flush();
                objectOut.close();

                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                return new CancellableInputStream(in, null);
            }
        });

        @SuppressWarnings("unchecked")
        ServerProvider<HttpServer> provider = support.createMock(ServerProvider.class);
        provider.getServer(EasyMock.eq(url));
        EasyMock.expectLastCall().andReturn(server);

        ServerProviderRegistry registry = support.createMock(ServerProviderRegistry.class);
        registry.getProvider(EasyMock.eq(HttpServer.class));
        EasyMock.expectLastCall().andReturn(provider);

        return registry;
    }
}
