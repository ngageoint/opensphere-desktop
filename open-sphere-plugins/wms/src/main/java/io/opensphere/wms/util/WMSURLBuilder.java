package io.opensphere.wms.util;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.server.customization.DateParamCustomizer;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig;
import io.opensphere.wms.layer.TileImageKey;
import io.opensphere.wms.sld.SldRegistry;
import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * Build a URL string which can be used to request a tile from the server.
 */
public final class WMSURLBuilder
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WMSURLBuilder.class);

    /**
     * Build the get capabilities url.
     *
     * @param conf configuration for the server.
     * @param wmsVersion The WMS version (for example "1.1.1" or "1.3.0").
     * @return newly created URL.
     */
    public static URL buildGetCapabilitiesURL(ServerConnectionParams conf, String wmsVersion)
    {
        StringBuilder sb = new StringBuilder(conf.getWmsUrl());
        setupURL(sb);
        sb.append("service=WMS&REQUEST=GetCapabilities&version=").append(wmsVersion);
//        if (conf.getServerCustomization() != null)
//        {
//            ServerCustomization cust = conf.getServerCustomization();
//            if (cust.getUsesSpecialOGCRequestToken())
//            {
//                sb.append('&').append(cust.getSpecialOGCRequestToken());
//            }
//        }
        URL url = null;
        try
        {
            url = new URL(sb.toString());
        }
        catch (MalformedURLException ex)
        {
            LOGGER.error("Could not build URL to retrieve Capabilities doc.", ex);
        }

        return url;
    }

    /**
     * Build the GetMap request URL string.
     *
     * @param sldRegistry the sld registry
     * @param layerConf configuration for the layer.
     * @param imageKey key which defines the tile size to request.
     * @param customization the server customization used to format the request.
     * @param wmsVersion The WMS version of the request (for example "1.1.1" or
     *            "1.3.0").
     * @return The URL to be used in the GetMap request
     */
    public static URL buildGetMapURL(SldRegistry sldRegistry, WMSLayerConfig layerConf, TileImageKey imageKey,
            ServerCustomization customization, String wmsVersion)
    {
        if (imageKey == null)
        {
            return null;
        }

        WMSLayerGetMapConfig gmConf = layerConf.getGetMapConfig();
        StringBuilder sb = new StringBuilder(256);
        sb.append(gmConf.getUsableGetMapURL());
        setupURL(sb);
        sb.append("service=WMS&request=GetMap");
        addParam(sb, "version", wmsVersion);

        if (gmConf.getStyleType() != null && gmConf.getStyleType().equals(WMSLayerGetMapConfig.StyleType.CLIENT))
        {
            handleClientParameters(sldRegistry, layerConf, gmConf, sb);
        }
        else
        {
            // For the styles it is valid to have "" and it is a required
            // field, so it cannot be omitted.
            if (StringUtils.isEmpty(gmConf.getStyle()))
            {
                sb.append("&styles=");
            }
            else
            {
                addParam(sb, "styles", gmConf.getStyle());
            }
            if (layerConf.isLayersParameterEnabled())
            {
                addParam(sb, "layers", layerConf.getLayerName());
            }

            if ("1.3.0".equals(wmsVersion))
            {
                addParam(sb, "crs", gmConf.getSRS());
            }
            else
            {
                addParam(sb, "srs", gmConf.getSRS());
            }
        }

        addParam(sb, "bgcolor", gmConf.getBGColor());
        // "transparent" parameter's value must be uppercase: "TRUE" or "FALSE"
        addParam(sb, "transparent", String.valueOf(gmConf.getTransparent()).toUpperCase());
        addParam(sb, "width", gmConf.getTextureWidth());
        addParam(sb, "height", gmConf.getTextureHeight());
        addParam(sb, "bbox", bboxToString(imageKey.getBoundingBox(), gmConf.getSRS(), wmsVersion));
        addParam(sb, "format", gmConf.getImageFormat());

        addTimeParam(imageKey, customization, gmConf, sb);
        addParam(sb, "exceptions", convertExceptions(gmConf.getExceptions(), wmsVersion));
        addParam(sb, "elevation", gmConf.getElevation());

//        if (customization != null && customization.getUsesSpecialOGCRequestToken())
//        {
//            sb.append('&').append(customization.getSpecialOGCRequestToken());
//        }
        if (StringUtils.isNotEmpty(gmConf.getCustomParams()))
        {
            sb.append('&').append(gmConf.getCustomParams());
        }

        URL url = null;
        try
        {
            url = new URL(sb.toString().replace(" ", "%20"));
        }
        catch (MalformedURLException ex)
        {
            LOGGER.error("Could not build URL to retrieve tile.", ex);
        }

        return url;
    }

    /**
     * Build the WCS GetCoverage request URL string.
     *
     * @param serverConfig The server configuration.
     * @param layerConf configuration for the layer.
     * @param imageKey key which defines the tile size to request.
     * @return The URL to be used in the GetMap request
     */
    public static URL buildGetCoverageURL(ServerConnectionParams serverConfig, WMSLayerConfig layerConf, TileImageKey imageKey)
    {
        if (imageKey == null)
        {
            return null;
        }

        WMSLayerGetMapConfig gmConf = layerConf.getGetMapConfig();

        String baseUrl = serverConfig.getWmsUrl().replace("wmsserver", "wcsserver");

        StringBuilder sb = new StringBuilder(256);
        sb.append(baseUrl);
        setupURL(sb);
        sb.append("service=WCS&version=1.0.0&request=GetCoverage&coverage=");
        sb.append(layerConf.getLayerName());
        sb.append("&interpolation=nearest%20neighbor");
        addParam(sb, "crs", gmConf.getSRS());
        addParam(sb, "width", gmConf.getTextureWidth());
        addParam(sb, "height", gmConf.getTextureHeight());
        addParam(sb, "bbox", bboxToString(imageKey.getBoundingBox(), gmConf.getSRS(), "1.1.1"));
        addParam(sb, "format", "GEOTIFFFLOAT32");
        if (StringUtils.isNotEmpty(gmConf.getCustomParams()))
        {
            sb.append('&').append(gmConf.getCustomParams());
        }

        URL url = null;
        try
        {
            url = new URL(sb.toString().replace(" ", "%20"));
        }
        catch (MalformedURLException ex)
        {
            LOGGER.error("Could not build URL to retrieve tile.", ex);
        }

        return url;
    }

    /**
     * Add a parameter to the URL string.
     *
     * @param sb string containing the url.
     * @param paramName name of the parameter to add.
     * @param paramValue value of the parameter.
     */
    private static void addParam(StringBuilder sb, String paramName, Object paramValue)
    {
        if (paramValue == null || StringUtils.isEmpty(paramValue.toString()))
        {
            return;
        }

        sb.append('&').append(paramName).append('=').append(paramValue.toString());
    }

    /**
     * Add the time parameter to the string.
     *
     * @param imageKey The image key for the tile.
     * @param customization the server customization used to fine-tune the
     *            dates.
     * @param gmConf The GetMap configuration.
     * @param sb The string being constructed.
     */
    private static void addTimeParam(TileImageKey imageKey, ServerCustomization customization, WMSLayerGetMapConfig gmConf,
            StringBuilder sb)
    {
        if (gmConf.getTime() == null || gmConf.getTime().length() == 0)
        {
            TimeSpan timeSpan = imageKey.getTimeSpan();
            if (timeSpan != null && timeSpan.isBounded())
            {
                addParam(sb, "time", getParamString(imageKey.getTimeSpan(), customization));
            }
        }
        else
        {
            addParam(sb, "time", gmConf.getTime());
        }
    }

    /**
     * Generate the parameter string for the bound box.
     *
     * @param bbox bounding box.
     * @param crs The coordinate space.
     * @param wmsVersion The WMS version of the request (for example "1.1.1" or
     *            "1.3.0").
     * @return A string in the format : "minX,minY,maxX,maxY"
     */
    private static String bboxToString(GeographicBoundingBox bbox, String crs, String wmsVersion)
    {
        StringBuilder sb = new StringBuilder(32);

        if ("EPSG:4326".equals(crs) && "1.3.0".equals(wmsVersion))
        {
            // Make x the latitude and y the longitude
            sb.append(bbox.getLowerLeft().getLatLonAlt().getLatD()).append(',');
            sb.append(bbox.getLowerLeft().getLatLonAlt().getLonD()).append(',');
            sb.append(bbox.getUpperRight().getLatLonAlt().getLatD()).append(',');
            sb.append(bbox.getUpperRight().getLatLonAlt().getLonD());
        }
        else
        {
            sb.append(bbox.getLowerLeft().getLatLonAlt().getLonD()).append(',');
            sb.append(bbox.getLowerLeft().getLatLonAlt().getLatD()).append(',');
            sb.append(bbox.getUpperRight().getLatLonAlt().getLonD()).append(',');
            sb.append(bbox.getUpperRight().getLatLonAlt().getLatD());
        }

        return sb.toString();
    }

    /**
     * In cases where we have a saved configuration, but the wmsVersion has
     * changed, we may have an invalid exceptions parameter value, in these
     * cases, we must convert to the correct value.
     *
     * @param exception The configured exceptions value.
     * @param wmsVersion The WMS version of the request (for example "1.1.1" or
     *            "1.3.0").
     * @return The exceptions value, converted if necessary.
     */
    private static String convertExceptions(String exception, String wmsVersion)
    {
        if ("1.1.1".equals(wmsVersion))
        {
            String upcase = exception.toUpperCase();
            if ("XML".equals(upcase))
            {
                return "application/vnd.ogc.se_xml";
            }
            else if ("INIMAGE".equals(upcase))
            {
                return "application/vnd.ogc.se_inimage";
            }
            else if ("BLANK".equals(upcase))
            {
                return "application/vnd.ogc.se_blank";
            }

            return exception;
        }
        else
        {
            String lowcase = exception.toLowerCase();
            if ("application/vnd.ogc.se_xml".equals(lowcase))
            {
                return "XML";
            }
            else if ("application/vnd.ogc.se_inimage".equals(lowcase))
            {
                return "INIMAGE";
            }
            else if ("application/vnd.ogc.se_blank".equals(lowcase))
            {
                return "BLANK";
            }

            return exception;
        }
    }

    /**
     * Generate a parameter string for a time span.
     *
     * @param timeSpan The time span.
     * @param customization the server customization used to fine-tune the
     *            dates.
     * @return The request string.
     */
    private static String getParamString(TimeSpan timeSpan, ServerCustomization customization)
    {
        if (customization instanceof DateParamCustomizer)
        {
            return ((DateParamCustomizer)customization).getDateParam(timeSpan);
        }
        else
        {
            return customization.getFormattedWMSTime(timeSpan);
        }
    }

    /**
     * This method will handle adding client sld parameters as well as the
     * encoded sld to the GetMap request.
     *
     * @param sldRegistry the sld registry
     * @param layerConf the layer configuration
     * @param gmConf the layer getMap configuration
     * @param sb the string builder for the current URL
     */
    private static void handleClientParameters(SldRegistry sldRegistry, WMSLayerConfig layerConf, WMSLayerGetMapConfig gmConf,
            StringBuilder sb)
    {
        if (StringUtils.isNotEmpty(gmConf.getStyle()))
        {
            StyledLayerDescriptor sld = sldRegistry.getSldByLayerAndName(layerConf.getLayerKey(), gmConf.getStyle());
            if (sld != null)
            {
                sb.append("&SLD_BODY=");
                try
                {
                    JAXBContext jc = JAXBContextHelper.getCachedContext(StyledLayerDescriptor.class);
                    Marshaller marshaller = jc.createMarshaller();
                    StringWriter writer = new StringWriter();
                    marshaller.marshal(sld, writer);
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("This is the request: " + writer.getBuffer());
                    }
                    sb.append(URLEncoder.encode(writer.toString(), "UTF-8"));
                }
                catch (JAXBException e1)
                {
                    LOGGER.warn("Unable to build GetMap URL for " + layerConf.getLayerKey());
                }
                catch (UnsupportedEncodingException e)
                {
                    LOGGER.warn("Unable to encode GetMap URL for " + layerConf.getLayerKey());
                }
            }
        }
    }

    /**
     * Set the first separator character for parameters. This will be "?" if one
     * is not already in the string and "&amp;" otherwise.
     *
     * @param sb string containing the URL.
     */
    private static void setupURL(StringBuilder sb)
    {
        if (sb.indexOf("?") == -1)
        {
            // If the URL does not currently contain a ?, add it.
            sb.append('?');
        }
        else if (sb.lastIndexOf("&") != sb.length() - 1 && sb.lastIndexOf("?") != sb.length() - 1)
        {
            // If the the URL already contains a ? but the last
            // character is neither ? nor &, add &.
            sb.append('&');
        }
    }

    /** Disallow instantiation. */
    private WMSURLBuilder()
    {
    }
}
