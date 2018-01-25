package io.opensphere.core.appl.versions.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;

import io.opensphere.core.Toolbox;
import io.opensphere.core.appl.versions.InstallDescriptor;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * A set of utility methods useful in auto-update operations.
 */
public final class AutoUpdateUtils
{
    /** The {@link Logger} instance used to capture output. */
    private static final Logger LOG = Logger.getLogger(AutoUpdateUtils.class);

    /** The JSON Object mapper used to read install descriptors. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Private constructor to prevent instantiation.
     */
    private AutoUpdateUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Constructs a properly formatted URL string from the supplied host and
     * query fragments.
     *
     * @param protocolString the protocol string with which to generate the URL.
     *            Must not be null.
     * @param hostString the host string, including the protocol and the
     *            hostname, with which to generate the URL. Must not be null.
     * @param queryString the query string with which to generate the URL. Must
     *            not be null.
     * @return a fully qualified URL generated from the supplied host and query
     *         strings.
     */
    public static String getUrlString(String protocolString, String hostString, String queryString)
    {
        Preconditions.checkNotNull(protocolString);
        Preconditions.checkNotNull(hostString);
        Preconditions.checkNotNull(queryString);

        String protocol = protocolString;
        String host = hostString;
        String query = queryString;

        if (!protocol.endsWith("://"))
        {
            protocol += "://";
        }
        if (protocol.equals("file://"))
        {
            protocol += "/";
        }

        if (host.endsWith("/"))
        {
            host.substring(0, host.length() - 2);
        }

        if (!query.startsWith("/"))
        {
            query = "/" + query;
        }

        return protocol + host + query;
    }

    /**
     * Extracts the version and build string from the supplied install
     * descriptor.
     *
     * @param descriptor the descriptor from which to extract the version.
     * @return the version + build string generated from the supplied
     *         descriptor.
     */
    public static String toVersion(InstallDescriptor descriptor)
    {
        return descriptor.getVersion() + "_" + descriptor.getBuild();
    }

    /**
     * Reads an {@link InstallDescriptor} from the supplied URI.
     *
     * @param uri the location from which to read the descriptor.
     * @return an install descriptor read from the URI, or null if the URI could
     *         not be read (an error message is logged in this case).
     */
    public static InstallDescriptor toDescriptor(URI uri)
    {
        try
        {
            return OBJECT_MAPPER.readValue(uri.toURL(), InstallDescriptor.class);
        }
        catch (IOException e)
        {
            LOG.error("Unable to read '" + uri.toString() + " as an install descriptor.", e);
            return null;
        }
    }

    /**
     * Downloads the JSONinstall descriptor file from remote server.
     *
     * @param installDescriptorURL the installdescriptor.json file location
     * @return the install descriptor for the update
     */
    public static InstallDescriptor getUpdateInstallDescriptor(URL installDescriptorURL)
    {
        try
        {
            return OBJECT_MAPPER.readValue(installDescriptorURL, InstallDescriptor.class);
        }
        catch (IOException e)
        {
            LOG.error("Failed to get install descriptor", e);
        }
        return null;
    }

    /**
     * Gets the name of the operating system for the installers.
     *
     * @return the name of the operating system for the installers.
     */
    public static String getOperatingSystemString()
    {
        if (SystemUtils.IS_OS_LINUX)
        {
            return "linux64";
        }
        return "windows64";
    }

    /**
     * Using the supplied version, performs a substitution for all known
     * variables within the supplied URL string.
     *
     * @param urlString the string in which substitution is performed.
     * @param version the value to substitute into the URL string for the
     *            version key.
     * @return the substituted URL.
     */
    public static String substituteUrl(String urlString, String version)
    {
        Properties properties = new Properties();
        properties.put("version", version);
        properties.put("os", getOperatingSystemString());

        urlString = StrSubstitutor.replace(urlString, properties);
        return urlString;
    }

    /**
     * Performs a request to retrieve the supplied URL.
     *
     * @param url the URL to request.
     * @param toolbox the toolbox from which the server provider is retrieved
     *            for remote requests
     * @return the response stream of the supplied URL.
     */
    public static InputStream performRequest(URL url, Toolbox toolbox)
    {
        if (url.getProtocol().equals("file"))
        {
            return submitLocalRequest(url);
        }
        else
        {
            return submitRemoteRequest(url, toolbox);
        }
    }

    /**
     * Reads a FILE URL to get an input stream.
     *
     * @param url the File URL to be read.
     * @return an InputStream from which the results can be read.
     */
    private static InputStream submitLocalRequest(URL url)
    {
        try
        {
            return new FileInputStream(new File(url.toURI()));
        }
        catch (IOException | URISyntaxException e)
        {
            LOG.error("Could not check for new version of the application", e);
            return null;
        }
    }

    /**
     * Submits an HTTP GET request to get an input stream.
     *
     * @param url the URL to which the request will be sent.
     * @param toolbox the toolbox from which the server provider is retrieved.
     * @return an InputStream from which the results can be read.
     */
    private static InputStream submitRemoteRequest(URL url, Toolbox toolbox)
    {
        try
        {
            final ServerProvider<HttpServer> provider = toolbox.getServerProviderRegistry().getProvider(HttpServer.class);
            final HttpServer server = provider.getServer(url);

            final ResponseValues response = new ResponseValues();
            LOG.debug("Submitting HTTP GET: '" + url.toString() + "'");

            final CancellableInputStream responseStream = server.sendGet(url, response);
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                return responseStream;
            }
            else
            {
                LOG.warn("HTTP GET Returned " + response.getResponseCode() + " indicating non-success.");
                return null;
            }
        }
        catch (IOException | URISyntaxException e)
        {
            LOG.error("Could not check for new version of the application", e);
            return null;
        }
    }
}
