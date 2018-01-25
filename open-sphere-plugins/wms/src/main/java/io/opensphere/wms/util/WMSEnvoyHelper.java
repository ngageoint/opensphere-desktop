package io.opensphere.wms.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.function.Function;

import javax.xml.bind.JAXBException;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.image.ImageMetrics;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.core.util.net.UnexpectedResponseReporter;
import io.opensphere.core.util.xml.WrappedXMLReader;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.capabilities.WMS111Capabilities;
import io.opensphere.wms.capabilities.WMS130Capabilities;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.layer.TileImageKey;
import io.opensphere.wms.sld.SldRegistry;
import net.opengis.wms._111.ServiceExceptionReport;
import net.opengis.wms._111.WMTMSCapabilities;
import net.opengis.wms_130.WMSCapabilities;

/** Utility class for handling interaction with the server. */
@SuppressWarnings("PMD.GodClass")
public final class WMSEnvoyHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WMSEnvoyHelper.class);

    /**
     * The amount of time in nanoseconds before a warning is issued to the
     * logger.
     */
    private static final long WARNING_TIME_NANOS = Integer.getInteger("opensphere.wms.warningTimeMilliseconds", 5000).longValue()
            * Constants.NANO_PER_MILLI;

    /**
     * Request an image from the server with retries if the request fails.
     *
     * @param sldRegistry the sld registry
     * @param serverConfig The server configuration.
     * @param layerConfig Configuration for the layer.
     * @param imageKey key which defines the tile to request.
     * @param toolbox The system toolbox.
     * @param wmsVersion The WMS version to use in the request.
     * @return Image as retrieved from the server.
     * @throws IOException If a connection error occurs.
     * @throws GeneralSecurityException If a security error occurs.
     * @throws InterruptedException If the request was cancelled.
     * @throws ImageFormatUnknownException If the image is an unknown format.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    public static Image getImageFromServer(SldRegistry sldRegistry, ServerConnectionParams serverConfig,
            WMSLayerConfig layerConfig, TileImageKey imageKey, Toolbox toolbox, String wmsVersion)
        throws IOException, GeneralSecurityException, InterruptedException, ImageFormatUnknownException, URISyntaxException
    {
        final ResponseValues response = new ResponseValues();
        final URL url = getRequestURL(sldRegistry, serverConfig, layerConfig, imageKey, wmsVersion);
        Image image;
        final ImageMetrics metrics = new ImageMetrics();
        try (CancellableInputStream stream = getImageStreamFromServer(serverConfig, url, response, layerConfig, imageKey,
                toolbox))
        {
            final String contentType = getContentType(response);
            final int contentLength = getContentLength(response);
            image = Image.read(stream, contentLength, contentType, metrics);
        }

        if (WARNING_TIME_NANOS < metrics.getDecodeTimeNanoseconds())
        {
            LOGGER.warn(StringUtilities.formatTimingMessage("Reading an image from the server at " + url.getHost() + " took ",
                    metrics.getDecodeTimeNanoseconds()));
        }
        else if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.formatTimingMessage("Time to read image from server stream: ",
                    metrics.getDecodeTimeNanoseconds()));
        }

        return image;
    }

    /**
     * Request the stream for an image from the server with retries if the
     * request fails.
     *
     * @param sldRegistry the sld registry
     * @param serverConfig The server configuration.
     * @param layerConfig Configuration for the layer.
     * @param imageKey key which defines the tile to request.
     * @param toolbox The system toolbox.
     * @param wmsVersion The WMS version to use in the request.
     * @return InputStream for the image as retrieved from the server.
     * @throws IOException If a connection error occurs.
     * @throws GeneralSecurityException If a security error occurs.
     * @throws InterruptedException If the request was cancelled.
     * @throws ImageFormatUnknownException If the stream from the server has an
     *             unrecognized format.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    public static InputStream getImageStreamFromServer(SldRegistry sldRegistry, ServerConnectionParams serverConfig,
            WMSLayerConfig layerConfig, TileImageKey imageKey, Toolbox toolbox, String wmsVersion)
        throws IOException, GeneralSecurityException, InterruptedException, ImageFormatUnknownException, URISyntaxException
    {
        InputStream result = null;

        final ResponseValues response = new ResponseValues();
        final URL url = getRequestURL(sldRegistry, serverConfig, layerConfig, imageKey, wmsVersion);
        final CancellableInputStream imageStreamFromServer = getImageStreamFromServer(serverConfig, url, response, layerConfig,
                imageKey, toolbox);
        final ImageMetrics metrics = new ImageMetrics();

        try
        {
            final InputStream transcodeInputStream = transcodeInputStream(response, imageStreamFromServer, metrics);

            if (WARNING_TIME_NANOS < metrics.getDecodeTimeNanoseconds())
            {
                LOGGER.warn(StringUtilities.formatTimingMessage("Reading an image from the server at " + url.getHost() + " took ",
                        metrics.getDecodeTimeNanoseconds()));
            }
            else if (LOGGER.isDebugEnabled() && metrics.getDecodeTimeNanoseconds() >= 0L)
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to read image from server stream: ",
                        metrics.getDecodeTimeNanoseconds()));
            }

            result = transcodeInputStream;
        }
        catch (IOException | ImageFormatUnknownException e)
        {
            if (imageStreamFromServer.isCancelled())
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Exception thrown due to image stream being cancelled: " + e, e);
                }
                // NOPMD:PreserveStackTrace
                throw new InterruptedException();
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            if (!Utilities.sameInstance(result, imageStreamFromServer))
            {
                try
                {
                    imageStreamFromServer.close();
                }
                catch (final IOException e)
                {
                    LOGGER.warn("Failed to close stream for " + url + ": " + e, e);
                }
            }
        }

        return result;
    }

    /**
     * Request the capabilities document from the server.
     *
     * @param serverConf the server configuration
     * @param toolbox The system toolbox.
     * @return The wrapped capabilities object.
     * @throws IOException If the server connection fails.
     * @throws GeneralSecurityException If server authentication fails.
     * @throws InterruptedException If the request was cancelled.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    public static WMSServerCapabilities requestCapabilitiesFromServer(ServerConnectionParams serverConf, Toolbox toolbox)
        throws IOException, GeneralSecurityException, InterruptedException, URISyntaxException
    {
        /* Request a 1.3.0 doc and if that fails, request a 1.1.1 doc */
        byte[] bytes;
        try
        {
            bytes = requestCapabilities(serverConf, toolbox, "1.3.0", false);
        }
        catch (IOException | InterruptedException e)
        {
            bytes = requestCapabilities(serverConf, toolbox, "1.1.1", true);
        }
        final InputStream stream = new ByteArrayInputStream(bytes);

        /* Regardless of the request, try to parse it both ways. */
        WMSCapabilities caps130;
        try
        {
            caps130 = getXmlObject(stream, WMSCapabilities.class, handler -> new NamespaceContentHandler(handler));
        }
        catch (final IOException e)
        {
            caps130 = null;
        }
        if (caps130 != null && caps130.getCapability() != null && "1.3.0".equals(caps130.getVersion()))
        {
            return new WMS130Capabilities(caps130);
        }

        stream.reset();

        final WMTMSCapabilities caps111 = getXmlObject(stream, WMTMSCapabilities.class, null);
        if (caps111 != null && caps111.getCapability() != null)
        {
            return new WMS111Capabilities(caps111);
        }

        return null;
    }

    /**
     * Gets the request URL.
     *
     * @param sldRegistry the sld registry
     * @param serverConfig The server configuration.
     * @param layerConfig Configuration for the layer.
     * @param imageKey key which defines the tile to request.
     * @param wmsVersion The WMS version to use in the request.
     * @return The URL
     */
    private static URL getRequestURL(SldRegistry sldRegistry, ServerConnectionParams serverConfig, WMSLayerConfig layerConfig,
            TileImageKey imageKey, String wmsVersion)
    {
        URL url;
        if (layerConfig.getLayerType() == WMSLayerConfig.LayerType.LIDAR)
        {
            url = WMSURLBuilder.buildGetCoverageURL(serverConfig, layerConfig, imageKey);
        }
        else
        {
            url = WMSURLBuilder.buildGetMapURL(sldRegistry, layerConfig, imageKey, serverConfig.getServerCustomization(),
                    wmsVersion);
        }
        return url;
    }

    /**
     * Check to see if the content type matches the type we expected and
     * generate error messages if required.
     *
     * @param contentType The content type we received from the server
     * @param expectedType The content type we expected to receive
     * @param url The url used for the request (used for error reporting)
     * @param stream The stream which contains the content (used for error
     *            reporting)
     * @throws IOException If the stream has the wrong content type.
     */
    private static void checkContentType(String contentType, String expectedType, URL url, InputStream stream) throws IOException
    {
        if (!contentType.contains(expectedType) && !expectedType.contains(contentType))
        {
            ServiceExceptionReport report = null;

            if (contentType.contains(ExceptionContentHandler.MIME_TYPE)
                    || contentType.contains(ExceptionContentHandler.MIME_TYPE_1_3))
            {
                report = new ExceptionContentHandler().getContent(stream, url, contentType);
            }

            if (report == null && contentType.contains("text/"))
            {
                UnexpectedResponseReporter.reportUnexpectedResponse(contentType, stream);
            }

            if (report == null || report.getServiceException().isEmpty())
            {
                throw new IOException("Stream returned for URL [" + url + "] has content-type [" + contentType
                        + "] instead of expected [" + expectedType + "]");
            }
            else
            {
                throw new IOException(report.getServiceException().get(0).getvalue() + " URL [" + url + "]");
            }
        }
    }

    /**
     * Request the stream for an image from the server.
     *
     * @param serverConfig The server configuration.
     * @param url The URL from which to acquire the stream.
     * @param response The HTTP response from the server connection attempt.
     * @param expectedContentType The expected content type for the returned
     *            image.
     * @param imageKey key which defines the tile to request.
     * @param toolbox The system toolbox.
     * @return InputStream for the image as retrieved from the server.
     * @throws IOException If an error occurs connecting to the server.
     * @throws GeneralSecurityException If authentication fails.
     * @throws InterruptedException If the request was cancelled.
     * @throws URISyntaxException If the url could not be converted to the URI.
     */
    private static CancellableInputStream doGetImageStreamFromServer(ServerConnectionParams serverConfig, URL url,
            ResponseValues response, String expectedContentType, TileImageKey imageKey, Toolbox toolbox)
        throws IOException, GeneralSecurityException, InterruptedException, URISyntaxException
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("GetMap begin from " + url);
        }

        try
        {
            final long t0 = System.nanoTime();
            final CancellableInputStream stream = WMSEnvoyHelper.openServerConnection(url, response, toolbox);
            final long t1 = System.nanoTime();

            boolean success = false;
            try
            {
                if (WARNING_TIME_NANOS < t1 - t0)
                {
                    LOGGER.warn(StringUtilities
                            .formatTimingMessage("Opening a connection to the server at " + url.getHost() + " took ", t1 - t0)
                            + (ThreadControl.isThreadCancelled() ? " (cancelled)" : ""));
                }
                else if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(StringUtilities
                            .formatTimingMessage("Opening a connection to the server at " + url.getHost() + " took ", t1 - t0)
                            + (ThreadControl.isThreadCancelled() ? " (cancelled)" : ""));
                }
                if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    final String contentType = getContentType(response);
                    if (contentType != null)
                    {
                        checkContentType(contentType, expectedContentType, url, stream);
                    }
                    success = true;
                    return stream;
                }
                else
                {
                    if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                    {
                        serverConfig.failedAuthentication();
                    }
                    throw new IOException("Failed to request image from server for URL: " + url + ", HTTP status code="
                            + response.getResponseCode() + ", HTTP status message=" + response.getResponseMessage());
                }
            }
            finally
            {
                if (!success)
                {
                    Utilities.close(stream);
                }
            }
        }
        catch (final GeneralSecurityException e)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Failed to read texture for URL: " + url + ", imageKey=" + imageKey + " " + e, e);
            }
            throw e;
        }
    }

    /**
     * Get the content length from the response.
     *
     * @param response The HTTP response.
     * @return The content length in bytes.
     */
    private static int getContentLength(ResponseValues response)
    {
        int contentLength;
        try
        {
            contentLength = Integer.parseInt(response.getHeaderValue("Content-Length"));
        }
        catch (final NumberFormatException ex)
        {
            contentLength = -1;
        }
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Content-Length is " + contentLength + " bytes");
        }
        return contentLength;
    }

    /**
     * Get the content type from the HTTP response.
     *
     * @param response The response.
     * @return The content MIME type.
     */
    private static String getContentType(ResponseValues response)
    {
        return response.getHeaderValue("Content-Type");
    }

    /**
     * Request the stream for an image from the server.
     *
     * @param serverConfig The server configuration.
     * @param url The URL from which to acquire the stream.
     * @param response The HTTP response from the server connection attempt.
     * @param layerConfig Configuration for the layer.
     * @param imageKey key which defines the tile to request.
     * @param toolbox The system toolbox.
     * @return InputStream for the image as retrieved from the server.
     * @throws IOException If a connection error occurs.
     * @throws GeneralSecurityException If a security error occurs.
     * @throws InterruptedException If the request was cancelled.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    private static CancellableInputStream getImageStreamFromServer(ServerConnectionParams serverConfig, URL url,
            ResponseValues response, WMSLayerConfig layerConfig, TileImageKey imageKey, Toolbox toolbox)
        throws IOException, GeneralSecurityException, InterruptedException, URISyntaxException
    {
        CancellableInputStream stream;
        try
        {
            stream = doGetImageStreamFromServer(serverConfig, url, response, layerConfig.getGetMapConfig().getImageFormat(),
                    imageKey, toolbox);
        }
        catch (final IOException e)
        {
            if (ThreadControl.isThreadCancelled())
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Request for image for key [" + imageKey + "] was interrupted.");
                }
            }
            else
            {
                warnImageRetrieveFailed(layerConfig.getLayerTitle(), imageKey, e);
            }
            throw e;
        }
        catch (RuntimeException | GeneralSecurityException e)
        {
            warnImageRetrieveFailed(layerConfig.getLayerTitle(), imageKey, e);
            throw e;
        }

        return stream;
    }

    /**
     * Request an object from the server.
     *
     * @param <T> The type of the object.
     * @param stream The {@link InputStream} from which to read the object.
     * @param target The class type of the object.
     * @param contentHandlerProducer optional producer of content handler
     * @return The object.
     * @throws IOException If there is a problem communicating with the server.
     * @throws GeneralSecurityException If there is a problem authenticating
     *             with the server.
     */
    private static <T> T getXmlObject(InputStream stream, Class<T> target,
            Function<ContentHandler, ContentHandler> contentHandlerProducer)
        throws IOException, GeneralSecurityException
    {
        T result = null;
        try
        {
            XMLReader reader = XMLUtilities.newXMLReader();
            if (contentHandlerProducer != null)
            {
                reader = new WrappedXMLReader(reader, contentHandlerProducer);
            }
            final SAXSource source = new SAXSource(reader, new InputSource(stream));
            result = XMLUtilities.readXMLObjectNoDTD(source, target);
        }
        catch (final JAXBException e)
        {
            throw new IOException("Failed to unmarshal type[" + target + "]", e);
        }
        finally
        {
            stream.close();
        }

        return result;
    }

    /**
     * Open a connection to the server and return the input stream.
     *
     * @param url The server URL.
     * @param response The optional HTTP response.
     * @param toolbox The system toolbox.
     * @return The input stream.
     * @throws IOException If an error occurs connecting to the server.
     * @throws GeneralSecurityException If authentication fails.
     * @throws InterruptedException If the request was cancelled.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    private static CancellableInputStream openServerConnection(URL url, ResponseValues response, Toolbox toolbox)
        throws IOException, GeneralSecurityException, InterruptedException, URISyntaxException
    {
        ThreadControl.check();

        final ServerProvider<HttpServer> provider = toolbox.getServerProviderRegistry().getProvider(HttpServer.class);
        final HttpServer serverConnection = provider.getServer(url);

        return serverConnection.sendGet(url, response);
    }

    /**
     * Request the capabilities document from the server.
     *
     * @param serverConf the server configuration
     * @param toolbox The system toolbox.
     * @param wmsVersion The WMS version to use in the request.
     * @param reauthOnFailure When true, any failure will cause
     *            re-authentication to occur on the next request.
     * @return The capabilities document.
     * @throws IOException If the server connection fails.
     * @throws GeneralSecurityException If server authentication fails.
     * @throws InterruptedException If the request was cancelled.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    private static byte[] requestCapabilities(ServerConnectionParams serverConf, Toolbox toolbox, String wmsVersion,
            boolean reauthOnFailure)
        throws IOException, GeneralSecurityException, InterruptedException, URISyntaxException
    {
        final URL url = WMSURLBuilder.buildGetCapabilitiesURL(serverConf, wmsVersion);
        final ResponseValues response = new ResponseValues();
        try (InputStream stream = openServerConnection(url, response, toolbox))
        {
            if (response.getResponseCode() == HttpURLConnection.HTTP_CLIENT_TIMEOUT)
            {
                throw new IOException("HTTP Connection error:" + response.getResponseCode() + " - "
                        + response.getResponseMessage() + "\n\n" + "Increase server timeout settings");
            }
            if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                serverConf.failedAuthentication();
                throw new GeneralSecurityException("HTTP Connection error: 401 - Unauthorized");
            }
            else if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                if (reauthOnFailure)
                {
                    serverConf.failedAuthentication();
                }

                throw new IOException(

                        "HTTP Connection error:" + response.getResponseCode() + " - " + response.getResponseMessage());
            }

            if (stream == null)
            {
                throw new IOException("Could not get server stream. Response code: " + response.getResponseCode()
                        + ", response message: " + response.getResponseMessage());
            }

            final String contentType = getContentType(response);
            if (contentType != null && contentType.contains(ExceptionContentHandler.MIME_TYPE))
            {
                String errorStr = null;
                final ServiceExceptionReport ser = new ExceptionContentHandler().getContent(stream, url, contentType);
                if (ser != null)
                {
                    final StringBuilder sb = new StringBuilder(
                            "Exception" + (ser.getServiceException().size() > 1 ? "s" : "") + " returned from Server:\n");
                    sb.append(ExceptionContentHandler.formatServiceException(ser, 100));
                    errorStr = sb.toString();
                }
                else
                {
                    errorStr = "Unknown Exception returned from Server.";
                }
                throw new IOException(errorStr);
            }

            return BufferUtilities
                    .toByteArray(new StreamReader(stream, (int)response.getContentLength(), -1).readStreamIntoBuffer());
        }
    }

    /**
     * Read the image off of the provided stream.
     *
     * @param response The HttpResponse.
     * @param stream The stream which contains the data.
     * @param metrics Optional metrics to report timing.
     *
     * @return The extracted image.
     * @throws IOException when connection to the server has a failure.
     * @throws ImageFormatUnknownException If the image is an unknown format.
     */
    private static InputStream transcodeInputStream(ResponseValues response, InputStream stream, ImageMetrics metrics)
        throws IOException, ImageFormatUnknownException
    {
        final String contentType = getContentType(response);
        InputStream ddsStream = stream;

        // DDSEncode images displayed on the globe. BIL, TIFF, and GEOTIFF don't
        // need to be encoded because they are used to display terrain and not
        // sent to the video card.
        if (contentType != null && !MimeType.BIL.getMimeType().equals(contentType)
                && !MimeType.TIFF.getMimeType().equals(contentType) && !MimeType.GEOTIFF.getMimeType().equals(contentType))
        {
            // Copy the stream into memory first before sending it to ImageIO.
            // This helps prevent IO errors
            // when panning and zooming around the globe.
            final StreamReader reader = new StreamReader(stream);
            final ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
            reader.copyStream(imageOut);
            final Image image = ImageIOImage.read(new ByteArrayInputStream(imageOut.toByteArray()), false);
            final DDSImage ddsImage = ((ImageIOImage)image).asDDSImage();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ObjectOutputStream output = new ObjectOutputStream(out);
            output.writeObject(ddsImage);
            ddsStream = new ByteArrayInputStream(out.toByteArray());
        }

        return ddsStream;
    }

    /**
     * Log a warning about a failed image retrieval.
     *
     * @param layerTitle The layer title.
     * @param imageKey The image key.
     * @param ex The exception.
     */
    private static void warnImageRetrieveFailed(String layerTitle, TileImageKey imageKey, Exception ex)
    {
        LOGGER.warn("Failed to read image for layer [" + layerTitle + "] with imageKey [" + imageKey + "]: " + ex);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Failed to read image for imageKey [" + imageKey + "]: " + ex, ex);
        }
    }

    /** Disallow instantiation. */
    private WMSEnvoyHelper()
    {
    }
}
