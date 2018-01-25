package io.opensphere.wfs.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.wfs.placenames.GMLPlaceNameSAXHandler311;
import io.opensphere.wfs.placenames.PlaceNameData;
import io.opensphere.wfs.placenames.PlaceNameTile;

/** Helper class for the WMS envoy. */
public final class WFSEnvoyPlaceNameHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WFSEnvoyPlaceNameHelper.class);

    /**
     * Build the URL for requesting the place name data.
     *
     * @param placeNameTile The tile for which data will be requested.
     * @param serverURL Base url for the server.
     * @return The URL for the request.
     */
    private static URL buildPlaceNameURL(PlaceNameTile placeNameTile, String serverURL)
    {
        String server = serverURL;

        if (server == null)
        {
            return null;
        }

        StringBuffer sb = new StringBuffer(server);
        if (sb.charAt(sb.length() - 1) != '?')
        {
            sb.append('?');
        }

        StringBuilder filter = new StringBuilder(512);
        filter.append("<Filter xmlns=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
                + "xmlns:smil20=\"http://www.w3.org/2001/SMIL20\" "
                + "xmlns:smil20lang=\"http://www.w3.org/2001/SMIL20/Language\"><And><BBOX><PropertyName>GEOM</PropertyName>"
                + "<gml:Envelope><gml:lowerCorner>");

        LatLonAlt lowerLeft = placeNameTile.getKey().getBounds().getLowerLeft().getLatLonAlt();
        LatLonAlt upperRight = placeNameTile.getKey().getBounds().getUpperRight().getLatLonAlt();
        filter.append(lowerLeft.getLonD()).append(' ');
        filter.append(lowerLeft.getLatD());
        filter.append("</gml:lowerCorner><gml:upperCorner>");
        filter.append(upperRight.getLonD()).append(' ');
        filter.append(upperRight.getLatD());
        filter.append("</gml:upperCorner></gml:Envelope></BBOX>");

        filter.append(placeNameTile.getLayer().getConfiguration().getFilter()).append("</And></Filter>");

        StringBuilder ab = new StringBuilder(131);
        // @formatter:off
        ab.append("version=1.1.0&TypeName=Place_Names"
                + "&Request=GetFeature"
                + "&Service=WFS"
                + "&OUTPUTFORMAT=text/xml;+subtype=gml/3.1.1"
                + "&MAXFEATURES=1500"
                + "&FILTER=");
        // @formatter:on
        sb.append(ab);
        try
        {
            sb.append(URLEncoder.encode(filter.toString(), "ISO-8859-1"));
            return new java.net.URL(sb.toString());
        }
        catch (UnsupportedEncodingException e1)
        {
            LOGGER.error("Failed to encode string to URL : " + filter, e1);
        }
        catch (MalformedURLException e)
        {
            LOGGER.error("Failed to create URL from string : " + sb.toString(), e);
        }

        return null;
    }

    /**
     * Read the image off of the provided stream.
     *
     * @param stream The stream which contains the data.
     * @return The extracted data.
     * @throws IOException when connection to the server has a failure.
     */
    private static PlaceNameData getPlaceNamesFromStream(InputStream stream) throws IOException
    {
        PlaceNameData placeNames = null;

        GMLPlaceNameSAXHandler311 handler = new GMLPlaceNameSAXHandler311();
        try
        {
            SAXParserFactory.newInstance().newSAXParser().parse(stream, handler);
            placeNames = handler.getPlaceNameData();
        }
        catch (SAXException | ParserConfigurationException e)
        {
            LOGGER.error("Failed to parse place names.", e);
        }

        return placeNames;
    }

    /**
     * Open a connection to the server and return the input stream.
     *
     * @param url The server URL.
     * @param response The optional HTTP response.
     * @param toolbox The system toolbox.
     * @return The input stream.
     * @throws IOException If an error occurs connection to the server.
     * @throws GeneralSecurityException If authentication fails.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    private static InputStream openServerConnection(URL url, ResponseValues response, Toolbox toolbox)
        throws IOException, GeneralSecurityException, URISyntaxException
    {
        ServerProvider<HttpServer> provider = toolbox.getServerProviderRegistry().getProvider(HttpServer.class);
        HttpServer serverConnection = provider.getServer(url);

        return serverConnection.sendGet(url, response);
    }

    /**
     * Retrieve place names from the server or cache.
     *
     * @param serverURL Base URL for the server.
     * @param source Source to use with the data registry.
     * @param placeNameTile Tile for which place names are desired.
     * @param dataRegistry Data registry.
     * @param toolbox The system toolbox.
     */
    public static void retrievePlaceNameData(String serverURL, String source, final PlaceNameTile placeNameTile,
            DataRegistry dataRegistry, Toolbox toolbox)
    {
        String family = PlaceNameData.class.getName();
        String category = placeNameTile.getLayer().getConfiguration().getDataSetName();
        DataModelCategory dataModelCategory = new DataModelCategory(source, family, category);
        SimpleQuery<PlaceNameData> query = new SimpleQuery<>(dataModelCategory, PlaceNameData.PROPERTY_DESCRIPTOR);
        dataRegistry.performLocalQuery(query);
        List<PlaceNameData> results = query.getResults();
        PlaceNameData placeNames = results.isEmpty() ? null : results.get(0);

        if (placeNames != null)
        {
            placeNameTile.receiveData(placeNames);
            return;
        }

        // Not in the cache, so request from the server.
        URL url = WFSEnvoyPlaceNameHelper.buildPlaceNameURL(placeNameTile, serverURL);
        InputStream stream = null;
        try
        {
            ResponseValues response = new ResponseValues();
            stream = WFSEnvoyPlaceNameHelper.openServerConnection(url, response, toolbox);
            placeNames = WFSEnvoyPlaceNameHelper.getPlaceNamesFromStream(stream);

            if (placeNames != null && !placeNames.getPlaceNames().isEmpty())
            {
                placeNameTile.receiveData(placeNames);

                dataRegistry.removeModels(dataModelCategory, false);
                Date expiration = new Date(System.currentTimeMillis() + 3600000);
                Collection<? extends PropertyAccessor<PlaceNameData, ?>> accessors = Collections
                        .singleton(SerializableAccessor.getHomogeneousAccessor(PlaceNameData.PROPERTY_DESCRIPTOR));
                CacheDeposit<PlaceNameData> deposit = new DefaultCacheDeposit<>(dataModelCategory, accessors,
                        Collections.singleton(placeNames), true, expiration, false);
                dataRegistry.addModels(deposit);
                return;
            }
            else
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(placeNameTile.getServerName() + ": No places name results returned for layer " + category);
                }
            }
        }
        catch (GeneralSecurityException | IOException | URISyntaxException e)
        {
            LOGGER.error("Failed to read place names for URL [" + url + "] placeNameKey [" + placeNameTile.getKeyString() + "]: ",
                    e);
        }
        finally
        {
            Utilities.closeQuietly(stream);
        }
    }

    /** Disallow construction. */
    private WFSEnvoyPlaceNameHelper()
    {
    }
}
